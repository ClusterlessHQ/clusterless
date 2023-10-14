/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.model;

import clusterless.cls.config.Config;

import java.net.URI;

public class WatchedStore extends Config {
    WatchType watchType = WatchType.s3;
    String region;
    URI path;
    int objectCount = 3;
    int pollingSleepSec = 60;
    int timeoutSec = 3 * 15 * 60;

    public WatchType watchType() {
        return watchType;
    }

    public String region() {
        return region;
    }

    public URI path() {
        return path;
    }

    public int objectCount() {
        return objectCount;
    }

    public int pollingSleepSec() {
        return pollingSleepSec;
    }

    public int timeoutSec() {
        return timeoutSec;
    }
}
