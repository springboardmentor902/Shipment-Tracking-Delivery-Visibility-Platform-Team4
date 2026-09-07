package com.shiptrack.shiptrack_pro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // ✅ This enables component scanning
public class ShiptrackProApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShiptrackProApplication.class, args);
		System.out.println("🚀 ShipTrack Pro is running!");
	}
}