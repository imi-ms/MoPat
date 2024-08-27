package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.model.conditions.Condition;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "slider_icon_config")
public class SliderIconConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "{sliderIconConfig.numberOfIcons.notNull}")
    @Column(name = "number_of_icons")
    private Integer numberOfIcons;

    @NotNull(message = "{sliderIconConfig.configName.notNull}")
    @Column(name = "config_name", unique = true)
    private String configName;

    @OneToMany(mappedBy = "sliderIconConfig", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<SliderIconDetail> icons = new ArrayList<>();

    public SliderIconConfig() {
    }

    public SliderIconConfig(Integer numberOfIcons, String configName) {
        this.numberOfIcons = numberOfIcons;
        this.configName = configName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumberOfIcons() {
        return numberOfIcons;
    }

    public void setNumberOfIcons(Integer numberOfIcons) {
        this.numberOfIcons = numberOfIcons;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public List<SliderIconDetail> getIcons() {
        return icons;
    }

    public void setIcons(List<SliderIconDetail> icons) {
        this.icons = icons;
    }
}
