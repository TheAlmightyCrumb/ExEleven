import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class Main {
    private static WebDriver driver;
    private static ExtentReports extent = new ExtentReports();
    private static ExtentTest test = extent.createTest("Using Selenium WebDriver", "Doing weird stuff on html and use extent");

    @BeforeClass
    public static void init() {
        System.setProperty("webdriver.chrome.driver", "/Users/batman/Downloads/chromedriver");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
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
}
