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

import static io.restassured.RestAssured.given;

public class withoutimagetesting {
    public static void main(String[] args) {
        String folderPath = "C:\\Users\\deepa\\OneDrive\\Documents\\DOCUMENTS\\AADHAR"; // Replace with your folder path
        String excelFilePath = "E:\\Aadhar Saas.xlsx";                 // Output Excel file path
        String apiKey = "kPOZGQSECN4yLd6p8NOok9p8OtoH9CpT833QR1wq";
        String authorizationToken = "7TKWMwTdzwE93odP";
        String apiUrl = "https://9psubxp46l.execute-api.ap-south-1.amazonaws.com/default/dockertest";

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
                    String base64Content = encodeFileToBase64(file);
                    Response response = callApi(apiUrl, apiKey, authorizationToken, base64Content);
                    String formattedJsonResponse = formatJson(response.getBody().asString());
                    saveResponseToExcel(sheet, serialNumber, file.getName(), formattedJsonResponse);
                    serialNumber++;
                } catch (Exception e) {
                    System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
                }
            }

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

    private static void createExcelHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Sr. No.");
        headerRow.createCell(1).setCellValue("File Name");
        headerRow.createCell(2).setCellValue("API Response");
    }

    private static String encodeFileToBase64(File file) throws IOException {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return Base64.encodeBase64String(fileContent);
    }

    private static Response callApi(String apiUrl, String apiKey, String authorizationToken, String base64Content) {
        RestAssured.baseURI = apiUrl;

        return given()
                .header("x-api-key", apiKey)
                .header("authorizationToken", authorizationToken)
                .multiPart("im", "b '" + base64Content + "'")
                .when()
                .post();
    }

    private static void saveResponseToExcel(Sheet sheet, int serialNumber, String fileName, String jsonResponse) {
        try {
            String formattedJson = formatJson(jsonResponse);
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(serialNumber);
            row.createCell(1).setCellValue(fileName);
            row.createCell(2).setCellValue(formattedJson);

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
        } catch (Exception e) {
            System.err.println("Error formatting JSON for Excel: " + e.getMessage());
        }
    }

    private static String formatJson(String jsonResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object jsonObject = mapper.readValue(jsonResponse, Object.class);
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        return writer.writeValueAsString(jsonObject);
    }
}
