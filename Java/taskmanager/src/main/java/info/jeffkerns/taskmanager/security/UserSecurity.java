package info.jeffkerns.taskmanager.security;

import info.jeffkerns.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("userSecurity")
public class UserSecurity {

    private final UserRepository userRepository;

    public UserSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public boolean isUserOwner(Long userId, String username) {
        return userRepository.findById(userId)
            .map(user -> username.equals(user.getUsername()))
            .orElse(false);
    }
}
