package com.github.suka.par;

import com.github.suka.Block;
import com.github.suka.ServiceResult;
import com.github.suka.dt.T1;
import com.github.suka.dt.T2;
import com.github.suka.dt.T3;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class ParallelExecutor {

    private ExecutorService executorService;

    public ParallelExecutor() {
        executorService = Executors.newFixedThreadPool(3);
    }

    public static ParallelExecutor create() {
        return new ParallelExecutor();
    }

    public <I, S1, S2, S3, F>
    ServiceResult<T3<S1, S2, S3>, F>
    call(I i,
         Function<I, ServiceResult<S1, F>> f1,
         Function<I, ServiceResult<S2, F>> f2,
         Function<I, ServiceResult<S3, F>> f3) {
        return call(f1, f2, f3).apply(i);
    }

    public <I, S1, S2, F>
    ServiceResult<T2<S1, S2>, F>
    call(I i,
         Function<I, ServiceResult<S1, F>> f1,
         Function<I, ServiceResult<S2, F>> f2) {
        return call(f1, f2).apply(i);
    }

    public <I, S1, S2, S3, F>
    Block<I, T3<S1, S2, S3>, F>
    call(
            Function<I, ServiceResult<S1, F>> f1,
            Function<I, ServiceResult<S2, F>> f2,
            Function<I, ServiceResult<S3, F>> f3) {

        return i -> {
            try {
                List<Future<Object>> futures = executorService.invokeAll(Arrays.asList(
                        () -> f1.apply(i),
                        () -> f2.apply(i),
                        () -> f3.apply(i)
                ));
                ServiceResult<S1, F> sr1 = (ServiceResult<S1, F>) futures.get(0).get();
                ServiceResult<S2, F> sr2 = (ServiceResult<S2, F>) futures.get(0).get();
                ServiceResult<S3, F> sr3 = (ServiceResult<S3, F>) futures.get(0).get();
                return sr1.andAppend(T1::appender, ignore -> sr2)
                        .andAppend(T2::appender, ignore -> sr3);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interruption of threads is not supported", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("One of function trowed exception which is not supported", e);
            }
        };
    }

    public <I, S1, S2, F>
    Block<I, T2<S1, S2>, F>
    call(
            Function<I, ServiceResult<S1, F>> f1,
            Function<I, ServiceResult<S2, F>> f2) {

        return i -> {
            try {
                List<Future<Object>> futures = executorService.invokeAll(Arrays.asList(
                        () -> f1.apply(i),
                        () -> f2.apply(i)
                ));
                ServiceResult<S1, F> sr1 = (ServiceResult<S1, F>) futures.get(0).get();
                ServiceResult<S2, F> sr2 = (ServiceResult<S2, F>) futures.get(0).get();
                return sr1.andAppend(T1::appender, ignore -> sr2);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interruption of threads is not supported", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("One of function trowed exception which is not supported", e);
            }
        };
    }
}
