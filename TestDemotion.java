public class TestDemotion extends UserlandProcess{
    String message;

    public TestDemotion(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        int i = 0;
        while (i < 200) {
            System.out.println(message);
            i++;
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
