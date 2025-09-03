package postman;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;

public class AadhaarOCRProcessor {
    private static final String API_URL = "https://aadharocr.angelbroking.ainxtkyc.com/angel_aadhar_ocr";
    private static final String API_KEY = "9cntWv84i28b4QLWyKwoz3E5SryVy9gl3bQoAE8l";
    private static final String AUTH_TOKEN = "h0sI79D7QSt0954A";
    private static final String IMAGE_FOLDER_PATH = "E:\\Deepali\\DOCUMENTS\\AADHAR\\QR Status 2\\New folder";  // Folder containing Aadhaar images
    private static final String OUTPUT_EXCEL_PATH = "E:\\masked_aadhar 1.xlsx";

    public static void main(String[] args) {
        File folder = new File(IMAGE_FOLDER_PATH);
        File[] imageFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("❌ No images found in folder: " + IMAGE_FOLDER_PATH);
            return;
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Masked Aadhaar Images");

            int rowNum = 0;
            for (File imageFile : imageFiles) {
                String base64Image = encodeImageToBase64(imageFile);
                String maskedBase64 = sendApiRequest(base64Image);

                Row row = sheet.createRow(rowNum);
                row.createCell(0).setCellValue(imageFile.getName());

                if (maskedBase64 != null) {
                    byte[] imageBytes = Base64.getDecoder().decode(maskedBase64);
                    String outputImagePath = "E:\\Masked_Images\\" + imageFile.getName(); // Save masked image
                    saveImage(imageBytes, outputImagePath);

                    // Insert Image into Excel
                    InputStream inputStream = new FileInputStream(outputImagePath);
                    byte[] excelImageBytes = inputStream.readAllBytes();
                    int pictureIdx = workbook.addPicture(excelImageBytes, Workbook.PICTURE_TYPE_PNG);
                    inputStream.close();

                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    CreationHelper helper = workbook.getCreationHelper();
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(1);
                    anchor.setRow1(rowNum);
                    Picture picture = drawing.createPicture(anchor, pictureIdx);
                    picture.resize();
                } else {
                    row.createCell(1).setCellValue("❌ No Masked Aadhaar Image Returned");
                }

                rowNum++;
            }

            // Save Excel File
            try (FileOutputStream fileOut = new FileOutputStream(OUTPUT_EXCEL_PATH)) {
                workbook.write(fileOut);
            }
            System.out.println("✅ Excel file created: " + OUTPUT_EXCEL_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Convert Image to Base64
    private static String encodeImageToBase64(File imageFile) {
        try {
            byte[] fileContent = Files.readAllBytes(imageFile.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Send API Request and Get Masked Aadhaar Image Base64
    private static String sendApiRequest(String base64Image) {
        Response response = RestAssured.given()
                .header("x-api-key", API_KEY)
                .header("authorizationToken", AUTH_TOKEN)
                .multiPart("im", base64Image)
                .post(API_URL)
                .then()
                .extract()
                .response();

        if (response.statusCode() == 200) {
            JsonPath jsonPath = new JsonPath(response.asString());
            String maskedValue = jsonPath.getString("item.details.aadhaar.masked_value");

            if (maskedValue == null || maskedValue.isEmpty()) {
                System.out.println("❌ No masked Aadhaar image returned for this request.");
                return null;
            }
            return maskedValue;
        } else {
            System.out.println("❌ API request failed. Status Code: " + response.statusCode());
            return null;
        }
    }

    // Save Base64 Image to File
    private static void saveImage(byte[] imageBytes, String outputPath) {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(imageBytes);
            System.out.println("✅ Masked Aadhaar image saved: " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
