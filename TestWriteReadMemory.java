public class TestWriteReadMemory extends UserlandProcess {

    private int amountToAllocate;
    private int dataToWrite;
    private String message;
    private int sleepFor;

    public TestWriteReadMemory(int amountToAllocate, int dataToWrite, String message, int sleepFor){
        this.amountToAllocate = amountToAllocate;
        this.dataToWrite = dataToWrite;
        this.message = message;
        this.sleepFor = sleepFor;
    }

    @Override
    public void run() { //This test process writes to two memory addresses in each page that it allocates, it then reads from those pages
        int startingAddress = OS.AllocateMemory(amountToAllocate * 1024);
        System.out.println();
        System.out.println(message+" writing the value: "+dataToWrite + " to two addresses in every page of its memory");
        for(int i = 0; i < amountToAllocate * 1024; i = i + 512) {
            this.Write((startingAddress + i), (byte) dataToWrite);
            System.out.println("Writing the value: " + dataToWrite + " to virtual page #" + i/1024 +" and virtual address: " + i);
            System.out.println("-----------------------------------------------------------------------------");
        }
        System.out.println(message+" finished writing");
        OS.Sleep(sleepFor);
        System.out.println();
        System.out.println(message+" reading values from all of its memory");
        for(int i = 0; i < amountToAllocate * 1024;  i = i + 512) {
            System.out.println("Reading the value: " + this.Read((startingAddress + i)) + " from virtual page #" + i/1024 +" and virtual address: " + i);
            System.out.println("-----------------------------------------------------------------------------");
        }
        System.out.println(message+" finished reading");
        OS.FreeMemory(startingAddress, 4 * 1024);
    }


}
