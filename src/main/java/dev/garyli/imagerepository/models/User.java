package dev.garyli.imagerepository.models;

import java.util.*;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
public class User {
	public final static List<SimpleGrantedAuthority> ROLE_USER = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	private String uid;

	private String username;
	private String hashedPassword;
	
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	private Set<Image> uploads;

	public String getHashedPassword() {
		return hashedPassword;
	}

	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}
	
	public Set<Image> getUploads() {
		return uploads;
	}
	
	public void setUploads(Set<Image> uploads) {
		this.uploads = uploads;
	}
}
