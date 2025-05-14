package com.zkril.aerial_back;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.zkril.aerial_back.mapper")
@SpringBootApplication
public class AerialBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(AerialBackApplication.class, args);
	}

}
