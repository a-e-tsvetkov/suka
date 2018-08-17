package plain;

public class Client {
    private ServiceA serviceA = new ServiceA();
    private ServiceB serviceB = new ServiceB();
    private ServiceC serviceC = new ServiceC();

    public void usage() {
        String data = serviceA.loadData();
        Integer processedData = serviceB.processData(data);
        serviceC.store(processedData);
    }

    public void orSimplier() {
        serviceC.store(
                serviceB.processData(
                        serviceA.loadData()
                )
        );
    }
}
