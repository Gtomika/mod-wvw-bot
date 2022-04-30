package com.gaspar.modwvwbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ModWvwBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModWvwBotApplication.class, args);
	}

}
