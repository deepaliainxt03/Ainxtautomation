//package APIs;
//
//import io.restassured.RestAssured;
//import io.restassured.response.Response;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.json.JSONObject;
//
//import java.io.FileOutputStream;
//import java.util.Base64;
//
//public class encryptiontesting {
//    private static final String ENCRYPTION_API = "https://sdrgeq0iq2.execute-api.ap-south-1.amazonaws.com/default/lmbd_ainxt_standard_encryption";
//    private static final String TARGET_API = "https://tpi7b58zk5.execute-api.ap-south-1.amazonaws.com/default/lmbd_get_status_eStamping";
//    private static final String DECRYPTION_API = "https://13zy9y85p1.execute-api.ap-south-1.amazonaws.com/default/lmbd_ainxt_standard_decryption";
//    private static final String EXCEL_FILE_PATH = "E://DecryptedResponse.xlsx"; // Path for saving Excel output
//    private static final String API_KEY = "064Z3w2N5gYCv7rSItNY"; // API Key for Encryption/Decryption API
//
//    public static void main(String[] args) {
//        try {
//            // 1️⃣ Encrypt the Request Body
//            JSONObject requestBody = new JSONObject();
//            requestBody.put("ReferenceNo", "Test-03");
//
//            String encryptedData = encryptData(requestBody.toString());
//            if (encryptedData == null) {
//                System.err.println("❌ Encryption failed! Aborting.");
//                return;
//            }
//
//            // 2️⃣ Send the Encrypted Request to the Target API
//            String encryptedResponse = sendEncryptedRequest(encryptedData);
//            if (encryptedResponse == null) {
//                System.err.println("❌ Target API request failed! Aborting.");
//                return;
//            }
//
//            // 3️⃣ Decrypt the API Response
//            String decryptedData = decryptData(encryptedResponse);
//            if (decryptedData == null) {
//                System.err.println("❌ Decryption failed! Aborting.");
//                return;
//            }
//
//            // 4️⃣ Save the Decrypted Response to an Excel file
//            saveJsonToExcel(decryptedData, EXCEL_FILE_PATH);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // 🔹 Step 1: Encrypt Data using Local API
//    public static String encryptData(String plainText) {
//        JSONObject requestPayload = new JSONObject().put("ReferenceNo", "Test-03");
//        System.out.println("🔹 Encrypting Data: " + requestPayload);
//
//        Response response = RestAssured.given()
//                .header("Content-Type", "application/json")
//                .body(requestPayload.toString())
//                .post(ENCRYPTION_API);
//
//        if (response.getStatusCode() != 200) {
//            System.err.println("❌ Encryption API failed with status: " + response.getStatusCode());
//            return null;
//        }
//
//        JSONObject jsonResponse = new JSONObject(response.getBody().asString());
//        System.out.println("✅ Encrypted Response: " + jsonResponse);
//
//        return jsonResponse.optString("encryptedText", null);
//    }
//
//    // 🔹 Step 2: Send Encrypted Data to the Target API
//    public static String sendEncryptedRequest(String encryptedData) {
//        JSONObject requestPayload = new JSONObject().put("data", encryptedData);
//        System.out.println("🔹 Sending Encrypted Request: " + requestPayload);
//
//        Response response = RestAssured.given()
//        		.header("api_key", API_KEY)
//                .header("Content-Type", "application/json")
//                .body(requestPayload.toString())
//                .post(TARGET_API);
//
//        if (response.getStatusCode() != 200) {
//            System.err.println("❌ Target API request failed with status: " + response.getStatusCode());
//            return null;
//        }
//
//        String responseBody = response.getBody().asString();
//        System.out.println("✅ Target API Response: " + responseBody);
//
//        return responseBody;
//    }
//
//    // 🔹 Step 3: Decrypt Data using Local API
//    public static String decryptData(String encryptedResponse) {
//        JSONObject requestPayload = new JSONObject().put("data", encryptedResponse);
//        System.out.println("🔹 Sending Decryption Request: " + requestPayload);
//
//        Response response = RestAssured.given()
//                .header("Content-Type", "application/json")
//                .body(requestPayload.toString())
//                .post(DECRYPTION_API);
//
//        if (response.getStatusCode() != 200) {
//            System.err.println("❌ Decryption API failed with status: " + response.getStatusCode());
//            return null;
//        }
//
//        String decryptedText = response.getBody().asString();
//        System.out.println("✅ Decrypted Response: " + decryptedText);
//
//        // ✅ Check if response is valid JSON and not Base64 encoded
//        if (isValidJson(decryptedText)) {
//            return decryptedText;
//        }
//
//        // ✅ If it's Base64, decode it
//        try {
//            byte[] decodedBytes = Base64.getDecoder().decode(decryptedText);
//            return new String(decodedBytes);
//        } catch (IllegalArgumentException e) {
//            System.err.println("❌ Response is not valid Base64 or already decrypted.");
//            return null;
//        }
//    }
//
//    // 🔹 Step 4: Save JSON Response to Excel
//    public static void saveJsonToExcel(String jsonData, String filePath) {
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("Response Data");
//
//            // Create Header Row
//            Row headerRow = sheet.createRow(0);
//            headerRow.createCell(0).setCellValue("Key");
//            headerRow.createCell(1).setCellValue("Value");
//
//            // Convert JSON String to JSON Object
//            JSONObject jsonObject = new JSONObject(jsonData);
//
//            int rowNum = 1;
//            for (String key : jsonObject.keySet()) {
//                Row row = sheet.createRow(rowNum++);
//                row.createCell(0).setCellValue(key);
//                row.createCell(1).setCellValue(jsonObject.optString(key, ""));
//            }
//
//            // Save to Excel File
//            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
//                workbook.write(fileOut);
//            }
//
//            System.out.println("✅ Decrypted response saved to: " + filePath);
//
//        } catch (Exception e) {
//            System.err.println("❌ Failed to save response to Excel.");
//            e.printStackTrace();
//        }
//    }
//
//    // ✅ Helper: Check if a String is Valid JSON
//    public static boolean isValidJson(String json) {
//        try {
//            new JSONObject(json);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//}
//

