package com.github.suka.ext;

import com.github.suka.Block;
import com.github.suka.ServiceResult;

public class Validator {
    public static <S, F> Block<S, S, F> notNull(F failure) {
        return s -> {
            if (s == null) {
                return ServiceResult.fail(failure);
            } else {
                return ServiceResult.ok(s);
            }
        };
    }
}
