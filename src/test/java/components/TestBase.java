package components;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import static helper.AttachmentsHelper.*;

public class TestBase {



  public WebStepsForNegativetest steps = new WebStepsForNegativetest();

    @BeforeAll
    static void conditions(){
        Configuration.browserSize ="300x800";
        Configuration.browser="chrome";

      /* Configuration.browserSize = System.getProperty("browserSize", "1920x1080");
        Configuration.browser = System.getProperty("browser", "chrome");
        Configuration.browserVersion = System.getProperty("browserVersion", "100.0");
        Configuration.remote = System.getProperty("remote", "https://user1:1234@selenoid.autotests.cloud/wd/hub");
       Configuration.timeout = 10000;
        Configuration.baseUrl="https://demoqa.com/automation-practice-form";*/
    }
   @BeforeEach
     void addlistener(){
       SelenideLogger.addListener("allure",new AllureSelenide());
   }
   @AfterEach
    void attach(){
        screenshotAs("End of test");
        pageSource();
    }
}
