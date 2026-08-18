package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.GeocodingResponse;
import com.shiptrack.shiptrack_pro.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geocoding")
@RequiredArgsConstructor
public class GeocodingController {

    private final GeocodingService geocodingService;

    @GetMapping
    public ResponseEntity<GeocodingResponse> geocode(
            @RequestParam String address) {

        return ResponseEntity.ok(
                geocodingService.geocode(address)
        );
    }
}