package postman;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class FacelivelinessAPI {

    // Path to folder containing images
    private static final String IMAGE_FOLDER = "C:\\Users\\deepa\\Downloads\\singleday\\Testing";

    // Path to save the Excel file
    private static final String EXCEL_FILE = "C:\\Users\\deepa\\Downloads\\API Automation.xlsx";

    // API URL
    private static final String API_URL = "https://9dho34v6re.execute-api.ap-south-1.amazonaws.com/prod/face_validate";

    // API Headers
    private static final String API_KEY = "SzzuBHHDxZM2LHyB1Uea8HMYGelp5sM1YgOKD9H9";
    private static final String AUTH_TOKEN = "hN01JDOcLcQ58ybO";

    // Query Parameters
    private static final String LATITUDE = "10.8467427";
    private static final String LONGITUDE = "76.314679";

    public static void main(String[] args) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create an Excel sheet
            Sheet sheet = workbook.createSheet("Sheet1");

            // Add header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Image");
            headerRow.createCell(1).setCellValue("API Response");

            File folder = new File(IMAGE_FOLDER);
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));

            if (files == null || files.length == 0) {
                throw new IOException("No images found in the specified folder.");
            }

            int rowIndex = 1;

            for (File file : files) {
                String imageName = file.getName();

                // Convert image to Base64
                byte[] imageBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                // Wrap Base64 string in `b'...'` format as required by your API
                String wrappedBase64Image = "b'" + base64Image + "'";

                // Debug: Print image being sent
                System.out.println("Processing image: " + imageName);
                System.out.println("Base64 (truncated): " + wrappedBase64Image.substring(0, 100) + "...");

                // Send API request with Base64 image, headers, and query parameters
                Response response = RestAssured.given()
                        .contentType(ContentType.MULTIPART)  // Set form-data content type
                        .header("x-api-key", API_KEY)       // Add x-api-key header
                        .header("authorizationToken", AUTH_TOKEN) // Add authorizationToken header
                        .queryParam("LATITUDE", LATITUDE)   // Add LATITUDE query parameter
                        .queryParam("LONGITUDE", LONGITUDE) // Add LONGITUDE query parameter
                        .multiPart("im", wrappedBase64Image) // Add Base64 image with key "im"
                        .post(API_URL);

                // Get the API response
                String apiResponse = response.getBody().asString();

                // Debug: Print response details
                System.out.println("Status Code: " + response.getStatusCode());
                System.out.println("API response for " + imageName + ": " + apiResponse);

                // Save image name and API response to Excel
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(imageName);
                row.createCell(1).setCellValue(apiResponse);
            }

            // Save Excel file
            try (FileOutputStream outputStream = new FileOutputStream(EXCEL_FILE)) {
                workbook.write(outputStream);
                System.out.println("Excel file saved at " + EXCEL_FILE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
