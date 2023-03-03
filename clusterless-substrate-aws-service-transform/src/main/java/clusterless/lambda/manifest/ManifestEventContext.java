/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.manifest;

import clusterless.lambda.EventContext;

import java.net.URI;

/**
 *
 */
public class ManifestEventContext implements EventContext {
    private String lotId;
    private int datasetItemsSize;
    private URI manifestURI;

    public String lotId() {
        return lotId;
    }

    public int datasetItemsSize() {
        return datasetItemsSize;
    }

    public URI manifestURI() {
        return manifestURI;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    public void setDatasetItemsSize(int datasetItemsSize) {
        this.datasetItemsSize = datasetItemsSize;
    }

    public void setManifestURI(URI manifestURI) {
        this.manifestURI = manifestURI;
    }
}
