public class TestSwapFile extends UserlandProcess{
    private int input;
    private String message;

    public TestSwapFile(int input, String message){
        this.input = input;
        this.message = message;
    }

    @Override
    public void run() { //This test process fills up all the physical memory to allow for swap file testing
        int startingAddress = OS.AllocateMemory(100 * 1024);
        for(int i = 0; i < 100 * 1024 ; i = i + 1024){
            System.out.print(message);
            Write(startingAddress + i, (byte) input);
        }
        System.out.println("-----------------------------------------------------------------------------");
        OS.Sleep(300);
        for(int i = 0; i < 100 * 1024 ; i = i + 1024){
            System.out.print(message);
            Read(startingAddress + i);
        }
        System.out.println("-----------------------------------------------------------------------------");
        OS.Sleep(5000);
    }
}
