package tests;

import components.CssElements;
import components.TestBase;
import components.Variables;
import io.qameta.allure.Link;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static helper.AttachmentsHelper.*;
import static io.qameta.allure.Allure.step;

@DisplayName("Students Registration")
@Owner("Volosin Anatolii")
@Severity(SeverityLevel.BLOCKER)
@Link(value = "site",url = "https://demoqa.com/automation-practice-form")
@Tag("positive")
public class PositiveTest extends TestBase {
    CssElements cssElements =new CssElements();
    Variables variables = new Variables();
    @Test
    void FilFormTest(){

        step("open the main page",()->{
            cssElements.openPage();});
        step("set the userName",()->{
            cssElements.setFirstName(variables.userName);
        });
        step("set the userLastName",()->{
            cssElements.setLastName(variables.userLastName);
        });
        step("set the email",()->{
            cssElements.setEmail(variables.userEmail);
        });
        step("set male gender",()->{
            cssElements.setGender(variables.genderFemale);
        });
        step("set phone number",()->{
            cssElements.setPhoneNumber(variables.phoneNumber);
        });
        step("set BirthDay",()->{
            cssElements.setBirthDay(variables.dateOfBirth, variables.monthOfBirth, variables.yearOfBirth);
        });
        step("set subject-English",()->{
            cssElements.setSubject(variables.subjectEnglish);
        });
        step("set hobby-music",()->{
            cssElements.setHobbies(variables.hobbyMusic);
        });
        step("set photo",()->{
            cssElements.setPhoto(variables.photo);
        });
        step("set adress",()->{
            cssElements.setAdress(variables.currentAddress);
        });
        step("set state-Haryana",()->{
            cssElements.setState(variables.stateHaryana);
        });
        step("set city-Karnel",()->{
            cssElements.setCity(variables.cityKarnel);
        });
        step("click-submit",()->{
            cssElements.submit();
        });
        //verification
        step("check- name+surname",()->{
            cssElements.checkRegistrationForm("Student Name",variables.userName + " "+ variables.userLastName);
        });
        step("check- userEmail",()->{
            cssElements.checkRegistrationForm("Student Email",variables.userEmail);
        });
        step("check- phoneNumber",()->{
            cssElements.checkRegistrationForm("Mobile",variables.phoneNumber);
        });
        step("check- genderMale",()->{
            cssElements.checkRegistrationForm("Gender",variables.genderFemale);
        });
        step("check- Date of Birth",()->{
            cssElements.checkRegistrationForm("Date of Birth",variables.dateOfBirth+" "+variables.monthOfBirth+","+ variables.yearOfBirth);
        });
        step("check- Subjects-English",()->{
            cssElements.checkRegistrationForm("Subjects",variables.subjectEnglish);
        });
        step("check- Hobbies-Music",()->{
            cssElements.checkRegistrationForm("Hobbies",variables.hobbyMusic);
        });
        step("check- photo",()->{
            cssElements.checkRegistrationForm("Picture",variables.photo);
        });
        step("check- currentAddress",()->{
            cssElements.checkRegistrationForm("Address",variables.currentAddress);
        });
        step("check- State and City-Haryana Karnal",()->{
            cssElements.checkRegistrationForm("State and City",variables.stateHaryana+" "+variables.cityKarnel);
        });
        step("attached files",()->{
            screenshotAs("End of test screen");
            pageSource();
        });
    }

}
