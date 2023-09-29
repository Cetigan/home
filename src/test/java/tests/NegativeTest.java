package tests;

import com.codeborne.selenide.Configuration;
import components.TestBase;
import io.qameta.allure.Link;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static helper.AttachmentsHelper.*;

@DisplayName("Students Registration ")
@Owner("Volosin Anatolii")
@Severity(SeverityLevel.BLOCKER)
@Link(value = "site",url = "https://demoqa.com/automation-practice-form")
@Tag("negative")
public class NegativeTest extends TestBase {
 @Test
    public  void oldschool(){
     Configuration.browserSize ="800x600";
        // When the browser size is greater than 800px the "Submite"button is not clickable
       steps.openMainpage();
        steps.userName();
        steps.LastName();
        steps.email();
        steps.gender();
        steps.phone();
        steps.BirthDay();
        steps.subject();
        steps.hobby();
        steps.photo();
        steps.adress();
        steps.state();
        steps.city();
        steps.submit();
        //verification
        steps.checkName();
        steps.checkUserEmail();
        steps.checkPhoneNumber();
        steps.checkGenderMale();
        steps.checkBirth();
        steps.checkEnglish();
        steps.checkMusic();
        steps.checkPhoto();
        steps.checkCurrentAddress();
        steps.checkState();
        screenshotAs("End of test screen");
        pageSource();
 }
}
