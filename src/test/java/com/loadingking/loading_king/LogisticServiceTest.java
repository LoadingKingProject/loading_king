package com.loadingking.loading_king;

import com.loadingking.loading_king.core.logistics.application.LogisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class LogisticServiceTest {

    @Test
    public void test() {
        String raw = "|V1|10310479378231|송파구 백제고분로 348|105-303|손*미|23fca56e|37.50328200403601|127.10416104644537|CB1|1769464800000|";
        String[] tokens = raw.split("\\|");
        List<String> parts = new ArrayList<>();
        for (String token : tokens) {
            if (token == null) continue;
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) parts.add(trimmed);
        }

        for(String part : parts){
            System.out.println(part);
        }

//        String parsedAddress = fallbackAddress;
//        if (parts.size() > 2 && !parts.get(2).isBlank()) {
//            parsedAddress = parts.get(2);
//        }

        Double lat = parseDoubleSafe(parts.size() > 8 ? parts.get(8) : null);
        Double lng = parseDoubleSafe(parts.size() > 9 ? parts.get(9) : null);


        System.out.println("lat : " + lat);
        System.out.println("lng : " + lng);
    }

    private Double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
