/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform;

import clusterless.model.Struct;
import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.partial.PathFilter;
import clusterless.substrate.uri.ManifestURI;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransformProps implements Struct {
    @JsonProperty(required = true)
    protected String lotUnit;
    @JsonProperty(required = true)
    protected ManifestURI manifestCompletePath;
    @JsonProperty(required = true)
    protected ManifestURI manifestPartialPath;
    @JsonProperty(required = true)
    protected Dataset dataset;
    protected String eventBusName;
    protected PathFilter filter;

    public String lotUnit() {
        return lotUnit;
    }

    public ManifestURI manifestCompletePath() {
        return manifestCompletePath;
    }

    public ManifestURI manifestPartialPath() {
        return manifestPartialPath;
    }

    public Dataset dataset() {
        return dataset;
    }

    public String eventBusName() {
        return eventBusName;
    }

    public PathFilter filter() {
        return filter;
    }
}
