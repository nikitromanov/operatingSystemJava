public class TestDeviceOverflow extends UserlandProcess {
    private String message;
    public TestDeviceOverflow(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        System.out.println();
        System.out.println(message);
        OS.Sleep(4000);
        System.out.println();
        System.out.println("Creating 11 RandomDevices to check for error return");
        OS.Open("random 10"); //create 11 devices to overflow the RandomDevice array
        OS.Open("random 10");
        OS.Open("random 10");
        OS.Open("random 10");
        OS.Open("random 10");
        OS.Open("random 10");
        OS.Open("random 10");
        OS.Open("random 10");
        OS.Open("random 10");
        OS.Open("random 10");
        System.out.println();
        System.out.println("After overflowing the RandomDevice data array, it returns: "+OS.Open("random 10"));



    }

}
