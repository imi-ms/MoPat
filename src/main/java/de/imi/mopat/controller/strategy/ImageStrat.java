package de.imi.mopat.controller.strategy;

import de.imi.mopat.controller.QuestionController;
import de.imi.mopat.model.ImageAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import de.imi.mopat.validator.MoPatValidator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BindingResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageStrat implements CreateOrUpdateAnswerStrategy {
    @Override
    public void createOrUpdateAnswer(QuestionDTO questionDTO, Question question, QuestionController controller, BindingResult result, Questionnaire questionnaire) {
        // Merge the question if it's new to get its ID to save the
        // image with this specific ID
        if (questionDTO.getId() == null) {
            controller.getQuestionDao().merge(question);
        }
        AnswerDTO answerDTO = questionDTO.getAnswers().get(0L);
        Boolean isEnabled = answerDTO.getIsEnabled();

        String storagePath;

        // Upload the new Image if the question is new or the image
        // has changed
        if (!answerDTO.getImageFile().isEmpty()) {
            // Store the extension of the image and the path with the
            // questionnaire ID
            String imageExtension =
                    FilenameUtils.getExtension(answerDTO.getImageFile()
                            .getOriginalFilename());
            String imagePath =
                    (controller.getConfigurationDao().getImageUploadPath()
                            + "/question/" + questionnaire.getId());

            // Check if the upload dir exists. If not, create it
            File uploadDir = new File(imagePath);
            if (!uploadDir.isDirectory()) {
                uploadDir.mkdirs();
            }
            // Set the upload filename to question and its ID
            File uploadFile = new File(imagePath,
                    "question" + question.getId() + "." + imageExtension);
            try {
                // Write the image to disk
                BufferedImage uploadImage = ImageIO.read(
                        answerDTO.getImageFile().getInputStream());
                ImageIO.write(uploadImage, imageExtension, uploadFile);
            } catch (IOException ex) {
                // If an error occures while uploading, write it down
                // and delete the question if it was new
                result.pushNestedPath("answers[0]");
                result.rejectValue("imageFile", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        controller.getMessageSource().getMessage("imageAnswer.error.upload", new Object[]{},
                                LocaleContextHolder.getLocale()));
                result.popNestedPath();
                if (questionDTO.getId() == null) {
                    controller.getQuestionDao().remove(question);
                }
            }
            // Store the full storage path with name and extension
            storagePath =
                    questionnaire.getId()
                            + "/question" + question.getId()
                            + "." + imageExtension;
        } else {
            // If the image has not changed use the old image path
            storagePath = answerDTO.getImagePath();
        }

        if (!question.getAnswers().isEmpty()) {
            // Update answer
            ImageAnswer imageAnswer = (ImageAnswer) question.getAnswers().get(0);
            imageAnswer.setIsEnabled(isEnabled);
            imageAnswer.setImagePath(storagePath);
        } else {
            // Create new answer
            ImageAnswer imageAnswer = new ImageAnswer(question, isEnabled, storagePath);
        }
    }
}
