package de.imi.mopat.helper.controller;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Helper class for manipulating images.
 */
public class GraphicsUtilities {

    /**
     * Resizes a given image using the given width.
     *
     * @param originalImage The original image which will be resized.
     * @param width         The width of the returned image.
     * @return Returns the resized image with given width and corresponding height.
     */
    public static BufferedImage resizeImage(final BufferedImage originalImage, final int width) {
        if (width <= originalImage.getWidth()) {
            Double height = ((double) width / originalImage.getWidth()) * originalImage.getHeight();
            BufferedImage resizedImage = new BufferedImage(width, height.intValue(),
                originalImage.getType());
            Graphics2D resizedGraphics = resizedImage.createGraphics();
            resizedGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            resizedGraphics.drawImage(originalImage, 0, 0, width, height.intValue(), null);
            resizedGraphics.dispose();
            return resizedImage;
        } else {
            return originalImage;
        }
    }
}
