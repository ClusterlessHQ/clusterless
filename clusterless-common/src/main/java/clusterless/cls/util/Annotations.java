/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 *
 */
public class Annotations {
    public static <A extends Annotation> Optional<A> find(Class<?> type, Class<A> annotationClass) {
        A annotation = type.getAnnotation(annotationClass);

        if (annotation != null) {
            return Optional.of(annotation);
        }

        Class<?>[] interfaces = type.getInterfaces();

        Optional<A> result = find(annotationClass, interfaces);

        if (result.isPresent()) {
            return result;
        }

        return find(type.getSuperclass(), annotationClass);
    }

    private static <A extends Annotation> Optional<A> find(Class<A> annotationClass, Class<?>[] interfaces) {
        for (Class<?> annotatedInterface : interfaces) {
            A annotation = annotatedInterface.getAnnotation(annotationClass);

            if (annotation != null) {
                return Optional.of(annotation);
            }

            Optional<A> other = find(annotationClass, annotatedInterface.getInterfaces());

            if (other.isPresent()) {
                return other;
            }
        }

        return Optional.empty();
    }
}
