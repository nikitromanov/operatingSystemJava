public class TestFreeMemory extends UserlandProcess{


    private int amountToAllocate;
    private int dataToWrite;
    private String message;

    public TestFreeMemory(int amountToAllocate, int dataToWrite, String message){
        this.amountToAllocate = amountToAllocate;
        this.dataToWrite = dataToWrite;
        this.message = message;
    }

    @Override
    public void run() { //This process allocates and writes to memory, then frees that memory
        int startingAddress = OS.AllocateMemory(amountToAllocate * 1024);
        System.out.println();
        System.out.println(message+" writing the number: " + dataToWrite + " to every page of its memory");
        for(int i = 0; i < amountToAllocate * 1024;  i = i + 1024) {
            this.Write((startingAddress + i), (byte) dataToWrite);
            System.out.println("-----------------------------------------------------------------------------");
        }
        System.out.println(message);
        System.out.println("Finished writing");
        System.out.println("Freed memory: " + OS.FreeMemory(startingAddress, amountToAllocate * 1024));
    }
}
