package withsuka;

import com.github.suka.ServiceResult;

public class ServiceB {
    public ServiceResult<Integer, String> processData(String input) {
        try {
            int result = Integer.parseInt(input);
            return ServiceResult.ok(result);
        } catch (NumberFormatException ignore) {
            return ServiceResult.fail("Unable to process");
        }
    }

    public ServiceResult<Integer, String> processData2(String input) {
        try {
            int result = Integer.parseInt(input.replace("0", "9"));
            return ServiceResult.ok(result);
        } catch (NumberFormatException ignore) {
            return ServiceResult.fail("Unable to process");
        }
    }

    public ServiceResult<Integer, String> processDataIfFail(String input) {
        try {
            int result = Integer.parseInt(input.replaceAll("\\D", ""));
            return ServiceResult.ok(result);
        } catch (NumberFormatException ignore) {
            return ServiceResult.fail("Unable to process");
        }
    }

    public ServiceResult<Integer, String> processMultipleData(String input1, Double input2) {
        try {
            Double result = Integer.parseInt(input1) * input2;
            return ServiceResult.ok(result.intValue());
        } catch (NumberFormatException ignore) {
            return ServiceResult.fail("Unable to process");
        }
    }
}
