package APIs;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.testng.annotations.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Produces an Excel sheet that shows: Test_ID | File Name | Image | API
 * Response | Masked Image | Testing Status and the API-response JSON now
 * contains every field from Document_type through item.details.format_match
 * (with masked_value and faceString trimmed to 10 characters).
 */
public class AadharMaskingTest {

	/* ─────── CONFIG ─────── */

	private static final Path INPUT_FOLDER = Paths
			.get("E:\\DOCUMENTS\\DOCUMENTS\\AADHAR\\Aadhar imgs    ");
	private static final Path OUTPUT_XLSX = Paths.get("E:/Aaadhar Masked and Cropped.xlsx");

	private static final String API_URL = "https://4c2qvpodza.execute-api.ap-south-1.amazonaws.com/default/dockertest";
	//private static final String API_KEY_HEADER = "x-api-key";
	//private static final String API_KEY_VALUE = "rJZGX7mbpW8NSgydtOfAcaZZ1xT3xdcl1gBCOhtX";
	// If you use an auth header, add it back here.
	// private static final String AUTH_KEY_HEADER = "authorizationToken";
	// private static final String AUTH_KEY_VALUE = "W5O8b5e1GW5KWnaM";

	/** Extension → Apache-POI picture type */
	private static final Map<String, Integer> EXT_TO_TYPE = new HashMap<>();
	static {
		EXT_TO_TYPE.put("jpg", Workbook.PICTURE_TYPE_JPEG);
		EXT_TO_TYPE.put("jpeg", Workbook.PICTURE_TYPE_JPEG);
		EXT_TO_TYPE.put("png", Workbook.PICTURE_TYPE_PNG);
		// EXT_TO_TYPE.put("bmp", Workbook.PICTURE_TYPE_PNG); // bmp → PNG preview
	}

	/* ─────── FIELDS ─────── */
	private CloseableHttpClient http;
	private Workbook wb;
	private Sheet sheet;

	/* ───── TestNG life-cycle ───── */

	@BeforeClass
	public void setUp() throws IOException {
		http = HttpClients.createDefault();
		wb = new XSSFWorkbook();
		sheet = wb.createSheet("Aadhar Colored");

		// Header row
		Row head = sheet.createRow(0);
		head.createCell(0).setCellValue("Test_ID");
		head.createCell(1).setCellValue("File Name");
		head.createCell(2).setCellValue("Image");
		head.createCell(3).setCellValue("API Response");
		head.createCell(4).setCellValue("Masked Image");
		head.createCell(5).setCellValue("Testing Status");
	}

	@Test
	public void runMaskingFlow() throws Exception {
		String imgRegex = "(?i).*\\.(png|jpe?g|bmp)$";
		final int[] rowCounter = { 1 };

		try (Stream<Path> imgs = Files.list(INPUT_FOLDER)) {
			imgs.filter(p -> p.getFileName().toString().matches(imgRegex)).forEach(img -> {
				try {
					processImage(img, rowCounter[0]++);
				} catch (Exception e) {
					System.err.println("Error with " + img + " : " + e.getMessage());
				}
			});
		}
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() throws Exception {
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);

		try (OutputStream out = Files.newOutputStream(OUTPUT_XLSX)) {
			wb.write(out);
		}
		wb.close();
		http.close();
	}

	/* ─────── CORE ─────── */

