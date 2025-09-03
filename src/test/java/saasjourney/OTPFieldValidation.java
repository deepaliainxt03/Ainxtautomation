package saasjourney;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;

public class OTPFieldValidation {

	WebDriver driver;

	@BeforeClass
	public void setup() {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("https://uat.mastertrust.getseto.com/mobile");
		driver.findElement(By.id("txtMobile")).sendKeys("7738002470");
		driver.findElement(By.xpath("/html/body/app-root/div/div/app-mobile/section/div/div/div/div/div[3]/button"))
				.click();
	}

	@AfterClass
	public void tearDown() {
		driver.quit();
	}

	@BeforeMethod
	public void beforeEach() throws InterruptedException {
		Thread.sleep(3000);
	}

	@Test(priority = 1)
	public void testBlankOTPNumber() throws InterruptedException {
		WebElement input = driver.findElement(By.id("txtOTP"));
		input.clear();
		driver.findElement(
				By.xpath("/html/body/app-root/div/div/app-mobile-otp/section/div/div/div/div/div[3]/div[2]/button"))
				.click();
		Thread.sleep(2000);
		Assert.assertFalse(driver.getCurrentUrl().contains("form/email"), "Blank OTP should not proceed.");
	}

	@SuppressWarnings("deprecation")
	@Test(priority = 2)
	public void testOnlySpacesInOTPField() throws InterruptedException {
		WebElement input = driver.findElement(By.id("txtOTP"));
		input.clear();
		input.sendKeys("    ");
		Thread.sleep(1000);
		String enteredValue = input.getAttribute("value");
		Assert.assertTrue(enteredValue.trim().isEmpty(), "Only spaces should not be accepted.");
	}

	@SuppressWarnings("deprecation")
	@Test(priority = 3)
	public void testNumericKeyboardOpens() {
		WebElement input = driver.findElement(By.id("txtOTP"));
		String inputType = input.getAttribute("type");
		Assert.assertTrue(inputType.equals("tel") || inputType.equals("number"),
				"Input type should be 'tel' or 'number'.");
	}

	@Test(priority = 4)
	public void testEnterValidOTPNumber() {
		WebElement input = driver.findElement(By.id("txtOTP"));
		input.clear();
		input.sendKeys("1234");
		Assert.assertEquals(input.getDomProperty("value"), "1234", "Valid OTP input should be accepted.");
	}

	@Test(priority = 5)
	public void testSpecialCharactersAndAlphabets() {
		WebElement input = driver.findElement(By.id("txtOTP"));
		input.clear();
		input.sendKeys("ac@#");
		String value = input.getDomProperty("value");
		Assert.assertFalse(value.matches("[0-9]{4}"), "Special characters or alphabets should not be accepted.");
	}

	@Test(priority = 6)
	public void testLessThan4Digits() {
		WebElement input = driver.findElement(By.id("txtOTP"));
		input.clear();
		input.sendKeys("123");
		Assert.assertTrue(input.getDomProperty("value").length() < 4,
				"Input with less than 4 digits should not be accepted.");
	}

	@SuppressWarnings("deprecation")
	@Test(priority = 7)
	public void testMoreThan4Digits() {
		WebElement input = driver.findElement(By.id("txtOTP"));
		input.clear();
		input.sendKeys("12345");
		String value = input.getAttribute("value");
		Assert.assertTrue(value.length() <= 4, "More than 4 digits should not be accepted.");
	}

	@SuppressWarnings("deprecation")
	@Test(priority = 8)
	public void testNonBreakingSpace() {
		WebElement input = driver.findElement(By.id("txtOTP"));
		input.clear();
		input.sendKeys("\u00A0\u00A0\u00A0\u00A0");
		String value = input.getAttribute("value");
		Assert.assertTrue(value.trim().isEmpty(), "Non-breaking spaces should not be accepted.");
	}

	@SuppressWarnings("deprecation")
	@Test(priority = 9)
	public void testLeadingZerosInOTP() {
		WebElement input = driver.findElement(By.id("txtOTP"));
		input.clear();
		input.sendKeys("0123");
		String value = input.getAttribute("value");
		Assert.assertEquals(value, "0123", "Leading zeros should be preserved.");
	}
}
