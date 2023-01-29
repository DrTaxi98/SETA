package debug;

public class Debug {

    public static final boolean SLEEP = false;

    public static void sleep() {
        if (SLEEP)
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}
