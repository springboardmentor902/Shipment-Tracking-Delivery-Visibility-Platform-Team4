package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.PackageRequest;
import com.shiptrack.shiptrack_pro.entity.Package;

import java.util.List;

public interface PackageService {

    Package createPackage(PackageRequest request, String userEmail);

    List<Package> getAllPackages(String userEmail);

    Package getPackageById(Long id, String userEmail);

    Package updatePackage(Long id, PackageRequest request, String userEmail);

    void deletePackage(Long id, String userEmail);
}