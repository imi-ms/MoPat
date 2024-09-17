package de.imi.mopat.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * The database table model for slider icons
 */
@Entity
@Table(name = "slider_icons")
public class SliderIcon {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "position")
    private Integer iconPosition = null;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "icon_id", referencedColumnName = "id")
    private PredefinedSliderIcon predefinedSliderIcon;

    @NotNull(message = "{response.answer.notNull}")
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "answer_id", referencedColumnName = "id")
    private SliderAnswer answer;

    protected SliderIcon() {
        // default constructor (in protected state), should not be accessible to
        // anything else but the JPA implementation (here: Hibernate) and the
        // JUnit tests
    }

    public SliderIcon(final Integer iconPosition, final PredefinedSliderIcon predefinedSliderIcon, final SliderAnswer answer) {
        setIconPosition(iconPosition);
        setPredefinedSliderIcon(predefinedSliderIcon);
        setAnswer(answer);
    }

    /**
     * @return Might be <code>null</code> for newly created objects. Is never
     * <code>&lt;= 0</code>.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @return iconPosition of slider icon
     */
    public Integer getIconPosition() {
        return iconPosition;
    }

    /**
     * @param iconPosition of slider icon
     */
    public void setIconPosition(final Integer iconPosition) {
        this.iconPosition = iconPosition;
    }

    /**
     * @return string for icon
     */
    public PredefinedSliderIcon getPredefinedSliderIcon() {
        return predefinedSliderIcon;
    }

    /**
     * @param icon string for icon
     */
    public void setPredefinedSliderIcon(final PredefinedSliderIcon icon) {
        this.predefinedSliderIcon = icon;
    }

    /**
     * @return answer entity
     */
    public SliderAnswer getAnswer() {
        return answer;
    }

    /**
     * Sets answer reference and adds icon object to answer object
     *
     * @param answer entity
     */
    public void setAnswer(final SliderAnswer answer) {
        assert answer != null : "The given Answer was null";
        this.answer = answer;
        // Check if answer already has icons
        if (answer.getIcons() != null) {
            // Then add the current one, if it is not contained
            if (!answer.getIcons().contains(this)) {
                answer.addIcon(this);
            }
        } else { //Otherwise create a new set and add it
            Set<SliderIcon> sliderIcons = new HashSet<>();
            sliderIcons.add(this);
            answer.setIcons(sliderIcons);
        }
    }
}
