package org.by1337.bmenu.animation;

import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public boolean isEnd() {
        return pos - 1 >= context.maxTick;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public static class AnimatorContext {
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

        public void join(AnimatorContext other) {
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
    }

    public static class Frame {
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
    }
}
