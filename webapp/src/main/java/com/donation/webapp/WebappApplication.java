package com.donation.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.donation.webapp", "com.donation.common", "com.donation.email", "com.donation.data", "com.donation.payment.gateway"})
@EntityScan(basePackages = {"com.donation.webapp", "com.donation.common"})
@EnableJpaRepositories(basePackages = {"com.donation.common"})
@EnableJpaAuditing
public class WebappApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebappApplication.class, args);
    }

}