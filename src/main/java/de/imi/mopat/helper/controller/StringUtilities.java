package de.imi.mopat.helper.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

/**
 * This class implements {@link String} utilities, which are are not part of the {@link String}
 * implementataion in Java, but useful to simplify the source code
 */
@Service
public class StringUtilities {

    Map<String, String> germanUmlautsReplacement = new HashMap<>();

    @PostConstruct
    public void init() {
        // Fill the map of characters to exchange for replaceGermanUmlauts
        // (String germanText)
        germanUmlautsReplacement.put("Ä", "Ae");
        germanUmlautsReplacement.put("ä", "ae");
        germanUmlautsReplacement.put("Ö", "Oe");
        germanUmlautsReplacement.put("ö", "oe");
        germanUmlautsReplacement.put("Ü", "Ue");
        germanUmlautsReplacement.put("ü", "ue");
        germanUmlautsReplacement.put("ß", "ss");
    }

    /**
     * This method exchanges all german umlauts in a given german text to an appropriate english
     * spelling.
     *
     * @param germanText The german text, where the umlauts are changed to the english spelling.
     * @return The german text without any german umlauts
     */
    public String replaceGermanUmlauts(String germanText) {
        for (String character : germanUmlautsReplacement.keySet()) {
            germanText = germanText.replaceAll(character, germanUmlautsReplacement.get(character));
        }
        return germanText;
    }

    /*
     * Converts an image file given by the filePath and the fileName to base64
     * encoded String.
     *
     * @param filePath Path to the target file.
     * @param fileName Name of the target file.
     * @return The image encoded as base64 String.
     * @throws java.io.IOException
     */
    public static String convertImageToBase64String(String filePath, String fileName)
        throws IOException {
        BufferedImage image;
        String base64String = null;
        image = ImageIO.read(new File(filePath));
        String imageMimeType = fileName.substring(fileName.lastIndexOf(".") + 1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, imageMimeType, out);
        byte[] imageByteArray = out.toByteArray();
        base64String = "data:image/" + imageMimeType + ";base64," + StringUtils.newStringUtf8(
            Base64.encodeBase64(imageByteArray, false));

        return base64String;
    }

    /**
     * Decodes a base64 String and writes the resulting byte array to an image file.
     *
     * @param base64String String that has to be encoded.
     * @param imagePath    Path to the resultung image file.
     * @param fileName     Name of the resulting image file.
     * @throws java.io.IOException If an I/O error occurs
     */
    public static void convertAndWriteBase64StringToImage(final String base64String,
        final String imagePath, final String fileName) throws IOException {
        byte[] imageByteArray = Base64.decodeBase64(
            base64String.substring(base64String.lastIndexOf(";base64,") + ";base64,".length()));

        File uploadDir = new File(imagePath);
        if (!uploadDir.isDirectory()) {
            uploadDir.mkdirs();
        }

        File imageFile = new File(imagePath, fileName);
        FileUtils.writeByteArrayToFile(imageFile, imageByteArray);
    }

    /**
     * Extracts the mime type from a base64 String and returns it as String.
     *
     * @param base64 The base64 encoded String containing the mime type.
     * @return The mime type as String.
     */
    public static String getMimeTypeFromBase64String(final String base64) {
        String mimeType = base64.substring("data:image/".length(), base64.lastIndexOf(";base64,"));

        if (mimeType.equals("svg+xml")) {
            return "svg";
        }

        return mimeType;
    }

    /**
     * Strips off the HTML code of a given string.
     *
     * @param htmlString String with HTML code
     * @return String without HTML code
     */
    public static String stripHTML(final String htmlString) {
        final Pattern REMOVE_TAGS = Pattern.compile("(<.+?>|&nbsp;)");
        if (htmlString == null || htmlString.isEmpty()) {
            return htmlString;
        }
        Matcher matcher = REMOVE_TAGS.matcher(htmlString);
        return matcher.replaceAll("");
    }
}
