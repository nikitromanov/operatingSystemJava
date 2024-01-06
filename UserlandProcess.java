import java.util.Random;

public abstract class UserlandProcess implements Runnable {

    private static byte[] memory;
    private static int[][] tlb;

    public static void Initialize() {
        tlb = new int[2][2];
        for (int i = 0; i < tlb.length; i++) {
            tlb[i][0] = -1;
            tlb[i][1] = -1;
        }
        memory = new byte[1024 * 1024]; // memory of size 1,048,576 bytes
    }

    public byte Read(int virtualAddress) {
        int physicalAddress = getPhysicalAddress(virtualAddress); //get the physical address using the virtual address
        byte value = memory[physicalAddress]; //look inside memory using the physical address and return the corresponding byte
        System.out.println("Reading the value " + ((int) value) + " from physical page #"+physicalAddress/1024 + " and physical address: " + physicalAddress);
        return value;
    }

    public void Write(int virtualAddress, byte value) {
        int physicalAddress = getPhysicalAddress(virtualAddress); //get the physical address using the virtual address
        System.out.println("Writing the value " + value + " to physical page #"+physicalAddress/1024 + " and physical address: " + physicalAddress);
        memory[physicalAddress] = value; //write the byte to the memory at the physical address
    }

    public int getPhysicalAddress(int virtualAddress){
        int virtualPage = virtualAddress / 1024; //get the virtual page number by diving the virtual address by 1024 since there are 1024 bytes per page
        int physicalAddress = searchTlb(virtualPage, virtualAddress); //search the tlb for that virtual page, to get the physical page and convert it to physical address
        if(physicalAddress == -1){ //if the physical address is -1, meaning it was not found in the tlb
            OS.GetMapping(virtualPage); //call GetMapping to randomly fill one of the tlb entries with the needed mapping
            physicalAddress = searchTlb(virtualPage, virtualAddress); //search the tlb once more
        }
        return physicalAddress; //return the physical address
    }

    public int searchTlb(int virtualPage, int virtualAddress){
        for(int i = 0; i < tlb.length; i++){ //for each entry in the tlb
            if(virtualPage == tlb[i][0]){ //if the virtual page needed is found in the tlb
                int physicalPage = tlb[i][1]; //get the corresponding physical page
                int physicalAddress = physicalPage * 1024 + (virtualAddress % 1024); //convert the physical page to the exact physical address
                return physicalAddress;
            }
        }
        return -1; //if the virtual page number to physical page number mapping was not found, return -1
    }

    public static void setTlbEntry(int virtualPageNumber, int physicalPageNumber) {
        Random random = new Random();
        int randomTlbIndex = random.nextInt(2); //generate a random number between 0 and 1
        tlb[randomTlbIndex][0] = virtualPageNumber; //set the virtual and physical page numbers at that entry
        tlb[randomTlbIndex][1] = physicalPageNumber;
    }

    public static void clearTlb() {
        for(int i = 0; i < tlb.length; i++){ //set both virtual and physical page entries to -1, clearing the tlb
            tlb[i][0] = -1;
            tlb[i][1] = -1;
        }
    }

    public static byte[] getMemory(){
        return memory;
    }

}
