package postman;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.restassured.response.Response;

public class Imagetestingwithapiurlonly {
    public static void main(String[] args) {
        String folderPath = "E:\\DOCUMENTS\\DOCUMENTS\\AADHAR\\New folder"; // <-- Update this path
        String excelFilePath = "E:\\Aadhar.xlsx"; // <-- Update this path
        String apiUrl = "https://4c2qvpodza.execute-api.ap-south-1.amazonaws.com/default/dockertest";

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Cheque");
        createExcelHeader(sheet);

        try {
            File folder = new File(folderPath);
            File[] files = folder.listFiles((dir, name) -> 
                    name.toLowerCase().endsWith(".jpg") ||
                    name.toLowerCase().endsWith(".jpeg") ||
                    name.toLowerCase().endsWith(".png") ||
                    name.toLowerCase().endsWith(".pdf")
            );

            if (files == null || files.length == 0) {
                System.out.println("No valid image files found.");
                return;
            }

            int serialNumber = 1;

            for (File file : files) {
                try {
                    System.out.println("Processing: " + file.getName());

                    // Step 1: Encode file to base64 with b' prefix and ' suffix
                    String base64 = encodeFileToBase64(file);
                    String formatted = "b'" + base64 + "'";

                    // Step 2: Send to API
                    Response response = given()
                            .multiPart("im", formatted)
//                            .queryParam("requestid", "1234")
//                            .queryParam("IsSdk", "0")
//                            .queryParam("Isdigital", "0")
                            .post(apiUrl);

                    String rawJson = response.getBody().asString();

                    // Step 3: Remove "cropped_sign"
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(rawJson);
                    //ObjectMapper mapper = new ObjectMapper();
        			//JsonNode rootNode = mapper.readTree(jsonResponse);

        			// Remove base64 fields (Modify if necessary)
        			((ObjectNode) rootNode).remove("item.details.aadhaar.masked_value");
        			((ObjectNode) rootNode).remove("item.details.face.faceString");
        			
                    ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
                    String cleanedJson = writer.writeValueAsString(rootNode);

                    // Step 4: Save to Excel
                    saveToExcel(sheet, serialNumber++, file, cleanedJson);

                } catch (Exception e) {
                    System.err.println("Error with file " + file.getName() + ": " + e.getMessage());
                }
            }

            // Save workbook
            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
                System.out.println("Excel saved to: " + excelFilePath);
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

    private static String encodeFileToBase64(File file) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        return Base64.encodeBase64String(fileBytes);
    }

    private static void createExcelHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Sr No.");
        header.createCell(1).setCellValue("File Name");
        header.createCell(2).setCellValue("API Response");
        header.createCell(3).setCellValue("Image");
    }

    private static void saveToExcel(Sheet sheet, int serial, File file, String jsonResponse) throws IOException {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(serial);
        row.createCell(1).setCellValue(file.getName());
        row.createCell(2).setCellValue(jsonResponse);

        // Insert image
        FileInputStream fis = new FileInputStream(file);
        byte[] imgBytes = IOUtils.toByteArray(fis);
        fis.close();

        int pictureIdx = sheet.getWorkbook().addPicture(imgBytes, Workbook.PICTURE_TYPE_PNG);
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        CreationHelper helper = sheet.getWorkbook().getCreationHelper();

        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(3);
        anchor.setRow1(row.getRowNum());
        anchor.setCol2(4);
        anchor.setRow2(row.getRowNum() + 1);

        drawing.createPicture(anchor, pictureIdx);

        // Optional: Autosize
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.setColumnWidth(3, 8000);
    }
}
