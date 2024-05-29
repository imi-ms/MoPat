package de.imi.mopat.model.user;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * The database table model for <i>acl_object_identity</i>. The Access Control List (ACL) framework
 * maintains the permissions for instances of 'registered' classes of the MoPat model. The
 * AclObjectIdentity contains the information for an object of type <i>object_id_class</i>. The
 * Identity of a class is referenced via primary keys, which are retrieved from its origin
 * database.
 */
@Entity
@Table(name = "acl_object_identity")
public class AclObjectIdentity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @NotNull(message = "{aclObjectIdentity.objectIdIdentity.notNull}")
    @Column(name = "object_id_identity")
    private Long objectIdIdentity;
    @NotNull(message = "{aclObjectIdentity.entriesInheriting.notNull}")
    @Column(name = "entries_inheriting")
    private Boolean entriesInheriting;
    @JoinColumn(name = "owner_sid", referencedColumnName = "id")
    @ManyToOne
    private User owner;
    @JoinColumn(name = "object_id_class", referencedColumnName = "id")
    @ManyToOne
    private AclClass objectIdClass;
    @JoinColumn(name = "parent_object", referencedColumnName = "id")
    @ManyToOne
    private AclObjectIdentity parentObject;
    @OneToMany(mappedBy = "aclObjectIdentity", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<AclEntry> aclEntries = new HashSet<>();
    @ManyToMany(mappedBy = "assignedClinics")
    private Set<Invitation> invitation = new HashSet<>();

    public AclObjectIdentity() {
    }

    /**
     * Uses the setters to set attributes
     *
     * @param objectIdIdentity  See {@link AclObjectIdentity#setObjectIdIdentity(Long)} for a
     *                          description.
     * @param entriesInheriting See {@link AclObjectIdentity#setEntriesInheriting(Boolean)} for a
     *                          description.
     * @param objectIdClass     See {@link AclObjectIdentity#setObjectIdClass(AclClass)} for a
     *                          description.
     * @param owner             See {@link AclObjectIdentity#setOwner(User) } for a description.
     * @param parentObject      See {@link AclObjectIdentity#setParentObject(AclObjectIdentity)} for
     *                          a description.
     */
    public AclObjectIdentity(Long objectIdIdentity, Boolean entriesInheriting,
        AclClass objectIdClass, User owner, AclObjectIdentity parentObject) {
        setObjectIdIdentity(objectIdIdentity);
        setEntriesInheriting(entriesInheriting);
        setObjectIdClass(objectIdClass);
        setOwner(owner);
    }

    /**
     * @return The current id of this object. Might be <code>null</code> for newly created objects.
     * If <code>!null</code>, it's never
     * <code> &lt;= 0</code>.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the primary key for this objects supervised class' instance.
     *
     * @return The primary key of the current object. Is never
     * <code>null</code>. Is <code> &gt; 0 </code>.
     */
    public Long getObjectIdIdentity() {
        return objectIdIdentity;
    }

    /**
     * See {@link AclObjectIdentity#getObjectIdIdentity() } for a description.
     *
     * @param objectIdIdentity The primary key of the current object. Is never
     *                         <code>null</code>. Is <code> &gt; 0 </code>.
     */
    public void setObjectIdIdentity(Long objectIdIdentity) {
        assert objectIdIdentity != null : "The given parameter was null";
        assert objectIdIdentity > 0 : "The given parameter was < 1";
        this.objectIdIdentity = objectIdIdentity;
    }

    /**
     * Returns a flag to indicate if the permissions are inherited for the objects downwards in the
     * hierarchy.
     *
     * @return A flag to indicate if in a hierarchy of domain objects permissions are inherited
     * (<code>true</code>) or not (<code>false</code>) between the objects. Is never
     * <code>null</code>.
     */
    public Boolean getEntriesInheriting() {
        return entriesInheriting;
    }

    /**
     * See {@link AclObjectIdentity#getEntriesInheriting()} for a description.
     *
     * @param entriesInheriting The flag whether the given object inherits permissions. Must not be
     *                          <code>null</code>.
     */
    protected void setEntriesInheriting(final Boolean entriesInheriting) {
        assert entriesInheriting != null : "The given parameter was null";
        this.entriesInheriting = entriesInheriting;
    }

    /**
     * @return The {@link User} object who owns the object referred to in
     * {@link AclObjectIdentity#getObjectIdClass()} and
     * {@link AclObjectIdentity#getObjectIdIdentity()}. Is never
     * <code>null</code>.
     */
    public User getOwner() {
        return owner;
    }

    /**
     * See {@link AclObjectIdentity#getOwner()} for a description.
     *
     * @param owner The {@link User} object who owns the object referred to in
     *              {@link AclObjectIdentity#getObjectIdClass()} and
     *              {@link AclObjectIdentity#getObjectIdIdentity()}. Must not be
     *              <code>null</code>.
     */
    public void setOwner(final User owner) {
        assert owner != null : "The given parameter was null";
        this.owner = owner;
    }

    /**
     * See {@link AclClass AclClass} for a description.
     *
     * @return The {@link AclClass} object of which the referred
     * {@link AclObjectIdentity#getObjectIdIdentity()} instance is a type of. Is never
     * <code>null</code>.
     */
    public AclClass getObjectIdClass() {
        return objectIdClass;
    }

    /**
     * See {@link AclObjectIdentity#getObjectIdClass()} for a description.
     *
     * @param objectIdClass The {@link AclClass AclClass} object which refers to the fully qualified
     *                      class name of the administrated mopat model instance. Must not be
     *                      <code>null</code>.
     */
    public void setObjectIdClass(final AclClass objectIdClass) {
        assert objectIdClass != null : "The given parameter was null";
        this.objectIdClass = objectIdClass;
    }

    /**
     * Returns an aclObjectIdentity object if current object has a parent.
     *
     * @return The {@link AclObjectIdentity} object if the managed object has a parent. Might be
     * <code>null</code> if the managed object has no parent.
     */
    public AclObjectIdentity getParentObject() {
        return parentObject;
    }

    /**
     * See {@link AclObjectIdentity#getParentObject()} for a description.
     *
     * @param parentObject The parent object for this object. Can be
     *                     <code>null</code>.
     */
    public void setParentObject(final AclObjectIdentity parentObject) {
        this.parentObject = parentObject;
    }

    public void addAclEntry(final AclEntry aclEntry) {
        aclEntries.add(aclEntry);
    }

    public void removeAclEntry(final AclEntry aclEntry) {
        aclEntries.remove(aclEntry);
    }

    public Set<AclEntry> getAclEntries() {
        return aclEntries;
    }

    /**
     * @return the sum of {@link AclObjectIdentity#getObjectIdClass()}'s hashCode and
     * {@link AclObjectIdentity#getObjectIdIdentity()}'s hashCode.
     */
    @Override
    public int hashCode() {
        return getObjectIdClass().hashCode() + getObjectIdIdentity().hashCode();
    }

    /**
     * @param obj can be <code>null</code>.
     * @return <code>false</code> if the given parameter is either
     * <code>null</code> or not an <code>instanceof</code>
     * {@link AclObjectIdentity}. <code>true</code> if the parameter's objectIdClass (see
     * {@link AclObjectIdentity#getObjectIdClass()}) and aclObjectIdentity (see
     * {@link AclObjectIdentity#getObjectIdIdentity()}) are equal to <code>this</code>' respective
     * attributes.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AclObjectIdentity other)) {
            return false;
        }

        return (getObjectIdClass().equals(other.getObjectIdClass()) && getObjectIdIdentity().equals(
            other.getObjectIdIdentity()));
    }

    @Override
    public String toString() {
        return "de.imi.mopat.model.AclObjectIdentity[ id=" + this.getId() + " ]";
    }

    /**
     * @return the invitation
     */
    public Set<Invitation> getInvitation() {
        return invitation;
    }

    /**
     * @param invitation the invitation to set
     */
    public void setInvitation(final Set<Invitation> invitation) {
        this.invitation = invitation;
    }
}
