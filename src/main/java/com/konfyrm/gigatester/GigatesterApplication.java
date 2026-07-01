package com.konfyrm.gigatester;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GigatesterApplication {

	public static void main(String[] args) {
		SpringApplication.run(GigatesterApplication.class, args);
	}

}
