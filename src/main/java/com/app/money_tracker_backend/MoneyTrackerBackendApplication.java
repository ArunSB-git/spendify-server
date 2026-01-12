package com.app.money_tracker_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoneyTrackerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyTrackerBackendApplication.class, args);
	}

}
