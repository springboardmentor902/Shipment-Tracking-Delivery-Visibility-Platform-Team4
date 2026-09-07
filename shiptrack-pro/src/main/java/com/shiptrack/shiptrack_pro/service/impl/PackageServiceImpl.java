package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.PackageRequest;
import com.shiptrack.shiptrack_pro.dto.PackageResponse;
import com.shiptrack.shiptrack_pro.entity.Package;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.repository.PackageRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.service.PackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {

    private final PackageRepository packageRepository;
    private final ShipmentRepository shipmentRepository;

    @Override
    @Transactional
    public PackageResponse createPackage(Long shipmentId, PackageRequest request) {
        log.info("Creating package for shipment: {}", shipmentId);

        // Check if shipment exists
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found with id: " + shipmentId
                ));

        Package pkg = Package.builder()
                .shipmentId(shipmentId)
                .description(request.getDescription())
                .weight(request.getWeight())
                .lengthCm(request.getLengthCm())
                .widthCm(request.getWidthCm())
                .heightCm(request.getHeightCm())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .declaredValue(request.getDeclaredValue())
                .isFragile(request.getIsFragile() != null ? request.getIsFragile() : false)
                .trackingNumber(request.getTrackingNumber())
                .notes(request.getNotes())
                .build();

        Package savedPackage = packageRepository.save(pkg);
        log.info("Package created with ID: {}", savedPackage.getId());

        return PackageResponse.fromEntity(savedPackage);
    }

    @Override
    public List<PackageResponse> getPackagesByShipment(Long shipmentId) {
        return packageRepository.findByShipmentId(shipmentId)
                .stream()
                .map(PackageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PackageResponse getPackageById(Long id) {
        Package pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Package not found with id: " + id
                ));
        return PackageResponse.fromEntity(pkg);
    }

    @Override
    @Transactional
    public PackageResponse updatePackage(Long id, PackageRequest request) {
        Package pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Package not found with id: " + id
                ));

        pkg.setDescription(request.getDescription());
        pkg.setWeight(request.getWeight());
        pkg.setLengthCm(request.getLengthCm());
        pkg.setWidthCm(request.getWidthCm());
        pkg.setHeightCm(request.getHeightCm());
        pkg.setQuantity(request.getQuantity());
        pkg.setDeclaredValue(request.getDeclaredValue());
        pkg.setIsFragile(request.getIsFragile());
        pkg.setTrackingNumber(request.getTrackingNumber());
        pkg.setNotes(request.getNotes());

        Package updatedPackage = packageRepository.save(pkg);
        return PackageResponse.fromEntity(updatedPackage);
    }

    @Override
    @Transactional
    public void deletePackage(Long id) {
        Package pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Package not found with id: " + id
                ));
        packageRepository.delete(pkg);
    }

    @Override
    @Transactional
    public void deletePackagesByShipment(Long shipmentId) {
        packageRepository.deleteByShipmentId(shipmentId);
    }
}