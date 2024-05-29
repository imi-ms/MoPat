package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.dto.BundleClinicDTO;
import de.imi.mopat.model.dto.ClinicDTO;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * The database table model for table <i>BundleClinic</i>. BundleClinic represents the joinTable for
 * {@link Bundle} and {@link Clinic}. With this both models know each other. The position attribute
 * puts the bundles for a given clinic in order.
 */
@Entity
@Table(name = "bundle_clinic")
public class BundleClinic implements Serializable, Comparable<BundleClinic> {

    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    @NotNull(message = "{bundleClinic.position.notNull}")
    @Column(nullable = false)
    private Integer position;
    @Id
    @ManyToOne
    @JoinColumn(name = "clinic_id", referencedColumnName = "id")
    private Clinic clinic;

    @Id
    @ManyToOne
    @JoinColumn(name = "bundle_id", referencedColumnName = "id")
    private Bundle bundle;

    protected BundleClinic() { //default constructor (in protected state),
        // should not be accessible to anything else but the JPA
        // implementation (here: Hibernate) and the JUnit tests
    }

    /**
     * uses the setters to set the attributes.See setters for constraints.
     *
     * @param position States the position for a bundle within a clinic
     * @param clinic   Object
     * @param bundle   Object
     */
    public BundleClinic(final Integer position, final Clinic clinic, final Bundle bundle) {
        setPosition(position);
        setClinic(clinic);
        setBundle(bundle);
    }

    /**
     * Returns the clinic of the bundle-clinic association.
     *
     * @return The {@link Clinic Clinic} object of the {@link Bundle Bundle}-{@link Clinic Clinic}
     * association. Might be
     * <code>null</code> (if no {@link Clinic} is currently set; quite unusual).
     */
    public Clinic getClinic() {
        return clinic;
    }

    /**
     * Sets a new clinic for the bundle-clinic association.Takes care that the {@link Clinic}
     * objects refers to this one, too.
     *
     * @param clinic The new {@link Clinic Clinic} object of the
     *               {@link Bundle Bundle}-{@link Clinic Clinic} association. Must not be
     *               <code>null</code>.
     */
    public void setClinic(final Clinic clinic) {
        assert clinic != null : "The given Clinic was null";
        this.clinic = clinic;
        //take care that the objects know each other
        if (!clinic.getBundleClinics().contains(this)) {
            clinic.addBundleClinic(this);
        }
    }

    /**
     * Returns the bundle of the bundle-clinic association.
     *
     * @return The {@link Bundle Bundle} object of the {@link Bundle Bundle}-{@link Clinic Clinic}
     * association. Might be
     * <code>null</code> (if no {@link Bundle} is currently set; quite unusual).
     */
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Sets a new bundle for the bundle-clinic association.
     *
     * @param bundle The new {@link Bundle Bundle} object of the
     *               {@link Bundle Bundle}-{@link Clinic Clinic} association. Must not be
     *               <code>null</code>.
     */
    public void setBundle(final Bundle bundle) {
        assert bundle != null : "The given bundle was null";
        this.bundle = bundle;
        //take care that the object know each other
        if (!bundle.getBundleClinics().contains(this)) {
            bundle.addBundleClinic(this);
        }
    }

    /**
     * Returns the position of the bundle within the list of bundles associated with the clinic. The
     * numbering starts with 1.
     *
     * @return The position of the {@link Bundle Bundle} within the list of bundles associated with
     * the {@link Clinic Clinic}. Is always
     * <code> &gt;= 1</code>.
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Sets a new position for the bundle within the list of bundles associated with the clinic.
     *
     * @param position The new position of of the {@link Bundle Bundle} within the list of bundles
     *                 associated with the {@link Clinic Clinic}. Must be
     *                 <code>&gt;= 1</code>.
     */
    public void setPosition(final Integer position) {
        assert position >= 1 : "The given position was < 1";
        this.position = position;
    }

    /**
     * Shall never be called by something else but {@link Bundle#removeBundleClinic(BundleClinic)}.
     */
    protected void removeBundle() {
        if (bundle != null) {
            Bundle bundleTemp = bundle;
            bundle = null;
            if (bundleTemp.getBundleClinics().contains(this)) {
                bundleTemp.removeBundleClinic(this);
            }
        }
    }

    /**
     * Shall never be called by something else but {@link Clinic#removeBundleClinic(BundleClinic)}.
     */
    protected void removeClinic() {
        if (clinic != null) {
            Clinic clinicTemp = clinic;
            clinic = null;
            if (clinicTemp.getBundleClinics().contains(this)) {
                clinicTemp.removeBundleClinic(this);
            }
        }
    }

    private String getUUID() {
        return this.uuid;
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
        if (!(obj instanceof BundleClinic)) {
            return false;
        }
        BundleClinic other = (BundleClinic) obj;
        return getUUID().equals(other.getUUID());
    }

    /**
     * Compares another {@link BundleClinic} object to this one based on its
     * {@link BundleClinic#getPosition() }. Note: this class has a natural ordering that is
     * inconsistent with equals.
     */
    @Override
    public int compareTo(final BundleClinic o) {
        return getPosition().compareTo(o.getPosition());
    }

}
