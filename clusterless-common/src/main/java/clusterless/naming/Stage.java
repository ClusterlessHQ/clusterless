package clusterless.naming;

import java.util.Locale;

public class Stage extends Fixed {
    public static Stage of(String stage) {
        return new Stage(stage);
    }

    protected Stage(String value) {
        super(value == null ? null : value.toUpperCase(Locale.ROOT));
    }

    public Label asLower() {
        if (isNull()) {
            return Fixed.fixedNull();
        }
        return new Fixed(value.toLowerCase());
    }
}
