package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.entity.ETAPrediction;

public interface ETAService {

    ETAPrediction predictETA(Long shipmentId);

    ETAPrediction getPrediction(Long shipmentId);
}
