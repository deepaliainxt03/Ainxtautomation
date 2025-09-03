package postman;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SignatureCanvasAutomate {
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

			// Screenshot folder
			File folder = new File("screenshots");
			if (!folder.exists())
				folder.mkdir();

			// Loop through different drawing actions
			String[][] actions = { { "line", "moveToElement", "clickAndHold", "moveByOffset", "release" },
					{ "dot", "moveToElement", "click", "" },
					{ "fish", "moveToElement", "clickAndHold", "moveByOffset", "moveByOffset", "release" },
					{ "star", "moveToElement", "clickAndHold", "moveByOffset", "moveByOffset", "moveByOffset",
							"moveByOffset", "moveByOffset", "moveByOffset", "moveByOffset", "moveByOffset", "release" },
					{ "scribble", "moveToElement", "clickAndHold", "moveByOffset", "moveByOffset", "moveByOffset",
							"moveByOffset", "moveByOffset", "release" },
					{ "number8", "moveToElement", "clickAndHold", "moveByOffset", "moveByOffset", "moveByOffset",
							"moveByOffset", "moveByOffset", "moveByOffset", "moveByOffset", "release" },
					{ "at", "moveToElement", "clickAndHold", "moveByOffset", "moveByOffset", "moveByOffset",
							"moveByOffset", "release" },
					{ "letterA", "moveToElement", "clickAndHold", "moveByOffset", "moveByOffset", "moveToElement",
							"moveByOffset", "release" } };

			// Iterate through drawing actions
			for (String[] action : actions) {
				// Draw object based on action type
				switch (action[0]) {
				case "line":
					draw.moveToElement(canvas, startX, startY).clickAndHold().moveByOffset(80, 0).release().perform();
					break;

				case "dot":
					draw.moveToElement(canvas, startX, startY).click().perform();
					break;

				case "fish":
					draw.moveToElement(canvas, startX, startY).clickAndHold();
					for (int i = 0; i < 20; i++) {
						draw.moveByOffset(5, (int) (5 * Math.sin(i * Math.PI / 10))); // curve-like fish tail
					}
					draw.release().perform();
					break;

				case "star":
					int[][] points = { { 0, -30 }, { 11, -9 }, { 29, -9 }, { 18, 3 }, { 22, 20 }, { 0, 10 },
							{ -22, 20 }, { -18, 3 }, { -29, -9 }, { -11, -9 }, { 0, -30 } };
					draw.moveToElement(canvas, startX, startY).clickAndHold();
					for (int[] p : points)
						draw.moveByOffset(p[0], p[1]);
					draw.release().perform();
					break;

				case "scribble":
					draw.moveToElement(canvas, startX, startY).clickAndHold();
					for (int i = 0; i < 10; i++) {
						draw.moveByOffset((i % 2 == 0 ? 10 : -10), 10);
					}
					draw.release().perform();
					break;

				case "number8":
					draw.moveToElement(canvas, startX, startY).clickAndHold();
					for (int i = 0; i < 20; i++) {
						int dx = (int) (10 * Math.cos(i * Math.PI / 10));
						int dy = (int) (10 * Math.sin(i * 2 * Math.PI / 20));
						draw.moveByOffset(dx, dy);
					}
					draw.release().perform();
					break;

				case "at":
					draw.moveToElement(canvas, startX, startY).clickAndHold();
					for (int i = 0; i < 30; i++) {
						int dx = (int) (8 * Math.cos(i * Math.PI / 15));
						int dy = (int) (8 * Math.sin(i * Math.PI / 15));
						draw.moveByOffset(dx, dy);
					}
					draw.moveByOffset(3, 0).release().perform(); // inner loop
					break;

				case "letterA":
					draw.moveToElement(canvas, startX, startY + 20).clickAndHold().moveByOffset(10, -30)
							.moveByOffset(10, 30).moveToElement(canvas, startX + 5, startY + 5).clickAndHold()
							.moveByOffset(10, 0).release().perform();
					break;
				}

				// Wait after each drawing
				Thread.sleep(5000);

				// Click continue button
				WebElement continueBtn = driver.findElement(By.xpath("//button[contains(text(),'Continue')]"));
				continueBtn.click();
				Thread.sleep(3000);

				// Take screenshot
				String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				File dest = new File(folder, "canvas_shapes_" + timestamp + ".png");
				org.openqa.selenium.io.FileHandler.copy(screenshot, dest);
				System.out.println("Screenshot saved at: " + dest.getAbsolutePath());

				// Click clear button (if exists)
				try {
					WebElement clearBtn = driver.findElement(By.xpath("//button[contains(text(),'Clear')]"));
					clearBtn.click();
					Thread.sleep(2000);
				} catch (NoSuchElementException e) {
					System.out.println("Clear button not found.");
				}
			}

		} finally {
			driver.quit();
		}
	}
}
