package clusterless.managed.component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface ArcLocalExecutor {

    enum OS {
        mac,
        win,
        linux
    }

    class Command {
        Map<String, String> environment;
        List<String> command;

        public Command(Map<String, String> environment, List<String> command) {
            this.environment = environment;
            this.command = command;
        }

        public Command(List<String> command) {
            this.environment = Collections.emptyMap();
            this.command = command;
        }

        public Map<String, String> environment() {
            return environment;
        }

        public List<String> command() {
            return command;
        }
    }

    List<Command> commands(OS os);
}
