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
import java.util.*;
import java.util.stream.Stream;

public class Aadhartestingwithmaskingandcropping {

	private static final Path INPUT_FOLDER = Paths.get("C:\\Users\\deepa\\OneDrive\\Pictures\\Aadhar masking and cropping");
	private static final Path OUTPUT_XLSX = Paths.get("E:/Aaadhar Masking and Cropping.xlsx");

	private static final String API_URL = "https://4c2qvpodza.execute-api.ap-south-1.amazonaws.com/default/dockertest";

	private static final Map<String, Integer> EXT_TO_TYPE = new HashMap<>();
	static {
		EXT_TO_TYPE.put("jpg", Workbook.PICTURE_TYPE_JPEG);
		EXT_TO_TYPE.put("jpeg", Workbook.PICTURE_TYPE_JPEG);
		EXT_TO_TYPE.put("png", Workbook.PICTURE_TYPE_PNG);
	}

	private CloseableHttpClient http;
	private Workbook wb;
	private Sheet sheet;

	@BeforeClass
	public void setUp() throws IOException {
		http = HttpClients.createDefault();
		wb = new XSSFWorkbook();
		sheet = wb.createSheet("Aadhar Colored");

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

	private void processImage(Path imgPath, int rowIdx) throws IOException {
		byte[] origBytes = Files.readAllBytes(imgPath);
		String plain64 = Base64.encodeBase64String(origBytes).trim();
		String wrapped64 = "b'" + plain64 + "'";

		JSONObject resp = callApi(wrapped64);
		Row row = sheet.createRow(rowIdx);
		row.createCell(0).setCellValue(rowIdx);
		row.createCell(1).setCellValue(imgPath.getFileName().toString());
		embedPic(origBytes, rowIdx, 2, getPoiType(imgPath));

		String maskedB64 = cleanBase64(resp.optString("masking", ""));
		if (!maskedB64.isEmpty()) {
			try {
				byte[] maskedBytes = Base64.decodeBase64(maskedB64);
				embedPic(maskedBytes, rowIdx, 4, detectPoiType(maskedBytes));
			} catch (IllegalArgumentException e) {
				System.err.println("Bad base64 for masked image: " + imgPath.getFileName());
			}
		}

		try {
			JSONObject item = resp.optJSONObject("item");
			JSONObject details = item != null ? item.optJSONObject("details") : null;

			JSONObject aadhaar = details != null ? details.optJSONObject("aadhaar") : null;
			JSONObject address = details != null ? details.optJSONObject("address") : null;
			JSONObject face = details != null ? details.optJSONObject("face") : null;
			JSONObject gender = details != null ? details.optJSONObject("gender") : null;
			JSONObject husband = details != null ? details.optJSONObject("husband") : null;
			JSONObject name = details != null ? details.optJSONObject("name") : null;
			JSONObject father = details != null ? details.optJSONObject("father") : null;
			JSONObject daughter = details != null ? details.optJSONObject("daughter_of") : null;

			Map<String, Object> flatMap = new LinkedHashMap<>();
			flatMap.put("document_type", details != null ? details.optString("document_type", "") : "");
			flatMap.put("aadhaar.ismasked", aadhaar != null ? aadhaar.optString("ismasked", "") : "");
			flatMap.put("aadhaar.qrstatus", aadhaar != null ? aadhaar.optString("qrstatus", "") : "");
			flatMap.put("aadhaar.masked_value", aadhaar != null ? trimString(aadhaar.optString("masked_value", ""), 10) : "");
			flatMap.put("aadhaar.is_aadhar_present", aadhaar != null ? aadhaar.optString("is_aadhar_present", "") : "");
			flatMap.put("aadhaar.value", aadhaar != null ? aadhaar.optString("value", "") : "");
			flatMap.put("aadhaar.unmasked_value", aadhaar != null ? aadhaar.optString("unmasked_value", "") : "");
			flatMap.put("address.care_of", address != null ? address.optString("care_of", "") : "");
			flatMap.put("address.city", address != null ? address.optString("city", "") : "");
			flatMap.put("address.district", address != null ? address.optString("district", "") : "");
			flatMap.put("address.house_number", address != null ? address.optString("house_number", "") : "");
			flatMap.put("address.landmark", address != null ? address.optString("landmark", "") : "");
			flatMap.put("address.line1", address != null ? address.optString("line1", "") : "");
			flatMap.put("address.line2", address != null ? address.optString("line2", "") : "");
			flatMap.put("address.pin", address != null ? address.optString("pin", "") : "");
			flatMap.put("address.state", address != null ? address.optString("state", "") : "");
			flatMap.put("dob", details != null ? details.optString("dob", "") : "");
			flatMap.put("face.faceString", face != null ? trimString(face.optString("faceString", ""), 10) : "");
			flatMap.put("gender.value", gender != null ? gender.optString("value", "") : "");
			flatMap.put("husband.value", husband != null ? husband.optString("value", "") : "");
			flatMap.put("name.value", name != null ? name.optString("value", "") : "");
			flatMap.put("father.value", father != null ? father.optString("value", "") : "");
			flatMap.put("daughter_of.value", daughter != null ? daughter.optString("value", "") : "");
			flatMap.put("aadhaar.color", aadhaar != null ? aadhaar.optString("color", "") : "");
			flatMap.put("aadhaar.is_screenshot", aadhaar != null ? aadhaar.optString("is_screenshot", "") : "");
			flatMap.put("format_match", details != null ? details.optString("format_match", "") : "");

			JSONObject flatJson = new JSONObject(flatMap);
			row.createCell(3).setCellValue(flatJson.toString(2));
			
			System.out.println("Processing: " + imgPath.getFileName());
			System.out.println("Masked B64 (start): " + maskedB64.substring(0, Math.min(40, maskedB64.length())));


		} catch (Exception e) {
			System.err.println("Field extraction failed for " + imgPath.getFileName() + ": " + e.getMessage());
			row.createCell(3).setCellValue("Field extraction error: " + e.getMessage());
		}

		row.createCell(5).setCellValue("");
	}

	private JSONObject callApi(String wrappedB64) throws IOException {
		HttpPost post = new HttpPost(API_URL);
		HttpEntity entity = MultipartEntityBuilder.create().addTextBody("im", wrappedB64, ContentType.TEXT_PLAIN).build();
		post.setEntity(entity);

		try (CloseableHttpResponse res = http.execute(post)) {
			String body = new String(IOUtils.toByteArray(res.getEntity().getContent()), "UTF-8");
			return new JSONObject(body);
		}
	}

	private void embedPic(byte[] bytes, int rowIdx, int colIdx, int poiType) {
		int picIdx = wb.addPicture(bytes, poiType);
		CreationHelper hlp = wb.getCreationHelper();
		Drawing<?> dr = sheet.createDrawingPatriarch();

		XSSFClientAnchor anc = (XSSFClientAnchor) hlp.createClientAnchor();
		anc.setRow1(rowIdx);
		anc.setRow2(rowIdx + 1);
		anc.setCol1(colIdx);
		anc.setDx1(Units.toEMU(20));
		anc.setCol2(colIdx + 1);
		anc.setDx2(Units.toEMU(-20));

		dr.createPicture(anc, picIdx);
		sheet.getRow(rowIdx).setHeightInPoints(80);
		sheet.setColumnWidth(colIdx, 20 * 256);
	}

	private static int getPoiType(Path p) {
		String ext = p.getFileName().toString().replaceFirst(".*\\.", "").toLowerCase(Locale.ROOT);
		return EXT_TO_TYPE.getOrDefault(ext, Workbook.PICTURE_TYPE_JPEG);
	}

	private static String cleanBase64(String s) {
		if (s == null) return "";
		s = s.trim();
		if (s.startsWith("b'") && s.endsWith("'")) s = s.substring(2, s.length() - 1);
		return s.replaceAll("[\\r\\n\" ]", "");
	}

	private static int detectPoiType(byte[] bytes) {
		return (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8)
				? Workbook.PICTURE_TYPE_JPEG
				: Workbook.PICTURE_TYPE_PNG;
	}

	private static String trimString(String input, int length) {
		if (input == null) return "";
		return input.length() > length ? input.substring(0, length) : input;
	}
}
