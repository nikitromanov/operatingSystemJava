public class TestAllocateMemory extends UserlandProcess{

    private int amountToAllocate;
    private int dataToWrite;
    private String message;

    public TestAllocateMemory(int amountToAllocate, int dataToWrite, String message){
        this.amountToAllocate = amountToAllocate;
        this.dataToWrite = dataToWrite;
        this.message = message;
    }

    @Override
    public void run() { //This process allocates memory and writes to it
        OS.Sleep(400);
        int startingAddress = OS.AllocateMemory(amountToAllocate * 1024);
        System.out.println();
        System.out.println(message);
        System.out.println("Writing the number: " + dataToWrite + " to all of its memory");
        for(int i = 0; i < amountToAllocate * 1024;  i = i + 1024) {
            this.Write((startingAddress + i), (byte) dataToWrite);
        }
        System.out.println("Finished writing");
    }
}
