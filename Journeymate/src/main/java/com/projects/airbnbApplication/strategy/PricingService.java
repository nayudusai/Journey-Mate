package com.projects.airbnbApplication.strategy;

import com.projects.airbnbApplication.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor

public class PricingService {
    public BigDecimal calculateDynamicPrice(Inventory inventory) {
        PricingStrategy pricingStrategy = new BasePricingStrategy();

        // apply the additional strategies

        pricingStrategy = new SurgePricingStrategy(pricingStrategy);
        pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);
        pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);
        pricingStrategy = new HolidayPricingStrategy(pricingStrategy);

        return pricingStrategy.calculatePrice(inventory);
    }

    public BigDecimal calculatePrice(List<Inventory> inventoryList) {
        return inventoryList.stream()
                .map(inventory -> calculateDynamicPrice(inventory))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
