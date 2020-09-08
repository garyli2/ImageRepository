package dev.garyli.imagerepository.controllers;

import java.security.Principal;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dev.garyli.imagerepository.Application;
import dev.garyli.imagerepository.UserRepository;

@Controller
@CrossOrigin(allowCredentials = "true")
public class AuthenticationController {
	final private static String USERNAME_COOKIE_NAME = "username";
	final private static String USERNAME_COOKIE_PATH = "/";
	
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @PostMapping(path = "/auth/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password, HttpServletResponse response) {
        username = username.toLowerCase(); // permit lowercase usernames only

        if (!userExist(username)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(token); // this works because we've set a default password encoder, which tells spring what type of encryption the password is. Ex. BCrypt

        // put credentials into this worker thread
        SecurityContext cur = SecurityContextHolder.getContext();
        cur.setAuthentication(authentication);

        // check if user is authenticated after this
        if (cur.getAuthentication().isAuthenticated()) {
        	Cookie usernameIndicator = buildUsernameCookie(username);
    		response.addCookie(usernameIndicator);
            return new ResponseEntity<>(HttpStatus.OK);
        } 
        
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    
    @GetMapping(path = "/auth/authenticationStatus")
    public ResponseEntity<String> checkIfAuthenticated(Principal principal, HttpServletResponse response) {
    	if (principal != null) { // check if user is logged in
    		Cookie usernameIndicator = buildUsernameCookie(principal.getName());
    		response.addCookie(usernameIndicator);
    	}
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    private boolean userExist(String username) {
    	return userRepository.findByUsername(username) != null;
    }
    
    private Cookie buildUsernameCookie(String username) {
    	Cookie usernameIndicator = new Cookie(USERNAME_COOKIE_NAME, username);
		usernameIndicator.setHttpOnly(false);
		usernameIndicator.setDomain(Application.COOKIE_DOMAIN);
		usernameIndicator.setPath(USERNAME_COOKIE_PATH);
		return usernameIndicator;
    }
}