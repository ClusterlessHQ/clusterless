package clusterless.util;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

public class Runtimes {
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }


    public static Optional<Path> findExecutable(String name) {
        String path = System.getenv("PATH");

        String[] split = path.split(File.pathSeparator);

        return Arrays.stream(split)
                .map(s -> Paths.get(s).resolve(name))
                .filter(Files::exists).findFirst();
    }

    public static String getHome(Class<?> type) {
        try {
            String jarPath = type
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            Path resolved = Paths.get(jarPath).getParent();

            if (jarPath.endsWith(".jar")) {
                resolved = resolved.getParent();
            }

            return resolved.toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
