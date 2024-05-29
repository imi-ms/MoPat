package de.imi.mopat.model.user;

import de.imi.mopat.helper.model.UUIDGenerator;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;

/**
 * The database table model for <i>forgot_password_token</i>. The forgot password token table holds
 * the tokens sent to users, which have forgotten their password.
 */
@Entity
@Table(name = "forgot_password_token")
public class ForgotPasswordToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @NotNull
    @Column(name = "uuid")
    private final String uuid = UUIDGenerator.createUUID();
    @NotNull(message = "{invitation.expirationDate.notNull}")
    @Column(name = "expirationDate")
    @Temporal(TemporalType.DATE)
    private final Date expirationDate;
    @JoinColumn(name = "user", referencedColumnName = "id")
    @OneToOne
    private User user;

    public ForgotPasswordToken() {
        // [bt] each forgot password token shall have an expiration date 7
        // days in the future from its instantiation.
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 7);
        expirationDate = calendar.getTime();
    }

    /**
     * Uses the setters to set attributes.See setters for constraints.
     *
     * @param user The {@link User user} who requested this forgot password token object.
     */
    public ForgotPasswordToken(final User user) {
        this();
        setUser(user);
    }

    /**
     * Returns the id of <code>this</code>.
     *
     * @return The current id of this forgot password token object. Might be
     * <code>null</code> for newly created objects. Is not <code> &lt;=0
     * </code>.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the uuid of <code>this</code>.
     *
     * @return The current uuid of this forgot password token object. Is never
     * <code>null</code>.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Returns the expiration date for this forgot password token object. It states the date when
     * this forgot password token object will be invalid.
     *
     * @return the expiration date for this forgot password token object. Is never
     * <code>null</code>.
     */
    public Date getExpirationDate() {
        return expirationDate;
    }

    /**
     * Returns the {@link User User} object who requested this forgot password token object.
     *
     * @return The {@link User User} object who requested this forgot password token object. Is
     * never <code>null</code>.
     */
    public User getUser() {
        return user;
    }

    /**
     * See {@link ForgotPasswordToken#getUser()} for a description.
     *
     * @param user The {@link User user} who requested this forgot password token object. Must not
     *             be <code>null</code>.
     */
    public void setUser(final User user) {
        assert user != null : "The given parameter was null";
        this.user = user;
    }

    /**
     * Checks if the current {@link ForgotPasswordToken} is active or not.
     *
     * @return True if the {@link ForgotPasswordToken} is active, otherwise false.
     */
    public boolean isActive() {
        return getExpirationDate().getTime() > new Date().getTime();
    }

    /**
     * @return A hashCode based on <code>this</code>' UUID.
     */
    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }

    /**
     * @param obj can be <code>null</code>.
     * @return <code>false</code> if the given parameter is either
     * <code>null</code> or not an <code>instanceof</code>
     * {@link ForgotPasswordToken}. <code>true</code> if the parameter's UUID is equal to
     * <code>this</code>' UUID.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ForgotPasswordToken other)) {
            return false;
        }
        return getUUID().equals(other.getUUID());
    }

    private String getUUID() {
        return this.uuid;
    }
}