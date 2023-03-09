package clusterless.manifest;

import clusterless.model.manifest.ManifestState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ManifestStateTest {
    @Test
    void parse() {
        Assertions.assertEquals(ManifestState.partial, ManifestState.parse("state=partial"));
        Assertions.assertEquals(ManifestState.partial, ManifestState.parse("STATE=PARTIAL"));
        Assertions.assertEquals(ManifestState.partial, ManifestState.parse("partial"));
    }
}
