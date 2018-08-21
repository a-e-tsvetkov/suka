package com.github.suka;


import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.function.Function.identity;

public class ServiceResult<S, F> {
    private final State state;
    private final S success;
    private final F failure;

    private ServiceResult(State state, S success, F failure) {
        this.state = state;
        this.success = success;
        this.failure = failure;
    }

    public static <S, F> ServiceResult<S, F> ok(S success) {
        return new ServiceResult<>(State.SUCCESS, success, null);
    }

    public static <F> ServiceResult<Void, F> ok() {
        return new ServiceResult<>(State.SUCCESS, null, null);
    }

    public static <S, F> ServiceResult<S, F> fail(F failure) {
        return new ServiceResult<>(State.FAILURE, null, failure);
    }

    public static <S, NS, F> Function<S, ServiceResult<NS, F>> lift(Function<S, NS> f) {
        return v -> ok(f.apply(v));
    }

    public ServiceResult<S, F> recover(Supplier<ServiceResult<S, F>> f) {
        return to(
                success -> this,
                failure -> f.get()
        );
    }

    public ServiceResult<S, F> recover(Function<F, ServiceResult<S, F>> f) {
        return to(
                success -> this,
                f
        );
    }

    private <T> T to(Function<S, T> s, Function<F, T> f) {
        switch (state) {
            case SUCCESS:
                return s.apply(success);
            case FAILURE:
                return f.apply(failure);
            default:
                throw new IllegalStateException();
        }
    }

    public <NF> ServiceResult<S, NF> mapFailure(Function<F, NF> f) {
        return to(
                ServiceResult::ok,
                failure -> fail(f.apply(failure))
        );
    }

    public <NS> ServiceResult<NS, F> map(Function<S, NS> f) {
        return to(
                success -> ok(f.apply(success)),
                ServiceResult::fail
        );
    }

    public <NS> ServiceResult<NS, F> andThen(Function<S, ServiceResult<NS, F>> f) {
        return to(
                f,
                ServiceResult::fail
        );
    }

    public ServiceResult<S, F> onSuccess(Consumer<S> f) {
        switch (state) {
            case SUCCESS:
                f.accept(success);
                break;
        }
        return this;
    }

    public ServiceResult<S, F> onFailure(Consumer<F> f) {
        switch (state) {
            case FAILURE:
                f.accept(failure);
                break;
        }
        return this;
    }

    public static <T> Boolean notNull(T t) {
        return t != null;
    }

    public boolean isSuccess() {
        return state == State.SUCCESS;
    }

    public boolean isFailure() {
        return state == State.FAILURE;
    }

    public F getFailure() {
        return to(
                success -> {
                    throw new IllegalStateException("Not failure");
                },
                identity()
        );
    }

    public S getSuccess() {
        return to(
                identity(),
                success -> {
                    throw new IllegalStateException("Not success");
                }
        );
    }

    private enum State {
        SUCCESS, FAILURE
    }
}
