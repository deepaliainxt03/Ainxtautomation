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
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class Signaturetesting {
	private static final String API_KEY = "GFSBjiUeFs3awWpJiRlWA4DBHFdoS0gO78iBe5Ab";
	private static final String EXCEL_FILE_PATH = "E:\\Signature upload.xlsx";
	private static final String FOLDER_PATH = "C:\\Users\\deepa\\Downloads\\Signature all type samples\\Upload Signature";
	private static final String API_URL = "https://2umpw5n3qb.execute-api.ap-south-1.amazonaws.com/default/testing";
	//private static final String AUTHORIZATION_TOKEN = "xXRKPwi4xA49VnjmiMpOp5KjB0g4Gf7g1WKYcP54";

	public static void main(String[] args) {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Color Signature");

		// Add Summary Rows at the Top
		addSummaryRows(sheet);

		// Create Table Header
		createExcelHeader(sheet);

		try {
			File folder = new File(FOLDER_PATH);
			File[] files = folder
					.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png")
							|| name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".pdf"));

			if (files == null || files.length == 0) {
				System.out.println("No valid files found in the folder.");
				return;
			}

			int serialNumber = 001;

			for (File file : files) {
				System.out.println("Processing: " + file.getName());

				try {
					String base64Content = encodeFileToBase64(file);
					Response response = callApi(API_URL, base64Content);

					// Parse, clean, and format JSON response
					String formattedJson = cleanAndFormatJson(response.getBody().asString());

					saveResponseToExcel(sheet, serialNumber, file, formattedJson);
					serialNumber++;
				} catch (Exception e) {
					System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
				}
			}

			try (FileOutputStream fos = new FileOutputStream(EXCEL_FILE_PATH)) {
				workbook.write(fos);
				System.out.println("Excel file saved successfully at: " + EXCEL_FILE_PATH);
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

	// Method to add Summary Rows at the TOP
	private static void addSummaryRows(Sheet sheet) {
		String[] summaryHeaders = { "Total", "Passed", "Failed", "Observation" };
		for (int i = 0; i < summaryHeaders.length; i++) {
			Row row = sheet.createRow(i);
			row.createCell(0).setCellValue(summaryHeaders[i]);
			row.createCell(1).setCellValue(""); // Placeholder for values
		}
	}

	// Method to create the Excel Header (now starts at row 6)
	private static void createExcelHeader(Sheet sheet) {
		Row headerRow = sheet.createRow(5); // Start after summary section
		headerRow.createCell(0).setCellValue("TestID");
		headerRow.createCell(1).setCellValue("Test Description");
		headerRow.createCell(2).setCellValue("File Name");
		headerRow.createCell(3).setCellValue("Image");
		headerRow.createCell(4).setCellValue("API Response");
		headerRow.createCell(5).setCellValue("Expected Result");
		headerRow.createCell(6).setCellValue("Actual Result");
		headerRow.createCell(7).setCellValue("Testing Status");
	}

	// Method to encode a file to Base64
	private static String encodeFileToBase64(File file) throws IOException {
		byte[] fileContent = Files.readAllBytes(file.toPath());
		return Base64.encodeBase64String(fileContent);
	}

	// Method to call API with Base64 encoded image
	private static Response callApi(String apiUrl, String base64Content) {
		RestAssured.baseURI = apiUrl;
		return given()
				.header("x-api-key", API_KEY)
				.multiPart("im", "b '" + base64Content + "'")
				.queryParam("IsSdk", 0)
				.queryParam("Isdigital", 0)
				.when().post();
	}

	// Method to clean and format JSON response (removes "cropped_sign" & formats
	// JSON)
	private static String cleanAndFormatJson(String jsonResponse) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(jsonResponse);

			if (rootNode.has("cropped_sign")) {
				((ObjectNode) rootNode).remove("cropped_sign");
			}

			// Pretty-print formatted JSON
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		} catch (Exception e) {
			System.err.println("Error formatting JSON response: " + e.getMessage());
			return jsonResponse; // Return original if error occurs
		}
	}

	// Method to save API response and image in Excel
	private static void saveResponseToExcel(Sheet sheet, int serialNumber, File file, String apiResponse) {
		try {
			int rowNum = sheet.getLastRowNum() + 1;
			Row row = sheet.createRow(rowNum);
			row.createCell(0).setCellValue("Test_" + serialNumber);
			row.createCell(1).setCellValue("");
			row.createCell(2).setCellValue(file.getName());

			// Insert image into Excel
			FileInputStream fis = new FileInputStream(file);
			byte[] imageBytes = IOUtils.toByteArray(fis);
			int pictureIndex = sheet.getWorkbook().addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
			fis.close();

			Drawing<?> drawing = sheet.createDrawingPatriarch();
			CreationHelper helper = sheet.getWorkbook().getCreationHelper();
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(3);
			anchor.setCol2(4);
			anchor.setRow1(row.getRowNum());
			anchor.setRow2(row.getRowNum() + 1);
			drawing.createPicture(anchor, pictureIndex);

			// Save formatted API response
			row.createCell(4).setCellValue(apiResponse);

			// Auto-size columns for better readability
			sheet.autoSizeColumn(0);
			sheet.autoSizeColumn(1);
			sheet.autoSizeColumn(2);
			sheet.setColumnWidth(3, 8000);
			sheet.autoSizeColumn(4);
			sheet.autoSizeColumn(5);
			sheet.autoSizeColumn(6);
			sheet.autoSizeColumn(7);
		} catch (Exception e) {
			System.err.println("Error inserting image into Excel: " + e.getMessage());
		}
	}
}



