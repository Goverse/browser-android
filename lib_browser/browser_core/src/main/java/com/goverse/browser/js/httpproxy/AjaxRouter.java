package com.goverse.browser.js.httpproxy;

import android.util.Log;

import com.goverse.browser.js.JsNameSpace;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

@JsNameSpace(namespace = "")
public class AjaxRouter {
    private final String TAG = AjaxRouter.class.getSimpleName();

    public void onAjaxRequest(JSONObject requestData, CompletionHandler handler) {

        Log.d(TAG, "onAjaxRequest");
        Map<String, Object> responseData = new HashMap<>();
        try {
            responseData.put("statusCode", 0);
            String contentType = "";
            boolean encode = false;
            String responseType = requestData.optString("responseType");
            if (responseType != null && responseType.equals("stream")) encode = true;

            Request.Builder rb = new Request.Builder();
            rb.url(requestData.optString("url"));
            JSONObject headers = requestData.getJSONObject("headers");

            Iterator iterator = headers.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                String value = headers.getString(key);
                if (key.equalsIgnoreCase("cookie")) {
                    continue;
                }
                if (key.equalsIgnoreCase("content-type")) {
                    contentType = value;
                }
                rb.header(key, value);
            }

            if (requestData.getString("method").equals("POST")) {
                RequestBody requestBody = RequestBody
                        .create(MediaType.parse(contentType), requestData.optString("body"));
                rb.post(requestBody);
            }
            final boolean finalEncode = encode;
//            RetrofitHelper.getOkHttpClient().newCall(rb.build()).enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    Log.d(TAG, "onAjaxRequest---onFailure: " + e.getMessage());
//                    responseData.put("responseText", e.getMessage());
//                    handler.onFailed(new JSONObject(responseData));
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//
//                    Log.d(TAG, "onAjaxRequest---onResponse");
//                    if (response == null || response.body() == null) {
//                        handler.onFailed(new JSONObject(responseData));
//                        return;
//                    }
//
//                    String data = (finalEncode) ? Base64.encodeToString(response.body().bytes(), Base64.DEFAULT) : response.body().string();
//
//                    try {
//                        responseData.put("statusCode", response.code());
//                        responseData.put("statusMessage", response.message());
//                        if (!TextUtils.isEmpty(data)) responseData.put("responseText", new JSONObject(data));
//                        if (response.headers() != null) {
//                            Map<String, List<String>> responseHeaders = response.headers().toMultimap();
//                            responseData.put("headers", responseHeaders);
//                        }
//                        handler.onCompleted(new JSONObject(responseData));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        handler.onFailed(new JSONObject(responseData));
//                    }
//
//                }
//            });
        } catch (Exception e) {
            Log.d(TAG, "onAjaxRequest---Exception: " + e.getMessage());
        }
    }
}