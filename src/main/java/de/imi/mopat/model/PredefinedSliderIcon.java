package de.imi.mopat.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "predefined_slider_icon")
public class PredefinedSliderIcon {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "icon_name")
    private String iconName;

    protected PredefinedSliderIcon() {
    }

    public PredefinedSliderIcon(String iconName) {
        this.iconName = iconName;
    }

    public String getIconName() {
        return iconName;
    }

    public Long getId() {
        return id;
    }

    public PredefinedSliderIcon setId(Long id) {
        this.id = id;
        return this;
    }
}
