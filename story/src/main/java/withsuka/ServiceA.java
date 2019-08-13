package withsuka;

import com.github.suka.ServiceResult;

public class ServiceA {
    public ServiceResult<String, Void> loadData() {
        return ServiceResult.ok("data");
    }

    public ServiceResult<Double, Void> loadData2() {
        return ServiceResult.ok(42.0);
    }

    public ServiceResult<Integer, Void> loadData3() {
        return ServiceResult.ok(42);
    }

    public ServiceResult<String, Void>  loadBackupData() {
        return ServiceResult.ok("backup data");
    }

    public String loadBackupOldStyle() {
        return "backup data";
    }
}
