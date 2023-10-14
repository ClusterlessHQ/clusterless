/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * This is set globally when using this class, not locally `@JsonInclude(JsonInclude.Include.NON_NULL)`
 * <p>
 * If we want to write out a template for a config, we need to write null fields.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Config {

}
