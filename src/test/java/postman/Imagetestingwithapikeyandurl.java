package postman;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class Imagetestingwithapikeyandurl {
    private static final String API_KEY = "zwiIEd45fN88tuZGCaUgz8O3qPhKQYoI4up5bZis";

    public static void main(String[] args) {
        String folderPath = "C:\\Users\\deepa\\Downloads\\Mirae PI 29-05-25\\screenshotimages";
        String excelFilePath = "E:\\Mirae PI.xlsx";
        String apiUrl = "https://6reqec10w8.execute-api.ap-south-1.amazonaws.com/default/test?requestId=sdfghjk";

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Mirae PI");
        createExcelHeader(sheet);

        try {
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
                    String base64Content = "'" + encodeFileToBase64(file) + "'";
                    Response response = callApi(apiUrl, base64Content);
                    String formattedJsonResponse = formatJson(response.getBody().asString());
                    saveResponseToExcel(sheet, serialNumber, file, formattedJsonResponse);
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
        headerRow.createCell(2).setCellValue("Image");
        headerRow.createCell(3).setCellValue("API Response");
        headerRow.createCell(4).setCellValue("Testing Status");
    }

    private static String encodeFileToBase64(File file) throws IOException {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return Base64.encodeBase64String(fileContent);
    }

    private static Response callApi(String apiUrl, String base64Content) {
        RestAssured.baseURI = apiUrl;
        return given()
                .header("x-api-key", API_KEY)
                .multiPart("im", "b '" + base64Content + "'")
               // .queryParam("password", "Amol1234")
                .when().post();
    }

    private static void saveResponseToExcel(Sheet sheet, int serialNumber, File file, String jsonResponse) {
        try {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(serialNumber);
            row.createCell(1).setCellValue(file.getName());

            FileInputStream fis = new FileInputStream(file);
            byte[] imageBytes = IOUtils.toByteArray(fis);
            int pictureIndex = sheet.getWorkbook().addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
            fis.close();

            Drawing<?> drawing = sheet.createDrawingPatriarch();
            CreationHelper helper = sheet.getWorkbook().getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(2);
            anchor.setCol2(3);
            anchor.setRow1(row.getRowNum());
            anchor.setRow2(row.getRowNum() + 1);

            drawing.createPicture(anchor, pictureIndex);

            row.createCell(3).setCellValue(jsonResponse);

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(3);
            sheet.setColumnWidth(2, 8000);
            sheet.autoSizeColumn(4);
        } catch (Exception e) {
            System.err.println("Error inserting image for Excel: " + e.getMessage());
        }
    }

    private static String formatJson(String jsonResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object jsonObject = mapper.readValue(jsonResponse, Object.class);
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        return writer.writeValueAsString(jsonObject);
    }
}
