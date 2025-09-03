package saasjourney;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;


import io.github.bonigarcia.wdm.WebDriverManager;

public class mastertrusthappyflow {

public static WebDriver driver;
	
	public static void main (String[] args) throws InterruptedException {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("https://uat.mastertrust.getseto.com/mobile");
		
		//mobile
		driver.findElement(By.id("txtMobile")).sendKeys("7738002470");
		driver.findElement(By.xpath("/html/body/app-root/div/div/app-mobile/section/div/div/div/div/div[3]/button")).click();
		
		//mobile otp
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement otpInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='txtOTP']")));
		otpInput.sendKeys("1234");
		
		Thread.sleep(5000);
		WebDriverWait wait1 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement otpClick = wait1.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/div/app-mobile-otp/section/div/div/div/div/div[3]/div[2]/button")));
		otpClick.click();
		
		//email
		Thread.sleep(5000);
		WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement emailClick = wait2.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/div/app-email/section/div/div/div/div/div[2]/fieldset/div[3]/div")));
		emailClick.click();
		
	    Thread.sleep(5000);
	    driver.findElement(By.id("txtEmail")).sendKeys("deepali.gupta@ainxttech.com");
	    
	    Thread.sleep(5000);
		WebDriverWait wait3 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement validateEmail = wait3.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/div/app-email/section/div/div/div/div/div[2]/fieldset/div/div[2]/button")));
		validateEmail.click();
	    
	    //email otp
	    Thread.sleep(5000);
	    driver.findElement(By.id("txtOTP")).sendKeys("1234");
	    
	    WebDriverWait wait4 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement emailotpClick = wait4.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/div/app-email-otp/section/div/div/div/div/div[4]/button")));
		emailotpClick.click();
		
		//PAN
		Thread.sleep(8000);
		driver.findElement(By.id("txtPan")).sendKeys("BGLPG6841F");
		
		WebDriverWait wait5 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement fetchPan = wait5.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/div/app-pan/section/div/div/div/div/div[3]/button")));
		fetchPan.click();
		
		//continue PAN
		Thread.sleep(8000);
		WebDriverWait wait6 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement continuePAN = wait6.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"modal-verify-pan\"]/div/div/div/div/button")));
		continuePAN.click();
		
		//IFSC
		Thread.sleep(8000);
		driver.findElement(By.name("txtIfsc")).sendKeys("BKID0000043");
		
		WebDriverWait wait7 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement searchIFSC = wait7.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/div/app-bank-ifsc-code/section/div/div/div/div/div[3]/button")));
		searchIFSC.click();
		
		WebDriverWait wait8 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement submitIFSC = wait8.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"modal-choose-ifsc-code\"]/div/div/div/div/button")));
		submitIFSC.click();
		
		//acc no
		Thread.sleep(8000);
		driver.findElement(By.name("txtBankAccount")).sendKeys("012110110006212");
		
		WebDriverWait wait9 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement accno = wait9.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/div/app-bank-account-no/section/div/div/div/div/div[3]/button")));
		accno.click();
		Thread.sleep(10000);
		
		//digilocker
		WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement digi = wait10.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/div/app-document-proofs/section/div/div/div/div/div[2]/div/div[1]/label/div")));
		digi.click();
		Thread.sleep(7000);
		
		//authenticate
		WebDriverWait wait11 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement authen = wait11.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-userdata/div/div[2]/button")));
		authen.click();
		Thread.sleep(8000);
		
		//Digilocker Ainxt
		driver.findElement(By.id("aadhaar_1")).sendKeys("473887522439");
		Thread.sleep(8000);
		
		WebDriverWait wait12 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement captcha = wait12.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"button\"]")));
		captcha.click();
		Thread.sleep(9000);

		//digi otp
		WebDriverWait wait13 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement continuebtn = wait13.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"button\"]")));
		continuebtn.click();
		Thread.sleep(8000);
		
		//digi pin
		WebDriverWait wait14 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement continuebttn = wait14.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"button\"]")));
		continuebttn.click();
		Thread.sleep(8000);
		
		//documnet consent
		WebDriverWait wait15 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement allow = wait15.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"consent_form\"]/div[2]/div[7]/div[2]/div[2]/input[2]")));
		allow.click();
		Thread.sleep(10000);	
		
		//upload sign next
		WebElement element = driver.findElement(By.xpath("/html/body/app-root/div/div/app-upload-sign/section/div/div/div/div/app-file-upload/div[3]/div/div[2]/div/button[2]"));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);

		Thread.sleep(2000);
		
		WebElement uploadInput = driver.findElement(By.xpath("/html/body/app-root/div/div/app-upload-sign/section/div/div/div/div/app-file-upload/div[3]/div/div[2]/div/button[2]"));
		uploadInput.click();
		uploadInput.sendKeys("C:\\Users\\deepa\\Downloads\\20231018_113415.jpg");

		WebDriverWait wait16 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement next = wait16.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/div/app-upload-sign/section/div/div/div/div/app-file-upload/div[3]/div/div[3]/div/button")));
		next.click();
		Thread.sleep(8000);
		
		//personal details
		driver.findElement(By.id("txtFatherName")).sendKeys("Sashi");
		driver.findElement(By.id("txtMotherName")).sendKeys("Nitu");
		
		WebElement optQuarterly = driver.findElement(By.id("optQuarterly"));
		optQuarterly.click();

		WebDriverWait wait17 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement confirm = wait17.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/div/app-personal-details-form/section/div/div/div/div/div[2]/div/div[6]/div/button")));
		confirm.click();
		Thread.sleep(5000);
		
		//Financial 
		WebElement educationDropdown = driver.findElement(By.id("Education"));
		Select selectedu = new Select(educationDropdown);
		selectedu.selectByVisibleText(" Graduate ");
		
		WebElement occupationDropdown = driver.findElement(By.id("cboOccupation"));
		Select selectoccu = new Select(occupationDropdown);
		selectoccu.selectByVisibleText(" Others ");
		
		WebElement tradingDropdown = driver.findElement(By.id("cboOccupation"));
		Select selecttrade = new Select(tradingDropdown);
		selecttrade.selectByVisibleText(" No Experience ");
		
		WebDriverWait wait18 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement confir = wait18.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/div/app-financial-info/section/div/div/div/div/div[2]/div/div[8]/div/button")));
		confir.click();
		
		//nomination
		Thread.sleep(5000);
		WebDriverWait wait19 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement conbtn = wait19.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/div/app-financial-info/section/div/div/div/div/div[2]/div/div[8]/div/button")));
		conbtn.click();
		
		//selfie
		Thread.sleep(5000);
		WebDriverWait wait20 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement capture = wait20.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"lo\"]/div/app-selfie/section/div/div/div/div/button")));
		capture.click();
		
		Thread.sleep(5000);
		WebDriverWait wait21 = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement captureselfie = wait21.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"selfieButtonIcon\"]	")));
		captureselfie.click();
		
		
		
		Thread.sleep(5000);
		driver.close();
	}
}
