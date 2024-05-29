package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.dto.PointOnImageDTO;
import de.imi.mopat.model.enumeration.MoPatColor;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Realizes a PointOnImage of an {@link ImageAnswer ImageAnswer's} {@link Response response}.
 */
@Entity
@Table(name = "point_on_image")
public class PointOnImage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @JsonIgnore
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    @NotNull
    @Column(name = "position")
    private int position;
    @NotNull
    @Column(name = "x_coordinate")
    private Float xCoordinate;
    @NotNull
    @Column(name = "y_coordinate")
    private Float yCoordinate;
    @NotNull
    @Column(name = "color")
    private MoPatColor color;
    @JoinColumn(name = "response_id", referencedColumnName = "id")
    private Response response;

    public PointOnImage() {
    }

    /**
     * Realizes a PointOnImage of an {@link ImageAnswer ImageAnswer's} {@link Response response}.
     *
     * @param position    The position of the point in the array.
     * @param xCoordinate The x-Coordinate in percent relative to the width of the image.
     * @param yCoordinate The y-Coordinate in percent relative to the height of the image.
     * @param color       The color of the point.
     */
    public PointOnImage(int position, Float xCoordinate, Float yCoordinate, MoPatColor color) {
        setPosition(position);
        setxCoordinate(xCoordinate);
        setyCoordinate(yCoordinate);
        setColor(color);
    }

    /**
     * Returns the Id of this {@link PointOnImage}
     *
     * @return the Id
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the UUID of this {@link PointOnImage}
     *
     * @return the UUID
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * Returns the position in the array of this {@link PointOnImage}
     *
     * @return the position in the array
     */
    public int getPosition() {
        return position;
    }

    /**
     * Returns the x-Coordinate in percent relative to the width of the associated image of this
     * {@link PointOnImage}
     *
     * @return the x-Coordinate in percent relative to the width of the associated image
     */
    public Float getxCoordinate() {
        return xCoordinate;
    }

    /**
     * Returns the y-Coordinate in percent relative to the height of the associated image of this
     * {@link PointOnImage}
     *
     * @return the y-Coordinate in percent relative to the height of the associated image
     */
    public Float getyCoordinate() {
        return yCoordinate;
    }

    /**
     * Returns the color of this {@link PointOnImage}
     *
     * @return the color of the marked X
     */
    public MoPatColor getColor() {
        return color;
    }

    /**
     * Returns the associated {@link Response} of this {@link PointOnImage}
     *
     * @return the associated {@link Response}
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Sets the position in the array of this {@link PointOnImage}
     *
     * @param position Position in the array
     */
    public void setPosition(final int position) {
        assert position > 0 : "The given postion was <= 0";
        this.position = position;
    }

    /**
     * Sets the x-Coordinate in percent depending on width of the associated image of this
     * {@link PointOnImage}
     *
     * @param xCoordinate x-Coordinate in percent depending on the width of the associated image
     */
    public void setxCoordinate(final Float xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    /**
     * Sets the y-Coordinate in percent relative to the height of the associated image of this
     * {@link PointOnImage}
     *
     * @param yCoordinate y-Coordinate in percent relative to the height of the associated image
     */
    public void setyCoordinate(final Float yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    /**
     * Sets the color of this {@link PointOnImage}
     *
     * @param color color of the marked X
     */
    public void setColor(final MoPatColor color) {
        this.color = color;
    }

    /**
     * Sets the associated {@link Response} of this {@link PointOnImage}
     *
     * @param response The associated {@link Response}
     */
    public void setResponse(final Response response) {
        this.response = response;
    }

    /**
     * Returns the {@link PointOnImageDTO} holding the information of this {@link PointOnImage}.
     *
     * @return The {@link PointOnImageDTO} holding the information of this {@link PointOnImage}.
     */
    @JsonIgnore
    public PointOnImageDTO toPointOnImageDTO() {
        return new PointOnImageDTO(position, xCoordinate, yCoordinate, color.getColorCode());
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
        if (!(obj instanceof PointOnImage)) {
            return false;
        }
        PointOnImage other = (PointOnImage) obj;
        return getUUID().equals(other.getUUID());
    }
}
