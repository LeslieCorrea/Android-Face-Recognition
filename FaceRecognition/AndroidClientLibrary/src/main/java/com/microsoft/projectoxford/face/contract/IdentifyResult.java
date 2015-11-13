// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.projectoxford.face.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IdentifyResult {
    public UUID faceId;

    public List<Candidate> candidates = new ArrayList<Candidate>();
}
