public class TestExtendAndFreeMemory extends UserlandProcess{

    private int amountToAllocate;
    private int dataToWrite;
    private String message;

    public TestExtendAndFreeMemory(int amountToAllocate, int dataToWrite, String message){
        this.amountToAllocate = amountToAllocate;
        this.dataToWrite = dataToWrite;
        this.message = message;
    }

    @Override
    public void run() { //This process tests extending memory, freeing memory in the middle, and then allocating to that gap in virtual memory
        int startingAddress = OS.AllocateMemory(amountToAllocate * 1024);
        System.out.println();
        System.out.println(message);
        System.out.println("Allocated "+ amountToAllocate + " pages of memory starting from virtual page #" + startingAddress/1024);
        System.out.println("Writing the number: "+dataToWrite + " to every page of its memory");
        for(int i = 0; i < amountToAllocate * 1024; i = i + 1024) {
            this.Write((startingAddress + i), (byte) dataToWrite);
        }
        System.out.println("Finished writing");
        int secondStartingAddress = OS.AllocateMemory(amountToAllocate * 1024);
        System.out.println("Allocated "+ amountToAllocate + " pages of memory starting from virtual page #" + secondStartingAddress/1024);
        System.out.println("Writing the number: "+dataToWrite + " to every page of the new memory");
        for(int i = 0; i < amountToAllocate * 1024; i = i + 1024) {
            this.Write((secondStartingAddress + i), (byte) dataToWrite);
        }
        System.out.println("Reading from all of its memory");
        for(int i = 0; i < amountToAllocate * 1024 * 2;  i = i + 1024) {
            this.Read((startingAddress + i));
        }
        System.out.println("Finished writing");
        OS.FreeMemory(startingAddress + 1 * 1024, 2 * 1024);
        System.out.println("Freeing 2 pages of memory starting from virtual page #"+(startingAddress + 1 * 1024)/1024);
        int thirdStartingAddress = OS.AllocateMemory(2 * 1024);
        System.out.println("Allocated 2 pages of memory starting from virtual page #" + thirdStartingAddress/1024);
        System.out.println("Writing the number: 6 to every page of the new memory");
        for(int i = 0; i < 2 * 1024; i = i + 1024) {
            this.Write((thirdStartingAddress + i), (byte) 6);
        }
        System.out.println("Reading from all of its memory");
        for(int i = 0; i < amountToAllocate * 1024 * 2;  i = i + 1024) {
            this.Read((startingAddress + i));
        }
    }
}
