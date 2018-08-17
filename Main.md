**Example of usages**

Most of the code in our applications is following one pattern.
It invoke some service to load data, process it and store results.
Some time it save som audit or logging information.

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


This code works fine until we realize that some service may fail. Usual way to handle this situation is to throw exception.

If you never need to handle errors or add logging audit you can stop reading here folowing article will not be usefull for you.

In case your want to see my solution or just curiouse welcome aboard.

**Good old `if(...){return;}`**

Simplest way to achieve desired brhaviour will be handle each possible failure.

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

I slightly extended our busness logic by adding fallback to second data source and explicit auditing of errors.

As you can see from 17 line of code only 4 is buseness logic and 3 is audit.
How can we improve that and get rid of boiler plate code?

**Extracting common code**

All this code is full of same pattern:

```java
Result result = service();
if(result == null) {
    return;
}
otherService(result);
```  

Lets create method that can do it for us:


void combine(Function<)
