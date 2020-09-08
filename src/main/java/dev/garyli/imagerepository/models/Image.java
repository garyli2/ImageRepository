package dev.garyli.imagerepository.models;

import java.util.UUID;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Image {
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	private String identifier;
	@JsonIgnore
	private String locationReference;
	private String fileName;
	

	private String owner;
	private ImagePermission permission;
	private String fileExtension;

	public Image() {
	}

	public Image(String locationReference, String owner, ImagePermission permission, String fileExtension) {
		this.locationReference = locationReference;
		this.owner = owner;
		this.permission = permission;
		this.setFileExtension(fileExtension);
	}

	public String getLocationReference() {
		return locationReference;
	}

	public void setLocationReference(String locationReference) {
		this.locationReference = locationReference;
	}

	public ImagePermission getPermission() {
		return permission;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setPermission(ImagePermission permission) {
		this.permission = permission;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
		
		return this.identifier.equals(((Image)other).identifier);
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	

}
