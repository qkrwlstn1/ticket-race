package com.jinsu.ticketrace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TicketraceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketraceApplication.class, args);
	}

}
