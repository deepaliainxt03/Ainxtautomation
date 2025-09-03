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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.json.JSONObject;
import org.testng.annotations.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class DockerPanMaskingTest {

    private static final Path INPUT_FOLDER = Paths.get("C:\\Users\\deepa\\Downloads\\Kotak Aadhar Masking\\Aadhar PDF");
    private static final Path OUTPUT_XLSX = Paths.get("E:/Aaadhar PDF.xlsx");

    private static final String API_URL = "https://tpdxsyby0l.execute-api.ap-south-1.amazonaws.com/default/docker_pan";
    private static final String API_KEY_HEADER = "x-api-key";
    private static final String API_KEY_VALUE = "rJZGX7mbpW8NSgydtOfAcaZZ1xT3xdcl1gBCOhtX";

    private static final Map<String, Integer> EXT_TO_TYPE = new HashMap<>();
    static {
        EXT_TO_TYPE.put("jpg", Workbook.PICTURE_TYPE_JPEG);
        EXT_TO_TYPE.put("jpeg", Workbook.PICTURE_TYPE_JPEG);
        EXT_TO_TYPE.put("png", Workbook.PICTURE_TYPE_PNG);
        EXT_TO_TYPE.put("bmp", Workbook.PICTURE_TYPE_PNG);
        EXT_TO_TYPE.put("pdf", Workbook.PICTURE_TYPE_PNG);  // PDF converted to PNG
    }

    private CloseableHttpClient http;
    private Workbook wb;
    private Sheet sheet;

    @BeforeClass
    public void setUp() throws IOException {
        http = HttpClients.createDefault();
        wb = new XSSFWorkbook();
        sheet = wb.createSheet("Aadhar PDF");

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
        String imgRegex = "(?i).*\\.(png|jpe?g|bmp|pdf)$";
        int[] rowCounter = {1};

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
        for (int i = 0; i <= 5; i++) sheet.autoSizeColumn(i);

        try (OutputStream out = Files.newOutputStream(OUTPUT_XLSX)) {
            wb.write(out);
        }
        wb.close();
        http.close();
    }

    private void processImage(Path imgPath, int rowIdx) throws IOException {
        String ext = getExtension(imgPath);

        byte[] origBytes;
        if (ext.equals("pdf")) {
            origBytes = convertPdfToImageBytes(imgPath);
        } else {
            origBytes = Files.readAllBytes(imgPath);
        }

        String plain64 = Base64.encodeBase64String(origBytes).trim();

        JSONObject resp;
        try {
            resp = callApi(plain64);
        } catch (IOException ex) {
            System.err.println("API call failed for " + imgPath.getFileName() + ": " + ex.getMessage());
            Row row = sheet.createRow(rowIdx);
            row.createCell(0).setCellValue(rowIdx);
            row.createCell(1).setCellValue(imgPath.getFileName().toString());
            row.createCell(5).setCellValue("FAIL - API error");
            return;
        }

        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(rowIdx);
        row.createCell(1).setCellValue(imgPath.getFileName().toString());

        embedPic(origBytes, rowIdx, 2, getPoiType(imgPath));

        String maskedB64 = cleanBase64(resp.optString("masking", ""));
        String trimmedMasked = maskedB64.length() > 30 ? maskedB64.substring(0, 30) : maskedB64;

        if (!maskedB64.isEmpty()) {
            try {
                byte[] maskedBytes = Base64.decodeBase64(maskedB64);
                embedPic(maskedBytes, rowIdx, 4, detectPoiType(maskedBytes));
            } catch (IllegalArgumentException e) {
                System.err.println("Bad base64 for masked image: " + imgPath.getFileName());
            }
        }

     // ✔️ Extract and write full response values
        int statusCode = resp.optInt("status", -1);
       
        Map<String, Object> orderedResp = new LinkedHashMap<>();
        orderedResp.put("status", statusCode);
        orderedResp.put("masking", trimmedMasked);
        orderedResp.put("ismasked", resp.optString("ismasked", ""));
        orderedResp.put("remark", resp.optString("remark", ""));

        // ✔️ Write full JSON to Excel
        JSONObject trimmedResp = new JSONObject(orderedResp);
        row.createCell(3).setCellValue(trimmedResp.toString(2));

        // ✔️ Write status (PASS if status == 1, otherwise FAIL)
        String testingStatus = (statusCode == 1) ? "PASS" : "FAIL";
        row.createCell(5).setCellValue(testingStatus);

    }

    private JSONObject callApi(String base64) throws IOException {
        HttpPost post = new HttpPost(API_URL);
        post.setHeader(API_KEY_HEADER, API_KEY_VALUE);

        HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody("im", base64, ContentType.TEXT_PLAIN)
                .build();
        post.setEntity(entity);

        try (CloseableHttpResponse res = http.execute(post)) {
            int statusCode = res.getStatusLine().getStatusCode();
            String body = new String(IOUtils.toByteArray(res.getEntity().getContent()), "UTF-8");

            if (statusCode != 200) {
                System.err.println("API Error (" + statusCode + "): " + body);
                throw new IOException("Non-200 response: " + statusCode);
            }

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
        String ext = getExtension(p);
        return EXT_TO_TYPE.getOrDefault(ext, Workbook.PICTURE_TYPE_JPEG);
    }

    private static String getExtension(Path p) {
        return p.getFileName().toString().replaceFirst(".*\\.", "").toLowerCase(Locale.ROOT);
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

    private byte[] convertPdfToImageBytes(Path pdfPath) throws IOException {
        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 150); // first page, 150 DPI
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bim, "png", baos);
            return baos.toByteArray();
        }
    }
}
