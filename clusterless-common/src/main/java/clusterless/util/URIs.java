/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public class URIs {
    public static URI create(String scheme, String authority, String path) {
        try {
            return new URI(scheme, authority, normalize("/", path), null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("unable to create uri", e);
        }
    }

    public static URI copyAppendPath(URI uri, String... path) {
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), normalize(uri.getPath(), path), null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("unable to copy uri", e);
        }
    }

    public static URI copyWithPath(URI uri, String path) {
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), normalize("/", path), null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("unable to copy uri", e);
        }
    }

    public static URI normalizeURI(@Nullable URI uri) {
        if (uri == null) {
            return null;
        }

        String normalize = normalize(uri.getPath());

        return copyWithPath(uri, normalize);
    }

    /**
     * Returns a normalized path, that is multiple slashes and dots are removed.
     * <p>
     * Unlike Path#normalize, the last slash is retained.
     *
     * @param path
     * @return
     */
    public static String normalize(String path) {
        //  todo:  return value.replaceAll("(?<!:)/{2,}", "/");
        String empty = Strings.nullToEmpty(path);
        return normalize(Paths.get(empty), empty);
    }

    public static String normalize(String path, String append) {
        if (append == null || append.isEmpty()) {
            return normalize(path);
        }

        return normalize(path, new String[]{append});
    }

    public static String normalize(String path, String... appends) {
        String first = Strings.nullToEmpty(path);

        Path head = Paths.get(first);
        String second = first;

        for (String append : appends) {
            second = Strings.nullToEmpty(append);

            if (second.charAt(0) == '/') {
                second = second.substring(1);
            }

            head = head.resolve(second);
        }

        return normalize(head, second);
    }

    private static String normalize(Path head, String second) {
        String result = head.normalize().toString();

        if (result.length() > 1 && second.endsWith("/")) {
            return result + "/";
        }

        return result;
    }

    public static String asKeyPath(URI uri) {
        String normalize = uri.normalize().getPath();

        if (normalize.isEmpty()) {
            return null;
        }

        if (normalize.charAt(0) == '/') {
            if (normalize.length() == 1) {
                return null;
            }

            return normalize.substring(1);
        }

        return normalize;
    }

    public static URI fromTo(URI fromBase, URI from, URI toBase) {
        String fromBaseScheme = fromBase.getScheme();
        String fromScheme = from.getScheme();
        if (!fromBaseScheme.equals(fromScheme)) {
            throw new IllegalArgumentException(String.format("fromBase and from must have the same scheme, got fromBase: %s, from: %s", fromBaseScheme, fromScheme));

        }

        String fromBaseHost = fromBase.getHost();
        String fromHost = from.getHost();
        if (!fromBaseHost.equals(fromHost)) {
            throw new IllegalArgumentException(String.format("fromBase and from must have the same host, got fromBase: %s, from: %s", fromBaseHost, fromHost));
        }

        String fromBasePath = normalize(fromBase.getPath(), "/");
        String fromPath = normalize(from.getPath());
        if (!fromPath.startsWith(fromBasePath)) {
            throw new IllegalArgumentException(String.format("fromBase and from must have a common path, got fromBase: %s, from: %s", fromBasePath, fromPath));
        }

        return copyAppendPath(toBase, fromPath.substring(fromBasePath.length()));
    }
}
