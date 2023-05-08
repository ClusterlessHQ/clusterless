package clusterless.naming;

import java.util.Locale;

public class Stage extends Fixed {
    private static final Stage NULL_STAGE = new Stage(null);

    public static Stage of(String stage) {
        return new Stage(stage == null ? null : stage.toUpperCase(Locale.ROOT));
    }

    public static Stage nullStage() {
        return NULL_STAGE;
    }

    protected Stage(String value) {
        super(value);
    }

    public Stage asLower() {
        if (isNull()) {
            return NULL_STAGE;
        }
        return new Stage(value.toLowerCase());
    }
}
