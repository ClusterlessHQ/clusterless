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

import static clusterless.managed.NameTest.Value.Case;
import static clusterless.managed.NameTest.Value.Lower;

/**
 *
 */
public class NameTest {
    @Test
    void string() {
        Assertions.assertEquals("Lower", Label.of("lower").camelCase());
        Assertions.assertEquals("Lower", Label.fromLowerHyphen("lower").camelCase());
        Assertions.assertEquals("lower", Label.fromLowerHyphen("lower").lowerHyphen());
        Assertions.assertEquals("lower", Label.fromLowerHyphen("lower").lowerUnderscore());

        Label with = Label.of("lower").with(Label.of("case"));
        Assertions.assertEquals("LowerCase", with.camelCase());
        Assertions.assertEquals("lower-case", with.lowerHyphen());
        Assertions.assertEquals("lower_case", with.lowerUnderscore());

        Label abbr = Label.of("lower").abbreviated(Label.of("lwr")).with(Label.of("case").abbreviated(Label.of("cs")));
        Assertions.assertEquals("LowerCase", abbr.camelCase());
        Assertions.assertEquals("LwrCs", abbr.shortCamelCase());
        Assertions.assertEquals("lwr-cs", abbr.shortLowerHyphen());
        Assertions.assertEquals("lwr_cs", abbr.shortLowerUnderscore());

        Label abbr2 = Label.of("lower", "lwr").with(Label.of("case", "cs"));
        Assertions.assertEquals("LowerCase", abbr2.camelCase());
        Assertions.assertEquals("LwrCs", abbr2.shortCamelCase());
        Assertions.assertEquals("lwr-cs", abbr2.shortLowerHyphen());
        Assertions.assertEquals("lwr_cs", abbr2.shortLowerUnderscore());
    }

    enum Value implements Label.EnumLabel {
        Lower("Lwr"),
        Case("Cs");

        private final String abbr;

        Value(String abbr) {
            this.abbr = abbr;
        }

        @Override
        public Label abbreviated() {
            return Label.of(abbr);
        }
    }

    @Test
    void enumeration() {
        Assertions.assertEquals("Lower", Label.of(Lower).camelCase());
        Assertions.assertEquals("Lwr", Label.of(Lower).shortCamelCase());

        Assertions.assertEquals("LowerCase", Label.of(Lower).with(Case).camelCase());
        Assertions.assertEquals("lwr-cs", Label.of(Lower).with(Case).shortLowerHyphen());
    }
}
