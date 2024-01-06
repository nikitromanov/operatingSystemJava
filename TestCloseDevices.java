public class TestCloseDevices extends UserlandProcess {

    private String message;
    public TestCloseDevices(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        System.out.println();
        System.out.println(message);
        System.out.println("Testing RandomDevice Open");
        int deviceIDRandom = OS.Open("random 10");
        System.out.print("Testing RandomDevice Read, generating/printing 10 random bytes:");
        OS.Read(deviceIDRandom, 10);
        byte[] readResult = OS.Read(deviceIDRandom, 10);
        for(int i = 0; i < readResult.length; i++){ //print out the bytes for testing purposes
            System.out.print("  "+readResult[i]);
        }
        System.out.println();
        OS.Close(deviceIDRandom);
        System.out.println("Testing FakeFileSystem Open, opening testingFile1");
        int file1 = OS.Open("file testingFile1.txt");
        byte[] fileReadResult = OS.Read(file1, 23);
        System.out.println("Testing FakeFileSystem Read, reading data from testingFile1.txt");
        OS.Close(file1);
        System.out.println("Testing FakeFileSystem Open, opening testingFile2");
        int file2 = OS.Open("file testingFile2.txt");
        OS.Seek(file2, 69);
        System.out.println("Testing FakeFileSystem Seek, jumping to the 69th byte in testingFile2");
        OS.Write(file1, fileReadResult);
        System.out.println("Testing FakeFileSystem Write, writing the data from testingFile1.txt to the end of testingFile2.txt");
        OS.Close(file1);
        System.out.println("Testing RandomDevice Open");
        int deviceIDRandom2 = OS.Open("random 10");
        OS.Close(deviceIDRandom2);
    }

}
