package postman;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelImageUploader {
	public static void main(String[] args) throws Exception {
		// Path to your images folder
		String folderPath = "C:\\Users\\deepa\\Downloads\\SendAnywhere_735112"; // Replace with your folder path

		// Excel file path
		String excelFilePath = "E:\\MiraeLiveliness.xlsx"; // Output Excel file path

		// Create a new workbook and sheet
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Web Link");

		// Add headers
		Row headerRow = sheet.createRow(0);
		headerRow.createCell(0).setCellValue("Sr. No.");
		headerRow.createCell(1).setCellValue("Images");
		headerRow.createCell(2).setCellValue("Testing Status");

		// Set column widths
		sheet.setColumnWidth(0, 4000); // Sr. No.
		sheet.setColumnWidth(1, 10000); // Images
		sheet.setColumnWidth(2, 8000); // Testing Status

		// Load images from the folder
		File folder = new File(folderPath);
		File[] files = folder
				.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png")
						|| name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".pdf"));

		if (files == null || files.length == 0) {
			System.out.println("No valid image files found in the folder.");
			workbook.close();
			return;
		}

		int rowNumber = 1;
		Thread.sleep(2000);

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			Thread.sleep(2000);

			// Add Sr. No.
			Row row = sheet.createRow(rowNumber);
			row.createCell(0).setCellValue(i + 1);

			// Add Image
			try (FileInputStream fis = new FileInputStream(file)) {
				byte[] imageBytes = IOUtils.toByteArray(fis);
				int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
				CreationHelper helper = workbook.getCreationHelper();
				Drawing<?> drawing = sheet.createDrawingPatriarch();

				ClientAnchor anchor = helper.createClientAnchor();
				anchor.setCol1(1); // Column for image
				anchor.setRow1(rowNumber); // Row for image
				anchor.setCol2(2); // Image fits within one cell
				anchor.setRow2(rowNumber + 1);

				drawing.createPicture(anchor, pictureIdx);
			} catch (IOException e) {
				System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
			}

			// Add Testing Status
			row.createCell(2).setCellValue("Pending");

			rowNumber++;
		}

		// Save the Excel file
		try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
			workbook.write(fos);
			System.out.println("Excel file saved successfully at: " + excelFilePath);
		}

		// Close workbook
		workbook.close();
	}
}
