package dev.garyli.imagerepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.garyli.imagerepository.models.User;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        
        // in the scenario that the user does not exist
        if (user == null) throw new UsernameNotFoundException(username);

        return buildUserForAuthentication(user);
    }
    
    // convert our own type of User to Spring's User.
 	private org.springframework.security.core.userdetails.User buildUserForAuthentication(User user) {
 		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getHashedPassword(), true, true, true, true, User.ROLE_USER);
 	}
}
