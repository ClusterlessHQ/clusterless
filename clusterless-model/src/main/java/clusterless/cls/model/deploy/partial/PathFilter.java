/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.model.deploy.partial;

import clusterless.cls.model.Struct;

import java.util.ArrayList;
import java.util.List;

public class PathFilter implements Struct {
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Filter{");
        sb.append("includes=").append(includes);
        sb.append(", excludes=").append(excludes);
        sb.append(", pathSeparator=").append(pathSeparator);
        sb.append(", ignoreCase=").append(ignoreCase);
        sb.append('}');
        return sb.toString();
    }
}
