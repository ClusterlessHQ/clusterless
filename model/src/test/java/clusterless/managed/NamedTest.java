/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static clusterless.managed.NamedTest.Value.Case;
import static clusterless.managed.NamedTest.Value.Lower;

/**
 *
 */
public class NamedTest {
    @Test
    void string() {
        Assertions.assertEquals("Lower", Named.of("lower").camelCase());
        Assertions.assertEquals("Lower", Named.fromLowerHyphen("lower").camelCase());
        Assertions.assertEquals("lower", Named.fromLowerHyphen("lower").lowerHyphen());
        Assertions.assertEquals("lower", Named.fromLowerHyphen("lower").lowerUnderscore());

        Named with = Named.of("lower").with(Named.of("case"));
        Assertions.assertEquals("LowerCase", with.camelCase());
        Assertions.assertEquals("lower-case", with.lowerHyphen());
        Assertions.assertEquals("lower_case", with.lowerUnderscore());

        Named abbr = Named.of("lower").abbreviated(Named.of("lwr")).with(Named.of("case").abbreviated(Named.of("cs")));
        Assertions.assertEquals("LowerCase", abbr.camelCase());
        Assertions.assertEquals("LwrCs", abbr.shortCamelCase());
        Assertions.assertEquals("lwr-cs", abbr.shortLowerHyphen());
        Assertions.assertEquals("lwr_cs", abbr.shortLowerUnderscore());

        Named abbr2 = Named.of("lower", "lwr").with(Named.of("case", "cs"));
        Assertions.assertEquals("LowerCase", abbr2.camelCase());
        Assertions.assertEquals("LwrCs", abbr2.shortCamelCase());
        Assertions.assertEquals("lwr-cs", abbr2.shortLowerHyphen());
        Assertions.assertEquals("lwr_cs", abbr2.shortLowerUnderscore());
    }

    enum Value implements Named.EnumNamed {
        Lower("Lwr"),
        Case("Cs");

        private final String abbr;

        Value(String abbr) {
            this.abbr = abbr;
        }

        @Override
        public Named abbreviated() {
            return Named.of(abbr);
        }
    }

    @Test
    void enumeration() {
        Assertions.assertEquals("Lower", Named.of(Lower).camelCase());
        Assertions.assertEquals("Lwr", Named.of(Lower).shortCamelCase());

        Assertions.assertEquals("LowerCase", Named.of(Lower).with(Case).camelCase());
        Assertions.assertEquals("lwr-cs", Named.of(Lower).with(Case).shortLowerHyphen());
    }
}
