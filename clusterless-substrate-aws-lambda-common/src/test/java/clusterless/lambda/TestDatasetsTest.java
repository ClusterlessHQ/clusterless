package clusterless.lambda;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestDatasetsTest {

    @Test
    void datasets() {
        Assertions.assertEquals(1, new TestDatasets().datasetMap().size());
        Assertions.assertEquals(2, new TestDatasets("main", "index").datasetMap().size());
        Assertions.assertEquals(2, new TestDatasets("main", "index").datasetMap().values().stream().map(Object::toString).distinct().count());
        Assertions.assertEquals(10, new TestDatasets(10).datasetMap().values().stream().map(Object::toString).distinct().count());
    }

    @Test
    void manifestsPaths() {
        Assertions.assertEquals(1, new TestDatasets().manifestPathMap().size());
        Assertions.assertEquals(2, new TestDatasets("main", "index").manifestPathMap().size());
        Assertions.assertEquals(2, new TestDatasets("main", "index").manifestPathMap().values().stream().map(Object::toString).distinct().count());
        Assertions.assertEquals(10, new TestDatasets(10).manifestPathMap().values().stream().map(Object::toString).distinct().count());
    }

    @Test
    void manifests() {
        Assertions.assertEquals(1, new TestDatasets().manifestIdentifierMap("20230227PT5M287").size());
        Assertions.assertEquals(2, new TestDatasets("main", "index").manifestIdentifierMap("20230227PT5M287").size());
        Assertions.assertEquals(2, new TestDatasets("main", "index").manifestIdentifierMap("20230227PT5M287").values().stream().map(Object::toString).distinct().count());
        Assertions.assertEquals(10, new TestDatasets(10).manifestIdentifierMap("20230227PT5M287").values().stream().map(Object::toString).distinct().count());
    }
}
