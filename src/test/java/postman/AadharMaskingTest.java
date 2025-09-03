package postman;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Produces an Excel sheet that shows: Test_ID | File Name | Image | Masked
 * Image | API Response | Testing Status
 */
public class AadharMaskingTest {

	/* ─────── CONFIG ─────── */

	private static final Path INPUT_FOLDER = Paths.get("E:\\DOCUMENTS\\DOCUMENTS\\AADHAR\\aadhar samples\\Tested Samples");
	private static final Path OUTPUT_XLSX = Paths.get("E:/Aaadhar Colored.xlsx");

	private static final String API_URL = "https://tpdxsyby0l.execute-api.ap-south-1.amazonaws.com/default/docker_pan";
	private static final String API_KEY_HEADER = "x-api-key";
	private static final String API_KEY_VALUE = "rJZGX7mbpW8NSgydtOfAcaZZ1xT3xdcl1gBCOhtX";
//	private static final String AUTH_KEY_HEADER = "authorizationToken";
//	private static final String AUTH_KEY_VALUE = "W5O8b5e1GW5KWnaM";

	/** Extension → Apache-POI picture type */
	private static final Map<String, Integer> EXT_TO_TYPE = new HashMap<>();
	static {
		EXT_TO_TYPE.put("jpg", Workbook.PICTURE_TYPE_JPEG);
		EXT_TO_TYPE.put("jpeg", Workbook.PICTURE_TYPE_JPEG);
		EXT_TO_TYPE.put("png", Workbook.PICTURE_TYPE_PNG);
		 //EXT_TO_TYPE.put("pdf", Workbook.PICTURE_TYPE_PDF);
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

		/* headers */
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
		int[] rowCounter = { 1 };

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
		String wrapped64 = "b'" + plain64 + "'";

		/* 2️⃣ Call API */
		JSONObject resp = callApi(wrapped64);

		/* 3️⃣ Create Excel row */
		Row row = sheet.createRow(rowIdx);
		row.createCell(0).setCellValue(rowIdx); // Test_ID
		row.createCell(1).setCellValue(imgPath.getFileName().toString()); // File Name

		/* Original image */
		embedPic(origBytes, rowIdx, 2, getPoiType(imgPath));

		/* Masked image (if any) and trimming base64 */
		// Clean and trim masking base64
		// Clean and trim masking base64
		String maskedB64 = cleanBase64(resp.optString("masking", ""));
		String trimmedMasked = maskedB64.length() > 30 ? maskedB64.substring(0, 30) : maskedB64;

		// Embed masked image if valid
		if (!maskedB64.isEmpty()) {
			try {
				byte[] maskedBytes = Base64.decodeBase64(maskedB64);
				embedPic(maskedBytes, rowIdx, 4, detectPoiType(maskedBytes)); // Col 4 = Masked Image
			} catch (IllegalArgumentException e) {
				System.err.println("Bad base64 for masked image: " + imgPath.getFileName());
			}
		}

		// Ensure status comes before masking in output JSON
		Map<String, Object> orderedResp = new LinkedHashMap<>();
		orderedResp.put("status", resp.optInt("status", -1));
		orderedResp.put("masking", trimmedMasked);
		orderedResp.put("ismasked", resp.optString("ismasked", ""));
		orderedResp.put("remark", resp.optString("remark", ""));

		JSONObject trimmedResp = new JSONObject(orderedResp);
		row.createCell(3).setCellValue(trimmedResp.toString(2)); // Col 3 = API Response


		/* Testing Status left blank (column 5) */
		row.createCell(5).setCellValue("");
	}

	/* ─────── HTTP helper ─────── */

	private JSONObject callApi(String wrappedB64) throws IOException {
		HttpPost post = new HttpPost(API_URL);
		post.setHeader(API_KEY_HEADER, API_KEY_VALUE);
		//post.setHeader(AUTH_KEY_HEADER, AUTH_KEY_VALUE);

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
		CreationHelper hlp = wb.getCreationHelper();
		Drawing<?> dr = sheet.createDrawingPatriarch();

		XSSFClientAnchor anc = (XSSFClientAnchor) hlp.createClientAnchor();

		// Span the picture across more horizontal space for centering effect
		anc.setRow1(rowIdx);
		anc.setRow2(rowIdx + 1);

		// Use part of the column before and after to center
		anc.setCol1(colIdx);
		anc.setDx1(Units.toEMU(20)); // Shift a bit from left
		anc.setCol2(colIdx + 1);
		anc.setDx2(Units.toEMU(-20)); // Shift a bit from right


		dr.createPicture(anc, picIdx);

		sheet.getRow(rowIdx).setHeightInPoints(80);
		sheet.setColumnWidth(colIdx, 20 * 256);
	}

	private static int getPoiType(Path p) {
		String ext = p.getFileName().toString().replaceFirst(".*\\.", "").toLowerCase(Locale.ROOT);
		return EXT_TO_TYPE.getOrDefault(ext, Workbook.PICTURE_TYPE_JPEG);
	}

	/* ─────── Utility helpers ─────── */

	/**
	 * Strip whitespace/newlines/quotes and optional python b'..' literal wrapper
	 */
	private static String cleanBase64(String s) {
		if (s == null)
			return "";
		s = s.trim();
		if (s.startsWith("b'") && s.endsWith("'"))
			s = s.substring(2, s.length() - 1);
		return s.replaceAll("[\\r\\n\" ]", "");
	}

	/** Peek at first few bytes to decide JPEG vs PNG for POI */
	private static int detectPoiType(byte[] bytes) {
		return (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8)
				? Workbook.PICTURE_TYPE_JPEG
				: Workbook.PICTURE_TYPE_PNG;
	}
}
