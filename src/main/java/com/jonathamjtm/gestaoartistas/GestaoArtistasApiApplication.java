package com.jonathamjtm.gestaoartistas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GestaoArtistasApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestaoArtistasApiApplication.class, args);
	}

}
