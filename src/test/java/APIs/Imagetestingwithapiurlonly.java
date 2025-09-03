package APIs;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.restassured.response.Response;

public class Imagetestingwithapiurlonly {
    public static void main(String[] args) {
        String folderPath = "C:\\Users\\deepa\\Downloads\\SendAnywhere_289765"; // Update as needed
        String excelFilePath = "E:\\Signature.xlsx"; // Update as needed
        String apiUrl = "https://ajmmjwpb8i.execute-api.ap-south-1.amazonaws.com/default/test_2"; // Update as needed

        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sign API Output");

        // Header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("File Name");
        header.createCell(1).setCellValue("sign_label");
        header.createCell(2).setCellValue("remark");
        header.createCell(3).setCellValue("status");
        header.createCell(4).setCellValue("cropped_sign (10 chars)");
        header.createCell(5).setCellValue("request_id");
        header.createCell(6).setCellValue("API Response");

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter prettyWriter = mapper.writerWithDefaultPrettyPrinter();

        int rowNum = 1;

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    try {
                        // Convert image to base64
                        FileInputStream fis = new FileInputStream(file);
                        byte[] imageBytes = fis.readAllBytes();
                        fis.close();
                        String base64Image = Base64.encodeBase64String(imageBytes);

                        // Prepare and send request
                        String payload = "{\"image\":\"" + base64Image + "\"}";

                        Response response = given()
                                .header("Content-Type", "application/json")
                                .body(payload)
                                .post(apiUrl);

                        JsonNode json = mapper.readTree(response.getBody().asString());

                        String signLabel = json.has("sign_label") ? json.get("sign_label").asText() : "";
                        String remark = json.has("remark") ? json.get("remark").asText() : "";
                        int status = json.has("status") ? json.get("status").asInt() : -1;
                        String croppedSign = json.has("cropped_sign") ? json.get("cropped_sign").asText() : "";
                        String croppedSignShort = croppedSign.length() > 10 ? croppedSign.substring(0, 10) : croppedSign;
                        String requestId = json.has("request_id") ? json.get("request_id").asText() : "";
                        String prettyJson = prettyWriter.writeValueAsString(json);

                        // Write to Excel
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(file.getName());
                        row.createCell(1).setCellValue(signLabel);
                        row.createCell(2).setCellValue(remark);
                        row.createCell(3).setCellValue(status);
                        row.createCell(4).setCellValue(croppedSignShort);
                        row.createCell(5).setCellValue(requestId);
                        row.createCell(6).setCellValue(prettyJson); // full formatted JSON

                        System.out.println("Processed: " + file.getName());

                    } catch (Exception e) {
                        System.out.println("Error processing: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }

            // Save Excel file
            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                sheet.autoSizeColumn(6); // Autofit JSON column
                workbook.write(fos);
                workbook.close();
                System.out.println("Excel saved at: " + excelFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No files found in folder.");
        }
    }
}