//package APIs;
//
//import io.restassured.RestAssured;
//import io.restassured.response.Response;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.json.JSONObject;
//
//import java.io.FileOutputStream;
//import java.util.Base64;
//
//public class encryptiontesting {
//    private static final String ENCRYPTION_API = "https://sdrgeq0iq2.execute-api.ap-south-1.amazonaws.com/default/lmbd_ainxt_standard_encryption";
//    private static final String TARGET_API = "https://tpi7b58zk5.execute-api.ap-south-1.amazonaws.com/default/lmbd_get_status_eStamping";
//    private static final String DECRYPTION_API = "https://13zy9y85p1.execute-api.ap-south-1.amazonaws.com/default/lmbd_ainxt_standard_decryption";
//    private static final String EXCEL_FILE_PATH = "E://DecryptedResponse 123.xlsx"; // Save path
//    private static final String API_KEY = "064Z3w2N5gYCv7rSItNY"; // API Key
//
//    public static void main(String[] args) {
//        try {
//            // 1️⃣ Encrypt the Request Body
//            JSONObject requestBody = new JSONObject();
//            requestBody.put("ReferenceNo", "Test-03");
//
//            String encryptedData = encryptData(requestBody.toString());
//            if (encryptedData == null) {
//                System.err.println("❌ Encryption failed! Aborting.");
//                return;
//            }
//
//            // 2️⃣ Send the Encrypted Request to the Target API
//            String encryptedResponse = sendEncryptedRequest(encryptedData);
//            if (encryptedResponse == null) {
//                System.err.println("❌ Target API request failed! Aborting.");
//                return;
//            }
//
//            // 3️⃣ Decrypt the API Response
//            String decryptedData = decryptData(encryptedResponse);
//            if (decryptedData == null) {
//                System.err.println("❌ Decryption failed! Aborting.");
//                return;
//            }
//
//            // 4️⃣ Save the Decrypted Response to an Excel file
//            saveJsonToExcel(decryptedData, EXCEL_FILE_PATH);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // 🔹 Step 1: Encrypt Data
//    public static String encryptData(String plainText) {
//        JSONObject requestPayload = new JSONObject().put("ReferenceNo", "Test-03");
//        System.out.println("🔹 Encrypting Data: " + requestPayload);
//
//        Response response = RestAssured.given()
//                .header("Content-Type", "application/json")
//                .body(requestPayload.toString())
//                .post(ENCRYPTION_API);
//
//        if (response.getStatusCode() != 200) {
//            System.err.println("❌ Encryption API failed with status: " + response.getStatusCode());
//            return null;
//        }
//
//        JSONObject jsonResponse = new JSONObject(response.getBody().asString());
//        System.out.println("✅ Encrypted Response: " + jsonResponse);
//
//        return jsonResponse.optString("encryptedText", null);
//    }
//
//    // 🔹 Step 2: Send Encrypted Data to Target API
//    public static String sendEncryptedRequest(String encryptedData) {
//        JSONObject requestPayload = new JSONObject().put("data", encryptedData);
//        System.out.println("🔹 Sending Encrypted Request: " + requestPayload);
//
//        Response response = RestAssured.given()
//                .header("api_key", API_KEY)
//                .header("Content-Type", "application/json")
//                .body(requestPayload.toString())
//                .post(TARGET_API);
//
//        if (response.getStatusCode() != 200) {
//            System.err.println("❌ Target API request failed with status: " + response.getStatusCode());
//            return null;
//        }
//
//        String responseBody = response.getBody().asString();
//        System.out.println("✅ Target API Response: " + responseBody);
//
//        return responseBody;
//    }
//
//    // 🔹 Step 3: Decrypt Data
//    public static String decryptData(String encryptedResponse) {
//        JSONObject requestPayload = new JSONObject().put("data", encryptedResponse);
//        System.out.println("🔹 Sending Decryption Request: " + requestPayload);
//
//        Response response = RestAssured.given()
//                .header("Content-Type", "application/json")
//                .body(requestPayload.toString())
//                .post(DECRYPTION_API);
//
//        if (response.getStatusCode() != 200) {
//            System.err.println("❌ Decryption API failed with status: " + response.getStatusCode());
//            return null;
//        }
//
//        String decryptedText = response.getBody().asString();
//        System.out.println("✅ Decrypted Response: " + decryptedText);
//
//        // ✅ Check if response is valid JSON and not Base64 encoded
//        if (isValidJson(decryptedText)) {
//            return decryptedText;
//        }
//
//        // ✅ If it's Base64, decode it
//        try {
//            byte[] decodedBytes = Base64.getDecoder().decode(decryptedText);
//            return new String(decodedBytes);
//        } catch (IllegalArgumentException e) {
//            System.err.println("❌ Response is not valid Base64 or already decrypted.");
//            return null;
//        }
//    }
//
//    // 🔹 Step 4: Save JSON Response to a Single Excel Cell
//    public static void saveJsonToExcel(String jsonData, String filePath) {
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("Response Data");
//
//            // Create a single row and column to store JSON
//            Row row = sheet.createRow(0);
//            Cell cell = row.createCell(0);
//            cell.setCellValue(jsonData); // Store full JSON in one cell
//
//            // Auto-adjust column width
//            sheet.autoSizeColumn(0);
//
//            // Save to Excel file
//            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
//                workbook.write(fileOut);
//            }
//
//            System.out.println("✅ JSON response saved to Excel: " + filePath);
//
//        } catch (Exception e) {
//            System.err.println("❌ Failed to save response to Excel.");
//            e.printStackTrace();
//        }
//    }
//
//    // ✅ Helper: Check if a String is Valid JSON
//    public static boolean isValidJson(String json) {
//        try {
//            new JSONObject(json);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//}

