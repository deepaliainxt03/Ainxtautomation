package postman;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;

public class AadharMaskedImageSaver {
    private static final String API_KEY = "9cntWv84i28b4QLWyKwoz3E5SryVy9gl3bQoAE8l";
    private static final String AUTHORIZATION_TOKEN = "h0sI79D7QSt0954A";
    private static final String OUTPUT_IMAGE_PATH = "E:\\Deepali\\DOCUMENTS\\AADHAR\\MaskedImages\\"; 
    private static final String EXCEL_FILE_PATH = "E:\\Angel Aadhar OCR 2.xlsx";
    private static final String API_URL = "https://aadharocr.angelbroking.ainxtkyc.com/angel_aadhar_ocr";

    public static void main(String[] args) {
        String folderPath = "E:\\Deepali\\DOCUMENTS\\AADHAR\\old secure qr\\Testing angel";
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Angel Aadhar OCR");
        createExcelHeader(sheet);

        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") ||
                name.toLowerCase().endsWith(".png") ||
                name.toLowerCase().endsWith(".jpeg") ||
                name.toLowerCase().endsWith(".pdf"));

        if (files == null || files.length == 0) {
            System.out.println("No valid files found in the folder.");
            return;
        }

        int serialNumber = 1;
        for (File file : files) {
            System.out.println("Processing: " + file.getName());
            try {
                String base64Content = encodeFileToBase64(file);
                Response response = callApi(API_URL, base64Content);

                String jsonResponse = response.getBody().asString();
                JsonNode rootNode = new ObjectMapper().readTree(jsonResponse);

                // Extract base64 masked value
                JsonNode maskedValueNode = rootNode.at("/item/details/aadhaar/masked_value");
                String maskedBase64 = maskedValueNode.asText();

                // Convert base64 to image and save
                String maskedImagePath = saveBase64Image(maskedBase64, file.getName());

                // Save data to Excel
                saveResponseToExcel(sheet, serialNumber, file, jsonResponse, maskedImagePath);
                serialNumber++;
            } catch (Exception e) {
                System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
            }
        }

        // Save Excel file
        try (FileOutputStream fos = new FileOutputStream(EXCEL_FILE_PATH)) {
            workbook.write(fos);
            System.out.println("Excel file saved successfully at: " + EXCEL_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createExcelHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Sr. No.");
        headerRow.createCell(1).setCellValue("File Name");
        headerRow.createCell(2).setCellValue("Original Image");
        headerRow.createCell(3).setCellValue("API Response");
        headerRow.createCell(4).setCellValue("Masked Aadhaar Image");
        headerRow.createCell(5).setCellValue("Testing Status");
    }

    private static String encodeFileToBase64(File file) throws IOException {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }

    private static Response callApi(String apiUrl, String base64Content) {
        return given()
                .header("x-api-key", API_KEY)
                .header("authorizationToken", AUTHORIZATION_TOKEN)
                .multiPart("im", base64Content)
                .when().post(apiUrl);
    }

    private static String saveBase64Image(String base64String, String originalFileName) {
        if (base64String == null || base64String.isEmpty()) {
            return "No masked image";
        }
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            String maskedImagePath = OUTPUT_IMAGE_PATH + originalFileName + "_masked.png";
            try (FileOutputStream fos = new FileOutputStream(maskedImagePath)) {
                fos.write(imageBytes);
            }
            return maskedImagePath;
        } catch (IOException e) {
            System.err.println("Error saving masked image: " + e.getMessage());
            return "Error saving image";
        }
    }

    private static void saveResponseToExcel(Sheet sheet, int serialNumber, File file, String jsonResponse, String maskedImagePath) {
        try {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(serialNumber);
            row.createCell(1).setCellValue(file.getName());

            // Insert Original Image
            insertImageIntoExcel(sheet, file, row, 2);

            // Save API response
            row.createCell(3).setCellValue(jsonResponse);

            // Insert Masked Aadhaar Image
            File maskedImage = new File(maskedImagePath);
            if (maskedImage.exists()) {
                insertImageIntoExcel(sheet, maskedImage, row, 4);
            }

            // Auto-size columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(3);
            sheet.setColumnWidth(2, 8000);
            sheet.setColumnWidth(4, 8000);
            sheet.autoSizeColumn(5);
        } catch (Exception e) {
            System.err.println("Error inserting images in Excel: " + e.getMessage());
        }
    }

    private static void insertImageIntoExcel(Sheet sheet, File imageFile, Row row, int column) {
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            byte[] imageBytes = IOUtils.toByteArray(Files.newInputStream(imageFile.toPath()));
            int pictureIndex = sheet.getWorkbook().addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            CreationHelper helper = sheet.getWorkbook().getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(column);
            anchor.setCol2(column + 1);
            anchor.setRow1(row.getRowNum());
            anchor.setRow2(row.getRowNum() + 1);
            drawing.createPicture(anchor, pictureIndex);
        } catch (IOException e) {
            System.err.println("Error inserting image into Excel: " + e.getMessage());
        }
    }
}
