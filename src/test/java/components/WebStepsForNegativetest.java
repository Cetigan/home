package components;

import io.qameta.allure.Step;

public class WebStepsForNegativetest {
    CssElements cssElementsAndVariables =new CssElements();
   Variables variables = new Variables();
   @Step("open main page")
   public  void openMainpage(){cssElementsAndVariables.openPage();}
    @Step("set the userName")
    public void userName(){cssElementsAndVariables.setFirstName(variables.userName);}
    @Step("set the userLastName")
    public void LastName(){cssElementsAndVariables.setLastName(variables.userLastName);}
    @Step("set the email")
    public void email(){cssElementsAndVariables.setEmail(variables.userEmail);}
    @Step("set male gender")
    public void gender(){cssElementsAndVariables.setGender(variables.genderMale);}
    @Step("set phone number")
    public void phone(){cssElementsAndVariables.setPhoneNumber(variables.phoneNumber);}
    @Step("set BirthDay")
    public void BirthDay(){cssElementsAndVariables.setBirthDay(variables.dateOfBirth, variables.monthOfBirth, variables.yearOfBirth);}
    @Step("set subject-English")
    public void subject(){cssElementsAndVariables.setSubject(variables.subjectEnglish);}
    @Step("set hobby-music")
    public void hobby(){cssElementsAndVariables.setHobbies(variables.hobbyMusic);}
    @Step("set photo")
    public void photo(){cssElementsAndVariables.setPhoto(variables.photo);}
    @Step("set adress")
    public void adress(){cssElementsAndVariables.setAdress(variables.currentAddress);}
    @Step("set state-Haryana")
    public void state(){cssElementsAndVariables.setState(variables.stateHaryana);}
    @Step("set city-Karnel")
    public void city(){cssElementsAndVariables.setCity(variables.cityKarnel);}
    @Step("click-submit")
    public void submit(){cssElementsAndVariables.submit();}
   @Step("check- name+surname")
   public void checkName(){cssElementsAndVariables.checkRegistrationForm("Student Name",variables.userName + " "+ variables.userLastName);}
   @Step("check- userEmail")
   public void checkUserEmail(){cssElementsAndVariables.checkRegistrationForm("Student Email",variables.userEmail);}
   @Step("check- phoneNumber")
   public void checkPhoneNumber(){cssElementsAndVariables.checkRegistrationForm("Mobile",variables.phoneNumber);}
   @Step("check- genderMale")
   public void checkGenderMale(){cssElementsAndVariables.checkRegistrationForm("Gender",variables.genderMale);}
   @Step("check- Date of Birth")
   public void checkBirth(){cssElementsAndVariables.checkRegistrationForm("Date of Birth",variables.dateOfBirth+" "+variables.monthOfBirth+","+ variables.yearOfBirth);}
   @Step("check- Subjects-English")
   public void checkEnglish(){cssElementsAndVariables.checkRegistrationForm("Subjects",variables.subjectEnglish);}
   @Step("check- Hobbies-Music")
   public void checkMusic(){cssElementsAndVariables.checkRegistrationForm("Hobbies",variables.hobbyMusic);}
   @Step("check- photo")
   public void checkPhoto(){cssElementsAndVariables.checkRegistrationForm("Picture",variables.photo);}
   @Step("check- currentAddress")
   public void checkCurrentAddress(){cssElementsAndVariables.checkRegistrationForm("Address",variables.currentAddress);}
   @Step("check- State and City-Haryana Karnal")
   public void checkState(){cssElementsAndVariables.checkRegistrationForm("State and City",variables.stateHaryana+" 00"+variables.cityKarnel);}
    //-I made error to fail this test-correct-----variables.stateHaryana+" "+variables.cityKarnel (space instead of 00)
}


