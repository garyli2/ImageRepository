package dev.garyli.imagerepository.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import dev.garyli.imagerepository.UserRepository;
import dev.garyli.imagerepository.models.User;

@Controller
@CrossOrigin
public class RegistrationController {
	@Autowired
	private UserRepository userRepo;

	@PostMapping(path = "/users")
	public ResponseEntity<String> signupUser(@RequestParam String username, @RequestParam String password) {
		var bCryptEncoder = new BCryptPasswordEncoder();

		// validate if username is already taken
		if (isUsernameTaken(username)) {
			return new ResponseEntity<String>("Username already taken", HttpStatus.BAD_REQUEST);
		}
		
		// username or password cannot be empty
		if (username.trim().equals("") || password.trim().equals("")) {return new ResponseEntity<>(HttpStatus.BAD_REQUEST);}

		// construct new user object with provided credentials
		User newUser = new User();
		newUser.setUsername(username);
		newUser.setHashedPassword(bCryptEncoder.encode(password));

		userRepo.save(newUser);
		createUserGalleryFolder(username);
		
		return new ResponseEntity<String>(HttpStatus.OK);
	}

	private boolean isUsernameTaken(String username) {
		return userRepo.findByUsername(username) != null;
	}

	private void createUserGalleryFolder(String username) {
		try {
			Files.createDirectories(Paths.get(GalleryController.GALLERY_FOLDER_NAME + File.separatorChar + username));
		} catch (IOException iox) {
			System.out.println("IO Error!");
			iox.printStackTrace();
		}

	}

}
