package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.PackageRequest;
import com.shiptrack.shiptrack_pro.entity.Package;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.repository.PackageRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.service.PackageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PackageServiceImpl implements PackageService {

    private final PackageRepository packageRepository;
    private final ShipmentRepository shipmentRepository;

    public PackageServiceImpl(
            PackageRepository packageRepository,
            ShipmentRepository shipmentRepository) {

        this.packageRepository = packageRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public Package createPackage(
            PackageRequest request,
            String userEmail) {

        Shipment shipment = getUserShipment(
                request.getShipmentId(),
                userEmail
        );

        Package pkg = Package.builder()
                .shipment(shipment)
                .description(request.getDescription())
                .weightKg(request.getWeightKg())
                .lengthCm(request.getLengthCm())
                .widthCm(request.getWidthCm())
                .heightCm(request.getHeightCm())
                .quantity(request.getQuantity())
                .declaredValue(request.getDeclaredValue())
                .fragile(request.getFragile())
                .build();

        return packageRepository.save(pkg);
    }

    @Override
    public List<Package> getAllPackages(String userEmail) {

        List<Package> packages =
                packageRepository.findByShipmentUserId(
                        getUserIdFromShipment(userEmail)
                );

        return packages;
    }

    @Override
    public Package getPackageById(
            Long id,
            String userEmail) {

        Package pkg = packageRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Package not found with id: " + id
                        )
                );

        validateOwnership(pkg, userEmail);

        return pkg;
    }

    @Override
    public Package updatePackage(
            Long id,
            PackageRequest request,
            String userEmail) {

        Package existingPackage =
                getPackageById(id, userEmail);

        Shipment shipment = getUserShipment(
                request.getShipmentId(),
                userEmail
        );

        existingPackage.setShipment(shipment);
        existingPackage.setDescription(
                request.getDescription()
        );
        existingPackage.setWeightKg(
                request.getWeightKg()
        );
        existingPackage.setLengthCm(
                request.getLengthCm()
        );
        existingPackage.setWidthCm(
                request.getWidthCm()
        );
        existingPackage.setHeightCm(
                request.getHeightCm()
        );
        existingPackage.setQuantity(
                request.getQuantity()
        );
        existingPackage.setDeclaredValue(
                request.getDeclaredValue()
        );
        existingPackage.setFragile(
                request.getFragile()
        );

        return packageRepository.save(existingPackage);
    }

    @Override
    public void deletePackage(
            Long id,
            String userEmail) {

        Package pkg = getPackageById(id, userEmail);

        packageRepository.delete(pkg);
    }

    private Shipment getUserShipment(
            Long shipmentId,
            String userEmail) {

        Shipment shipment = shipmentRepository
                .findById(shipmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Shipment not found with id: "
                                        + shipmentId
                        )
                );

        if (shipment.getUser() == null ||
                !shipment.getUser()
                        .getEmail()
                        .equals(userEmail)) {

            throw new RuntimeException(
                    "You are not authorized to access this shipment"
            );
        }

        return shipment;
    }

    private void validateOwnership(
            Package pkg,
            String userEmail) {

        if (pkg.getShipment() == null ||
                pkg.getShipment().getUser() == null ||
                !pkg.getShipment()
                        .getUser()
                        .getEmail()
                        .equals(userEmail)) {

            throw new RuntimeException(
                    "You are not authorized to access this package"
            );
        }
    }

    private Long getUserIdFromShipment(
            String userEmail) {

        List<Shipment> shipments =
                shipmentRepository.findAll();

        return shipments.stream()
                .filter(shipment ->
                        shipment.getUser() != null &&
                                shipment.getUser()
                                        .getEmail()
                                        .equals(userEmail))
                .map(shipment ->
                        shipment.getUser().getId())
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "No shipments found for user"
                        )
                );
    }
}