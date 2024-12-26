package com.payway.utils.constants;

import java.time.Year;

public class WalletConstants {
    public static String generateAccountNumber(){
        Year currentYear = Year.now();
        int min = 100000;
        int max = 999999;
        int randomNumber = (int) Math.floor(Math.random() * (max-min + 1) + min);

        return String.valueOf(String.valueOf(currentYear) + String.valueOf(randomNumber));
    }
}
