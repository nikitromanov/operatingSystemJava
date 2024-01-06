public class TestRandomDevice extends UserlandProcess {
    private String message;
    private int deviceID;
    private int seed;

    public TestRandomDevice(String message, int seed) {
        this.message = message;
        this.deviceID = 0;
        this.seed = seed;
    }

    @Override
    public void run() {
        System.out.println();
        System.out.println(message);
        System.out.println("Testing RandomDevice Open");
        deviceID = OS.Open("random "+seed);
        System.out.print("Testing RandomDevice Read, generating/printing 10 random bytes:");
        byte[] readResult = OS.Read(deviceID, 10);
        for(int i = 0; i < readResult.length; i++){ //print out the bytes for testing purposes
            System.out.print("  "+readResult[i]);
        }
        System.out.println();
        OS.Sleep(100);
        System.out.println();
        System.out.println(message);
        System.out.print("Testing RandomDevice Seek, skipping over bytes:");
        OS.Seek(deviceID, 10);
        System.out.println();
        int writeResult = OS.Write(deviceID, readResult);
        System.out.println("Testing RandomDevice Write, the output should be 0 and it is: "+writeResult);
    }
}
