package com.projects.airbnbApplication.strategy;

import com.projects.airbnbApplication.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor

public class HolidayPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        Boolean isTodayHoliday = true;
        BigDecimal price = wrapped.calculatePrice(inventory);

        if(isTodayHoliday) {
            return  price.multiply(BigDecimal.valueOf(1.25));
        }
        return price;
    }
}
