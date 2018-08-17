package com.github.suka;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Block<S, D, F> {


    public static <S, D, F> RecoverBlock<S, D, F> performWithRecover(Function<S, ServiceResult<D, F>> f) {
        return new RecoverBlock<>(f, null);
    }

    public static <S, D, F> PerformWithRecoverBlock<S, D, F> performWithRecover() {
        return new PerformWithRecoverBlock<>();
    }

    public static <F> MultipleSource<F> multipleSource() {
        return new MultipleSource<>();
    }

    public static <F> ConditionalBlock<F> condition() {
        return new ConditionalBlock<>();
    }

    public static class PerformWithRecoverBlock<S, D, F> {
        public RecoverBlock<S, D, F> perform(Function<S, ServiceResult<D, F>> f) {
            return new RecoverBlock<>(f, null);
        }
    }

    @Getter(AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class RecoverBlock<S, D, F> implements Function<S, ServiceResult<D, F>>, MyList<RecoverBlock<S, D, F>> {
        private final Function<S, ServiceResult<D, F>> f;
        private final RecoverBlock<S, D, F> prev;

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

    public static class MultipleSource<F> {
        public <T> Entry1<T> source(Supplier<ServiceResult<T, F>> loadData) {
            return new Entry1<>(loadData);
        }

        @AllArgsConstructor
        @Getter
        public class Entry1<T1> {
            private final Supplier<ServiceResult<T1, F>> loadData1;

            public <NT> Entry2<NT> source(Supplier<ServiceResult<NT, F>> loadData) {
                return new Entry2<>(loadData);
            }

            @AllArgsConstructor
            @Getter
            public class Entry2<T2> {
                private final Supplier<ServiceResult<T2, F>> loadData2;

                public <NT> Entry3<NT> source(Supplier<ServiceResult<NT, F>> loadData) {
                    return new Entry3<>(loadData);
                }

                public <R> ServiceResult<R, F> combine(BiFunction<T1, T2, R> f) {
                    return loadData1.get()
                            .andThen(data1 ->
                                    loadData2.get()
                                            .andThen(data2 ->
                                                    ServiceResult.ok(f.apply(data1, data2))
                                            )
                            );

                }

                @AllArgsConstructor
                @Getter
                public class Entry3<T3> {
                    private final Supplier<ServiceResult<T3, F>> loadData3;
                }
            }
        }
    }

    public static class ConditionalBlock<F> {
        public <I, S> Entry<I, S, F> inCase(Function<I, Boolean> condition, Function<I, ServiceResult<S, F>> processor) {
            return new Entry<>(condition, processor, null);
        }

        @AllArgsConstructor
        @Getter(AccessLevel.PRIVATE)
        public static class Entry<I, S, F> implements Function<I, ServiceResult<S, F>>, MyList<Entry<I, S, F>> {
            private final Function<I, Boolean> condition;
            private final Function<I, ServiceResult<S, F>> processor;
            private final Entry<I, S, F> prev;

            public Entry<I, S, F> inCase(Function<I, Boolean> condition, Function<I, ServiceResult<S, F>> processor) {
                return new Entry<>(condition, processor, this);
            }

            public Function<I, ServiceResult<S, F>> otherwise(Function<I, ServiceResult<S, F>> processor) {
                return new Entry<>(ignore -> true, processor, this);
            }

            @Override
            public ServiceResult<S, F> apply(I input) {
                for (Entry<I, S, F> curr : toList()) {
                    if (curr.condition.apply(input)) {
                        return curr.processor.apply(input);
                    }
                }
                throw new RuntimeException("No condition is true");
            }
        }
    }

    private interface MyList<T extends MyList<T>> {
        T getPrev();

        default List<T> toList() {
            List<T> list = Stream.<T>iterate(
                    (T) this,
                    x -> x.getPrev() != null,
                    x -> x.getPrev())
                    .collect(Collectors.toList());
            Collections.reverse(list);
            return list;
        }
    }

}
