package clusterless.util;

import java.util.Optional;

public class Optionals {
    public static <T> Optional<T> optional(int index, T[] array) {
        if (index > array.length - 1) {
            return Optional.empty();
        }

        return Optional.ofNullable(array[index]);
    }
}
