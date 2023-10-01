package components;

import com.github.javafaker.Faker;

public class Variables {
    Faker faker = new Faker();
    public String userName = faker.name().firstName(),
            userLastName = faker.name().lastName(),
            userEmail = faker.internet().emailAddress(),
            currentAddress = faker.address().streetAddress(),
            phoneNumber = faker.phoneNumber().subscriberNumber(10),
            photo = "1.png",
            genderMale ="Male",
            genderFemale = "Female",
            gendrOther = "Other",
            subjectEnglish="English",
            hobbyMusic = "Music",
            readingHobby= "Reading",
            hobbySports = "Sports",
            dateOfBirth ="30",
            monthOfBirth="May",
            yearOfBirth="1985",
            stateHaryana="Haryana",
            cityKarnel="Karnal";
}
