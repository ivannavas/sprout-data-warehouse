package io.github.ivannavas.sprout_data_warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SproutDataWarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SproutDataWarehouseApplication.class, args);
    }
}
