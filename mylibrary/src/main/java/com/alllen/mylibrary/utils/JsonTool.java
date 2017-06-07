package com.alllen.mylibrary.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by allen.z on 2017-05-09.
 */
public class JsonTool {
    /**
     * 得到一个json类型的字符串对象
     * @param key
     * @param value
     * @return
     */
    public static String getJsonString(String key, Object value)
    {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * 得到一个json对象
     * @param key
     * @param value
     * @return
     */
    public static JSONObject getJsonObject(String key, Object value)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
