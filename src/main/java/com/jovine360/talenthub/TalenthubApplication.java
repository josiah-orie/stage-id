package com.jovine360.talenthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class TalenthubApplication {

	public static void main(String[] args) {
		SpringApplication.run(TalenthubApplication.class, args);
	}

}
