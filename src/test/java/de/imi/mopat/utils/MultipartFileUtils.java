package de.imi.mopat.utils;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class MultipartFileUtils {

    public static final String VALID_LOGO_FILENAME = "test.png";
    public static final String VALID_LOGO_CONTENT_TYPE = "image/png";
    public static final String INVALID_LOGO_FILENAME = "test.fail";
    public static final String INVALID_LOGO_CONTENT_TYPE = "image/fail";
    public static final String LOGO_FIELD_NAME = "logo";

    public static final byte[] VALID_LOGO_CONTENT = new byte[] {(byte)137, (byte)80, (byte)78, (byte)71, (byte)13, (byte)10, (byte)26, (byte)10};
    public static final byte[] INVALID_LOGO_CONTENT = "Invalid content".getBytes();
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static MultipartFile getEmptyLogo(){
        return new MockMultipartFile(LOGO_FIELD_NAME, EMPTY_BYTE_ARRAY);
    }

    public static MultipartFile getValidLogoFile() {
        return new MockMultipartFile(LOGO_FIELD_NAME, VALID_LOGO_FILENAME, VALID_LOGO_CONTENT_TYPE, VALID_LOGO_CONTENT);
    }

    public static MultipartFile getInvalidLogoFile() {
        return new MockMultipartFile(LOGO_FIELD_NAME, INVALID_LOGO_FILENAME, INVALID_LOGO_CONTENT_TYPE, INVALID_LOGO_CONTENT);
    }
}
