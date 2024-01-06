public class TestSleep extends UserlandProcess {
    String message;
    int sleepFor;

    public TestSleep(String message, int sleepAmount){
        this.message = message;
        this.sleepFor = sleepAmount;
    }

    @Override
    public void run() {
        int i = 0;
        while (i < 10) {
            System.out.println(message);
            i++;
            if(i % 5 == 0){
                OS.Sleep(sleepFor);
            }
            try {
                Thread.sleep(50);
            } catch (Exception e) { System.out.println(e.getMessage());}
        }
    }
}
