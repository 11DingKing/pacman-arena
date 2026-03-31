package com.pacman;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pacman.mapper")
public class PacmanApplication {
    public static void main(String[] args) {
        SpringApplication.run(PacmanApplication.class, args);
    }
}
