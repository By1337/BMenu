package org.by1337.bmenu.animation;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.JsonSchemaTypeBuilder;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.impl.GotoAnimOpcode;
import org.by1337.bmenu.animation.impl.SoundAnimOpcode;
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

    public void tick(MenuItem[] matrix, Menu menu) {
        var list = context.framePosToFrames.get(pos++);
        if (list != null) {
            for (Frame frame : list) {
                frame.apply(matrix, menu, this);
            }
        }
    }

    public void forceEnd(MenuItem[] matrix, Menu menu) {
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

            @Override
            public AnimatorContext decode(YamlValue yamlValue) {
                List<YamlMap> list = yamlValue.decode(YamlCodec.YAML_MAP_LIST);
                List<Animator.Frame> frameList = new ArrayList<>();
                int lastPos = 0;
                for (YamlMap frame : list) {
                    for (YamlMap yamlMap : frame.get("opcodes").decode(YamlCodec.YAML_MAP_LIST, List.of())) {
                        List<FrameOpcode> opcodes = new ArrayList<>(
                                FrameOpcodes.FRAMES_CODEC.decode(YamlValue.wrap(yamlMap)).values()
                        );
                        int pos = frame.get("tick").getAsInt(++lastPos);
                        lastPos = pos;
                        frameList.add(new Animator.Frame(pos, opcodes));
                    }
                }
                return new AnimatorContext(frameList);
            }

            @Override
            public YamlValue encode(AnimatorContext ctx) {
                List<YamlMap> result = new ArrayList<>();
                for (Frame frame : ctx.frames) {
                    YamlMap map = new YamlMap();
                    map.setRaw("tick", frame.frame);
                    map.set("opcodes",
                            frame.opcodes.stream().filter(e -> e.type() != null).collect(Collectors.toMap(
                                    e -> e.type().getId(),
                                    e -> e
                            )),
                            FrameOpcodes.FRAMES_CODEC
                    );
                    result.add(map);
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

        public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
            for (FrameOpcode opcode : opcodes) {
                opcode.apply(matrix, menu, animator);
            }
        }

        public void safeApply(MenuItem[] matrix, Menu menu, Animator animator) {
            for (FrameOpcode opcode : opcodes) {
                if (opcode instanceof GotoAnimOpcode || opcode instanceof SoundAnimOpcode) continue;
                opcode.apply(matrix, menu, animator);
            }
        }
    }
}
