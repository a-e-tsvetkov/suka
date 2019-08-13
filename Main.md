# Divide and conquer

#### Why we are here?

Most of the code in our applications is following one pattern.
It invoke service to load data, process it and store results.
Maybe save some audit or logging information.

Lets start with example code:

```java
public class Client {
    private ServiceA serviceA = new ServiceA();
    private ServiceB serviceB = new ServiceB();
    private ServiceC serviceC = new ServiceC();

    public void usage() {
        String data = serviceA.loadData();
        Integer processedData = serviceB.processData(data);
        serviceC.store(processedData);
    }
}
```


This code works fine until we realize that one of services may fail. Usually this situation is handled this by throwing exception.

If you never need to handle errors or add audit you can stop reading here, folowing article will not be usefull for you.

In case your want to see my solution or just curiouse welcome aboard.

#### No exception

While exceptions definitely do the job they have some disadvantages:
 * They did not checked by compiler unless it checked exception
 * No one like checked exception :) Really, you can rarely see them in popular libraries (except JDK, but they have to use them because they invent them)
 * You can't use generics as exception, so you can't write generic code for handling and transforming
 * `try ... catch` have too verbose syntax
 
 So I decide to find a way to be able to signal about failure without using exceptions. 

#### Good old `if(...){return;}`

Simplest way to achieve desired behaviour will be handle each possible failure.

```java
public class Client {
    private ServiceA serviceA = new ServiceA();
    private ServiceB serviceB = new ServiceB();
    private ServiceC serviceC = new ServiceC();
    private ServiceLog serviceLog = new ServiceLog();

    public void usage(){
        String data = serviceA.loadData();
        if(data == null) {
            data = serviceA.loadBackupData();
        }
        if(data == null){
            serviceLog.logError("Unable to load");
            return;
        }
        Integer processedData = serviceB.processData(data);
        if(processedData == null){
            serviceLog.logError("Unable to process");
            return;
        }
        boolean result = serviceC.store(processedData);
        if(!result) {
            serviceLog.logError("Unable to save");
        }
    }
}
```

I slightly extended our business logic by adding fallback to second data source and explicit auditing of errors.

As you can see from 17 line of code only 4 is buseness logic and 3 is audit.
How can we improve that and get rid of boiler plate code?

#### Extracting common code

This code constantly repeats same pattern:

```java
Result result = service();
if(result == null) {
    return;
}
otherService(result);
```  

Lets create method that can do it for us:

```java
    <T1, T2, T3> T3 combine(T1 input, Function<T1, T2> f1, Function<T2, T3> f2) {
        T2 value1 = f1.apply(input);
        if (value1 != null) {
            return f2.apply(value1);
        } else {
            return null;
        }
    }
```

Now we can use it following way:

```java
        combine(
                1,
                x -> service1(x),
                x -> service2(x)
        );
```

Looks better, it will return `null` if any of service1 or service2 return `null`.
Unfortunately if we have more then two service in our chain we get ugly results:

```java
        combine(
                1,
                x -> service1(x),
                x -> combine(x,
                        y -> service2(y),
                        y -> service3(y)
                )
        );
```

Another problem with this approach is that we can't have any error details.

#### Wrap it

Lets start from later problem.

Solution is trivial. We have to wrap result of our service call to class:

```java
    @AllArgsConstructor
    public class ServiceResult<S, F> {
        private final State state;
        private final S success;
        private final F failure;
        public enum State {
            SUCCESS, FAILURE
        }
    }

```

*NOTE*: Here I use annotation `@AllArgsConstructor` from project lombok.

For convenience I added two factory methods that create successful and unsuccessful results.

```java
        public static <S, F> ServiceResult<S, F> ok(S s) {
            return new ServiceResult<>(State.SUCCESS, s, null);
        }

        public static <S, F> ServiceResult<S, F> fail(F f) {
            return new ServiceResult<>(State.SUCCESS, null, f);
        }

```

#### Doing business

Our business code is actually algorithm which can be expressed in next sentence: "Do service1 and then service2 and then service3".
Of course we imply that if service method fails we do not want to proceed, but it is so obvious that we do not want to explain it every time.

Lets try put this in code. 

```java
        service1_v2(1)
          .andThen(x -> service2_v2(x))
          .andThen(x -> service3_v2(x));

```

`andThen` is just our `combine` method, but now it is instance method in `ServiceResult`.

```java
        public <NS> ServiceResult<NS, F> andThen(Function<S, ServiceResult<NS, F>> f) {
            if (state == State.SUCCESS) {
                return f.apply(success);
            } else {
                return fail(failure);
            }
        }
```

Our service methods defined in the following way:

