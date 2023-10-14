/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.config;

import java.nio.file.Path;

public class ConfigOptions {
    private final Path homePath;
    private final Path globalConfigPath;
    private final Path globalConfigName;
    private final Path localPath;
    private final Path localConfigName;
    private final String configNamespace;
    private final Class<? extends Configuration> configClass;

    private ConfigOptions(Builder builder) {
        homePath = builder.homePath;
        globalConfigPath = builder.globalConfigPath;
        globalConfigName = builder.globalConfigName;
        localPath = builder.localPath;
        localConfigName = builder.localConfigName;
        configNamespace = builder.configNamespace;
        configClass = builder.configClass;
    }

    public Path homePath() {
        return homePath;
    }

    public Path globalConfigPath() {
        return globalConfigPath;
    }

    public Path globalConfigName() {
        return globalConfigName;
    }

    public Path localPath() {
        return localPath;
    }

    public Path localConfigName() {
        return localConfigName;
    }

    public String configNamespace() {
        return configNamespace;
    }

    public Class<? extends Configuration> configClass() {
        return configClass;
    }

    /**
     * {@code ConfigOptions} builder static inner class.
     */
    public static final class Builder {
        private Path homePath = ConfigManager.HOME_DIR;
        private Path globalConfigPath = ConfigManager.GLOBAL_CONFIG_DIR;
        private Path globalConfigName;
        private Path localPath = ConfigManager.CURRENT_DIR;
        private Path localConfigName;
        private String configNamespace;
        private Class<? extends Configuration> configClass;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets the {@code homePath} and returns a reference to this Builder enabling method chaining.
         *
         * @param homePath the {@code homePath} to set
         * @return a reference to this Builder
         */
        public Builder withHomePath(Path homePath) {
            this.homePath = homePath;
            return this;
        }

        /**
         * Sets the {@code globalConfigPath} and returns a reference to this Builder enabling method chaining.
         *
         * @param globalConfigPath the {@code globalConfigPath} to set
         * @return a reference to this Builder
         */
        public Builder withGlobalConfigPath(Path globalConfigPath) {
            this.globalConfigPath = globalConfigPath;
            return this;
        }

        /**
         * Sets the {@code globalConfigName} and returns a reference to this Builder enabling method chaining.
         *
         * @param globalConfigName the {@code globalConfigName} to set
         * @return a reference to this Builder
         */
        public Builder withGlobalConfigName(Path globalConfigName) {
            this.globalConfigName = globalConfigName;
            return this;
        }

        /**
         * Sets the {@code currentPath} and returns a reference to this Builder enabling method chaining.
         *
         * @param localPath the {@code currentPath} to set
         * @return a reference to this Builder
         */
        public Builder withLocalPath(Path localPath) {
            this.localPath = localPath;
            return this;
        }

        /**
         * Sets the {@code localConfigName} and returns a reference to this Builder enabling method chaining.
         *
         * @param localConfigName the {@code localConfigName} to set
         * @return a reference to this Builder
         */
        public Builder withLocalConfigName(Path localConfigName) {
            this.localConfigName = localConfigName;
            return this;
        }

        /**
         * Sets the {@code configNamespace} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code configNamespace} to set
         * @return a reference to this Builder
         */
        public Builder withConfigNamespace(String val) {
            configNamespace = val;
            return this;
        }

        /**
         * Sets the {@code configClass} and returns a reference to this Builder enabling method chaining.
         *
         * @param configClass the {@code configClass} to set
         * @return a reference to this Builder
         */
        public Builder withConfigClass(Class<? extends Configuration> configClass) {
            this.configClass = configClass;
            return this;
        }

        /**
         * Returns a {@code ConfigOptions} built from the parameters previously set.
         *
         * @return a {@code ConfigOptions} built with parameters of this {@code ConfigOptions.Builder}
         */
        public ConfigOptions build() {
            return new ConfigOptions(this);
        }
    }
}
