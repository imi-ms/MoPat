package de.imi.mopat.model.user;

import de.imi.mopat.model.enumeration.PermissionType;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;

/**
 * The database table model for <i>acl_entry</i>. The Access Control List (ACL) framework secures a
 * given class and the model <i>AclEntry</i> contains the actual permission for a given class'
 * object and given user.
 */
@Entity
@Table(name = "acl_entry")
public class AclEntry implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @JsonIgnore
    @Column(name = "uuid")
    private final String uuid = UUIDGenerator.createUUID();
    //TODO The benefit of aceOrder has to be clarified.
    @NotNull(message = "{aclEntry.aceOrder.notNull}")
    @Column(name = "ace_order")
    private Integer aceOrder;
    @NotNull(message = "{aclEntry.permissionType.notNull}")
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "mask")
    private PermissionType permissionType;
    @NotNull(message = "{aclEntry.granting.notNull}")
    @Column(name = "granting")
    private Boolean granting;
    @NotNull(message = "{aclEntry.auditSuccess.notNull}")
    @Column(name = "audit_success")
    private Boolean auditSuccess;
    @NotNull(message = "{aclEntry.autidFailure.notNull}")
    @Column(name = "audit_failure")
    private Boolean auditFailure;
    @JoinColumn(name = "sid", referencedColumnName = "id")
    @ManyToOne
    private User user;
    @JoinColumn(name = "acl_object_identity", referencedColumnName = "id")
    @ManyToOne
    private AclObjectIdentity aclObjectIdentity;

    public AclEntry() {
    }

    /**
     * Uses the setters to set attributes.See setters for constraints.
     *
     * @param aceOrder          See {@link AclEntry#setAceOrder(Integer)} for description
     * @param permissionType    See
     *                          <p>
     *                          <p>
     *                          <p>
     *                          <p>
     *                          <p>
     *                          <p>
     *                          <p>
     *                          {@link AclEntry#setPermissionType(PermissionType)} for a
     *                          description.
     * @param granting          See {@link AclEntry#setGranting(Boolean)} for a description.
     * @param auditSuccess      See {@link AclEntry#setAuditSuccess(Boolean)} for a description.
     * @param auditFailure      See {@link AclEntry#setAuditFailure(Boolean)} for a description.
     * @param user              See {@link AclEntry#setUser(User)} for a description.
     * @param aclObjectIdentity See{@link AclEntry#setAclObjectIdentity(AclObjectIdentity)} for a
     *                          description.
     */
    public AclEntry(final User user, final AclObjectIdentity aclObjectIdentity, final int aceOrder,
        final PermissionType permissionType, final boolean granting, final boolean auditSuccess,
        final boolean auditFailure) {
        setAceOrder(aceOrder);
        setPermissionType(permissionType);
        setGranting(granting);
        setAuditSuccess(auditSuccess);
        setAuditFailure(auditFailure);
        setUser(user);
        setAclObjectIdentity(aclObjectIdentity);
    }

    /**
     * Returns the id of the current AclEntry object
     *
     * @return The current id of this object. Might be <code>null</code> for newly created objects.
     * If <code>!null</code>, it is never <code> &lt;= 0
     * </code>
     */
    public Long getId() {
        return id;
    }

    private String getUUID() {
        return this.uuid;
    }

    /**
     * Returns the ordering of an AccessControlEntry. Represents the hierarchy of the set masks.
     *
     * @return The current order of this object. Is never <code>null</code>
     */
    public Integer getAceOrder() {
        return aceOrder;
    }

    /**
     * See {@link AclEntry#getAceOrder()} for a description.
     *
     * @param aceOrder The new order for this AclEntry object. Must not be
     *                 <code>null</code>.
     */
    public void setAceOrder(final Integer aceOrder) {
        assert aceOrder != null : "The given parameter is invalid";
        this.aceOrder = aceOrder;
    }

    /**
     * See {@link PermissionType PermissionType} for type definition.
     *
     * @return The {@link PermissionType PermissionType} for this AclEntry instance. Is never
     * <code>null</code>.
     */
    public PermissionType getPermissionType() {
        return permissionType;
    }

    /**
     * See {@link AclEntry#getPermissionType()} for a description.
     *
     * @param permissionType Must not be <code>null</code>.
     */
    public void setPermissionType(final PermissionType permissionType) {
        assert permissionType != null : "The given parameter was null";
        this.permissionType = permissionType;
    }

    /**
     * Returns whether the current permission is to be interpreted as grant access or deny access.
     *
     * @return The flag for grant (<code>true</code>) or deny (<code>false</code>) access for the
     * referred MoPat model instance. Is never <code>null</code>.
     */
    public Boolean getGranting() {
        return granting;
    }

    /**
     * See {@link AclEntry#getGranting()} for a description.
     *
     * @param granting The flag for grant (<code>true</code>) or deny (<code>false</code>) access
     *                 for the referred MoPat model instance. Must not be <code>null</code>.
     */
    public void setGranting(final Boolean granting) {
        assert granting != null : "The given parameter was null";
        this.granting = granting;
    }

    /**
     * Returns a flag to indicate whether to audit a successful permission for the referred MoPat 2
     * model instance or not.
     *
     * @return The flag to indicate whether to audit a successful (<code>true</code>) permission for
     * the referred MoPat 2 model instance or not (<code>false</code>). Is never <code>null</code>.
     */
    public Boolean getAuditSuccess() {
        return auditSuccess;
    }

    /**
     * See {@link AclEntry#getAuditSuccess()} for a description.
     *
     * @param auditSuccess The flag to indicate whether to audit a successful permission
     *                     (<code>true</code>) for the referred MoPat 2 model instance or
     *                     not(<code>false</code>). Must not be
     *                     <code>null</code>.
     */
    public void setAuditSuccess(final Boolean auditSuccess) {
        assert auditSuccess != null : "The given parameter was null";
        this.auditSuccess = auditSuccess;
    }

    /**
     * Returns a flag to indicate whether to audit an unsuccessful permission for the referred MoPat
     * 2 model instance or not.
     *
     * @return The flag to indicate whether to audit an unsuccessful permission (<code>true</code>)
     * for the referred MoPat 2 model instance or not (<code>false</code>). Is never
     * <code>null</code>.
     */
    public Boolean getAuditFailure() {
        return auditFailure;
    }

    /**
     * See {@link AclEntry#getAuditFailure()} for a description.
     *
     * @param auditFailure The flag to indicate whether to audit an unsuccessful permission
     *                     (<code>true</code>) for the referred MoPat 2 model instance or not
     *                     (<code>false</code>).
     */
    public void setAuditFailure(final Boolean auditFailure) {
        assert auditFailure != null : "The given parameter was null";
        this.auditFailure = auditFailure;
    }

    /**
     * See {@link User User} for a description.
     *
     * @return The user object from {@link User User} to identify the user for whom the permission
     * apply. Is never <code>null</code>.
     */
    public User getUser() {
        return user;
    }

    /**
     * See {@link AclEntry#getUser()} for a description.
     *
     * @param user The {@link User} object to whom the permission should apply. Must not be
     *             <code>null</code>.
     */
    public void setUser(final User user) {
        assert user != null : "The given parameter was null";

        this.user = user;
        if (!user.getRights().contains(this)) {
            user.addRight(this);
        }
    }

    /**
     * See {@link AclObjectIdentity AclObjectIdentity} for a further description.
     *
     * @return The {@link AclObjectIdentity} object for this aclEntry object. Is never
     * <code>null</code>.
     */
    public AclObjectIdentity getAclObjectIdentity() {
        return aclObjectIdentity;
    }

    /**
     * See {@link AclEntry#getAclObjectIdentity()} for a description.
     *
     * @param aclObjectIdentity The new {@link AclObjectIdentity} object on which a given user
     *                          receives a permission. Must not be <code>null</code>.
     */
    public void setAclObjectIdentity(final AclObjectIdentity aclObjectIdentity) {
        assert aclObjectIdentity != null : "The given parameter was null";
        if (this.aclObjectIdentity != null) {
            this.aclObjectIdentity.removeAclEntry(this);
        }
        this.aclObjectIdentity = aclObjectIdentity;
        aclObjectIdentity.addAclEntry(this);
    }

    /**
     * @return hashCode based on this' {@link #getUUID() UUID}.
     */
    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }

    /**
     * @param obj can be <code>null</code>.
     * @return <code>false</code> if the given parameter is either
     * <code>null</code> or not an <code>instanceof</code> {@link AclEntry}.
     * <code>true</code> if the parameter's user (see
     * {@link AclEntry#getUser()}), aclObjectIdentity (see {@link AclEntry#getAclObjectIdentity()})
     * and permissionType (see {@link AclEntry#getPermissionType()}) are equal to <code>this</code>'
     * respective attributes.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AclEntry other)) {
            return false;
        }
        return getUUID().equals(other.getUUID());
    }

    @Override
    public String toString() {
        return "de.imi.mopat.model.AclEntry[ id=" + this.getId() + " ]";
    }
}