/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import software.amazon.awscdk.services.lambda.Code;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class Assets {

    public static final String CLS_ASSETS_PATH = "CLS_ASSETS_PATH";

    public static Code find(Pattern pattern) {
        Path assetDistributionPath = getAssetDistributionPath();

        if (System.getenv(CLS_ASSETS_PATH) != null) {
            return Code.fromAsset(assetDistributionPath.toAbsolutePath().toString());
        }

        List<Path> files;
        try (Stream<Path> list = Files.list(assetDistributionPath)) {
            files = list
                    .filter(f -> pattern.matcher(f.getFileName().toString()).matches())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (files.isEmpty()) {
            throw new IllegalStateException(String.format("no assets matching: %s found in: %s", pattern, assetDistributionPath.toAbsolutePath()));
        }

        if (files.size() > 1) {
            Set<Path> collect = files.stream().map(Path::getFileName).collect(Collectors.toSet());
            throw new IllegalStateException(String.format("too many assets matching: %s found in: %s, found: %s", pattern, assetDistributionPath.toAbsolutePath(), collect));
        }

        Path path = files.get(0);

        return Code.fromAsset(path.toAbsolutePath().toString());
    }

    public static Path getAssetDistributionPath() {
        String clsAssetsPath = System.getenv(CLS_ASSETS_PATH);

        if (clsAssetsPath != null) {
            return Paths.get(clsAssetsPath).toAbsolutePath();
        }

        URL location = Assets.class.getProtectionDomain()
                .getCodeSource()
                .getLocation();

        Path assets = Paths.get(URI.create(location.toString()));

        //  drop filename and lib folder, move back up to assets
        assets = assets.getParent().getParent().resolve("assets");

        if (!Files.isDirectory(assets)) {
            throw new IllegalStateException("asset distribution path does not exist: " + assets);
        }

        return assets;
    }
}
