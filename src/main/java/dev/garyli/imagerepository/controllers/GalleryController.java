package dev.garyli.imagerepository.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import dev.garyli.imagerepository.ImageRepository;
import dev.garyli.imagerepository.UserRepository;
import dev.garyli.imagerepository.models.DeleteRequest;
import dev.garyli.imagerepository.models.Image;
import dev.garyli.imagerepository.models.ImagePermission;
import dev.garyli.imagerepository.models.User;

import org.apache.commons.io.FileUtils;
import org.jasypt.util.binary.StrongBinaryEncryptor;

@Controller
@CrossOrigin(allowCredentials = "true")
public class GalleryController {
	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ImageRepository imageRepo;

	final static String GALLERY_FOLDER_NAME = "gallery";
	public final static String GALLERY_LOCATION = "" + Paths.get("").toAbsolutePath() + File.separatorChar + GALLERY_FOLDER_NAME;

	@GetMapping(path = "/users/{username}/gallery")
	public ResponseEntity<Object> fetchUserGallery(@PathVariable(value = "username") String requestedUsername,
			Principal principal) {
		// following REST conventions, username is a resource. However it must match
		// with current requester's username
		if (!principal.getName().equals(requestedUsername))
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		User curUser = userRepo.findByUsername(requestedUsername);

		if (curUser == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(curUser.getUploads(), HttpStatus.OK);
	}

	@DeleteMapping(path = "/users/{username}/gallery")
	public ResponseEntity<String> deleteEntireUserGallery(@PathVariable(value = "username") String requestedUsername,
			Principal principal) {
		if (!principal.getName().equals(requestedUsername))
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		User curUser = userRepo.findByUsername(requestedUsername);

		if (curUser == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		try {
			// empty user's gallery folder
			FileUtils.cleanDirectory(new File(GALLERY_LOCATION + File.separatorChar + requestedUsername));

		} catch (IOException e) {
			System.out.println("IO Error when deleting user gallery.");
			e.printStackTrace();
		}

		curUser.getUploads().clear();
		userRepo.save(curUser);

		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@DeleteMapping(path = "/users/{username}/gallery/{imageUUID}")
	public ResponseEntity<String> deleteSingleImage(@PathVariable(value = "username") String requestedUsername, @PathVariable(value="imageUUID") String imageUUID, Principal principal) {
		if (!principal.getName().equals(requestedUsername))
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		User curUser = userRepo.findByUsername(requestedUsername);

		if (curUser == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Set<Image> uploads = curUser.getUploads();

		Image imageToDelete = imageRepo.findByIdentifier(imageUUID);
		uploads.remove(imageToDelete); // remove from user's HashSet
		File imageToDeleteFile = new File(imageToDelete.getLocationReference());
		imageToDeleteFile.delete();

		userRepo.save(curUser);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(path = "/batchDelete")
	public ResponseEntity<String> bulkDeleteUserGallery(Principal principal, @RequestBody DeleteRequest deleteRequest) {
		String username = principal.getName();
		User curUser = userRepo.findByUsername(username);
		Set<Image> uploads = curUser.getUploads();

		for (String imageUUID : deleteRequest.getImagesToDelete()) {
			Image imageToDelete = imageRepo.findByIdentifier(imageUUID);

			// user specified nonexistent identifier, end result is the same..
			if (imageToDelete == null) {
				continue;
			}

			// attempted to delete image that is not of their own
			if (!imageToDelete.getOwner().equals(username)) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}

			uploads.remove(imageToDelete); // remove from user's HashSet
			File imageToDeleteFile = new File(imageToDelete.getLocationReference()); // delete from gallery folder as well
			imageToDeleteFile.delete();
		}

		userRepo.save(curUser);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(path = "/users/{username}/gallery/{photoUUID}")
	public ResponseEntity<byte[]> serveImage(@PathVariable(value = "username") String username, @PathVariable(value = "photoUUID") String photoUUID, Principal principal) {
		User uploader = userRepo.findByUsername(username);
		Image image = imageRepo.findByIdentifier(photoUUID);
		MediaType fileMediaType;
		byte[] encryptedImageData = null, decryptedImageData = null;
		Path imagePath;
		StrongBinaryEncryptor binaryEncryptor = new StrongBinaryEncryptor();
		binaryEncryptor.setPassword(uploader.getHashedPassword());

		// requested image was not found in system
		if (image == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		// only scenario to deny request, if the requester differs from owner and permission is private
		if (image.getPermission() == ImagePermission.PRIVATE) {
			if (principal == null || !principal.getName().equals(username)) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}	
		}
		
		fileMediaType = determineMediaType(image.getFileExtension());
		imagePath = Path.of(image.getLocationReference());

		try {
			encryptedImageData = Files.readAllBytes(imagePath);
			decryptedImageData = binaryEncryptor.decrypt(encryptedImageData);
		} catch (IOException e) {
			System.out.println("IO Exception while trying to serve image");
			e.printStackTrace();
		}

		return ResponseEntity.ok().contentType(fileMediaType).body(decryptedImageData);

	}

	@GetMapping(path = "/publicGallery")
	public ResponseEntity<List<Image>> getAllPublicImages() {
		List<Image> publicImages = imageRepo.findByPermission(ImagePermission.PUBLIC);

		return ResponseEntity.ok(publicImages);
	}

	private MediaType determineMediaType(String fileExtension) {
		// determine MediaType from file extension
		switch (fileExtension) {
		case "jpeg": case "jpg":
			return MediaType.IMAGE_JPEG;
		case "png":
			return MediaType.IMAGE_PNG;
		default:
			return MediaType.IMAGE_GIF;
		}
	}
}
