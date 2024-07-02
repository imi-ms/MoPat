package de.imi.mopat.model.user;

import de.imi.mopat.helper.controller.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.Instant;
import java.util.Date;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(name = "pin_authorization")
public class PinAuthorization {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @JoinColumn(name = "sid", referencedColumnName = "id")
    @ManyToOne
    private User user;


    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    private Date createDate;

    @Column(name = "remaining_tries")
    private int remainingTries;

    public PinAuthorization() {
        setCurrentDate();
        setInitialTries();
    }

    public PinAuthorization(User user) {
        setUser(user);
        setCurrentDate();
        setInitialTries();
    }


    public Long getId() {
        return this.id;
    }

    public User getUser() {
        return this.user;
    }

    private void setUser(User user) {
        this.user = user;
    }

    public Date getCreateDate() {
        return this.createDate;
    }

    private void setCurrentDate() {
        this.createDate = Date.from(Instant.now());
    }

    private void setInitialTries() {
        this.remainingTries = Constants.PIN_MAX_TRIES;
    }

    public int getRemainingTries() {
        return remainingTries;
    }

    public void setRemainingTries(int remainingTries) {
        this.remainingTries = remainingTries;
    }

    public void decreaseRemainingTries() {
        this.remainingTries -= 1;
    }
}
