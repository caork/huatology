package com.digitaltwin.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class DigitalTwinBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalTwinBackendApplication.class, args);
    }

}
