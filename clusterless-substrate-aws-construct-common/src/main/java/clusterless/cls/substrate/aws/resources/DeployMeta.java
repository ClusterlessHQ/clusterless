/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resources;

import clusterless.cls.model.Struct;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeployMeta implements Struct {

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public static class Item implements Struct {
        String nameRef;
        String idRef;
        String arnRef;
        String name;
        String id;

        public Item setNameRef(String nameRef) {
            this.nameRef = nameRef;
            return this;
        }

        public Item setIdRef(String idRef) {
            this.idRef = idRef;
            return this;
        }

        public Item setArnRef(String arnRef) {
            this.arnRef = arnRef;
            return this;
        }

        public Item setName(String name) {
            this.name = name;
            return this;
        }

        public Item setId(String id) {
            this.id = id;
            return this;
        }

        public String nameRef() {
            return nameRef;
        }

        public String idRef() {
            return idRef;
        }

        public String arnRef() {
            return arnRef;
        }

        public String id() {
            return id;
        }

        public String name() {
            return name;
        }
    }

    Instant createDateTime = Instant.now();

    Map<String, Item> exports = new LinkedHashMap<>();

    public Instant createDateTime() {
        return createDateTime;
    }

    public Map<String, Item> exports() {
        return exports;
    }

    private Item get(String name) {
        return exports.computeIfAbsent(name, n -> new Item());
    }

    public DeployMeta setIdRef(String name, String value) {
        get(name).setIdRef(value);
        return this;
    }

    public DeployMeta setNameRef(String name, String value) {
        get(name).setNameRef(value);
        return this;
    }

    public DeployMeta setArnRef(String name, String value) {
        get(name).setArnRef(value);
        return this;
    }

    public DeployMeta setId(String name, String value) {
        get(name).setId(value);
        return this;
    }

    public DeployMeta setName(String name, String value) {
        get(name).setName(value);
        return this;
    }
}
