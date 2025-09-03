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

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class Aadharmaskedencryptdecrypt {

	private static final String API_KEY = "rJZGX7mbpW8NSgydtOfAcaZZ1xT3xdcl1gBCOhtX";

	public static void main(String[] args) {
		String folderPath = "E:\\DOCUMENTS\\DOCUMENTS\\AADHAR\\Aadhar imgs";
		String excelFilePath = "E:\\Kotak Aadhar Masking.xlsx";
		String apiUrl = "https://tpdxsyby0l.execute-api.ap-south-1.amazonaws.com/default/docker_pan";

		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Aadhar");
		createExcelHeader(sheet);

		try {
			File folder = new File(folderPath);
			File[] files = folder
					.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png")
							|| name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".pdf"));

			if (files == null || files.length == 0) {
				System.out.println("No valid files found in the folder.");
				return;
			}

			int serialNumber = 1;

			for (File file : files) {
				System.out.println("Processing: " + file.getName());
				try {
					String base64Content = encodeFileToBase64(file);
					Response response = callApi(apiUrl, base64Content);
					String responseBody = response.getBody().asString();

					saveResponseToExcel(sheet, serialNumber, file, responseBody);
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
		headerRow.createCell(2).setCellValue("Input Image");
		headerRow.createCell(3).setCellValue("Masked Image (if status == 1)");
		headerRow.createCell(4).setCellValue("API Response (only if status != 1)");
	}

	private static String encodeFileToBase64(File file) throws IOException {
		byte[] fileContent = Files.readAllBytes(file.toPath());
		return Base64.encodeBase64String(fileContent);
	}

	private static Response callApi(String apiUrl, String base64Content) {
		RestAssured.baseURI = apiUrl;
		return given().header("x-api-key", API_KEY).multiPart("im", "b'" + base64Content + "'").when().post();
	}

	private static void saveResponseToExcel(Sheet sheet, int serialNumber, File file, String rawJson) {
		try {
			Row row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeightInPoints(120); // Set row height for visibility
			row.createCell(0).setCellValue(serialNumber);
			row.createCell(1).setCellValue(file.getName());

			Drawing<?> drawing = sheet.createDrawingPatriarch();
			CreationHelper helper = sheet.getWorkbook().getCreationHelper();

			// === INPUT IMAGE ===
			FileInputStream fis = new FileInputStream(file);
			byte[] inputImageBytes = IOUtils.toByteArray(fis);
			int inputImgIdx = sheet.getWorkbook().addPicture(inputImageBytes, Workbook.PICTURE_TYPE_PNG);
			fis.close();

			ClientAnchor inputAnchor = helper.createClientAnchor();
			inputAnchor.setCol1(2); // Column C
			inputAnchor.setRow1(row.getRowNum());
			Picture inputPicture = drawing.createPicture(inputAnchor, inputImgIdx);
			inputPicture.resize(1.0); // Fit cell

			// === PARSE JSON ===
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(rawJson);
			int status = rootNode.has("status") ? rootNode.get("status").asInt() : -1;

			// === MASKED IMAGE ONLY IF status == 1 ===
			if (status == 1 && rootNode.has("masking")) {
				String maskingBase64 = rootNode.get("masking").asText();
				if (maskingBase64.contains(",")) {
					maskingBase64 = maskingBase64.split(",")[1];
				}

				if (!maskingBase64.isEmpty()) {
					String formula = "IMAGE(\"data:image/png;base64," + maskingBase64 + "\")";
					row.createCell(3).setCellFormula(formula);
				}
			}

			// === RAW JSON ONLY IF status != 1 ===
			if (status != 1) {
				String formattedJson = formatJson(rawJson);
				row.createCell(4).setCellValue(formattedJson); // Column E
			}

			// Adjust column widths (if not already set globally)
			sheet.setColumnWidth(2, 10000); // Input Image column
			sheet.setColumnWidth(3, 10000); // Masked Image column
			sheet.setColumnWidth(4, 18000); // API Response

		} catch (Exception e) {
			System.err.println("Error saving to Excel: " + e.getMessage());
		}
	}

	private static String formatJson(String jsonResponse) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Object jsonObject = mapper.readValue(jsonResponse, Object.class);
		ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
		return writer.writeValueAsString(jsonObject);
	}
}
