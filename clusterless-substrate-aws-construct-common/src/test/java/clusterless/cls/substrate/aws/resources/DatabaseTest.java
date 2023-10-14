/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static java.net.URI.create;

public class DatabaseTest {
    @Test
    void parse() {
        URI uri = create("glue://catalog/database/table");
        Assertions.assertEquals("catalog", Databases.catalogFrom(uri));
        Assertions.assertEquals("database", Databases.databaseFrom(uri));
        Assertions.assertEquals("table", Databases.tableFrom(uri));
    }

    @Test
    void parseNull() {
        URI uri = create("glue:///database/table");
        Assertions.assertNull(Databases.catalogFrom(uri));
        Assertions.assertEquals("database", Databases.databaseFrom(uri));
        Assertions.assertEquals("table", Databases.tableFrom(uri));
    }
}
