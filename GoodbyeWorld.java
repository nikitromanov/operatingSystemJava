import java.nio.charset.StandardCharsets;

public class GoodbyeWorld extends UserlandProcess {
    @Override
    public void run() {
        while(true) {
            System.out.println("Goodbye World");
            try {
                OS.Sleep(50); // sleep the process for 50 ms
            } catch (Exception e) { }
        }
    }
}
