package debug;

public class Debug {

    private static final boolean SLEEP = false;
    private static final long MILLIS = 5000;

    public static void sleep() {
        if (SLEEP)
            try {
                Thread.sleep(MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}
