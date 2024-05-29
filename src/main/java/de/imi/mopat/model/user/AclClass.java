package de.imi.mopat.model.user;

import java.io.Serializable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * The database table model for <i>acl_class</i>. The Access Control List (ACL) framework allows to
 * administrate access rights for every different object that is 'registered' as an
 * {@link AclClass}. This model holds the information about the fully qualified name for each of the
 * classes administrated with ACLs in MoPat. An instance of this class represents a record about a
 * single class in the MoPat model.
 */
@Entity
@Table(name = "acl_class")
public class AclClass implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @NotNull(message = "{aclClass.className.notNull}")
    @Size(min = 21, max = 255, message = "{aclClass.className.Size}")
    @Column(name = "class", nullable = false)
    private String className;

    public AclClass() {
    }

    /**
     * Uses the setters to set attributes.See setters for constraints.
     *
     * @param className The fully qualified name of the MoPat model class. Must not be
     *                  <code>null</code>, will be trimmed. Has to be at least <i>21</i> characters
     *                  in length, the first 20 characters must be
     *                  <code>de.imi.mopat.model.</code>
     */
    public AclClass(final String className) {
        setClassName(className);
    }

    /**
     * Returns the id for the current AclClass object
     *
     * @return The current id of this aclClass object. Might be
     * <code>null</code> for newly created objects. If <code>!null</code>, it's
     * never <code> &lt;= 0</code>.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the fully qualified class name for the MoPat model class administrated with this.
     *
     * @return The fully qualified name of the class set for the ACL permission framework. Is never
     * <code>null</code> and has at least <i>21</i> Characters, the first <i>20</i> characters are
     * always
     * <code>de.imi.mopat.model.</code> and the <i>21st.</i> and following
     * characters denominate the model name.
     */
    public String getClassName() {
        return className;
    }

    /**
     * See {@link AclClass#getClassName()} for a description.
     *
     * @param aclClass The fully qualified name of the MoPat model class. Must not be
     *                 <code>null</code>, will be trimmed. Has to be at least <i>21</i> characters
     *                 in length, the first 20 characters must be
     *                 <code>de.imi.mopat.model.</code>
     */
    private void setClassName(final String aclClass) {
        assert aclClass != null : "The given parameter was null";
        assert
            aclClass.trim().length() >= 20 :
            "The given paramter has < 21 characters (after " + "trimming)";
        assert aclClass.trim().startsWith("de.imi.mopat.model.") :
            "The given class" + " name was not from de.imi.mopat.model.";
        this.className = aclClass.trim();
    }

    /**
     * @return The {@link String#hashCode()} of the {@link AclClass#getClassName()} value.
     */
    @Override
    public int hashCode() {
        return className.hashCode();
    }

    /**
     * @param obj can be <code>null</code>.
     * @return <code>false</code> if the given parameter is either
     * <code>null</code> or not an <code>instanceof</code> {@link AclClass}.
     * <code>true</code> if the parameter's className (see
     * {@link AclClass#getClassName()} is equal to <code>this</code>' className.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AclClass other)) {
            return false;
        }
        return getClassName().equals(other.getClassName());
    }
}
