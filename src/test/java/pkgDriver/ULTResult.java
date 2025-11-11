package pkgDriver;


public class ULTResult {
    public static boolean returnTestResult(String testLabel, boolean testResult) {
        if (testResult) {
            System.out.printf("%10s: PASS\n", testLabel);
        } else {
            System.out.printf("%10s: FAIL\n", testLabel);
        }  //  if (testVar)
        return testResult;
    }  //  public static boolean returnTestResult(...)
}
