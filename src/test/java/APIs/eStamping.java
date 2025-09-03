package APIs;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import java.io.FileOutputStream;
import java.util.Base64;

public class eStamping {
    private static final String ENCRYPTION_API = "https://sdrgeq0iq2.execute-api.ap-south-1.amazonaws.com/default/lmbd_ainxt_standard_encryption";
    private static final String TARGET_API = "https://tpi7b58zk5.execute-api.ap-south-1.amazonaws.com/default/lmbd_get_status_eStamping";
    private static final String DECRYPTION_API = "https://13zy9y85p1.execute-api.ap-south-1.amazonaws.com/default/lmbd_ainxt_standard_decryption";
    //private static final String API_KEY = "Wb8FlKWx8nCCIdHmNssf"; // API Key for Encryption/Decryption API
    private static final String EXCEL_FILE_PATH = "E://DecryptedResponse.xlsx"; // Excel File Name

    public static void main(String[] args) {
        try {
            // 1️⃣ Encrypt the Request Body
            JSONObject requestBody = new JSONObject();
            requestBody.put("ReferenceNo", "Test-03");

            String encryptedData = encryptData(requestBody.toString());
            System.out.println("Encrypted Data: " + encryptedData);

            // 2️⃣ Send the Encrypted Request to the Target API
            String encryptedResponse = sendEncryptedRequest(encryptedData);
            System.out.println("Encrypted Response from API: " + encryptedResponse);

            // 3️⃣ Decrypt the API Response
            String decryptedData = decryptData(encryptedResponse);
            System.out.println("Decrypted Response: " + decryptedData);

            // 4️⃣ Save the Decrypted Response to an Excel file
            saveJsonToExcel(decryptedData, EXCEL_FILE_PATH);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 Step 1: Encrypt Data using Local API
    public static String encryptData(String plainText) {
        Response response = RestAssured.given()
                //.header("API_KEY", API_KEY)
                .header("Content-Type", "application/json")
                .body(new JSONObject().put("data", plainText).toString()) // Sending plain text inside "data"
                .post(ENCRYPTION_API);

        return response.getBody().asString();
    }

    // 🔹 Step 2: Send Encrypted Data to the Target API
    public static String sendEncryptedRequest(String encryptedData) {
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(new JSONObject().put("data", encryptedData).toString()) // Sending encrypted data
                .post(TARGET_API);

        return response.getBody().asString();
    }

    // 🔹 Step 3: Decrypt Data using Local API
    public static String decryptData(String encryptedResponse) {
        Response response = RestAssured.given()
                //.header("API_KEY", API_KEY)
                .header("Content-Type", "application/json")
                .body(new JSONObject().put("data", encryptedResponse).toString()) // Sending encrypted response
                .post(DECRYPTION_API);

        String decryptedText = response.getBody().asString();

        // ✅ Check if response is already JSON and not Base64
        if (isValidJson(decryptedText)) {
            return decryptedText; // No need to decode
        }

        // ✅ If it's Base64, decode it
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(decryptedText);
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Response is not valid Base64 or already decrypted: " + decryptedText);
        }
    }

    // 🔹 Step 4: Save JSON Response to Excel
    public static void saveJsonToExcel(String jsonData, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Response Data");

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Key");
            headerRow.createCell(1).setCellValue("Value");

            // Convert JSON String to JSON Object
            JSONObject jsonObject = new JSONObject(jsonData);

            int rowNum = 1;
            for (String key : jsonObject.keySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(key);
                row.createCell(1).setCellValue(jsonObject.get(key).toString());
            }

            // Save to Excel File
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            System.out.println("Decrypted response saved to: " + filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Helper: Check if a String is Valid JSON
    public static boolean isValidJson(String json) {
        try {
            new JSONObject(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
