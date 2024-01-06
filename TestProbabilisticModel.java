public class TestProbabilisticModel extends UserlandProcess {

    String message;

    public TestProbabilisticModel(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        int i = 0;
        while (i < 10) {
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

