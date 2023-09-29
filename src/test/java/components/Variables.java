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
            hobbyMusic = "Music",
            readingHobby= "Reading",
            hobbySports = "Sports";
}
