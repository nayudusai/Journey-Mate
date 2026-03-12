package com.projects.airbnbApplication.strategy;

import com.projects.airbnbApplication.entity.Inventory;

import java.math.BigDecimal;


public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);

}
