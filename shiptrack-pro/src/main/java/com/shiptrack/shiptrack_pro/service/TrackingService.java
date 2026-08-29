package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.TrackingEventRequest;
import com.shiptrack.shiptrack_pro.dto.TrackingEventResponse;

import java.util.List;

public interface TrackingService {

    TrackingEventResponse createTrackingEvent(
            Long shipmentId,
            TrackingEventRequest request,
            String userEmail
    );

    List<TrackingEventResponse> getTrackingEvents(
            Long shipmentId
    );
}