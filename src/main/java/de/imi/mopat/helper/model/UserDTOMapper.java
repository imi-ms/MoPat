package de.imi.mopat.helper.model;

import de.imi.mopat.model.dto.UserDTO;
import de.imi.mopat.model.user.User;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UserDTOMapper implements Function<User, UserDTO> {

    /*
     * Converts this {@link User} object to an {@link UserDTO} object.
     *
     * @return An {@link UserDTO} object based on this {@link User}
     * object.
     */
    @Override
    public UserDTO apply(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setFirstname(user.getFirstname());
        userDTO.setLastname(user.getLastname());
        userDTO.setEmail(user.getEmail());

        return userDTO;
    }
}