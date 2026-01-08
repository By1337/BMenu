package dev.by1337.bmenu.command.menu;

import dev.by1337.yaml.codec.YamlCodec;

public enum OpenCommandArguments {
   // PLAYER()
    ;
    private final YamlCodec<OpenCommandArgument> codec;
    private final String[] names;

    OpenCommandArguments(YamlCodec<OpenCommandArgument> codec, String... names) {
        this.codec = codec;
        this.names = names;
    }
}
