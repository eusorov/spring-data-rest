package com.example.spring_data_rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.ibatis.logging.LogFactory;

@SpringBootApplication
public class DemoApplication {

	static {
		LogFactory.useNoLogging();
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
