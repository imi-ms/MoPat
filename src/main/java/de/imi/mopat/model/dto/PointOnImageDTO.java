package de.imi.mopat.model.dto;

import de.imi.mopat.model.PointOnImage;
import de.imi.mopat.model.enumeration.MoPatColor;

/**
 *
 */
public class PointOnImageDTO {

    private int position;
    private Float xCoordinate;
    private Float yCoordinate;
    private String color;

    public PointOnImageDTO() {
    }

    public PointOnImageDTO(final int position, final Float xCoordinate, final Float yCoordinate,
        final String color) {
        setPosition(position);
        setxCoordinate(xCoordinate);
        setyCoordinate(yCoordinate);
        setColor(color);
    }

    public int getPosition() {
        return position;
    }

    public Float getxCoordinate() {
        return xCoordinate;
    }

    public Float getyCoordinate() {
        return yCoordinate;
    }

    public String getColor() {
        return color;
    }

    public void setPosition(final int position) {
        this.position = position;
    }

    public void setxCoordinate(Float xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public void setyCoordinate(final Float yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    public PointOnImage toPointOnImage() {
        return new PointOnImage(this.position, this.xCoordinate, this.yCoordinate,
            MoPatColor.fromColorCode(color));
    }
}
