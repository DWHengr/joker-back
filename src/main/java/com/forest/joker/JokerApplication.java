package com.forest.joker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.forest.joker.mapper")
public class JokerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JokerApplication.class, args);
    }

}
