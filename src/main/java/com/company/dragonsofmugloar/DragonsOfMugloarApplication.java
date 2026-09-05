package com.company.dragonsofmugloar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DragonsOfMugloarApplication {

    public static void main(String[] args) {
        SpringApplication.run(DragonsOfMugloarApplication.class, args);
    }

}
