/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.manifest;

import java.util.LinkedHashMap;

public class AttemptCounter {
    LinkedHashMap<String, Integer> attempts = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<String, Integer> eldest) {
            return size() > 100;
        }
    };

    public String attemptId(String id) {
        int count = attempts.compute(id, (k, v) -> v == null ? 1 : v + 1);
        return id + "-" + count;
    }
}
