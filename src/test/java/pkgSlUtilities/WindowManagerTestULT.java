package pkgSlUtilities;

public class WindowManagerTestULT {

    public static void main(String[] args) {
        testSingleton();
        System.out.println("WindowManager singleton test passed!");
    }

    private static void testSingleton() {
        SlWindowManager a = SlWindowManager.getInstance();
        SlWindowManager b = SlWindowManager.getInstance();
        assert a == b : "Singleton failed: instances are not the same";
    }
}
