package org.by1337.bmenu.factory;

import org.by1337.blib.text.MessageFormatter;

public class InvalidMenuConfigException extends Exception {
    public InvalidMenuConfigException(String message) {
        super(message);
    }

    public InvalidMenuConfigException() {
    }

    public InvalidMenuConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidMenuConfigException(String message, Object... args) {
        super(
                args != null && args.length != 0 ? MessageFormatter.apply(message, args) : message,
                args != null && args.length != 0 && args[args.length - 1] instanceof Throwable t ? t : null
        );
    }
}
