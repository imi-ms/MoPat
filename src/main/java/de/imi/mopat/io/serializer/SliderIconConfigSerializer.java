package de.imi.mopat.io.serializer;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.SliderIconConfig;
import de.imi.mopat.model.SliderIconDetail;

import java.io.IOException;

public class SliderIconConfigSerializer extends JsonSerializer<SliderIconConfig> {

    private final ConfigurationDao configurationDao;

    public SliderIconConfigSerializer(ConfigurationDao configurationDao){
        this.configurationDao=configurationDao;
    }

    @Override
    public void serialize(SliderIconConfig sliderIconConfig,
        com.fasterxml.jackson.core.JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", String.valueOf(sliderIconConfig.getId()));
        jsonGenerator.writeStringField("numberOfIcons", String.valueOf(sliderIconConfig.getNumberOfIcons()));
        jsonGenerator.writeFieldName("sliderIconDetailDTOS");
        jsonGenerator.writeStartArray("sliderIconDetailDTOS");
        for(SliderIconDetail sliderIconDetail: sliderIconConfig.getIcons()){
            jsonGenerator.writeStartObject();
            if(sliderIconDetail.getUserSliderIcon()!=null){
                jsonGenerator.writeStringField("id", String.valueOf(sliderIconDetail.getId()));
                jsonGenerator.writeStringField("iconPosition", sliderIconDetail.getIconPosition().toString());
                jsonGenerator.writeStringField("userIconBase64", StringUtilities.convertImageToBase64String(
                        (configurationDao.getImageUploadPath()
                                + "/sliderIconConfig/"
                                + sliderIconDetail.getUserSliderIcon().getIconPath()
                        ),
                        sliderIconDetail.getUserSliderIcon().getIconPath().split("/")[1]));

            } else {
                jsonGenerator.writeStringField("id", String.valueOf(sliderIconDetail.getId()));
                jsonGenerator.writeStringField("iconPosition", String.valueOf(sliderIconDetail.getIconPosition()));
                jsonGenerator.writeStringField("predefinedSliderIcon", String.valueOf(sliderIconDetail.getPredefinedSliderIcon().getIconName()));
            }
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
