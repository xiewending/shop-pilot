package com.shoppilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.shoppilot.mapper")
@SpringBootApplication
public class ShopPilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopPilotApplication.class, args);
    }
}