```java
    ServiceResult<String, Err> service1_v2(Integer i);

    ServiceResult<Double, Err> service2_v2(String i);

    ServiceResult<Date, Err> service3_v2(Double i);

```

where `Err` is enum `enum Err {ERROR_CODE1}`, but it can be be more complex and contain any specific details like message, timestamp, service name etc.

Please note that in `andThen` method type for failure of both service should be same, otherwise we will need to convert first failure into second one to return from method.

#### That's all folks

Now we can combine services and do not worry about errors.

Unfortunately this simple class is not enough to have all case covered.
Limitation of java don't always allow us to have nice syntax when we use this approach.

#### Some extra stuff

I added some extra methods for convenience:
 * `recover` accept function that will be called if state is `FAILURE`, result of said function will be returned
 * `to` accept two function which map success and failure to single type
 * `map` and `mapFailure` transforms success and failure
 * `onSuccess` and `onFailure` both consume callback functions which will be called for corresponding states
 
#### More fun stuff

Here some extensions that I add from my experience.
They doesn't modify `ServiceResult` so you can modify them or create your own.

But Before we look into utils I want to introduce one more helper.

`andThen` allow us to combine our service call, but some time we may want to group that calls.
So I introduce interface called `Block`

```java
interface Block<S, D, F> extends Function<S, ServiceResult<D,F>> {}
```

it simplify signature of many methods and allow to combine two blocks into one with another `andThen` method.

```java
    default <ND> Block<S, ND, F> andThen(Block<D, ND, F> block) {
        return v -> this.apply(v).andThen(block);
    }
```

also it have method for changing failure type

```java
    default <NF> Block<S, D, NF> mapFailure(Function<F, NF> f) {
        return v -> this.apply(v).mapFailure(f);
    }
```


###### Integration

* `lift` accept function and return function that ready to be used with `andThen`

You can find example in `withsuka.Client#usageIntegration`

###### Try

* `Try.of` run supplied method and catch `RuntimeException`

You can find example in `withsuka.Client#usageTry`

###### DoWithRecovery

* `DoWithRecovery.<FROM, TO, FAILURE>newBlock()` create block that can perform some action and have one or more recovery actions.

You can find example in `withsuka.Client#usageWithRecover`

###### SwitchBlock

This one is questionable, but I add it to show one of extra option.

In case we have code like this:

```java
        serviceA.loadData()
                .mapFailure(ignore -> "Unable to load")
                .andThen(x -> {
                    if (x.startsWith("1")) {
                        return serviceB.processData(x);
                    } else {
                        return serviceB.processData2(x);
                    }
                })
                .andThen(serviceC::store);

```

we can rewrite it with `SwitchBlock`

```java
        serviceA.loadData()
                .mapFailure(ignore -> "Unable to load")
                .andThen(SwitchBlock.<String>newBlock()
                        .inCase(x -> x.startsWith("1"), serviceB::processData)
                        .otherwise(serviceB::processData2)
                )
                .andThen(serviceC::store);

```

New code doesn't simplify it much, but allow to combine this blocks.

Examples in:
 * `withsuka.Client#usageWhatIf`
 * `withsuka.Client#usageWhatIfButWithDeclarativeStyle`

#### What if you need to casll several services and combine result

I have handy set of tupple classes for this case.

You can use them following way:

```java
        var t1 = T1.of(1);
        var t2 = t1.and("2");
        var t3 = t2.and(3.0);
```

For example if we need to read two source before processing:

```java
        serviceA.loadData()
                .map(T1::of)
                .andThen(data1 ->
                        serviceA.loadData2()
                                .map(data1::and)
                )
                .andThen(data1 ->
                        serviceA.loadData3()
                                .map(data1::and)
                )
                .mapFailure(ignore -> "Unable to load")
                .andThen(x -> serviceB.processMultipleData(x.get_1(), x.get_2()))
                .andThen(serviceC::store);

```

Or with new `andAppend` method you can do it even simpler

```java
        serviceA.loadData()
                .andAppend(
                        T1::appender,
                        x -> serviceA.loadData2()
                )
                .andAppend(
                        T2::appender,
                        data1 -> serviceA.loadData3()
                )
                .mapFailure(ignore -> "Unable to load")
                .andThen(x -> serviceB.processMultipleData(x.get_1(), x.get_2()))
                .andThen(serviceC::store);
```

*NOTE*: you have to specify proper appender (now I miss implicit parameters from Scala).

#### Conclusion

While this library is not production quality and not intended to be used in real life it shows one interesting approach to write business logic.
You can borrow some idea or at least have liitle more practice with generics, lambdas and functional approach.
