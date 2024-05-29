package de.imi.mopat.model.enumeration;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Definition of colors supported within MoPat [@link ImageAnswer ImageAnswers}.
 */
public enum MoPatColor {

    BLACK("#000000", Color.BLACK), WHITE("#ffffff", Color.WHITE);

    private final String colorCode;
    private final Color colorClass;
    private static final Map<String, MoPatColor> colorCodeToEnum = new HashMap<>();
    private static final Map<Color, MoPatColor> colorClassToEnum = new HashMap<>();

    static // Initialize map from constant color code to enum constant
    {
        for (MoPatColor cValue : values()) {
            colorCodeToEnum.put(cValue.colorCode, cValue);
        }
    }

    static // Initialize map from constant color class to enum constant
    {
        for (MoPatColor cValue : values()) {
            colorClassToEnum.put(cValue.colorClass, cValue);
        }
    }

    MoPatColor(final String colorCode, final Color colorClass) {
        this.colorCode = colorCode;
        this.colorClass = colorClass;
    }

    public String getColorCode() {
        return colorCode;
    }

    public Color getColorClass() {
        return colorClass;
    }

    public static MoPatColor fromColorCode(final String colorCode) {
        return colorCodeToEnum.get(colorCode);
    }

    public static MoPatColor fromColorClass(final Color colorClass) {
        return colorClassToEnum.get(colorClass);
    }
}
