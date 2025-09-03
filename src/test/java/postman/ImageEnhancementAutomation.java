package postman;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.Base64;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.imageio.ImageIO;

public class ImageEnhancementAutomation {

    public static void main(String[] args) {
        try {
            // Define paths
            String inputFolder = "C:\\Users\\deepa\\Downloads\\image_size_Compress"; // Folder containing images to upload
            String outputFolder = "C:\\Users\\deepa\\Downloads\\AfterCompress"; // Folder to save enhanced images
            String excelFilePath = "E:\\ImageEnhancementLog.xlsx"; // Excel file for logging
            
            File inputDir = new File(inputFolder);
            File[] imageFiles = inputDir.listFiles();

            if (imageFiles == null || imageFiles.length == 0) {
                System.out.println("No images found in the input folder.");
                return;
            }

            // Create output folder if it doesn't exist
            new File(outputFolder).mkdirs();

            // Set up Excel workbook and sheet
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Image Enhancement Log");

            // Add headers to the Excel sheet
            String[] headers = {"Sr No", "Filename", "API Response (1/0)", "Size Before (bytes)", "Size After (bytes)"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int serialNumber = 1;

            for (File file : imageFiles) {
                if (!file.isFile()) continue;

                // Read and convert the image to Base64
                byte[] imageBytes = Files.readAllBytes(file.toPath());
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                long sizeBefore = imageBytes.length;

                System.out.println("Processing image: " + file.getName());

                // Call the API
                String apiResponseBase64 = callApi(base64Image);

                // Initialize variables for size after
                long sizeAfter = 0;

                if (apiResponseBase64 != null) {
                    // Decode and save the image from API response
                    String outputFileName = outputFolder + "/" + file.getName();
                    byte[] responseImageBytes = Base64.getDecoder().decode(apiResponseBase64);
                    sizeAfter = responseImageBytes.length;

                    try (FileOutputStream fos = new FileOutputStream(outputFileName)) {
                        fos.write(responseImageBytes);
                    }

                    System.out.println("Image enhanced and saved: " + outputFileName);
                } else {
                    System.err.println("Failed to process image: " + file.getName());
                }

                // Add details to the Excel file
                Row row = sheet.createRow(serialNumber);
                row.createCell(0).setCellValue(serialNumber);
                row.createCell(1).setCellValue(file.getName());
                row.createCell(2).setCellValue(apiResponseBase64 != null ? 1 : 0);
                row.createCell(3).setCellValue(sizeBefore);
                row.createCell(4).setCellValue(sizeAfter);

                serialNumber++;
            }

            // Save the Excel file
            try (FileOutputStream excelOut = new FileOutputStream(excelFilePath)) {
                workbook.write(excelOut);
            }

            workbook.close();
            System.out.println("Processing complete. Details logged in " + excelFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String callApi(String base64Image) {
        try {
            // Define the API endpoint
            URL url = new URL("https://9b9igbrtqi.execute-api.ap-south-1.amazonaws.com/default/image-enhancement");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set up the request headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("x-api-key", "Cy0ouu3mmE2PE9gbjhhS55dV9np7IDpg4XA2cpOt");
            connection.setRequestProperty("authorizationToken", "1ZnWuE0T0yc1bFtg");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary");

            // Enable output for the connection
            connection.setDoOutput(true);

            // Create the multipart request body
            String boundary = "----WebKitFormBoundary";
            String lineBreak = "\r\n";
            StringBuilder requestBody = new StringBuilder();

            requestBody.append("--").append(boundary).append(lineBreak)
                       .append("Content-Disposition: form-data; name=\"im\"").append(lineBreak)
                       .append(lineBreak)
                       .append(base64Image).append(lineBreak)
                       .append("--").append(boundary).append("--").append(lineBreak);

            // Write the request body to the connection
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.toString().getBytes());
            outputStream.flush();
            outputStream.close();

            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the API response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                System.out.println("API Response: " + response.toString());
                return response.toString();
            } else {
                // Log the error response
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();

                System.err.println("API Error Response: " + errorResponse.toString());
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
