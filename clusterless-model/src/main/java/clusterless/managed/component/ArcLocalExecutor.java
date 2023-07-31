/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed.component;

import clusterless.model.manifest.ManifestState;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface ArcLocalExecutor {

    class Command {
        String headerComment;
        Map<String, String> environmentComments = new LinkedHashMap<>();
        Map<String, String> environment = new LinkedHashMap<>();
        String commandComment;
        List<String> command = new LinkedList<>();

        public static Builder builder() {
            return Builder.builder();
        }

        public String headerComment() {
            return headerComment;
        }

        public Map<String, String> environmentComments() {
            return environmentComments;
        }

        public String commandComment() {
            return commandComment;
        }

        public Map<String, String> environment() {
            return environment;
        }

        public List<String> command() {
            return command;
        }

        public static final class Builder {
            String headerComment;
            Map<String, String> environmentComments = new LinkedHashMap<>();
            Map<String, String> environment = new LinkedHashMap<>();
            String commandComment;
            List<String> command = new LinkedList<>();

            private Builder() {
            }

            public static Builder builder() {
                return new Builder();
            }

            public Builder withHeaderComment(String headerComment) {
                this.headerComment = headerComment;
                return this;
            }

            public Builder withEnvironmentComments(Map<String, String> environmentComments) {
                this.environmentComments = environmentComments;
                return this;
            }

            public Builder withEnvironment(Map<String, String> environment) {
                this.environment = environment;
                return this;
            }

            public Builder withCommandComment(String commandComment) {
                this.commandComment = commandComment;
                return this;
            }

            public Builder withCommand(List<String> command) {
                this.command = command;
                return this;
            }

            public Command build() {
                Command command = new Command();
                command.headerComment = this.headerComment;
                command.command = this.command;
                command.commandComment = this.commandComment;
                command.environmentComments = this.environmentComments;
                command.environment = this.environment;
                return command;
            }
        }
    }

    List<Command> commands(String role, String lotId, ManifestState manifestState);
}
