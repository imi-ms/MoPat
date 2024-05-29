package de.imi.mopat.model.score;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.Encounter;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * The database table model for table <i>operator</i>. Every operator must support the evaluate and
 * the getFormula method.
 */
@Entity
@Table(name = "operator")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "operator_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Operator implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @JsonIgnore
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    @Column(name = "display_sign")
    private String displaySign;

    /**
     * Evaluate the given {@link Expression} for the given {@link Encounter}.
     *
     * @param expression The {@link Expression} which should be evaluated.
     * @param encounter  The {@link Encounter} for which this {@link Operator} with the given
     *                   {@link Expression} should be evaluated.
     * @return The value of the calculation. Null if the calculation fails.
     */
    public abstract Object evaluate(Expression expression, Encounter encounter);

    /**
     * Get the formula of the given {@link Expression} for the given {@link Encounter}.
     *
     * @param expression      The {@link Expression} from which the formula should be created.
     * @param encounter       The {@link Encounter} for which a formula with this {@link Operator}
     *                        and the given {@link Expression} should be created.
     * @param defaultLanguage The default language for this export.
     * @return The formula of this {@link Operator} with the {@link Expression}.
     */
    public abstract String getFormula(Expression expression, Encounter encounter,
        String defaultLanguage);

    /**
     * Returns the id of the current operator object.
     *
     * @return id The current id of this operator object. Might be
     * <code>null</code> for newly created objects. Is never
     * <code> &lt;= 0</code>.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the uuid of the current operator object.
     *
     * @return uuid The uuid of this operator object. Can not be
     * <code>null</code>.
     */
    public String getUUID() {
        return this.uuid;
    }

    /**
     * Returns the display sign of the current operator object.
     *
     * @return displaySign The display sign of this operator object. Can not be
     * <code>null</code>.
     */
    public String getDisplaySign() {
        return displaySign;
    }
}
