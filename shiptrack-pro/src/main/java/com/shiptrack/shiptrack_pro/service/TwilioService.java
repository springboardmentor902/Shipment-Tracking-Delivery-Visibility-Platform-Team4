package com.shiptrack.shiptrack_pro.service;

public interface TwilioService {

    void sendSms(String to, String message);
}