package de.imi.mopat.helper.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Manages the upload of multiple multipart files.
 */
@Service
public class MultiPartFileUploadBean {

    private List<MultipartFile> files;

    /**
     * Add all files from a request to a list of multipart file objects.
     *
     * @param files The list of multi part file objects from the request.
     */
    public void setFiles(final List<MultipartFile> files) {
        this.files = files;
    }

    /**
     * Returns the current list of multipart file objects from the request.
     *
     * @return The current list of multipart file objects from the request.
     */
    public List<MultipartFile> getFiles() {
        return files;
    }

    /**
     * Save a file with given file name and context path.
     *
     * @param fileName    The name of the file.
     * @param file        The file that should be saved.
     * @param contextPath The context path to save the image to the filesystem.
     * @return Return the name of the saved file.
     */
    public static String saveFile(final String fileName, final MultipartFile file,
        final String contextPath) {
        if (file != null && !file.isEmpty()) {
            // Generate new file object
            File uploadFile = new File(contextPath + "/files/", fileName);
            try {
                // Create new file at given path
                uploadFile.createNewFile();
                // Save the uploaded file to the new created file
                file.transferTo(uploadFile);
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }
}
