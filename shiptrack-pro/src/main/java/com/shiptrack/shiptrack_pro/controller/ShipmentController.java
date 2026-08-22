package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.ShipmentRequest;
import com.shiptrack.shiptrack_pro.dto.ShipmentResponse;
import com.shiptrack.shiptrack_pro.entity.ShipmentStatus;
import com.shiptrack.shiptrack_pro.service.ShipmentService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;


    // =========================================================
    // CREATE SHIPMENT
    // =========================================================

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(

            @Valid
            @RequestBody
            ShipmentRequest request,

            Authentication authentication

    ) {

        String userEmail =
                authentication.getName();


        ShipmentResponse response =
                shipmentService.createShipment(
                        request,
                        userEmail
                );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================================================
    // GET MY SHIPMENTS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<ShipmentResponse>>
    getAllShipments(
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();


        return ResponseEntity.ok(
                shipmentService.getAllShipments(
                        userEmail
                )
        );
    }


    // =========================================================
    // GET MY SHIPMENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse>
    getShipmentById(

            @PathVariable
            Long id,

            Authentication authentication

    ) {

        String userEmail =
                authentication.getName();


        return ResponseEntity.ok(
                shipmentService.getShipmentById(
                        id,
                        userEmail
                )
        );
    }


    // =========================================================
    // UPDATE MY SHIPMENT
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<ShipmentResponse>
    updateShipment(

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            ShipmentRequest request,

            Authentication authentication

    ) {

        String userEmail =
                authentication.getName();


        return ResponseEntity.ok(
                shipmentService.updateShipment(
                        id,
                        request,
                        userEmail
                )
        );
    }


    // =========================================================
    // UPDATE MY SHIPMENT STATUS
    // =========================================================

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentResponse>
    updateShipmentStatus(

            @PathVariable
            Long id,

            @RequestParam
            ShipmentStatus status,

            Authentication authentication

    ) {

        String userEmail =
                authentication.getName();


        return ResponseEntity.ok(
                shipmentService.updateShipmentStatus(
                        id,
                        status,
                        userEmail
                )
        );
    }


    // =========================================================
    // CANCEL MY SHIPMENT
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ShipmentResponse>
    cancelShipment(

            @PathVariable
            Long id,

            Authentication authentication

    ) {

        String userEmail =
                authentication.getName();


        ShipmentResponse response =
                shipmentService.cancelShipment(
                        id,
                        userEmail
                );


        return ResponseEntity.ok(
                response
        );
    }
    // =========================================================
// ASSIGN OPERATOR
// =========================================================

    @PatchMapping("/{id}/assign-operator")
    public ResponseEntity<ShipmentResponse> assignOperator(

            @PathVariable
            Long id,

            @RequestParam
            Long operatorId,

            Authentication authentication

    ) {

        String userEmail =
                authentication.getName();

        return ResponseEntity.ok(
                shipmentService.assignOperator(
                        id,
                        operatorId,
                        userEmail
                )
        );
    }
}