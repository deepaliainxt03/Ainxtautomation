package APIs;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class ImageUploaderAndScreenshotSaver {
    @SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
        String folderPath = "C:\\Users\\deepa\\Downloads\\Data Extraction 25-04";
        String excelPath = "E:\\ScreenshotResults.xlsx";
        String screenshotDir = "E:\\Screenshots\\";
        String baseUrl = "https://master.d82nsv5mlyhd5.amplifyapp.com/";

        // Setup Excel workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Results");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("File Name");
        header.createCell(1).setCellValue("Screenshot Path");

        // Setup Chrome with 75% zoom
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--force-device-scale-factor=1.1");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        //driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        File folder = new File(folderPath);
        File[] images = folder.listFiles((dir, name) -> name.matches(".*\\.(jpg|jpeg|png|gif|pdf)"));

        if (images == null || images.length == 0) {
            System.out.println("No image files found in the folder.");
            driver.quit();
            return;
        }

        int rowIndex = 1;

        for (File image : images) {
            driver.get(baseUrl);

            // Upload file
            WebElement uploadBtn = driver.findElement(By.xpath("//input[@type='file']"));
            uploadBtn.sendKeys(image.getAbsolutePath());

            // Wait for preview to load (you can use WebDriverWait here for robustness)
            Thread.sleep(5000);

            // Take screenshot of the entire page
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String screenshotPath = screenshotDir + image.getName() + ".png";
            Files.copy(src.toPath(), Paths.get(screenshotPath), StandardCopyOption.REPLACE_EXISTING);

            // Log results to Excel
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(image.getName());
            row.createCell(1).setCellValue(screenshotPath);
        }

        // Save Excel
        FileOutputStream out = new FileOutputStream(excelPath);
        workbook.write(out);
        out.close();
        workbook.close();

        // Cleanup
        driver.quit();
        System.out.println("✅ Process complete. Results saved in Excel.");
    }
}
