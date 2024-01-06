public class TestFakeFileSystem extends UserlandProcess{
    private String message;
    private int seekAmount;
    public TestFakeFileSystem(String message, int seekAmount) {
        this.message = message;
        this.seekAmount = seekAmount;
    }

    @Override
    public void run() {
        System.out.println();
        System.out.println(message);
        System.out.println("Testing FakeFileSystem Open, opening testingFile1");
        int file1 = OS.Open("file testingFile1.txt");
        System.out.println("Testing FakeFileSystem Open, opening testingFile2");
        int file2 = OS.Open("file testingFile2.txt");
        OS.Sleep(100);
        System.out.println();
        System.out.println(message);
        byte[] fileReadResult = OS.Read(file1, 23);
        System.out.println("Testing FakeFileSystem Read, reading data from testingFile1.txt");
        OS.Seek(file2, seekAmount);
        System.out.println("Testing FakeFileSystem Seek, jumping to the "+seekAmount+" byte in testingFile2");
        OS.Write(file2, fileReadResult);
        System.out.println("Testing FakeFileSystem Write, writing the data to testingFile2.txt");
    }
}
