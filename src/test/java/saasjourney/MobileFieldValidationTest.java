package saasjourney;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;

public class MobileFieldValidationTest {

	WebDriver driver;

	@BeforeClass
	public void setup() {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("https://uat.mastertrust.getseto.com/mobile");
	}

	@AfterClass
	public void tearDown() {
		driver.quit();
	}

	@BeforeMethod
	public void beforeEach() throws InterruptedException {
		driver.navigate().refresh();
		Thread.sleep(1000);
	}

	@Test(priority = 1)
	public void testBlankMobileNumber() throws InterruptedException {
		WebElement input = driver.findElement(By.id("txtMobile"));
		input.clear();
		driver.findElement(By.xpath("/html/body/app-root/div/div/app-mobile/section/div/div/div/div/div[3]/button")).click();
		Thread.sleep(2000);
		Assert.assertFalse(driver.getCurrentUrl().contains("verify-mobile"), "Blank mobile number should not proceed to OTP screen.");
	}

	@SuppressWarnings("deprecation")
	@Test(priority = 2)
	public void testOnlySpacesInMobileField() throws InterruptedException {
		WebElement input = driver.findElement(By.id("txtMobile"));
		input.sendKeys("          "); // 10 spaces
		Thread.sleep(1000);
		String enteredValue = input.getAttribute("value");
		Assert.assertTrue(enteredValue.trim().isEmpty(), "Only spaces should not be accepted.");
	}

	@SuppressWarnings("deprecation")
	@Test(priority = 3)
	public void testNumericKeyboardOpens() {
		WebElement input = driver.findElement(By.id("txtMobile"));
		String inputType = input.getAttribute("type");
		Assert.assertTrue(inputType.equals("tel") || inputType.equals("number"), "Input type should be 'tel' or 'number'.");
	}

	@Test(priority = 4)
	public void testEnterValidMobileNumber() {
		WebElement input = driver.findElement(By.id("txtMobile"));
		input.sendKeys("7738002470");
		Assert.assertEquals(input.getDomProperty("value"), "7738002470", "Valid mobile number should be accepted.");
	}

	@Test(priority = 5)
	public void testSpecialCharactersAndAlphabets() {
		WebElement input = driver.findElement(By.id("txtMobile"));
		input.sendKeys("abc@#123");
		String value = input.getDomProperty("value");
		Assert.assertFalse(value.matches("[0-9]{10}"), "Special characters or alphabets should not be accepted.");
	}

	@Test(priority = 6)
	public void testLessThan10Digits() {
		WebElement input = driver.findElement(By.id("txtMobile"));
		input.sendKeys("123456");
		Assert.assertTrue(input.getDomProperty("value").length() < 10, "Less than 10 digits should not be accepted.");
	}

	@Test(priority = 7)
	public void testMobileNumberStartsWith() throws InterruptedException {
		Thread.sleep(3000);
		String[] numbers = { "0123456789", "1234567890", "2345678901", "3456789012", "4567890123", "5678901234"};

		for (String num : numbers) {
			WebElement input = driver.findElement(By.id("txtMobile"));
			input.clear();
			Thread.sleep(3000);
			input.sendKeys(num);
			Thread.sleep(3000);
			char firstDigit = num.charAt(0);
			String value = input.getDomProperty("value");

			if (firstDigit >= '6') {
				Assert.assertTrue(value.startsWith(String.valueOf(firstDigit)), "Mobile number starting with " + firstDigit + " should be accepted.");
			} else {
				Assert.assertFalse(value.startsWith(String.valueOf(firstDigit)), "Mobile number starting with " + firstDigit + " should not be accepted.");
			}
		}
	}

	@Test(priority = 8)
	public void testRepeatedDigitMobileNumbers() throws InterruptedException {
		Thread.sleep(3000);
		String[] repeatedNumbers = {
			"1111111111", "2222222222", "3333333333", "4444444444",
			"5555555555", "6666666666", "7777777777", "8888888888", "9999999999"
		};

		for (String mobile : repeatedNumbers) {
			driver.navigate().refresh();
			Thread.sleep(3000);

			WebElement input = driver.findElement(By.id("txtMobile"));
			input.clear();
			input.sendKeys(mobile);
			Thread.sleep(5000);

			driver.findElement(By.xpath("/html/body/app-root/div/div/app-mobile/section/div/div/div/div/div[3]/button")).click();
			Thread.sleep(8000);

			try {
				WebElement popup = driver.findElement(By.xpath("//*[contains(text(),'Mobile Number should be 10 digit and must begin with 5, 6, 7, 8 or 9.')]"));
				Assert.assertTrue(popup.isDisplayed(), "Popup should be displayed for repeated number: " + mobile);
				WebElement okButton = driver.findElement(By.xpath("//button[text()='OK']"));
				okButton.click();
			} catch (Exception e) {
				Assert.fail("Popup not shown or repeated digits accepted: " + mobile);
			}
		}
	}
}
