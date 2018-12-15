package io.github.cepr0.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import static java.util.Arrays.asList;

@SpringBootApplication
public class Application {

	private final UserRepo userRepo;

	public Application(UserRepo userRepo) {
		this.userRepo = userRepo;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Profile("!test")
	@Bean
	public CommandLineRunner demoData(UserRepo userRepo) {
		return args -> userRepo.saveAll(asList(
				new User("user1"),
				new User("user2"),
				new User("user3")
		)).forEach(u -> System.out.println(Long.toBinaryString(u.getId())));
	}
}
