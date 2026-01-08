package dev.by1337.bmenu.animation;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.JsonSchemaTypeBuilder;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.item.SlotContent;
import dev.by1337.bmenu.animation.impl.GotoAnimOpcode;
import dev.by1337.bmenu.animation.impl.SoundAnimOpcode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Animator {
    private int pos;
    private final AnimatorContext context;

    public Animator(AnimatorContext context) {
        this.context = context;
    }

    public void tick(SlotContent[] matrix, Menu menu) {
        var list = context.framePosToFrames.get(pos++);
        if (list != null) {
            for (Frame frame : list) {
                frame.apply(matrix, menu, this);
            }
        }
    }

    public void forceEnd(SlotContent[] matrix, Menu menu) {
        int pos0 = pos;
        while (pos0 < context.maxTick) {
            var list = context.framePosToFrames.get(pos0++);
            if (list != null) {
                for (Frame frame : list) {
                    frame.safeApply(matrix, menu, this);
                }
            }
        }
        pos = pos0;
    }

    public boolean isEnd() {
        return pos - 1 >= context.maxTick;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public static class AnimatorContext {
        public static YamlCodec<AnimatorContext> CODEC = new CodecImpl();
        private final List<Frame> frames;
        private final Map<Integer, List<Frame>> framePosToFrames;
        private int maxTick;

        public AnimatorContext(List<Frame> frames) {
            this.frames = frames;
            framePosToFrames = new HashMap<>();
            for (Frame frame : frames) {
                framePosToFrames.computeIfAbsent(frame.frame, k -> new ArrayList<>()).add(frame);
                maxTick = Math.max(maxTick, frame.frame);
            }
        }

        public void merge(AnimatorContext other) {
            frames.addAll(other.frames);
            maxTick = Math.max(maxTick, other.maxTick);
            framePosToFrames.clear();
            for (Frame frame : frames) {
                framePosToFrames.computeIfAbsent(frame.frame, k -> new ArrayList<>()).add(frame);
                maxTick = Math.max(maxTick, frame.frame);
            }
        }

        public Animator createAnimator() {
            return new Animator(this);
        }

        private static class CodecImpl implements YamlCodec<AnimatorContext> {
            private final SchemaType schemaType = Frame.SCHEMA_TYPE.listOf();
            private static final YamlCodec<List<YamlMap>> YAML_MAP_LIST = YamlCodec.YAML_MAP.listOf();

            @Override
            public DataResult<AnimatorContext> decode(YamlValue yamlValue) {
               return yamlValue.decode(YAML_MAP_LIST).flatMap(list -> {
                   List<Animator.Frame> frameList = new ArrayList<>();
                   int lastPos = 0;
                   StringBuilder error = new StringBuilder();
                   for (YamlMap frame : list) {
                       List<FrameOpcode> opcodes = frame.get("opcodes").decode(YAML_MAP_LIST, List.of()).mapValue(l -> {
                           List<FrameOpcode> res1 = new ArrayList<>();
                           for (YamlMap yamlMap : l) {
                               DataResult<Map<String, FrameOpcode>> v = FrameOpcodes.FRAMES_CODEC.decode(YamlValue.wrap(yamlMap));
                               if (v.hasError()){
                                   error.append(v.error()).append("\n");
                               }
                               if (v.hasResult()){
                                   res1.addAll(v.result().values());
                               }
                           }
                           return res1;
                       }).result();
                       int pos = frame.get("tick").decode(INT, ++lastPos).getOrThrow();
                       lastPos = pos;
                       frameList.add(new Animator.Frame(pos, opcodes));
                   }
                   if (!error.isEmpty()){
                       error.setLength(error.length() -1);
                       return DataResult.error(error.toString()).partial(new AnimatorContext(frameList));
                   }
                   return DataResult.success(new AnimatorContext(frameList));
               });
            }

            @Override
            public YamlValue encode(AnimatorContext ctx) {
                List<Map<String, Object>> result = new ArrayList<>();
                for (Frame frame : ctx.frames) {
                    YamlMap map = new YamlMap();
                    map.set("tick", frame.frame);
                    map.set("opcodes",
                            frame.opcodes.stream().filter(e -> e.type() != null).collect(Collectors.toMap(
                                    e -> e.type().getId(),
                                    e -> e
                            )),
                            FrameOpcodes.FRAMES_CODEC
                    );
                    result.add(map.getRaw());
                }
                return YamlValue.wrap(result);
            }

            @Override
            public @NotNull SchemaType schema() {
                return schemaType;
            }
        }
    }

    public static class Frame {
        public static final SchemaType SCHEMA_TYPE = JsonSchemaTypeBuilder
                .create()
                .type(SchemaTypes.Type.OBJECT)
                .properties("tick", SchemaTypes.INT)
                .properties("opcodes", FrameOpcodes.FRAMES_CODEC.listOf().schema())
                .required("opcodes")
                .build();


        private final int frame;
        private final List<FrameOpcode> opcodes;

        public Frame(int frame, List<FrameOpcode> opcodes) {
            this.frame = frame;
            this.opcodes = opcodes;
        }

        public void apply(SlotContent[] matrix, Menu menu, Animator animator) {
            for (FrameOpcode opcode : opcodes) {
                opcode.apply(matrix, menu, animator);
            }
        }

        public void safeApply(SlotContent[] matrix, Menu menu, Animator animator) {
            for (FrameOpcode opcode : opcodes) {
                if (opcode instanceof GotoAnimOpcode || opcode instanceof SoundAnimOpcode) continue;
                opcode.apply(matrix, menu, animator);
            }
        }
    }
}
