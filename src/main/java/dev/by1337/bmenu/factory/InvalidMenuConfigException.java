package dev.by1337.bmenu.factory;


import dev.by1337.core.util.text.MessageFormatter;

public class InvalidMenuConfigException extends RuntimeException {
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
