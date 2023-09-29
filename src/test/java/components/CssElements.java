package components;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class CssElements {


    private final String titleText = "Practice Form";
    private SelenideElement
            firstNameInput = $("#firstName"),
            lastNameInput = $("#lastName"),
            emailInput = $("#userEmail"),
            phoneImput = $("#userNumber"),
            currentAdressInput = $("#currentAddress"),
            male = $("label[for=gender-radio-1]"),
            female = $("label[for=gender-radio-2]"),
            other= $("label[for=gender-radio-3]"),
            uploadPicture = $("#uploadPicture"),
            dateOfBirth =  $("#dateOfBirthInput"),
            subject =$("#subjectsInput"),
            sportHobby =$("label[for=hobbies-checkbox-1]"),
            readingHobby = $("label[for=hobbies-checkbox-2]"),
            musicHobby =$("label[for=hobbies-checkbox-3]"),
            submitButton =$("#submit");



    public CssElements openPage() {
        Selenide.open("https://demoqa.com/automation-practice-form");
        executeJavaScript("$('#adplus-anchor').remove();");
        executeJavaScript("$('footer').remove();");

        $(".main-header").shouldHave(text("Practice Form"));
        return this;
    }
    public CssElements setFirstName(String value) {
        firstNameInput.setValue(value );
        return this;

    }
    public CssElements setLastName(String value) {

        lastNameInput.setValue(value);
        return this;
    }
    public CssElements setEmail(String value) {
        emailInput.setValue(value);
        return this;
    }
    public CssElements setGender(String value) {
        if(value.equals("Male")){
            male.click();
            return this;}
        else if (value.equals("Female")){
            female.click();
            return this;}
        else if(value.equals("Other")){
            other.click();
            return this;}

        return this;
    }
    public CssElements setPhoneNumber(String value) {
        phoneImput.scrollIntoView(true);
        phoneImput.setValue(value);
        return this;
    }
    public CssElements setBirthDay(String date, String month, String year) {
        dateOfBirth.click();
        Calendar.setDate(date, month, year);
        dateOfBirth.scrollIntoView(true);
        return this;
    }
    public CssElements setSubject(String value) {
        subject.click();
        subject.setValue(value).pressEnter().scrollIntoView(true);
        return this;
    }
    public CssElements setHobbies(String value) {
        if(value.equals("Sports")){
            sportHobby.click();
            return this;}
        else if (value.equals("Reading")){
            readingHobby .click();
            return this;}
        else if(value.equals("Music")){
            musicHobby.click();
            return this;}
        return this;
    }
    public CssElements setPhoto(String value) {
        uploadPicture.uploadFromClasspath(value);
        return this;
    }
    public CssElements setAdress(String value) {
        currentAdressInput.setValue(value);
        currentAdressInput.scrollIntoView(true);
        return this;
    }
    public CssElements setState(String value) {
        $("#state").click();
        $("#stateCity-wrapper").$(byText(value)).click();
        return this;
    }
    public CssElements setCity(String value) {
        $("#city").click();
        $("#stateCity-wrapper").$(byText(value)).click();
        return this;
    }
    public CssElements submit() {
        submitButton.click();
        return null;
    }
    public CssElements checkRegistrationForm(String key , String value){
        $(".modal-body").$(byText(key)).parent().shouldHave(text(value));
          return this;
    }
}