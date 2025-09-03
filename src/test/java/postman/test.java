package postman;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class test {
    private static final String API_URL = "https://bzygqesyr2.execute-api.ap-south-1.amazonaws.com/default/dockerTest2";
    private static final String API_KEY = "gAHfieHMPj5szhTCUd6436LRpPi7F5tX4SvSC7Md";
    private static final String IMAGE_FOLDER_PATH = "C:\\Users\\deepa\\Downloads\\Manrega (1)\\Manrega\\Test"; // Change path
    private static final String EXCEL_FILE_PATH = "E:\\xyz.xlsx";

    public static void main(String[] args) {
        File folder = new File(IMAGE_FOLDER_PATH);
        File[] imageFiles = folder.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));

        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("No images found in the folder.");
            return;
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("API Responses");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Image Name");
            headerRow.createCell(1).setCellValue("API Response");

            int rowNum = 1;
            for (File image : imageFiles) {
                System.out.println("Processing: " + image.getName());

                // Convert image to Base64
                String base64Image = encodeImageToBase64(image);
                if (base64Image == null) continue;

                // Send API request using Rest Assured
                String response = sendApiRequest(base64Image);

                // Save response in Excel
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(image.getName());
                row.createCell(1).setCellValue(response);
            }

            // Save Excel file
            try (FileOutputStream fileOut = new FileOutputStream(EXCEL_FILE_PATH)) {
                workbook.write(fileOut);
            }
            System.out.println("Responses saved in Excel: " + EXCEL_FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Correct Base64 Encoding
    private static String encodeImageToBase64(File imageFile) {
        try {
            byte[] fileContent = Files.readAllBytes(imageFile.toPath());
            String base64String = Base64.getEncoder().encodeToString(fileContent);

            System.out.println("Encoded Base64 Length: " + base64String.length());
            System.out.println("Sample Base64: " + base64String.substring(0, 50)); // Print first 50 characters

            return base64String;
        } catch (IOException e) {
            System.err.println("Error encoding image: " + imageFile.getName());
            return null;
        }
    }

    // ✅ API Request with Full Debugging
    private static String sendApiRequest(String base64Image) {
        try {
            Map<String, String> requestBody = new HashMap<>();

            // 🛠️ Check if API requires MIME type prefix (Try Both)
            requestBody.put("im", "data:image/jpeg;base64," + base64Image);  // With MIME Type
            // requestBody.put("im", base64Image);  // Without MIME Type (Try this if needed)

            Response response = RestAssured
                    .given()
                    .header("x-api-key", API_KEY)
                    .header("Content-Type", "application/json")  // Explicitly define JSON format
                    .body(requestBody)
                    .when()
                    .post(API_URL)
                    .then()
                    .extract()
                    .response();

            // ✅ Print full API response for debugging
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody().asString());

            return response.getBody().asString();
        } catch (Exception e) {
            System.err.println("API request failed.");
            return "Error";
        }
    }
}
