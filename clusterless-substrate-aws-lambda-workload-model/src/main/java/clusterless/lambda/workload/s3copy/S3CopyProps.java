/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.workload.s3copy;

import clusterless.model.Struct;
import clusterless.model.deploy.WorkloadProps;

import java.util.ArrayList;
import java.util.List;

public class S3CopyProps extends WorkloadProps {
    public static class Filter implements Struct {
        List<String> includes = new ArrayList<>();
        List<String> excludes = new ArrayList<>();
        char pathSeparator = '/';
        boolean ignoreCase = false;

        public List<String> includes() {
            return includes;
        }

        public List<String> excludes() {
            return excludes;
        }

        public char pathSeparator() {
            return pathSeparator;
        }

        public boolean ignoreCase() {
            return ignoreCase;
        }
    }

    Filter filter = new Filter();

    float failArcOnPartialPercent = 0f;

    public Filter filter() {
        return filter;
    }

    public float failArcOnPartialPercent() {
        return failArcOnPartialPercent;
    }
}
