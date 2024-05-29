package de.imi.mopat.model.enumeration;

import java.math.RoundingMode;

/**
 * Definition of export rounding strategy types. Determines how a number should be rounded. Used in
 * {@link de.imi.mopat.model.ExportRuleFormat} objects.
 */
public enum ExportRoundingStrategyType {

    STANDARD(RoundingMode.HALF_UP), CEIL(RoundingMode.CEILING), FLOOR(RoundingMode.FLOOR);

    private final RoundingMode roundingMode;

    ExportRoundingStrategyType(final RoundingMode roundingMode) {
        this.roundingMode = roundingMode;
    }

    /**
     * Returns the java specific rounding mode used for the defined
     * {@link ExportRoundingStrategyType}.
     *
     * @return The java specific rounding mode used for the defined
     * {@link ExportRoundingStrategyType}.
     */
    public RoundingMode getRoundingMode() {
        return roundingMode;
    }
}
