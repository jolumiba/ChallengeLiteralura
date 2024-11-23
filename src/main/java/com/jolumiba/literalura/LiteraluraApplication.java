package com.jolumiba.literalura;

import com.jolumiba.literalura.Principal.Principal;
import com.jolumiba.literalura.service.AuthorR;
import com.jolumiba.literalura.service.BookR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiteraluraApplication implements CommandLineRunner {

	@Autowired
	private BookR bookR;
	@Autowired
	private AuthorR authorR;

	public static void main(String[] args) {
		SpringApplication.run(LiteraluraApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal(bookR, authorR);
		principal.menuPrincipal();
	}
}
