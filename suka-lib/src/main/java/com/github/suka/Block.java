package com.github.suka;

import java.util.function.Function;

public interface Block<S, D, F> extends Function<S, ServiceResult<D, F>> {
    default <ND> Block<S, ND, F> andThen(Block<D, ND, F> block) {
        return v -> this.apply(v).andThen(block);
    }

    default <NF> Block<S, D, NF> mapFailure(Function<F, NF> f) {
        return v -> this.apply(v).mapFailure(f);
    }
}
