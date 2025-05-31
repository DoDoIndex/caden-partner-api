package com.railway.cadenpartner.controller;

import com.railway.cadenpartner.model.PartnerRegistration;
import com.railway.cadenpartner.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/partner-registration")
@Validated
public class PartnerRegistrationController {

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<?> registerPartner(@Valid @RequestBody PartnerRegistration registration) {
        emailService.sendRegistrationEmail(registration);
        return ResponseEntity.ok().body("Registration submitted successfully.");
    }
}
