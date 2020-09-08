package dev.garyli.imagerepository.controllers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.jasypt.util.binary.StrongBinaryEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import dev.garyli.imagerepository.UserRepository;
import dev.garyli.imagerepository.models.Image;
import dev.garyli.imagerepository.models.ImagePermission;
import dev.garyli.imagerepository.models.User;

@Controller
@CrossOrigin(origins = "http://imagerepo.gary", allowCredentials = "true")
public class PhotoUploadController {
	private final static List<String> ACCEPTABLE_FILE_FORMATS = Arrays.asList("png", "jpeg", "jpg", "gif"); // fixed backed list

	@Autowired
	private UserRepository userRepo;

	@PostMapping(path = "/users/{username}/gallery")
	public ResponseEntity<String> receiveUploadedPhotos(Principal principal,
			@RequestParam(value = "files") MultipartFile[] uploadedFiles,
			@PathVariable(value = "username") String requestedUsername,
			@RequestParam(value = "isPrivate", required = false) Boolean isPrivate) {
		// since spring security is blocking unauthenticated requests, principal will not be null here
		String username = principal.getName();
		User curUser = userRepo.findByUsername(username);
		Set<Image> uploads = curUser.getUploads();

		// set isPrivate to false if its null, as if the checkbox is unchecked no value is sent at all
		isPrivate = isPrivate == null ? false : isPrivate;

		// rest api resource must match with authenticated user's username
		if (!username.equals(requestedUsername)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		// write each photo to disk
		try {
			long uploadInitiatedEpoch = Instant.now().getEpochSecond();
			StrongBinaryEncryptor binaryEncryptor = new StrongBinaryEncryptor();
			binaryEncryptor.setPassword(curUser.getHashedPassword());
			
			
			for (MultipartFile multipartFile : uploadedFiles) {
				String originalUploadName = multipartFile.getOriginalFilename();
				String fileExtension = FilenameUtils.getExtension(originalUploadName);
				String outputLocation = GalleryController.GALLERY_LOCATION + File.separatorChar + username + File.separatorChar + uploadInitiatedEpoch + "-" + originalUploadName;
				byte[] encryptedBytes = binaryEncryptor.encrypt(multipartFile.getBytes());

				if (!isAcceptableFileExtension(fileExtension)) {
					return new ResponseEntity<>("Cannot accept photo file format. Must be either" + ACCEPTABLE_FILE_FORMATS.toString(),HttpStatus.BAD_REQUEST);
				}

				Files.write(Paths.get(outputLocation), encryptedBytes); // save encrypted photo to disk
				uploads.add(new Image(outputLocation, username, isPrivate ? ImagePermission.PRIVATE : ImagePermission.PUBLIC, fileExtension));
			}

			userRepo.save(curUser); // save newly written data to database
		} catch (IOException iox) {
			System.out.println("IO Error when saving uploaded image");
			iox.printStackTrace();
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	private boolean isAcceptableFileExtension(String fileExtension) {
		return ACCEPTABLE_FILE_FORMATS.contains(fileExtension);
	}
}
