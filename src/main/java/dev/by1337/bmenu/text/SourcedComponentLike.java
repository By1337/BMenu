package dev.by1337.bmenu.text;

import dev.by1337.yaml.codec.YamlCodec;
import net.kyori.adventure.text.ComponentLike;

public interface SourcedComponentLike extends ComponentLike {
    YamlCodec<SourcedComponentLike> CODEC = YamlCodec.STRING
            .map(
                    SourcedComponentFactory::create,
                    SourcedComponentLike::source
            );
    String source();
}
