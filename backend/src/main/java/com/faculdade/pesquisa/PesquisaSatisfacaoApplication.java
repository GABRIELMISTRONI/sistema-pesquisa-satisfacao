package com.faculdade.pesquisa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PesquisaSatisfacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PesquisaSatisfacaoApplication.class, args);
    }
}
