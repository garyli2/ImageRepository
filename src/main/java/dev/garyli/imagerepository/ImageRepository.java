package dev.garyli.imagerepository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import dev.garyli.imagerepository.models.Image;
import dev.garyli.imagerepository.models.ImagePermission;

@Repository
public interface ImageRepository extends CrudRepository<Image, String> {
	public Image findByOwner(String username);
	
	public Image findByIdentifier(String identifier);
	
	public List<Image> findByPermission(ImagePermission permission);
	
}
