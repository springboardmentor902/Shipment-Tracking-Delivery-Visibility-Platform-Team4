package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.PackageRequest;
import com.shiptrack.shiptrack_pro.dto.PackageResponse;

import java.util.List;

public interface PackageService {

    PackageResponse createPackage(Long shipmentId, PackageRequest request);

    List<PackageResponse> getPackagesByShipment(Long shipmentId);

    PackageResponse getPackageById(Long id);

    PackageResponse updatePackage(Long id, PackageRequest request);

    void deletePackage(Long id);

    void deletePackagesByShipment(Long shipmentId);
}