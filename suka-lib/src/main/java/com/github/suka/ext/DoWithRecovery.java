package com.github.suka.ext;

import com.github.suka.Block;
import com.github.suka.ServiceResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

public class DoWithRecovery {
    public static <S, D, F> PerformWithRecoverBlock<S, D, F> newBlock() {
        return new PerformWithRecoverBlock<>();
    }

    public static class PerformWithRecoverBlock<S, D, F> {
        public RecoverBlock<S, D, F> perform(Function<S, ServiceResult<D, F>> f) {
            return new RecoverBlock<>(f, null);
        }
    }

    @Getter(AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class RecoverBlock<S, D, F> extends MyList<RecoverBlock<S, D, F>> implements Block<S, D, F> {
        private final Function<S, ServiceResult<D, F>> f;
        private final RecoverBlock<S, D, F> prev;

        @Override
        RecoverBlock<S, D, F> getThis() {
            return this;
        }

        public RecoverBlock<S, D, F> recover(Function<S, ServiceResult<D, F>> f) {
            return new RecoverBlock<>(f, this);
        }

        @Override
        public ServiceResult<D, F> apply(S input) {
            List<RecoverBlock<S, D, F>> blocks = toList();
            for (int i = 0; i < blocks.size() - 1; i++) {
                RecoverBlock<S, D, F> block = blocks.get(i);
                ServiceResult<D, F> result = block.f.apply(input);
                if (result.isSuccess()) {
                    return result;
                }
            }
            return blocks.get(blocks.size() - 1).getF().apply(input);
        }
    }
}
