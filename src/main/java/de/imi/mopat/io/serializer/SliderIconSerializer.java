package de.imi.mopat.io.serializer;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.imi.mopat.model.SliderIcon;
import java.io.IOException;

public class SliderIconSerializer extends JsonSerializer<SliderIcon> {

    @Override
    public void serialize(SliderIcon sliderIcon,
        com.fasterxml.jackson.core.JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", String.valueOf(sliderIcon.getId()));
        jsonGenerator.writeStringField("iconPosition", String.valueOf(sliderIcon.getIconPosition()));
        jsonGenerator.writeStringField("predefinedSliderIcon", sliderIcon.getPredefinedSliderIcon().getIconName());
        jsonGenerator.writeEndObject();
    }
}
