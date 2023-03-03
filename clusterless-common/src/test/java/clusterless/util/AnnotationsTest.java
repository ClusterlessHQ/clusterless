/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

/**
 *
 */
public class AnnotationsTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Deep {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Intermediate {
        String value();
    }

    @Deep("deep")
    public interface First {

    }

    public class Middle implements First {

    }

    @Intermediate("intermediate")
    public interface Second {

    }

    public class Final extends Middle implements Second {

    }

    @Test
    void name() {
        Optional<Intermediate> shallow = Annotations.find(Final.class, Intermediate.class);

        Assertions.assertTrue(shallow.isPresent());
        Assertions.assertEquals("intermediate", shallow.get().value());

        Optional<Deep> deep = Annotations.find(Final.class, Deep.class);

        Assertions.assertTrue(deep.isPresent());
        Assertions.assertEquals("deep", deep.get().value());
    }
}
