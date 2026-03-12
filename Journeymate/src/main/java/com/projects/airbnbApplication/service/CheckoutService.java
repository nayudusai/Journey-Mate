package com.projects.airbnbApplication.service;

import com.projects.airbnbApplication.entity.Booking;

public interface CheckoutService {

    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
