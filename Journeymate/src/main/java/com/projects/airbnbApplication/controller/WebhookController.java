package com.projects.airbnbApplication.controller;

import com.projects.airbnbApplication.service.BookingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/webhook")
public class WebhookController {

    @Value("whsec_7fad987f4c245ac9cebb3108e36d1ba09aae51c0a249ce74b582086add4fb093")
    private String endPointSecretKey;

    private final BookingService bookingService;

    @PostMapping("/payment")
    public ResponseEntity<Void> capturePayments(@RequestBody String payLoad , @RequestHeader("Stripe-signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payLoad,sigHeader, endPointSecretKey);
            bookingService.capturePayment(event);
            return ResponseEntity.noContent().build();
        }catch (SignatureVerificationException e) {
            throw new RuntimeException(e);
        }
    }
}