package APIs;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.Base64;

public class encryptiontesting {
	private static final String ENCRYPTION_API = "https://sdrgeq0iq2.execute-api.ap-south-1.amazonaws.com/default/lmbd_ainxt_standard_encryption";
	private static final String TARGET_API = "https://tpi7b58zk5.execute-api.ap-south-1.amazonaws.com/default/lmbd_get_status_eStamping";
	private static final String DECRYPTION_API = "https://13zy9y85p1.execute-api.ap-south-1.amazonaws.com/default/lmbd_ainxt_standard_decryption";
	private static final String EXCEL_FILE_PATH = "E://DecryptedRes.xlsx"; // Save path
	private static final String API_KEY = "064Z3w2N5gYCv7rSItNY"; // API Key

	public static void main(String[] args) {
		try {
			// 1️⃣ Encrypt the Request Body
			JSONObject requestBody = new JSONObject();
			requestBody.put("ReferenceNo", "Test-03");

			String encryptedData = encryptData(requestBody.toString());
			if (encryptedData == null) {
				System.err.println("❌ Encryption failed! Aborting.");
				return;
			}

			// 2️⃣ Send the Encrypted Request to the Target API
			String encryptedResponse = sendEncryptedRequest(encryptedData);
			if (encryptedResponse == null) {
				System.err.println("❌ Target API request failed! Aborting.");
				return;
			}

			// 3️⃣ Decrypt the API Response
			String decryptedData = decryptData(encryptedResponse);
			if (decryptedData == null) {
				System.err.println("❌ Decryption failed! Aborting.");
				return;
			}

			// 4️⃣ Save the Formatted JSON Response to an Excel file
			saveJsonToExcel(decryptedData, EXCEL_FILE_PATH);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 🔹 Step 1: Encrypt Data
	public static String encryptData(String plainText) {
		JSONObject requestPayload = new JSONObject().put("ReferenceNo", "Test-03");
		System.out.println("🔹 Encrypting Data: " + requestPayload);

		Response response = RestAssured.given().header("Content-Type", "application/json")
				.body(requestPayload.toString()).post(ENCRYPTION_API);

		if (response.getStatusCode() != 200) {
			System.err.println("❌ Encryption API failed with status: " + response.getStatusCode());
			return null;
		}

		JSONObject jsonResponse = new JSONObject(response.getBody().asString());
		System.out.println("✅ Encrypted Response: " + jsonResponse);

		return jsonResponse.optString("encryptedText", null);
	}

	// 🔹 Step 2: Send Encrypted Data to Target API
	public static String sendEncryptedRequest(String encryptedData) {
		JSONObject requestPayload = new JSONObject().put("data", encryptedData);
		System.out.println("🔹 Sending Encrypted Request: " + requestPayload);

		Response response = RestAssured.given().header("api_key", API_KEY).header("Content-Type", "application/json")
				.body(requestPayload.toString()).post(TARGET_API);

		if (response.getStatusCode() != 200) {
			System.err.println("❌ Target API request failed with status: " + response.getStatusCode());
			return null;
		}

		String responseBody = response.getBody().asString();
		System.out.println("✅ Target API Response: " + responseBody);

		return responseBody;
	}

	// 🔹 Step 3: Decrypt Data
	public static String decryptData(String encryptedResponse) {
		JSONObject requestPayload = new JSONObject().put("data", encryptedResponse);
		System.out.println("🔹 Sending Decryption Request: " + requestPayload);

		Response response = RestAssured.given().header("Content-Type", "application/json")
				.body(requestPayload.toString()).post(DECRYPTION_API);

		if (response.getStatusCode() != 200) {
			System.err.println("❌ Decryption API failed with status: " + response.getStatusCode());
			return null;
		}

		String decryptedText = response.getBody().asString();
		System.out.println("✅ Decrypted Response: " + decryptedText);

		// ✅ Check if response is valid JSON and not Base64 encoded
		if (isValidJson(decryptedText)) {
			return formatJson(decryptedText); // 🔥 Now beautifies JSON
		}

		// ✅ If it's Base64, decode it
		try {
			byte[] decodedBytes = Base64.getDecoder().decode(decryptedText);
			return formatJson(new String(decodedBytes)); // 🔥 Now beautifies JSON after decoding
		} catch (IllegalArgumentException e) {
			System.err.println("❌ Response is not valid Base64 or already decrypted.");
			return null;
		}
	}

	// 🔹 Step 4: Save Formatted JSON to a Single Excel Cell
	public static void saveJsonToExcel(String jsonData, String filePath) {
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Response Data");

			// Create a single row and column to store JSON
			Row row = sheet.createRow(0);
			Cell cell = row.createCell(0);
			cell.setCellValue(jsonData); // Store formatted JSON in one cell

			// Auto-adjust column width
			sheet.autoSizeColumn(0);

			// Save to Excel file
			try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
				workbook.write(fileOut);
			}

			System.out.println("✅ Formatted JSON saved to Excel: " + filePath);

		} catch (Exception e) {
			System.err.println("❌ Failed to save response to Excel.");
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

	// ✅ Helper: Beautify JSON
	public static String formatJson(String json) {
		try {
			JSONObject jsonObject = new JSONObject(json);
			return jsonObject.toString(4); // Indentation level 4 for readability
		} catch (Exception e) {
			System.err.println("❌ Failed to format JSON.");
			return json; // Return original if formatting fails
		}
	}
}