	private void processImage(Path imgPath, int rowIdx) throws IOException {

		/* 1️⃣ Read + encode + wrap */
		byte[] origBytes = Files.readAllBytes(imgPath);
		String plain64 = Base64.encodeBase64String(origBytes).trim();
		String wrapped64 = "b'" + plain64 + "'"; // the API expects Python-style wrapper

		/* 2️⃣ Call API */
		JSONObject resp = callApi(wrapped64);

		/* 3️⃣ Prepare Excel row */
		Row row = sheet.createRow(rowIdx);
		row.createCell(0).setCellValue(rowIdx); // Test_ID
		row.createCell(1).setCellValue(imgPath.getFileName().toString()); // File Name

		/* 3-a Original image preview */
		embedPic(origBytes, rowIdx, 2, getPoiType(imgPath));

		/* 4️⃣ Extract every field (Document_type → format_match) */
		Map<String, Object> fields = new LinkedHashMap<>();

		// top-level
		fields.put("Document_type", getNestedString(resp, "Document_type"));

		// Aadhaar section
		fields.put("item.details.aadhaar.ismasked", getNestedString(resp, "item", "details", "aadhaar", "ismasked"));
		fields.put("item.details.aadhaar.qrstatus", getNestedString(resp, "item", "details", "aadhaar", "qrstatus"));

		String maskedVal = getNestedString(resp, "item", "details", "aadhaar", "masked_value");
		fields.put("item.details.aadhaar.masked_value", trim10(maskedVal));

		fields.put("item.details.aadhaar.is_aadhar_present",
				getNestedString(resp, "item", "details", "aadhaar", "is_aadhar_present"));
		fields.put("item.details.aadhaar.value", getNestedString(resp, "item", "details", "aadhaar", "value"));
		fields.put("item.details.aadhaar.unmasked_value",
				getNestedString(resp, "item", "details", "aadhaar", "unmasked_value"));

		// Address
		fields.put("item.details.address.care_of", getNestedString(resp, "item", "details", "address", "care_of"));
		fields.put("item.details.address.city", getNestedString(resp, "item", "details", "address", "city"));
		fields.put("item.details.address.district", getNestedString(resp, "item", "details", "address", "district"));
		fields.put("item.details.address.house_number",
				getNestedString(resp, "item", "details", "address", "house_number"));
		fields.put("item.details.address.landmark", getNestedString(resp, "item", "details", "address", "landmark"));
		fields.put("item.details.address.line1", getNestedString(resp, "item", "details", "address", "line1"));
		fields.put("item.details.address.line2", getNestedString(resp, "item", "details", "address", "line2"));
		fields.put("item.details.address.pin", getNestedString(resp, "item", "details", "address", "pin"));
		fields.put("item.details.address.state", getNestedString(resp, "item", "details", "address", "state"));

		// DOB
		fields.put("item.details.dob", getNestedString(resp, "item", "details", "dob"));

		// Face
		String faceString = getNestedString(resp, "item", "details", "face", "faceString");
		fields.put("item.details.face.faceString", trim10(faceString));

		// Other personal relationships
		fields.put("item.details.gender.value", getNestedString(resp, "item", "details", "gender", "value"));
		fields.put("item.details.husband.value", getNestedString(resp, "item", "details", "husband", "value"));
		fields.put("item.details.father.value", getNestedString(resp, "item", "details", "father", "value"));
		fields.put("item.details.daughter_of.value", getNestedString(resp, "item", "details", "daughter_of", "value"));

		// Colour / screenshot flags
		fields.put("item.details.aadhaar.color", getNestedString(resp, "item", "details", "aadhaar", "color"));
		fields.put("item.details.aadhaar.is_screenshot",
				getNestedString(resp, "item", "details", "aadhaar", "is_screenshot"));

		// Format match
		fields.put("item.details.format_match", getNestedString(resp, "item", "details", "format_match"));

		/* 5️⃣ Masked image preview (optional) */
		if (!maskedVal.isEmpty()) {
			try {
				byte[] maskedBytes = Base64.decodeBase64(maskedVal);
				embedPic(maskedBytes, rowIdx, 4, detectPoiType(maskedBytes)); // Col-4 Masked Image
			} catch (IllegalArgumentException ignore) {
				/* not an image → skip */ }
		}

		/* 6️⃣ Write JSON to column 3 */
		row.createCell(3).setCellValue(new JSONObject(fields).toString(2));

		/* 7️⃣ Testing Status (col-5) left blank intentionally */
		row.createCell(5).setCellValue("");
	}

	/* ─────── HTTP helper ─────── */

	private JSONObject callApi(String wrappedB64) throws IOException {
		HttpPost post = new HttpPost(API_URL);
		//post.setHeader(API_KEY_HEADER, API_KEY_VALUE);
		// post.setHeader(AUTH_KEY_HEADER, AUTH_KEY_VALUE);

		HttpEntity entity = MultipartEntityBuilder.create().addTextBody("im", wrappedB64, ContentType.TEXT_PLAIN)
				.build();

		post.setEntity(entity);

		try (CloseableHttpResponse res = http.execute(post)) {
			String body = new String(IOUtils.toByteArray(res.getEntity().getContent()), "UTF-8");
			return new JSONObject(body);
		}
	}

	/* ─────── Excel embedding helpers ─────── */

	private void embedPic(byte[] bytes, int rowIdx, int colIdx, int poiType) {
		int picIdx = wb.addPicture(bytes, poiType);
		CreationHelper helper = wb.getCreationHelper();
		Drawing<?> drawing = sheet.createDrawingPatriarch();

		XSSFClientAnchor anchor = (XSSFClientAnchor) helper.createClientAnchor();
		anchor.setRow1(rowIdx);
		anchor.setRow2(rowIdx + 1);
		anchor.setCol1(colIdx);
		anchor.setDx1(Units.toEMU(20)); // slight left padding
		anchor.setCol2(colIdx + 1);
		anchor.setDx2(Units.toEMU(-20)); // slight right padding

		drawing.createPicture(anchor, picIdx);

		sheet.getRow(rowIdx).setHeightInPoints(80);
		sheet.setColumnWidth(colIdx, 20 * 256);
	}

	private static int getPoiType(Path p) {
		String ext = p.getFileName().toString().replaceFirst(".*\\.", "").toLowerCase(Locale.ROOT);
		return EXT_TO_TYPE.getOrDefault(ext, Workbook.PICTURE_TYPE_JPEG);
	}

	/* ─────── Utility helpers ─────── */

	/** Safely walk down a JSON object tree and return string or "" */
	private static String getNestedString(JSONObject root, String... path) {
		JSONObject obj = root;
		int last = path.length - 1;
		for (int i = 0; i < last; i++) {
			if (!obj.has(path[i]))
				return "";
			obj = obj.optJSONObject(path[i]);
			if (obj == null)
				return "";
		}
		return obj.optString(path[last], "");
	}

	/** Keep only first 10 characters (if longer) */
	private static String trim10(String s) {
		return (s == null) ? "" : (s.length() > 10 ? s.substring(0, 10) : s);
	}

	/** Quick JPEG vs PNG detection for embedded bytes */
	private static int detectPoiType(byte[] bytes) {
		return (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8)
				? Workbook.PICTURE_TYPE_JPEG
				: Workbook.PICTURE_TYPE_PNG;
	}
}
