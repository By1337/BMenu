package dev.by1337.bmenu.animation;


import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.JsonSchemaTypeBuilder;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import dev.by1337.bmenu.animation.opcode.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public enum FrameOpcodes {
    SET(SetAnimOpcode.CODEC, "set", "st"),
    REMOVE(RemoveAnimOpcode.CODEC, "remove", "rm"),
    MOVE(MoveAnimOpcode.CODEC, "move", "mv"),
    SWAP(SwapAnimOpcode.CODEC, "swap", "sw"),
    COPY(CopyAnimOpcode.CODEC, "copy", "cl", "cpy"),
    GOTO(GotoAnimOpcode.CODEC, "goto", "gt"),
    SOUND(SoundAnimOpcode.CODEC, "sound", "snd"),
    COMMANDS(CommandsAnimOpcode.CODEC, "commands", "cmd"),
    TITLE(SetTitleOpcode.CODEC, "title", "ttl"),
    FILL(FillAnimOpcode.CODEC, "fill", "fl"),
    REMOVE_IF_NOT_EMPTY(RemoveIfNotEmptyAnimOpcode.CODEC, "remove-if-not-empty", "rne"),
    COPY_FROM_BASE(CopyFromBaseAnimOpcode.CODEC, "copy-from-base", "cfb"),
    SET_IF_EMPTY(SetIfEmptyAnimOpcode.CODEC, "set-if-empty", "sie"),
    ;
    public static final YamlCodec<Map<String, FrameOpcode>> FRAMES_CODEC;
    private static final Map<String, FrameOpcodes> LOOKUP;
    private static final Logger log = LoggerFactory.getLogger("BMenu");
    private final YamlCodec<? extends FrameOpcode> codec;
    private final String id;
    private final String[] aliases;

    FrameOpcodes(YamlCodec<? extends FrameOpcode> codec, String id) {
        this.codec = codec;
        this.id = id;
        aliases = new String[]{};
    }

    FrameOpcodes(YamlCodec<? extends FrameOpcode> codec, String id, String... aliases) {
        this.codec =codec;
        this.id = id;
        this.aliases = aliases;
    }


    public YamlCodec<? extends FrameOpcode> getCodec() {
        return codec;
    }

    public String getId() {
        return id;
    }

    public String[] aliases() {
        return aliases;
    }

    public static FrameOpcodes byId(String id) {
        return LOOKUP.get(id);
    }

    static {
        LOOKUP = new HashMap<>();
        for (FrameOpcodes value : values()) {
            LOOKUP.put(value.id, value);
            for (String alias : value.aliases) {
                LOOKUP.put(alias, value);
            }
        }
        FRAMES_CODEC = new YamlCodec<Map<String, FrameOpcode>>() {
            private SchemaType schemaType;
            @Override
            public DataResult<Map<String, FrameOpcode>> decode(YamlValue yamlValue) {
               return yamlValue.asYamlMap().flatMap(map -> {
                   Map<String, FrameOpcode> result = new LinkedHashMap<>();
                   StringBuilder error = new StringBuilder();
                   for (String s : map.getRaw().keySet()) {
                       var type = byId(s);
                       if (type == null){
                           error.append("Unknown animation opcode type ").append(s).append("\n");
                       }else {
                           DataResult<? extends FrameOpcode> dataResult = type.codec.decode(map.get(s));
                           if (dataResult.hasError()){
                               error.append(dataResult.error()).append("\n");
                           }
                           if (dataResult.hasResult()){
                               result.put(s, dataResult.result());
                           }
                       }
                   }
                   if (!error.isEmpty()){
                       error.setLength(error.length()-1);
                       return DataResult.error(error.toString()).partial(result);
                   }
                   return DataResult.success(result);
               });
            }

            @Override
            @SuppressWarnings("unchecked")
            public YamlValue encode(Map<String, FrameOpcode> frames) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (FrameOpcode frame : frames.values()) {
                    var type = frame.type();
                    if (type != null){
                        map.put(type.id, ((YamlCodec<FrameOpcode>) type.codec).encode(frame).getValue());
                    }
                }
                return YamlValue.wrap(map);
            }

            @Override
            public @NotNull SchemaType schema() {
                if (schemaType != null) return schemaType;
                buildSchemaType();
                return schemaType;
            }

            private void buildSchemaType(){
                var builder = JsonSchemaTypeBuilder.create()
                        .types(SchemaTypes.Type.OBJECT)
                        .additionalProperties(false);

                for (FrameOpcodes value : FrameOpcodes.values()) {
                    builder.properties(value.id, value.codec.schema());
                    for (String alias : value.aliases) {
                        builder.properties(alias, value.codec.schema());
                    }
                }
                schemaType = builder.build();
            }
        };
    }

}
