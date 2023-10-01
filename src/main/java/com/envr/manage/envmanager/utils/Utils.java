package com.envr.manage.envmanager.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

import static com.envr.manage.envmanager.utils.AppConstants.NEW_LINE;

public class Utils {
    private Utils(){}
    public static String toSingleLine(String multiLineStr) {
        return multiLineStr.replace("\n", "");
    }

    public static String jsonStrToEnvStr(String jsonStr) {
        try {
            Gson gson = new Gson();
            Map<String, Object> result = gson.fromJson(jsonStr, Map.class);
            return mapToEnvString(result);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String mapToEnvString(Map<String, Object> mapData) {
        final String[] stringData = {""};
        mapData.forEach((k, v) -> {
            if (stringData[0].isEmpty()) {
                stringData[0] = k + "=" + v + ";" + NEW_LINE;
            } else {
                stringData[0] = stringData[0] + k + "=" + v + ";" + NEW_LINE;
            }
        });
        return stringData[0];
    }

    public static String toJson(Object obj) {
        String jsonInString;
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting()
                    .create();
            jsonInString = gson.toJson(obj);

        } catch (Exception e) {
            jsonInString = e.getMessage();
        }
        return jsonInString;
    }

}
