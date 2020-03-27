package com.dd.rsvp.processor.job.utility;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;

public class JsonUtility implements Serializable {

    private static Logger LOGGER = Logger.getLogger(JsonUtility.class);
    private static Gson gson = new Gson();

    private JsonUtility() {

    }

    public static JsonObject readAsJsonObject(String filePath) {
        InputStream jsonInputStream = JsonUtility.class.getClassLoader().getResourceAsStream(filePath);
        Reader reader = new InputStreamReader(jsonInputStream);
        return gson.fromJson(reader, JsonObject.class);
    }

    public static String writeObjectAsString(Object object) {
        return gson.toJson(object);
    }

    public static Gson getGson(){
        return gson;
    }
}
