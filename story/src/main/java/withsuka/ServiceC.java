package withsuka;

import com.github.suka.ServiceResult;

public class ServiceC {
    public ServiceResult<Void, String> store(Integer input){
        return ServiceResult.ok();
    }
}
