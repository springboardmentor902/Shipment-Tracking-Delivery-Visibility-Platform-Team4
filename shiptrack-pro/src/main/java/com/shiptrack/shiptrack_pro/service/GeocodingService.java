package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.GeocodingResponse;

public interface GeocodingService {

    GeocodingResponse geocode(String address);
}