package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.User;

public interface NotificationService {
    void send(String type, User user, Shipment shipment);
}