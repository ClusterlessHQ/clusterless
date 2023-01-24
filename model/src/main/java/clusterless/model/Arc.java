/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Arc extends Model {

    List<Dataset> sources = new ArrayList<>();
    Process process;
    List<Dataset> sinks = new ArrayList<>();

    public List<Dataset> sources() {
        return sources;
    }

    public Process process() {
        return process;
    }

    public List<Dataset> sinks() {
        return sinks;
    }
}
