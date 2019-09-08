package io.cryptolens.internal;

import com.google.gson.*;
import io.cryptolens.legacy.HttpsURLConnectionRequestHandler;
import io.cryptolens.legacy.RequestHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HelperMethods {

    public static <T extends BasicResult> T SendRequestToWebAPI(String method, Object model, Map<String,String> extraParams, Class<T> clazz) {

        Map<String,String> params = new HashMap<>();
        List<Field> allFields = new ArrayList<>();
        getAllFields(allFields, model.getClass());

        for(Field field : allFields) {
            field.setAccessible(true);
            try {
                Object value = field.get(model);
                if(value != null) {
                    params.put(field.getName(), value.toString());
                }
            } catch (Exception ex) {
                System.err.println(ex.getStackTrace());
            }
        }

        if(extraParams != null)
            params.putAll(extraParams);

        RequestHandler requestHandler = new HttpsURLConnectionRequestHandler();

        try {

            String response = requestHandler.makePostRequest("https://app.cryptolens.io/api/" + method, params);

            Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                    /*Instant instant = Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
                    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()*/
                    return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }
            }).create();

            T res = gson.fromJson(response, clazz);
            res.RawResponse = response;
            return res;

        } catch (Exception ex) {
            System.err.println(ex.getStackTrace());
        }

        return  null;
    }

    // from: https://stackoverflow.com/a/1042827/1275924
    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }
}
