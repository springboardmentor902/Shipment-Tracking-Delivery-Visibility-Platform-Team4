package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.GeocodingResponse;
import com.shiptrack.shiptrack_pro.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class GeocodingServiceImpl implements GeocodingService {

    private final JsonMapper jsonMapper;

    @Value("${osm.nominatim.base-url}")
    private String baseUrl;

    @Value("${osm.nominatim.user-agent}")
    private String userAgent;

    @Override
    public GeocodingResponse geocode(String address) {

        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("User-Agent", userAgent)
                    .build();

            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", address)
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(String.class);

            JsonNode results = jsonMapper.readTree(response);

            if (!results.isArray() || results.isEmpty()) {
                throw new RuntimeException(
                        "Location not found: " + address
                );
            }

            JsonNode result = results.get(0);

            GeocodingResponse geocodingResponse =
                    new GeocodingResponse();

            geocodingResponse.setDisplayName(
                    result.get("display_name").asString()
            );

            geocodingResponse.setLatitude(
                    result.get("lat").asDouble()
            );

            geocodingResponse.setLongitude(
                    result.get("lon").asDouble()
            );

            return geocodingResponse;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to geocode address: " + address,
                    e
            );
        }
    }
}