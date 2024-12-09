package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.dto.BundleClinicDTO;
import de.imi.mopat.model.dto.ClinicDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * The database table model for table <i>clinic</i>. The Clinic model represents an actual clinic
 * where MoPat is in use.
 */
@Entity
@Table(name = "clinic")
public class Clinic implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    @NotNull(message = "{clinic.name.notNull}")
    @Size(min = 3, max = 255, message = "{clinic.name.size}")
    @Column(name = "name", nullable = false)
    private String name; // short name of the clinic
    // @Size and @NotNull have to be combined to realize @NotEmpty
    @NotNull(message = "{clinic.description.notNull}")
    @Size(min = 1, message = "{clinic.description.notNull}")
    @Column(name = "description", columnDefinition = "TEXT NOT NULL")
    private String description;
    @Pattern(regexp = "^$|[A-Za-z0-9.!#$%&'*+-/=?^_`{|}~]+@[A-Za-z0-9"
        + ".!#$%&'*+-/=?^_`{|}~]+\\.[A-Za-z]{2,}+", message = "{global.datatype.email.notValid}")
    @Column(name = "email")
    private String email;
    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, orphanRemoval = true)
    @Valid
    private Set<BundleClinic> bundleClinics = new HashSet<>();

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL)
    private List<ClinicConfigurationMapping> clinicConfigurationMappings;
    @OneToMany(mappedBy = "clinic")
    private List<Encounter> encounters;

    public Clinic() { //default constructor (in protected state), should not
        // be accessible to anything else but the JPA implementation (here:
        // Hibernate) and the JUnit tests
    }

    /**
     * Uses the setters to set attributes.See setters for constraints.
     *
     * @param name        See {@link Clinic#getName()}
     * @param description See {@link Clinic#getDescription()}
     */
    public Clinic(final String name, final String description) {
        setName(name);
        setDescription(description);
    }

    /**
     * Returns the id of the current clinic object.
     *
     * @return The current id of this clinic object. Might be <code>null</code> for newly created
     * objects. Is never <code> &lt;= 0 </code>
     */
    public Long getId() {
        return id;
    }

    private String getUUID() {
        return this.uuid;
    }

    /**
     * Returns the short name of the current clinic object.
     *
     * @return The short name of the current clinic object. Is never
     * <code>null</code>, has a least 3 and at most 255 characters.
     */
    public String getName() {
        return name;
    }

    /**
     * See {@link Clinic#getName()} for a description.Sets a new short name for this clinic object.
     * <p>
     * Trims it beforehand.
     *
     * @param name The new short name for this clinic object. Must not be
     *             <code>null</code>. Has to be at least 3 characters after
     *             trimming and at most 255 characters after trimming.
     */
    public void setName(String name) {
        assert name != null : "The given name was null";
        assert name.trim().length() >= 3 : "The given name has < 3 characters (after trimming)";
        assert name.trim().length() <= 255 : "The given name has > 255 Characters (after trimming)";
        this.name = name.trim();
    }

    /**
     * Returns the description of the current clinic object. A description might be a longer name
     * for a clinic, or something else that clearly identifies it, such as the name of a study
     *
     * @return The description of the current clinic object. Is never
     * <code>null</code>. Is at least 3 and at most 255 characters long.
     */
    public String getDescription() {
        return description;
    }

    /**
     * See {@link Clinic#getDescription()} for a description.Sets a new description for this clinic
     * object.
     *
     * @param description The new description for this bundle object. Must not be <code>null</code>.
     *                    Has to be at least 3 and at most 255 characters in length (after
     *                    trimming). Will be trimmed when setting.
     */
    public void setDescription(final String description) {
        assert description != null : "The given description was null";
        assert
            description.trim().length() <= 255 :
            "The given description was longer than 255 " + "characters (after trimming)";
        assert
            description.trim().length() >= 3 :
            "The given description has less than 3 characters " + "after (trimming)";
        this.description = description.trim();
    }

    /**
     * Returns the email adress of the current clinic object.
     *
     * @return The email adress as String of the current clinic. Might be
     * <code>null</code>.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email adress of the current clinic object.
     *
     * @param email The new email adress of the current clinic object.
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Returns all {@link BundleClinic} objects of the current clinic object.
     *
     * @return The current {@link BundleClinic} objects of this clinic object. Is never
     * <code>null</code>. Might be empty. Is unmodifiable.
     */
    public SortedSet<BundleClinic> getBundleClinics() {
        return Collections.unmodifiableSortedSet(new TreeSet<BundleClinic>(bundleClinics));
    }

    /**
     * Adds all given {@link BundleClinic} objects that are not already associated with this clinic
     * to the corresponding set of BundleClinics .Takes care that the {@link BundleClinic} objects
     * refer to this one, too.
     *
     * @param bundleClinics The set of additional {@link BundleClinic BundleClinic} objects for this
     *                      clinic object. Must not be <code>null</code>. Can be empty.
     */
    public void addBundleClinics(final Set<BundleClinic> bundleClinics) {
        assert bundleClinics != null : "The given set was null";
        for (BundleClinic bundleClinic : bundleClinics) {
            addBundleClinic(bundleClinic);
        }
    }

    /**
     * Adds a new {@link BundleClinic} object to the corresponding set of BundleClinics.Takes care
     * that the given {@link BundleClinic} object refers to this one, too.
     *
     * @param bundleClinic The {@link BundleClinic} object, which will be added to this clinic. Must
     *                     not be <code>null</code>.
     */
    public void addBundleClinic(final BundleClinic bundleClinic) {
        assert bundleClinic != null : "The given BundleClinic object was null";

        this.bundleClinics.add(bundleClinic);
        //take care that the objects know each other
        if (bundleClinic.getClinic() == null || !bundleClinic.getClinic().equals(this)) {
            // Add this bundle to the BundleClinic
            bundleClinic.setClinic(this);
        }
    }

    /**
     * Removes a {@link BundleClinic} object from the set of BundleClinics.Has to be called together
     * with {@link Bundle#removeBundleClinic(BundleClinic)} to avoid false mappings/references.
     * Takes care that the {@link BundleClinic} does not refer to this object anymore.
     *
     * @param bundleClinic The {@link BundleClinic} object, which will be removed. Must not be
     *                     <code>null</code>.
     */
    public void removeBundleClinic(final BundleClinic bundleClinic) {
        assert bundleClinic != null : "The given BundleClinic was null";
        // If the BundleClinic is in the set, remove it and remove this
        // Clinic from the BundleClinic
        if (this.bundleClinics.remove(bundleClinic)) {
            bundleClinic.removeClinic();
        }
    }

    /**
     * Removes all given {@link BundleClinic} objects from this clinic.
     *
     * @param bundleClinics The {@link BundleClinic} objects, which will be removed from this
     *                      clinic
     */
    public void removeBundleClinics(final Collection<BundleClinic> bundleClinics) {
        assert bundleClinics != null : "The given bundleClinics was null";
        for (BundleClinic bundleClinic : bundleClinics) {
            removeBundleClinic(bundleClinic);
        }
    }

    /**
     * Removes all {@link BundleClinic} objects from this clinic. Takes care that the
     * {@link BundleClinic} objects do not refer to this object anymore.
     */
    public void removeAllBundleClinics() {
        Collection<BundleClinic> tempBundleClinics = new HashSet<>(bundleClinics);
        for (BundleClinic bundleClinic : tempBundleClinics) {
            removeBundleClinic(bundleClinic);
        }
        this.bundleClinics.clear();
    }


    public List<ClinicConfigurationMapping> getClinicConfigurationMappings() {
        return clinicConfigurationMappings;
    }

    public void setClinicConfigurationMappings(List<ClinicConfigurationMapping> clinicConfigurationMappings) {
        this.clinicConfigurationMappings = clinicConfigurationMappings;
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
        if (!(obj instanceof Clinic)) {
            return false;
        }
        Clinic other = (Clinic) obj;
        return getUUID().equals(other.getUUID());
    }

    public List<Encounter> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<Encounter> encounters) {
        this.encounters = encounters;
    }
}
