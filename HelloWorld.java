import java.nio.charset.StandardCharsets;

public class HelloWorld extends UserlandProcess {
    @Override
    public void run() {
        while(true) {
            System.out.println("Hello World");
            try {
                OS.Sleep(50); // sleep the process for 50 ms
            } catch (Exception e) { }
        }
    }
}
