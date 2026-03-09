package dev.by1337.bmenu.handler.trace;

import java.io.PrintStream;
import java.io.StringWriter;
import java.util.function.Consumer;

public interface EventTracer {
    EventTracer NOP = new EventTracer() {
        final TracerDepth nop = new TracerDepth() {
            @Override
            public void close() {
                //nop
            }

            @Override
            public void result(boolean b) {
                //nop
            }
        };

        @Override
        public TracerDepth enter(String name, String exit) {
            return nop;
        }

        @Override
        public TracerDepth enter(String name, Object o, String exit) {
            return nop;
        }

        @Override
        public void log(String s, Object... args) {
            //nop
        }

        @Override
        public void log(String s) {
            //nop
        }

        @Override
        public TimingScope timing(String s, Object... args) {
            return () -> {
            };
        }
    };

    static EventTracer defaultTracer(StringBuilder sb) {
        return defaultTracer(s -> sb.append(s).append("\n"));
    }

    static EventTracer defaultTracer(StringWriter sw) {
        return defaultTracer(s -> sw.append(s).append("\n"));
    }

    static EventTracer defaultTracer(PrintStream stream) {
        return defaultTracer(stream::println);
    }

    static EventTracer defaultTracer(Consumer<String> logger) {
        return new EventTracer() {
            public int depth;

            @Override
            public TracerDepth enter(String name, String exit) {
                log(name);
                depth++;
                return new TracerDepth() {
                    boolean result = false;

                    @Override
                    public void close() {
                        depth--;
                        log(exit, result);
                    }

                    @Override
                    public void result(boolean b) {
                        result = b;
                    }
                };
            }

            @Override
            public void log(String s, Object... args) {
                logger.accept("\s\s".repeat(depth) + String.format(s, args));
            }

            @Override
            public void log(String s) {
                logger.accept("\s\s".repeat(depth) + s);
            }
        };
    }

    static EventTracer performanceTracer(StringBuilder sb) {
        return performanceTracer(s -> sb.append(s).append("\n"));
    }

    static EventTracer performanceTracer(StringWriter sw) {
        return performanceTracer(s -> sw.append(s).append("\n"));
    }

    static EventTracer performanceTracer(PrintStream stream) {
        return performanceTracer(stream::println);
    }

    static EventTracer performanceTracer(Consumer<String> logger) {
        return new EventTracer() {
            public int depth;

            @Override
            public TracerDepth enter(String name, String exit) {
                log(name);
                depth++;
                return new TracerDepth() {
                    boolean result = false;
                    final long nanos = System.nanoTime();

                    @Override
                    public void close() {
                        depth--;
                        long l = System.nanoTime() - nanos;
                        String base = String.format(exit, result);
                        if (l < 1_000_000) {
                            log("%s ~%.2f us", base, l / 1000D);
                        } else {
                            log("%s ~%.2f ms", base, l / 1_000_000D);
                        }
                    }

                    @Override
                    public void result(boolean b) {
                        result = b;
                    }
                };
            }

            @Override
            public void log(String s, Object... args) {
                logger.accept("\s\s".repeat(depth) + String.format(s, args));
            }

            @Override
            public void log(String s) {
                logger.accept("\s\s".repeat(depth) + s);
            }
        };
    }

    TracerDepth enter(String name, String exit);

    default TracerDepth enter(String name, Object o, String exit) {
        return enter(String.format(name, o), exit);
    }


    void log(String s, Object... args);

    void log(String s);

    default TimingScope timing(String s, Object... args) {
        return new TimingScope() {
            final long nanos = System.nanoTime();

            @Override
            public void close() {
                long l = System.nanoTime() - nanos;
                String base = String.format(s, args);
                if (l < 1_000_000) {
                    log("%s ~%.2f us", base, l / 1000D);
                } else {
                    log("%s ~%.2f ms", base, l / 1_000_000D);
                }
            }
        };
    }

    interface TimingScope extends AutoCloseable {
        @Override
        void close();
    }

    interface TracerDepth extends AutoCloseable {

        @Override
        void close();

        void result(boolean b);
    }

}
