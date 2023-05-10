package clusterless.naming;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RefTest {
    @Test
    void name() {

        Label label = new Ref()
                .withProvider("aws")
                .withScope("bootstrap")
                .withScopeVersion("20230101")
                .withResourceNs("core")
                .withResourceType("compute")
                .withResourceName("spot")
                .withQualifier(Ref.Qualifier.Id)
                .label();

        Assertions.assertEquals("ref:aws:id:bootstrap:20230101:core:compute:spot", label.lowerColonPath());
    }
}
