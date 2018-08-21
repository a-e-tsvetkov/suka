package com.github.suka.ext;

import com.github.suka.Block;
import com.github.suka.ServiceResult;

import java.util.function.Function;
import java.util.function.Supplier;

public class Integration {
    public static <S, F> Supplier<ServiceResult<S, F>> lift(Supplier<S> f) {
        return () -> ServiceResult.ok(f.get());
    }

    public static <T1, T2, F> Block<T1, T2, F> lift(Function<T1, T2> f) {
        return v -> ServiceResult.ok(f.apply(v));
    }
}
