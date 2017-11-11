package me.momocow.chinesecraft.util;

import java.util.Objects;

@FunctionalInterface
public interface TriConsumer<S, T, U> {
	void accept(S var1, T var2, U var3);
	default TriConsumer<S, T, U> andThen(TriConsumer<? super S, ? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return (S s, T t, U u) -> { accept(s, t, u); after.accept(s, t, u); };
    }
}
