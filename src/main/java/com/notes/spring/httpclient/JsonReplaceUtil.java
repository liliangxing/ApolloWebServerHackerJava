package com.notes.spring.httpclient;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: lilx
 * @Date: 2019/12/16 15:03
 * @Description:
 */
public class JsonReplaceUtil {

    public static Object dealWithResponse(String html, String modJson) {
        if(StringUtils.isBlank(modJson)){
            return html;
        }
        if (html.startsWith("[")) {
            JSONArray jsonObject1 = JSONArray.parseArray(html);
            for (int i = 0; i < jsonObject1.size(); i++) {
                JSONObject jsonObj1 = jsonObject1.getJSONObject(i);
                doJsonObj(jsonObj1, modJson);
            }
            return jsonObject1;
        } else if (html.startsWith("{")) {
            JSONObject jsonObj1 = JSONObject.parseObject(html);
            doJsonObj(jsonObj1, modJson);
            return jsonObj1;
        }
        return html;
    }

    private static Object dealWithJSONObject(Object jsonObject, String modJson) {
        if (jsonObject.getClass() == JSONObject.class) {
            JSONObject jsonObj1 = (JSONObject) jsonObject;
            doJsonObj(jsonObj1, modJson);
        } else if (jsonObject.getClass() == JSONArray.class) {
            JSONArray jsonObject1 = (JSONArray) jsonObject;
            for (int i = 0; i < jsonObject1.size(); i++) {
                JSONObject jsonObj1 = jsonObject1.getJSONObject(i);
                doJsonObj(jsonObj1, modJson);
            }
        }
        return jsonObject;
    }

    private static void doJsonObj(JSONObject jsonObj1, String modJson) {
        JSONObject jsonObj2 = JSONObject.parseObject(modJson);
        for (String key : jsonObj1.keySet()) {
            for (String key2 : jsonObj2.keySet()) {
                if (key2.equals(key)) {
                    jsonObj1.put(key, jsonObj2.get(key));
                    break;
                }
            }
            //没有匹配到，对value进行处理
            Object subHtml = dealWithJSONObject(jsonObj1.get(key), modJson);
            jsonObj1.put(key, subHtml);
        }
    }
}
