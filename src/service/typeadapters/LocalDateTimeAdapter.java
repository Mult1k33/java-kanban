package service.typeadapters;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {

        if (localDateTime == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value(localDateTime.format(dtf));
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        try {
            if (jsonReader.peek() == null) {
                jsonReader.nextNull();
                return null;
            }

            String dateString = jsonReader.nextString();
            if (dateString == null || dateString.equalsIgnoreCase("null")) {
                return null;
            }

            try {
                return LocalDateTime.parse(dateString, dtf);
            } catch (DateTimeParseException e) {
                throw new JsonParseException(
                        "Неверный формат даты. Ожидается yyyy-MM-ddTHH:mm: " + dateString, e);
            }
        } catch (IllegalStateException e) {
            throw new JsonParseException("Ошибка чтения даты", e);
        }
    }
}