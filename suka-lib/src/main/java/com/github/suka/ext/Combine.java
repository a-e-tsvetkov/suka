package com.github.suka.ext;

import com.github.suka.Block;
import com.github.suka.ServiceResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class Combine {

    public static <F> MultipleSource<F> multipleSource() {
        return new MultipleSource<>();
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
}
