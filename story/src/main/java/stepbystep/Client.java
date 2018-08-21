package stepbystep;

import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.function.Function;

public abstract class Client {
    abstract String service1(Integer i);

    abstract Double service2(String i);

    abstract Date service3(Double i);

    <T1, T2, T3> T3 combine1(T1 input, Function<T1, T2> f1, Function<T2, T3> f2) {
        T2 value1 = f1.apply(input);
        if (value1 != null) {
            return f2.apply(value1);
        } else {
            return null;
        }
    }

    void use1() {
        combine1(
                1,
                x -> service1(x),
                x -> service2(x)
        );
    }

    void use1_with3call() {
        combine1(
                1,
                x -> service1(x),
                x -> combine1(x,
                        y -> service2(y),
                        y -> service3(y)
                )
        );
    }

    @AllArgsConstructor
    static class ServiceResult<S, F> {
        private final State state;
        private final S success;
        private final F failure;

        public enum State {
            SUCCESS, FAILURE
        }

        public static <S, F> ServiceResult<S, F> ok(S s) {
            return new ServiceResult<>(State.SUCCESS, s, null);
        }

        public static <S, F> ServiceResult<S, F> fail(F f) {
            return new ServiceResult<>(State.SUCCESS, null, f);
        }

        public <NS> ServiceResult<NS, F> andThen(Function<S, ServiceResult<NS, F>> f) {
            if (state == State.SUCCESS) {
                return f.apply(success);
            } else {
                return fail(failure);
            }
        }

    }

    void use2() {
        service1_v2(1)
                .andThen(x -> service2_v2(x))
                .andThen(x -> service3_v2(x));
    }

    abstract ServiceResult<String, Err> service1_v2(Integer i);

    abstract ServiceResult<Double, Err> service2_v2(String i);

    abstract ServiceResult<Date, Err> service3_v2(Double i);

    enum Err {ERROR_CODE1}

}
