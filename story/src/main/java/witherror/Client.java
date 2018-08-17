package witherror;

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
