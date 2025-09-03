package postman;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SignatureCanvasAutomation {
	public static void main(String[] args) throws Exception {
		WebDriverManager.chromedriver().setup();
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();

		try {
			driver.get("https://testting.d3lngv9hf46sr9.amplifyapp.com/");
			Thread.sleep(2000);

			WebElement canvas = driver.findElement(By.tagName("canvas"));
	        Dimension size = canvas.getSize();
	        int canvasWidth = size.getWidth();
	        int canvasHeight = size.getHeight();

	        // Draw within 10px margin to avoid edges and Clear button
	        int safeWidth = (int) (canvasWidth * 0.85);
	        int safeHeight = (int) (canvasHeight * 0.7);
	        int startX = 10;
	        int startY = 10;

	        Actions draw = new Actions(driver);

			// Draw line
			draw.moveToElement(canvas, startX - 40, startY - 40).clickAndHold().moveByOffset(80, 0).release()
					.perform();
			Thread.sleep(5000);

			// Draw dot
			draw.moveToElement(canvas, startX, startY).click().perform();
			Thread.sleep(5000);

			// Draw fish-like curve
			draw.moveToElement(canvas, startX - 30, startY).clickAndHold().moveByOffset(20, 20).moveByOffset(20, -20)
					.release().perform();
			Thread.sleep(5000);

			// Draw star (rough sketch)
			draw.moveToElement(canvas, startX, startY).clickAndHold();
	        for (int i = 0; i < 10; i++) {
	            draw.moveByOffset(10, (i % 2 == 0) ? 5 : -5);
	        }
	        draw.release().perform();

			// Scribble
			draw.moveToElement(canvas, startX - 30, startY + 40).clickAndHold();
			for (int i = 0; i < 10; i++) {
				draw.moveByOffset((i % 2 == 0 ? 5 : -5), 5);
			}
			draw.release().perform();
			Thread.sleep(3000);

			// Draw number 8 (rough loop)
			draw.moveToElement(canvas, startX + 50, startY - 30).clickAndHold().moveByOffset(-10, 20)
					.moveByOffset(10, 20).moveByOffset(10, -20).moveByOffset(-10, -20).moveByOffset(-10, 20)
					.moveByOffset(10, 20).moveByOffset(10, -20).moveByOffset(-10, -20).release().perform();
			Thread.sleep(5000);

			// Draw special char '@'
			draw.moveToElement(canvas, startX - 50, startY + 50).clickAndHold().moveByOffset(10, 0)
					.moveByOffset(0, 10).moveByOffset(-10, 0).moveByOffset(0, -10).release().perform();
			Thread.sleep(5000);

			// Draw a letter 'A'
			draw.moveToElement(canvas, startX + 30, startY + 40).clickAndHold().moveByOffset(10, -30)
					.moveByOffset(10, 30).moveToElement(canvas, startX + 30, startY + 25).moveByOffset(20, 0)
					.release().perform();

			Thread.sleep(5000);

			// Click continue
			WebElement continueBtn = driver.findElement(By.xpath("//button[contains(text(),'Continue')]"));
			continueBtn.click();
			Thread.sleep(2000);

			// Screenshot folder
			File folder = new File("screenshots");
			if (!folder.exists())
				folder.mkdir();

			String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			File dest = new File(folder, "canvas_shapes_" + timestamp + ".png");
			org.openqa.selenium.io.FileHandler.copy(screenshot, dest);

			System.out.println("Screenshot saved at: " + dest.getAbsolutePath());

		} finally {
			driver.quit();
		}
	}
}
