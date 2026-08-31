package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.service.TwilioService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TwilioServiceImpl implements TwilioService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void initializeTwilio() {

        if (accountSid == null || accountSid.startsWith("YOUR_")
                || authToken == null || authToken.startsWith("YOUR_")) {

            System.out.println(
                    "Twilio not configured. SMS notifications disabled."
            );

            return;
        }

        Twilio.init(accountSid, authToken);
    }

    @Override
    public void sendSms(String to, String message) {

        if (accountSid == null || accountSid.startsWith("YOUR_")
                || authToken == null || authToken.startsWith("YOUR_")
                || fromPhoneNumber == null
                || fromPhoneNumber.startsWith("YOUR_")) {

            System.out.println(
                    "Twilio not configured. Skipping SMS."
            );

            return;
        }

        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromPhoneNumber),
                message
        ).create();
    }
}