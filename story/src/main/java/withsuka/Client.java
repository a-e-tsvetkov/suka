package withsuka;

import com.github.suka.dt.T1;
import com.github.suka.dt.T2;
import com.github.suka.ext.*;

@SuppressWarnings("unused")
public class Client {
    private ServiceA serviceA = new ServiceA();
    private ServiceB serviceB = new ServiceB();
    private ServiceC serviceC = new ServiceC();
    private ServiceLog serviceLog = new ServiceLog();

    public void usage() {
        serviceA.loadData()
                .recover(serviceA::loadBackupData)
                .mapFailure(ignore -> "Unable to load")
                .andThen(serviceB::processData)
                .andThen(serviceC::store)
                .onFailure(serviceLog::logError);
    }

    public void usageIntegration() {
        serviceA.loadData()
                .recover(Integration.lift(serviceA::loadBackupOldStyle))
                //We have to provide error value
                .andThen(Validator.notNull(null))
                .mapFailure(ignore -> "Unable to load")
                .andThen(Integration.lift(serviceB::processDataOldStyle))
                .andThen(serviceC::store)
                .onFailure(serviceLog::logError);
    }

    public void usageTry() {
        serviceA.loadData()
                .mapFailure(ignore -> "Unable to load")
                .andThen(Try.of(serviceB::processDataOldStyle)
                        .mapFailure(Throwable::getMessage))
                .andThen(serviceC::store)
                .onFailure(serviceLog::logError);
    }

    public void usageWithRecover() {
        serviceA.loadData()
                .mapFailure(ignore -> "Unable to load")
                .andThen(DoWithRecovery.<String, Integer, String>newBlock()
                        .perform(serviceB::processData)
                        .recover(serviceB::processDataIfFail))
                .andThen(serviceC::store)
                .onFailure(serviceLog::logError);
    }

    public void usageTwoSource() {
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
    }

    public void usageTwoSourceBetter() {
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
    }

    public void usageWhatIf() {
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
    }

    public void usageWhatIfButWithDeclarativeStyle() {
        serviceA.loadData()
                .mapFailure(ignore -> "Unable to load")
                .andThen(SwitchBlock.<String>newBlock()
                        .inCase(x -> x.startsWith("1"), serviceB::processData)
                        .otherwise(serviceB::processData2)
                )
                .andThen(serviceC::store);
    }
}
