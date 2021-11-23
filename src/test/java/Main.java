import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final ExtentReports extent = new ExtentReports();
    private static final ExtentTest test = extent.createTest("Using Selenium WebDriver", "Doing weird stuff on html and use extent");

    @BeforeClass
    public static void init() {
        System.setProperty("webdriver.chrome.driver", "/Users/batman/Downloads/chromedriver");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ExtentSparkReporter spark = new ExtentSparkReporter("/Users/batman/IntelliJProjects/ExEleven/spark.html");
        extent.attachReporter(spark);
        test.log(Status.INFO, "WebDriver initialized, as well as this report.");
    }

    /* Ex. 1 */
    @Test
    public void test01_printIframeText() {
        try {
            driver.get("https://dgotlieb.github.io/Navigation/Navigation.html");
            driver.switchTo().frame("my-frame");
            WebElement iframeContainer = driver.findElement(By.cssSelector("div#iframe_container"));
            System.out.println(iframeContainer.getText());
            test.pass("Successfully printed iframe text");
        } catch(NoSuchElementException e) {
            e.printStackTrace();
            test.fail(e.getMessage(), MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot("test01", "png")).build());
        }
    }

    /* Ex. 2 */
    @Test
    public void test02_takeScreenshotIntoExtent() {
        try {
            driver.get("https://translate.google.co.il/");
            test.pass("Successfully reached Google-Translate!", MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot("translate", "png"))
                    .build());
            driver.findElement(By.cssSelector("textarea.er8xn[role=combobox]")).click();
            test.pass("Successfully clicked translation input box.");
            extent.setSystemInfo("companyName", "50M.Media");
        } catch(NoSuchElementException e) {
            e.printStackTrace();
            test.fail(e.getMessage(), MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot("test02", "png")).build());
        }
    }

    /* Ex. 3 */
    @Test
    public void test03_getUrlFromXml() {
        ArrayList<String> keyNames = new ArrayList<>();
        keyNames.add("url");
        try {
            HashMap<String, String> data = readFromXml("src/main/resources/config.xml", keyNames);
            String url = data.get("url");
            driver.get(url);
            test.pass("Successfully browsed to: '" + url + "' retrieved from xml.");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /* Ex. 4 */
    @Test
    public void test04_jsAlertAndPrompt() {
        driver.get("https://dgotlieb.github.io/Navigation/Navigation.html");
        try {
            /* Dealing with alert */
            driver.findElement(By.cssSelector("input#MyAlert")).click();
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            System.out.println(alert.getText());
            test.pass("Successfully printed alert's text: " + alert.getText());
            alert.accept();

            /* Dealing with prompt */
            driver.findElement(By.cssSelector("input#MyPrompt")).click();
            Alert prompt = wait.until(ExpectedConditions.alertIsPresent());
            String myName = "Sagiv";
            prompt.sendKeys(myName);
            prompt.accept();
            WebElement outputSpan = driver.findElement(By.cssSelector("span#output"));
            String outputText = outputSpan.getText();
            Assert.assertEquals(outputText, myName);
            test.pass("Successfully sent keys to prompt: " + outputText);

            /* Dealing with confirm */
            driver.findElement(By.cssSelector("input#MyConfirm")).click();
            Alert confirm = wait.until(ExpectedConditions.alertIsPresent());
            confirm.accept(); // Means that text should be 'Confirmed'.
            String expectedText = "Confirmed";
            outputText = outputSpan.getText();
            Assert.assertEquals(outputText, expectedText);
            test.pass("Successfully handled 'confirm' dialogue: " + outputText);

            /* Dealing with new tab */
            String mainWindow = driver.getWindowHandle();
            driver.findElement(By.cssSelector("input#openNewTab")).click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if(!mainWindow.contentEquals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            driver.close();
            driver.switchTo().window(mainWindow);
            test.pass("Successfully switched to new tab and back, closing it in the process.");

            /* Dealing with new window */
            driver.findElement(By.partialLinkText("New Window")).click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if(!mainWindow.contentEquals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            driver.close();
            driver.switchTo().window(mainWindow);
            test.pass("Successfully switched to new window and back, closing it in the process.");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDown() {
        driver.quit();
        test.log(Status.INFO, "We had fun, but test suite is done.");
        extent.flush();
    }

    private static String takeScreenshot(String filePath, String format) {
        TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
        File screenShotFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
        File destinationFile = new File(filePath + "." + format);
        try {
            FileUtils.copyFile(screenShotFile, destinationFile);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return destinationFile.getName();
    }

    private static HashMap<String, String> readFromXml(String filePath, ArrayList<String> keyNames) throws Exception {
        File fXmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();

        HashMap<String, String> data = new HashMap<>();
        for (String keyName: keyNames) {
            String content = doc.getElementsByTagName(keyName).item(0).getTextContent();
            data.put(keyName, content);
        }
        return data;
    }
}
