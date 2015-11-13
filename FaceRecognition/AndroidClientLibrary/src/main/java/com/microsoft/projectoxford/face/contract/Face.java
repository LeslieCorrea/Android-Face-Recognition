// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.projectoxford.face.contract;

import java.util.UUID;

public class Face {
    public UUID faceId;

    public FaceRectangle faceRectangle;

    public FaceLandmarks faceLandmarks;

    public FaceAttribute attributes;
}
