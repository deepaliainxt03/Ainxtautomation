package postman;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MiraeNameMatch {

    public static void main(String[] args) {
        String inputPath = "C:\\Users\\deepa\\Downloads\\Mirae Namematch Testing.xlsx";  // Input file
        String outputPath = "E:\\Mirae_Response.xlsx";          // Output file
        String sheetName = "Sheet1";  // change if needed

        try {
            // Load Input Excel
            FileInputStream fis = new FileInputStream(new File(inputPath));
            Workbook inputWb = new XSSFWorkbook(fis);
            Sheet inputSheet = inputWb.getSheet(sheetName);

            // Create Output Excel
            Workbook outputWb = new XSSFWorkbook();
            Sheet outputSheet = outputWb.createSheet("Responses");

            // Style for wrapped JSON text
            CellStyle wrapStyle = outputWb.createCellStyle();
            wrapStyle.setWrapText(true);

            // Header row in output sheet
            Row header = outputSheet.createRow(0);
            header.createCell(0).setCellValue("PANFirstName");
            header.createCell(1).setCellValue("PANMiddleName");
            header.createCell(2).setCellValue("PANLastName");
            header.createCell(3).setCellValue("BeneficiaryName");
            header.createCell(4).setCellValue("API Response");

            // Loop rows from input Excel
            for (int i = 1; i <= inputSheet.getLastRowNum(); i++) {
                Row inRow = inputSheet.getRow(i);
                if (inRow == null) continue;

                String panFirst = getCellValueAsString(inRow.getCell(0));
                String panMiddle = getCellValueAsString(inRow.getCell(1));
                String panLast = getCellValueAsString(inRow.getCell(2));
                String beneficiary = getCellValueAsString(inRow.getCell(3));

                // Build request body
                JSONObject names = new JSONObject();
                names.put("PANFirstName", panFirst);
                names.put("PANMiddleName", panMiddle);
                names.put("PANLastName", panLast);
                names.put("BeneficiaryNameWithBank", beneficiary);

                JSONObject body = new JSONObject();
                body.put("Names", names);

                // Call API
                Response response = RestAssured.given()
                        .header("x-api-key", "jDxgRAvqEL5rprjbe4oQn6MTxJ78ImkZ8Kn4BvS3")
                        .header("authorizationToken", "454fY6TfMG9F0qZV")
                        .header("Content-Type", "application/json")
                        .body(body.toString())
                        .post("https://u581iqrjof.execute-api.ap-south-1.amazonaws.com/default/mirae-namematch?requestid=ABC12345");

                // Pretty JSON response
                JSONObject responseJson = new JSONObject(response.getBody().asString());
                String prettyJson = responseJson.toString(4);

                // Write to output Excel
                Row outRow = outputSheet.createRow(i);
                outRow.createCell(0).setCellValue(panFirst);
                outRow.createCell(1).setCellValue(panMiddle);
                outRow.createCell(2).setCellValue(panLast);
                outRow.createCell(3).setCellValue(beneficiary);

                Cell responseCell = outRow.createCell(4, CellType.STRING);
                responseCell.setCellValue(prettyJson);
                responseCell.setCellStyle(wrapStyle);

                // Adjust row height for pretty JSON
                int lines = prettyJson.split("\n").length;
                outRow.setHeightInPoints(lines * inputSheet.getDefaultRowHeightInPoints());

                System.out.println("Row " + i + " processed successfully.");
            }

            // Save Output Excel
            FileOutputStream fos = new FileOutputStream(new File(outputPath));
            outputWb.write(fos);
            fos.close();

            // Close resources
            fis.close();
            inputWb.close();
            outputWb.close();

            System.out.println("✅ All API responses saved in " + outputPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Helper method to safely get any cell value as String
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double d = cell.getNumericCellValue();
                    if (d == Math.floor(d)) {
                        return String.valueOf((long) d); // integer without .0
                    } else {
                        return String.valueOf(d);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
