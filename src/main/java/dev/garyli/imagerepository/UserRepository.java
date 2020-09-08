package dev.garyli.imagerepository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import dev.garyli.imagerepository.models.Image;
import dev.garyli.imagerepository.models.User;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
	public User findByUsername(String username);
}
