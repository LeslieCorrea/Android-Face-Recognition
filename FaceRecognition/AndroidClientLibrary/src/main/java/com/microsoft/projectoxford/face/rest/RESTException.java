// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.projectoxford.face.rest;

import org.json.JSONObject;

public class RESTException extends Exception {

    public RESTException(String message) {
        super(message);
    }

    public RESTException(JSONObject errorObject) {
        super(errorObject.toString());
    }
}
