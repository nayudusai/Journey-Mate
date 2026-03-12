package com.projects.airbnbApplication.strategy;

import com.projects.airbnbApplication.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor

public class UrgencyPricingStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        LocalDate today = LocalDate.now();
        BigDecimal price = wrapped.calculatePrice(inventory);

        if(!inventory.getDate().isBefore(today) && inventory.getDate().isBefore(today.plusDays(7))) {
            return price.multiply(BigDecimal.valueOf(1.15));
        }
        return price;
    }
}
