package withsuka;

import com.github.suka.Block;
import com.github.suka.ServiceResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

@SuppressWarnings("unused")
public class Client {
    private ServiceA serviceA = new ServiceA();
    private ServiceB serviceB = new ServiceB();
    private ServiceC serviceC = new ServiceC();
    private ServiceLog serviceLog = new ServiceLog();

    public void usageClassic() {
        ServiceResult<String, Void> data = serviceA.loadData();
        if (data.isFailure()) {
            data = serviceA.loadBackupData();
        }
        if (data.isFailure()) {
            serviceLog.logError("Unable to load");
        }
        ServiceResult<Integer, String> processedData = serviceB.processData(data.getSuccess());
        if (processedData.isFailure()) {
            serviceLog.logError(processedData.getFailure());
        }
        ServiceResult<Void, String> storeResult = serviceC.store(processedData.getSuccess());
        if (storeResult.isFailure()) {
            serviceLog.logError(storeResult.getFailure());
        }
    }

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
                .recover(ServiceResult.lift(serviceA::loadBackupOldStyle))
                //We have to provide error value
                .validate(ServiceResult::notNull, null)
                .mapFailure(ignore -> "Unable to load")
                .andThen(x -> serviceB.processData(x)
                        .recover(() -> serviceB.processDataIfFail(x)))
                .andThen(serviceC::store)
                .onFailure(serviceLog::logError);
    }

    public void usageIntegrationButMoreDeclarative() {
        serviceA.loadData()
                .mapFailure(ignore -> "Unable to load")
                .andThen(Block.<String, Integer, String>performWithRecover()
                        .perform(serviceB::processData)
                        .recover(serviceB::processDataIfFail))
                .andThen(serviceC::store)
                .onFailure(serviceLog::logError);
    }

    public void usageTwoSource() {
        class U {
            private ServiceResult<Tuple, Void> loadBoth() {
                ServiceResult<String, Void> data1 = serviceA.loadData();
                if (data1.isFailure()) {
                    return ServiceResult.fail(data1.getFailure());
                }
                ServiceResult<Double, Void> data2 = serviceA.loadData2();
                if (data2.isFailure()) {
                    return ServiceResult.fail(data2.getFailure());
                }
                return ServiceResult.ok(new Tuple(data1.getSuccess(), data2.getSuccess()));
            }
        }
        var u = new U();
        u.loadBoth()
                .mapFailure(ignore -> "Unable to load")
                .andThen(x -> serviceB.processMultipleData(x.data1, x.data2))
                .andThen(serviceC::store);
    }

    public void usageTwoSourceButItIsUgly() {
        serviceA.loadData()
                .andThen(data1 ->
                        serviceA.loadData2()
                                .andThen(data2 ->
                                        ServiceResult.ok(new Tuple(data1, data2)))
                )

                .mapFailure(ignore -> "Unable to load")
                .andThen(x -> serviceB.processMultipleData(x.data1, x.data2))
                .andThen(serviceC::store);
    }

    public void usageTwoSourceButNowImplementationIsUgly() {
        Block.<Void>multipleSource()
                .source(serviceA::loadData)
                .source(serviceA::loadData2)
                .combine(Tuple::new)
                .mapFailure(ignore -> "Unable to load")
                .andThen(x -> serviceB.processMultipleData(x.getData1(), x.getData2()))
                .andThen(serviceC::store);

        Block
                .MultipleSource<Void>
                .Entry1<String>
                .Entry2<Double> tmp
                =
                Block.<Void>multipleSource()
                        .source(serviceA::loadData)
                        .source(serviceA::loadData2);

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
                .andThen(Block.<String>condition()
                        .inCase(x -> x.startsWith("1"), serviceB::processData)
                        .inCase(x -> x.endsWith("2"), serviceB::processData)
                        .otherwise(serviceB::processData2)
                )
                .andThen(serviceC::store);
    }

    @AllArgsConstructor
    @Getter
    private static class Tuple {
        private final String data1;
        private final Double data2;
    }
}
