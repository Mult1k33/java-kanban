package service.typeadapters;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeParseException;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        if (duration == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value(duration.toString());
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        try {
            if (jsonReader.peek() == null) {
                jsonReader.nextNull();
                return null;
            }
            String durationString = jsonReader.nextString();
            return Duration.parse(durationString);
        } catch (DateTimeParseException e) {
            throw new JsonParseException("Неверный формат продолжительности. Ожидается PTnHnMnS", e);
        }
    }
}