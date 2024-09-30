package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "slider_icon_detail")
public class SliderIconDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "icon_position")
    private Integer iconPosition;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "slider_icon_config_id", referencedColumnName = "id")
    private SliderIconConfig sliderIconConfig;

    @ManyToOne
    @JoinColumn(name = "predefined_slider_icon_id", referencedColumnName = "id")
    private PredefinedSliderIcon predefinedSliderIcon;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "user_slider_icon_id", referencedColumnName = "id")
    private UserSliderIcon userSliderIcon;

    protected SliderIconDetail() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SliderIconDetail(Integer iconPosition) {
        this.iconPosition = iconPosition;
    }

    public Integer getIconPosition() {
        return iconPosition;
    }

    public void setIconPosition(Integer iconPosition) {
        this.iconPosition = iconPosition;
    }

    public SliderIconConfig getSliderIconConfig() {
        return sliderIconConfig;
    }

    public void setSliderIconConfig(SliderIconConfig sliderIconConfig) {
        this.sliderIconConfig = sliderIconConfig;
    }

    public PredefinedSliderIcon getPredefinedSliderIcon() {
        return predefinedSliderIcon;
    }

    public void setPredefinedSliderIcon(PredefinedSliderIcon predefinedSliderIcon) {
        this.predefinedSliderIcon = predefinedSliderIcon;
    }

    public UserSliderIcon getUserSliderIcon() {
        return userSliderIcon;
    }

    public void setUserSliderIcon(UserSliderIcon userSliderIcon) {
        this.userSliderIcon = userSliderIcon;
    }

}
