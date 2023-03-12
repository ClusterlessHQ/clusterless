package clusterless.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PartitionTest {
    @Test
    void test() {
        Assertions.assertEquals("lower", Partition.of("lower").partition());
        Assertions.assertEquals("lower/case", Partition.of("lower").with("case").partition());
        Assertions.assertEquals("lower/case", Partition.of("lower").with(Partition.of("case")).partition());
        Assertions.assertEquals("case=lower", Partition.namedOf("case", "lower").partition());
        Assertions.assertEquals("case=lower/language", Partition.namedOf("case", "lower").with("language").partition());
        Assertions.assertEquals("case=lower/language=english", Partition.namedOf("case", "lower").withNamed("language", "english").partition());
        Assertions.assertEquals("case=lower/language=english", Partition.namedOf("case", "lower").with(Partition.namedOf("language", "english")).partition());
        Assertions.assertEquals("case=lower/language=english", Partition.namedOf("case", "lower").with(null).withNamed("language", "english").partition());
        Assertions.assertEquals("case=lower/language=english", Partition.namedOf("case", "lower").withNamed("language", "english").with(null).partition());

        Assertions.assertNull(Partition.namedOf("case", null).withNamed("language", null).with(null).partition());
        Assertions.assertEquals("case=lower", Partition.namedOf("case", "lower").withNamed("language", null).with(null).partition());
    }

    @Test
    void having() {
        Partition with = Partition.of("lower").having("one", "two", "three");

        Assertions.assertEquals("lower/one/two/three", with.partition());
        Assertions.assertEquals("lower/one/two/three/", with.partition(true));
        Assertions.assertEquals("/lower/one/two/three/", with.path());
    }

    @Test
    void havingSlashes() {
        Partition with = Partition.of("lower").having("/", "one", "/", "/", "two", "three");

        Assertions.assertEquals("lower/one/two/three", with.partition());
        Assertions.assertEquals("lower/one/two/three/", with.partition(true));
        Assertions.assertEquals("/lower/one/two/three/", with.path());
    }

    @Test
    void havingSlashesRoot() {
        Partition with = Partition.of("///").having("/", "one", "/", "/", "two", "three");

        Assertions.assertEquals("one/two/three", with.partition());
        Assertions.assertEquals("one/two/three/", with.partition(true));
        Assertions.assertEquals("/one/two/three/", with.path());
    }

    @Test
    void havingLabels() {
        Partition with = Partition.of("lower").with(Label.of("one").having("two", "three"));

        Assertions.assertEquals("lower/one/two/three", with.partition());
        Assertions.assertEquals("lower/one/two/three/", with.partition(true));
        Assertions.assertEquals("/lower/one/two/three/", with.path());
    }

    enum Case implements Partition.EnumPartition {
        Lower,
        Upper;
    }

    @Test
    void enumeration() {
        Assertions.assertEquals("case=lower", Partition.of(Case.Lower).partition());
        Assertions.assertEquals("case=lower/case=upper", Partition.of(Case.Lower).with(Case.Upper).partition());
        Assertions.assertEquals("case=lower/case=upper", Partition.of(Case.Lower).with(Case.Upper).with(null).partition());
    }
}
