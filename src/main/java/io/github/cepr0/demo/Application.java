package io.github.cepr0.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

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

	@EventListener
	public void onReady(ApplicationReadyEvent e) {
		userRepo.saveAll(asList(
				new User("user1"),
				new User("user2"),
				new User("user3")
		));

		userRepo.findAll().forEach(System.out::println);
	}
}
