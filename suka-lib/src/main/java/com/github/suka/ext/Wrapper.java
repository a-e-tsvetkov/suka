package com.github.suka.ext;

import com.github.suka.Block;
import com.github.suka.ServiceResult;

import java.util.concurrent.Callable;

public class Wrapper {
    public static <D, S, F> Block<D, S, F> ignoreInput(Callable<ServiceResult<S, F>> f) {
        return ignore -> {
            try {
                return f.call();
            } catch (Exception e) {
                throw new RuntimeException("Function trowed exception which is not supported", e);
            }
        };
    }
}
