package com.voltflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; 

@SpringBootApplication
@EnableScheduling 
public class VoltflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(VoltflowApplication.class, args);
	}
}