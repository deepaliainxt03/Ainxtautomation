package postman;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ImageToAPI {
    public static void main(String[] args) {
        String folderPath = "C:\\Users\\deepa\\OneDrive\\Documents\\DOCUMENTS\\AADHAR"; // Replace with your folder path
        String excelFilePath = "E:\\Aadhar Saas.xlsx"; // Output Excel file path
        String responseSavePath = "E:\\Aadhar_Responses\\"; // Folder to save long JSON responses
        String apiKey = "kPOZGQSECN4yLd6p8NOok9p8OtoH9CpT833QR1wq";
        String apiUrl = "https://9psubxp46l.execute-api.ap-south-1.amazonaws.com/default/dockertest";

        // Create output folder if it doesn't exist
        new File(responseSavePath).mkdirs();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("API Responses");
        createExcelHeader(sheet);

        try {
            File folder = new File(folderPath);
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg")
                    || name.toLowerCase().endsWith(".png")
                    || name.toLowerCase().endsWith(".jpeg")
                    || name.toLowerCase().endsWith(".pdf"));
            if (files == null || files.length == 0) {
                System.out.println("No valid files found in the folder.");
                return;
            }

            int serialNumber = 1;

            for (File file : files) {
                System.out.println("Processing: " + file.getName());

                try {
                    // Convert file to Base64
                    String base64Content = encodeFileToBase64(file);

                    // Call the API
                    Response response = callApi(apiUrl, apiKey, base64Content);

                    // Format JSON response for better readability
                    String formattedJsonResponse = formatJson(response.getBody().asString());

                    // Save serial number, file name, and formatted JSON response in Excel
                    saveResponseToExcel(sheet, serialNumber, file.getName(), formattedJsonResponse, responseSavePath);
                    serialNumber++;

                } catch (Exception e) {
                    System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
                }
            }

            // Save the Excel file
            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
                System.out.println("Excel file saved successfully at: " + excelFilePath);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Create header row in the Excel sheet
    private static void createExcelHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Sr. No.");
        headerRow.createCell(1).setCellValue("File Name");
        headerRow.createCell(2).setCellValue("API Response (or File Path)");
    }

    // Convert file to Base64
    private static String encodeFileToBase64(File file) throws IOException {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.encodeBase64String(fileContent);
        } catch (IOException e) {
            throw new IOException("Failed to encode file " + file.getName() + " to Base64. " + e.getMessage());
        }
    }

    // Call the API with Base64 content
    private static Response callApi(String apiUrl, String apiKey, String base64Content) {
        RestAssured.baseURI = apiUrl;

        // Format Base64 content as: b 'base64_url'
        String formattedBase64Content = "b '" + base64Content + "'";
        System.out.println("Sending Base64 Content (First 100 Characters): " + formattedBase64Content.substring(0, Math.min(100, formattedBase64Content.length())));

        return given()
                .header("x-api-key", apiKey)
                .multiPart("im", formattedBase64Content) // Correctly formatted Base64 content
                .when()
                .post();
    }

    // Save serial number, file name, and formatted JSON response to Excel
    private static void saveResponseToExcel(Sheet sheet, int serialNumber, String fileName, String jsonResponse, String savePath) {
        try {
            // Prettify the JSON response
            String formattedJson = formatJson(jsonResponse);

            // If the response length exceeds the limit, save to a file instead
            if (formattedJson.length() > 32000) {
                String filePath = savePath + fileName.replaceAll("\\W+", "_") + ".txt";
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write(formattedJson);
                }
                formattedJson = "(Saved in file: " + filePath + ")";
            }

            // Create a new row in the Excel sheet
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(serialNumber); // Serial Number
            row.createCell(1).setCellValue(fileName);    // File Name
            row.createCell(2).setCellValue(formattedJson); // Prettified JSON Response or file path

            // Automatically adjust column widths
            sheet.autoSizeColumn(0); // Serial Number
            sheet.autoSizeColumn(1); // File Name
            sheet.autoSizeColumn(2); // JSON Response

        } catch (Exception e) {
            System.err.println("Error formatting JSON for Excel: " + e.getMessage());
        }
    }

    // Format JSON response for better readability and remove sensitive fields
    private static String formatJson(String jsonResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object jsonObject = mapper.readValue(jsonResponse, Object.class);

        // Convert the object to a map for easy modification
        Map<String, Object> jsonMap = mapper.convertValue(jsonObject, Map.class);

        // Remove fields from the nested "item.details" map
        if (jsonMap.containsKey("item.details")) {
            Map<String, Object> details = (Map<String, Object>) jsonMap.get("item.details");
            details.remove("aadhar.masked_value");  // Remove masked aadhar value
            details.remove("faceString");          // Remove face string
        }

        // Prettify the modified JSON
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        return writer.writeValueAsString(jsonMap); // Returns a prettified JSON string
    }
}
