package de.imi.mopat.model.user;

import de.imi.mopat.helper.model.UUIDGenerator;

import java.io.Serializable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * The database table model for table userroles.
 */
@Entity
@Table(name = "userroles")
public class Authority implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "uuid")
    private final String uuid = UUIDGenerator.createUUID();
    @NotNull(message = "{userRole.user.notNull}")
    @ManyToOne
    @JoinColumn(name = "user", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @NotNull(message = "{userRole.authority.notNull}")
    @Column(name = "authority")
    private UserRole authority;

    /**
     * Default Constructor
     */
    protected Authority() { //default constructor (in protected state),
        // should not be accessible to anything else but the JPA
        // implementation (here: Hibernate) and the JUnit tests
    }

    /**
     * Constructor for new user object with given authority.Uses the setter to set attributes. See
     * setters for constraints
     *
     * @param authority The authority of the new user role object.
     * @param user      The new user object to be created
     */
    public Authority(final User user, final UserRole authority) {
        setUser(user);
        setAuthority(authority);
    }

    /**
     * Returns the id of the current user role object.
     *
     * @return The current id of this user role object. Might be
     * <code>null</code> for newly created objects. Is never <code> &lt;=0
     * </code>.
     */
    public Long getId() {
        return id;
    }

    private String getUUID() {
        return this.uuid;
    }

    /**
     * Returns the user of the current user role object.
     *
     * @return The current user of this user role object.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets a new user for this user role object.
     *
     * @param user The new user of this user role object. Must not be
     *             <code>null</code>.
     */
    public void setUser(final User user) {
        // FIXME shouldn't it be more like 'many users can have a Authority'?
        assert user != null : "The given user was null";
        this.user = user;
    }

    /**
     * Returns the authority of the current user role object.
     *
     * @return The current id of this user role object.
     */
    public String getAuthority() {
        // FIXME any constraints about this?
        return authority.getTextValue();
    }

    /**
     * Sets a new authority for this user role object.
     *
     * @param authority The new authority of this user role object.
     */
    public void setAuthority(final UserRole authority) {
        // FIXME any constraints about this?
        this.authority = authority;
    }

    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Authority other)) {
            return false;
        }
        return getUUID().equals(other.getUUID());
    }

    /**
     * Returns a representation of this user role object as a string value.
     *
     * @return The string, which represents this user role object.
     * @see Object
     */
    @Override
    public String toString() {
        // TODO maybe we should add some separators
        return this.getId() + this.getAuthority();
    }
}