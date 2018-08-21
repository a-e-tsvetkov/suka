package com.github.suka.ext;

import com.github.suka.Block;
import com.github.suka.ServiceResult;

import java.util.function.Function;

public class Try {

    public static <T1, T2> Block<T1, T2, RuntimeException> of(Function<T1, T2> f) {
        return v -> {
            try {
                return ServiceResult.ok(f.apply(v));
            } catch (RuntimeException e) {
                return ServiceResult.fail(e);
            }
        };
    }
}
