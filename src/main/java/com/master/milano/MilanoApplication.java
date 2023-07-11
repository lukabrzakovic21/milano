package com.master.milano;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MilanoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MilanoApplication.class, args);
	}

}
