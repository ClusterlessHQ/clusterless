package clusterless.manifest;

import clusterless.model.manifest.ManifestState;
import clusterless.model.state.ArcState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StateTest {
    @Test
    void arc() {
        Assertions.assertEquals(ArcState.partial, ArcState.parse("state=partial"));
        Assertions.assertEquals(ArcState.partial, ArcState.parse("STATE=PARTIAL"));
        Assertions.assertEquals(ArcState.partial, ArcState.parse("partial"));
        Assertions.assertEquals(ArcState.partial, ArcState.parse("partial.arc"));
    }

    @Test
    void manifest() {
        Assertions.assertEquals(ManifestState.partial, ManifestState.parse("state=partial"));
        Assertions.assertEquals(ManifestState.partial, ManifestState.parse("STATE=PARTIAL"));
        Assertions.assertEquals(ManifestState.partial, ManifestState.parse("partial"));
    }
}
