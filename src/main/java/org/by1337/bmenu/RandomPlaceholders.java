package org.by1337.bmenu;

import org.by1337.blib.chat.placeholder.Placeholder;

import java.util.Random;

public class RandomPlaceholders extends Placeholder {
    private static final RandomPlaceholders instance = new RandomPlaceholders();
    private final Random rand = new Random();

    private RandomPlaceholders() {
        registerPlaceholder("{rand_bool}", rand::nextBoolean);
        registerPlaceholder("{rand_10}", () -> rand.nextInt(10));
        registerPlaceholder("{rand_100}", () -> rand.nextInt(100));
        registerPlaceholder("{rand_1000}", () -> rand.nextInt(1000));
        registerPlaceholder("{rand_10000}", () -> rand.nextInt(10000));
        registerPlaceholder("{rand_100000}", () -> rand.nextInt(100000));
    }
    public static RandomPlaceholders getInstance() {
        return instance;
    }
}
