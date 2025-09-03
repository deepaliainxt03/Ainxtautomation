package postman;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class SignatureUploadAutomation {
    public static void main(String[] args) throws InterruptedException {
        // Setup WebDriver
        //.setProperty("webdriver.chrome.driver", "path_to_chromedriver");
    	WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        Thread.sleep(5000);

        try {
            // 1. Open the target page
            driver.get("https://testting.d3lngv9hf46sr9.amplifyapp.com/");
            Thread.sleep(5000);

            WebElement uploadSignature = driver.findElement(By.xpath("//button[contains(text(), 'JPG, JPEG, PNG: Max file size 20MB')]"));
            uploadSignature.click();
            Thread.sleep(5000);
            
            WebElement uploadDocument = driver.findElement(By.xpath("//button[contains(text(), 'Upload Document')]"));
            uploadDocument.click();
            Thread.sleep(5000);

            // 2. Locate the input[type='file'] element
            WebElement fileInput = driver.findElement(By.cssSelector("input[type='file']"));
            Thread.sleep(5000);

            // 3. Send absolute file path to the element
            fileInput.sendKeys("C:\\Users\\deepa\\data_1.jpg");  // <-- Your actual image path

            Thread.sleep(5000);

            // Optional: Click continue if needed
            WebElement continueButton = driver.findElement(By.xpath("//button[contains(text(), 'Continue')]"));
            continueButton.click();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Clean up
            driver.quit();
        }
    }
}
