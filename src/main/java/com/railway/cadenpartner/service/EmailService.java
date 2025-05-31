package com.railway.cadenpartner.service;

import com.railway.cadenpartner.model.PartnerRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.registration.recipient}")
    private String toEmail;

    public void sendRegistrationEmail(PartnerRegistration registration) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Caden Partner Registration Form");
        message.setText(buildEmailBody(registration));
        mailSender.send(message);
    }

    private String buildEmailBody(PartnerRegistration reg) {
        return String.format(
                "A new partner registration has been submitted.\n\n"
                + "Full Name: %s\n"
                + "Email: %s\n"
                + "Phone Number: %s\n"
                + "Company Name: %s\n"
                + "Message: %s\n\n"
                + "This person wants to be a partner of Caden Tile.",
                reg.getFullName(),
                reg.getEmail(),
                reg.getPhoneNumber(),
                reg.getCompanyName(),
                reg.getMessage() == null ? "(No message)" : reg.getMessage()
        );
    }
}
