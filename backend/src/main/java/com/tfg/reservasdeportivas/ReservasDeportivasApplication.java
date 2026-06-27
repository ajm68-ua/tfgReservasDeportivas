package com.tfg.reservasdeportivas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReservasDeportivasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservasDeportivasApplication.class, args);
    }

}
