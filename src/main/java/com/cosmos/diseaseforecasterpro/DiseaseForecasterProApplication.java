package com.cosmos.diseaseforecasterpro;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.cosmos.diseaseforecasterpro.mapper")
public class DiseaseForecasterProApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiseaseForecasterProApplication.class, args);
    }

}
