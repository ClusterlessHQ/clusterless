package clusterless.naming;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExportRefTest {
    @Test
    void name() {

        Label label = new ExportRef()
                .withProvider("aws")
                .withScope("bootstrap")
                .withScopeVersion("20230101")
                .withResourceType("compute")
                .withResourceName("spot")
                .withQualifier(ExportRef.ExportQualifier.Id)
                .label();

        Assertions.assertEquals("aws:bootstrap:20230101:compute:spot:id", label.lowerColonPath());
    }
}
