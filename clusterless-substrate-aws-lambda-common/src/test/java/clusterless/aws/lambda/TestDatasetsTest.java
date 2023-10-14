/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda;

import clusterless.cls.model.manifest.ManifestState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestDatasetsTest {

    @Test
    void datasets() {
        assertEquals(1, new TestDatasets().datasetMap().size());
        assertEquals(2, new TestDatasets("main", "index").datasetMap().size());
        assertEquals(2, new TestDatasets("main", "index").datasetMap().values().stream().map(Object::toString).distinct().count());
        assertEquals(10, new TestDatasets(10).datasetMap().values().stream().map(Object::toString).distinct().count());
    }

    @Test
    void manifestsPaths() {
        assertEquals(1, new TestDatasets().manifestPathMap().size());
        assertEquals(1, new TestDatasets("main").manifestPathMap().size());
        Assertions.assertTrue(new TestDatasets("main").manifestPathMap().get("main").uri().toString().endsWith("/"));
        assertEquals(2, new TestDatasets("main", "index").manifestPathMap().size());
        assertEquals(2, new TestDatasets("main", "index").manifestPathMap().values().stream().map(Object::toString).distinct().count());
        assertEquals(10, new TestDatasets(10).manifestPathMap().values().stream().map(Object::toString).distinct().count());
    }

    @Test
    void manifests() {
        assertEquals(1, new TestDatasets().manifestIdentifierMap("20230227PT5M287", ManifestState.complete).size());
        assertEquals(1, new TestDatasets("main").manifestIdentifierMap("20230227PT5M287", ManifestState.complete).size());
        Assertions.assertTrue(new TestDatasets("main").manifestIdentifierMap("20230227PT5M287", ManifestState.complete).get("main").uri().toString().endsWith("manifest.json"));
        assertEquals(2, new TestDatasets("main", "index").manifestIdentifierMap("20230227PT5M287", ManifestState.complete).size());
        assertEquals(2, new TestDatasets("main", "index").manifestIdentifierMap("20230227PT5M287", ManifestState.complete).values().stream().map(Object::toString).distinct().count());
        assertEquals(10, new TestDatasets(10).manifestIdentifierMap("20230227PT5M287", ManifestState.complete).values().stream().map(Object::toString).distinct().count());
    }

    @Test
    void sourceSinkManifests() {
        TestDatasets testDatasets = new TestDatasets("main", "index");
        assertEquals(2, testDatasets.manifestIdentifierMap("20230227PT5M287", testDatasets.sinkDatasetMap(), ManifestState.complete).size());
        assertEquals(2, testDatasets.manifestIdentifierMap("20230227PT5M287", testDatasets.sourceDatasetMap(), ManifestState.complete).size());
        assertNotEquals(testDatasets.manifestIdentifierMap("20230227PT5M287", testDatasets.sourceDatasetMap(), ManifestState.complete), testDatasets.manifestIdentifierMap("20230227PT5M287", testDatasets.sinkDatasetMap(), ManifestState.complete));
    }
}
