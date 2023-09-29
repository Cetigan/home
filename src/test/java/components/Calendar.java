package components;

import static com.codeborne.selenide.Selenide.$;

public class Calendar {
   static public void setDate(String date,String month,String year){
       $(".react-datepicker__month-select").selectOption(month);
       $(".react-datepicker__year-select").selectOption(year);
       $(".react-datepicker__day--0"+ date + ":not(.react-datepicker__day--outside-month)").click();
    }
}
