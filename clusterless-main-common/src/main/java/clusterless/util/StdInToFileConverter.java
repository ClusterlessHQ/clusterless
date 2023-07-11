/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import picocli.CommandLine;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class StdInToFileConverter implements CommandLine.ITypeConverter<File> {
    @Override
    public File convert(String value) throws Exception {
        if ("-".equals(value)) {
            Path project = Files.createTempFile("project", ".json");

            try (OutputStream out = Files.newOutputStream(project)) {
                System.in.transferTo(out);
            }

            return project.toFile();
        }

        return new File(value);
    }
}
