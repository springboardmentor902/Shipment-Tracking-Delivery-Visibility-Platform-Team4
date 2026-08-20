package com.shiptrack.shiptrack_pro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShiptrackProApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShiptrackProApplication.class, args);
		System.out.println("🚀 ShipTrack Pro is running!");
		System.out.println("📌 API available at: http://localhost:8080/api/");
	}
}