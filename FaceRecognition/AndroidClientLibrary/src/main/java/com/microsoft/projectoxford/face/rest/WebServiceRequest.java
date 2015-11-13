// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.projectoxford.face.rest;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class WebServiceRequest {
    private static final String headerKey = "ocp-apim-subscription-key";
    private DefaultHttpClient client = new DefaultHttpClient();
    private String subscriptionKey;

    public WebServiceRequest(String key) {
        this.subscriptionKey = key;
    }

    public String request(String url, String method, Map<String, Object> data, String contentType) throws RESTException {
        if (method.matches("GET")) {
            return get(url);
        } else if (method.matches("POST")) {
            return post(url, data, contentType);
        } else if (method.matches("PUT")) {
            return put(url, data);
        } else if (method.matches("DELETE")) {
            return delete(url);
        } else if (method.matches("PATCH")) {
            return patch(url, data, contentType);
        }

        throw new RESTException("Error! Incorrect method provided: " + method);
    }

    public String get(String url) throws RESTException {
        HttpGet request = new HttpGet(url);
        request.setHeader(headerKey, this.subscriptionKey);

        try {
            HttpResponse response = this.client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String json = readInput(response.getEntity().getContent());
            if (statusCode / 100 != 2) {
                ServiceCallException serviceCallException
                        = new Gson().fromJson(json, ServiceCallException.class);

                throw new Exception("Error executing GET request! Http Status: " + statusCode
                        + ". Error code: " + serviceCallException.code + ". Message: "
                        + serviceCallException.message);
            }

            return json;
        } catch (Exception e) {
            throw new RESTException(e.getMessage());
        }
    }

    public String post(String url, Map<String, Object> data, String contentType) throws RESTException {
        return webInvoke("POST", url, data, contentType);
    }

    public String patch(String url, Map<String, Object> data, String contentType) throws RESTException {
        return webInvoke("PATCH", url, data, contentType);
    }

    public String webInvoke(String method, String url, Map<String, Object> data, String contentType) throws RESTException {
        HttpPost request;

        if (method.matches("POST")) {
            request = new HttpPost(url);
        } else if (method.matches("PATCH")) {
            request = new HttpPatch(url);
        } else {
            throw new RESTException("Incorrect HTTP method used.");
        }

        boolean isStream = false;

        /*Set header*/
        if (contentType != null && !contentType.isEmpty()) {
            request.setHeader("Content-Type", contentType);
            if (contentType.toLowerCase().contains("octet-stream")) {
                isStream = true;
            }
        } else {
            request.setHeader("Content-Type", "application/json");
        }

        request.setHeader(headerKey, this.subscriptionKey);

        try {
            if (!isStream) {
                JSONObject jsonObj = new JSONObject(data);
                String jsonStr = jsonObj.toString();
                StringEntity entity = new StringEntity(jsonStr);
                request.setEntity(entity);
            } else {
                request.setEntity(new ByteArrayEntity((byte[]) data.get("data")));
            }

            HttpResponse response = this.client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String json = readInput(response.getEntity().getContent());

            if (statusCode / 100 != 2) {
                ServiceCallException serviceCallException
                        = new Gson().fromJson(json, ServiceCallException.class);

                throw new Exception("Error executing POST request! Http Status: " + statusCode
                        + ". Error code: " + serviceCallException.code + ". Message: "
                        + serviceCallException.message);
            }

            return json;
        } catch (Exception e) {
            throw new RESTException(e.getMessage());
        }
    }

    public String put(String url, Map<String, Object> data) throws RESTException {
        HttpPut request = new HttpPut(url);
        request.setHeader(headerKey, this.subscriptionKey);
        request.setHeader("Content-Type", "application/json");

        try {
            //request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            JSONObject jsonObj = new JSONObject(data);
            StringEntity entity = new StringEntity(jsonObj.toString());
            request.setEntity(entity);

            HttpResponse response = this.client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            String json = readInput(response.getEntity().getContent());
            if (statusCode / 100 != 2) {
                ServiceCallException serviceCallException
                        = new Gson().fromJson(json, ServiceCallException.class);

                throw new Exception("Error executing PUT request! Http Status: " + statusCode
                        + ". Error code: " + serviceCallException.code + ". Message: "
                        + serviceCallException.message);
            }
            return json;
        } catch (Exception e) {
            throw new RESTException(e.getMessage());
        }
    }

    public String delete(String url) throws RESTException {
        HttpDelete request = new HttpDelete(url);
        request.setHeader(headerKey, this.subscriptionKey);

        try {
            HttpResponse response = this.client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            String json = readInput(response.getEntity().getContent());
            if (statusCode / 100 != 2) {
                ServiceCallException serviceCallException
                        = new Gson().fromJson(json, ServiceCallException.class);

                throw new Exception("Error executing DELETE request! Http Status: " + statusCode
                        + ". Error code: " + serviceCallException.code + ". Message: "
                        + serviceCallException.message);
            }
            return json;
        } catch (Exception e) {
            throw new RESTException(e.getMessage());
        }
    }

    public static String getUrl(String path, Map<String, Object> params) {
        String url = path;

        boolean start = true;
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (start) {
                url += "?";
                start = false;
            } else {
                url += "&";
            }

            try {
                url += param.getKey() + "=" + URLEncoder.encode(param.getValue().toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return url;
    }

    @SuppressWarnings("unused")
    private JSONObject createErrorObject(String[] keys, Object[] values) {
        JSONObject json = new JSONObject();
        try {
            for (int i = 0; i < keys.length; i++) {
                json.put(keys[i], values[i]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String readInput(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String json = "", line;
        while ((line = br.readLine()) != null) {
            json += line;
        }
        return json;
    }
}
