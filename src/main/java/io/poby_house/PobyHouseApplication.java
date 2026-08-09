package io.poby_house;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PobyHouseApplication {

    static {
        System.setProperty("log4jdbc.spylogdelegator.name", "io.poby_house.log.CustomLog4JdbcCustomFormatter");
    }

    public static void main(String[] args) {
        SpringApplication.run(PobyHouseApplication.class, args);
    }
    
}
