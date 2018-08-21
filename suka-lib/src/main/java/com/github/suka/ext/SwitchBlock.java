package com.github.suka.ext;

import com.github.suka.Block;
import com.github.suka.ServiceResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

public class SwitchBlock {

    public static <F> ConditionalBlock<F> newBlock() {
        return new ConditionalBlock<>();
    }

    public static class ConditionalBlock<F> {
        public <I, S> ConditionalBlock.Entry<I, S, F> inCase(Function<I, Boolean> condition, Function<I, ServiceResult<S, F>> processor) {
            return new ConditionalBlock.Entry<>(condition, processor, null);
        }

        @AllArgsConstructor
        @Getter(AccessLevel.PROTECTED)
        public static class Entry<I, S, F> extends MyList<ConditionalBlock.Entry<I, S, F>> implements Block<I, S, F> {
            private final Function<I, Boolean> condition;
            private final Function<I, ServiceResult<S, F>> processor;
            private final ConditionalBlock.Entry<I, S, F> prev;

            @Override
            Entry<I, S, F> getThis() {
                return this;
            }

            public ConditionalBlock.Entry<I, S, F> inCase(Function<I, Boolean> condition, Function<I, ServiceResult<S, F>> processor) {
                return new ConditionalBlock.Entry<>(condition, processor, this);
            }

            public Function<I, ServiceResult<S, F>> otherwise(Function<I, ServiceResult<S, F>> processor) {
                return new ConditionalBlock.Entry<>(ignore -> true, processor, this);
            }

            @Override
            public ServiceResult<S, F> apply(I input) {
                for (ConditionalBlock.Entry<I, S, F> curr : toList()) {
                    if (curr.condition.apply(input)) {
                        return curr.processor.apply(input);
                    }
                }
                throw new RuntimeException("No condition is true");
            }
        }
    }
}
