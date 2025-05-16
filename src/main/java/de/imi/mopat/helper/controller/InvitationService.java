package de.imi.mopat.helper.controller;

import de.imi.mopat.model.user.Invitation;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class InvitationService {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(InvitationService.class);


    /**
     * Sorts a list of invitation in ascending order by their name.
     *
     * @param invitations The list of users to sort.
     * @return The sorted list of invitations.
     */
    public List<Invitation> sortInvitationByNameAsc(List<Invitation> invitations) {
        return invitations.stream()
            .sorted(Comparator.comparing(invitation -> invitation.getFirstName().toLowerCase()))
            .collect(Collectors.toList());
    }
}
