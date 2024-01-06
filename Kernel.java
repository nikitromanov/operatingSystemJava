public class Kernel implements Device {

    private Scheduler scheduler;
    private VFS vfs;
    private boolean[] inUsePhysicalPageList; //keep track of free pages in physical memory
    private int swapFile;
    private int nextDiskPageNumber;

    public Kernel() {
        scheduler = new Scheduler(this);
        vfs = new VFS();
        inUsePhysicalPageList = new boolean[1024];
        for (int i = 0; i < inUsePhysicalPageList.length; i++) {
            inUsePhysicalPageList[i] = false;
        }
        swapFile = vfs.getDeviceLookup("file").Open("swapFile.txt"); //create the swap file using fakeFileSystem
        nextDiskPageNumber = 0; //keep track of the next disk page number
    }

    public int CreateProcess(UserlandProcess up, KernelLandProcess.Priority priority) {
        return scheduler.CreateProcess(up, priority); //calls the schedulers CreateProcess
    } //call scheduler to create a process

    public void Sleep(int milliseconds) {
        scheduler.Sleep(milliseconds);
    }

    @Override
    public int Open(String deviceString) {
        KernelLandProcess runningProcess = scheduler.GetCurrentlyRunning();
        int[] openDevices = runningProcess.getOpenDevices(); //get the array of KernelLandProcess
        int emptyIndex = -1;
        for (int i = 0; i < openDevices.length; i++) { //find an empty index in the KernelLandProcess aray
            if (openDevices[i] == -1) {
                emptyIndex = i;
                break;
            }
        }
        if (emptyIndex == -1) { //if there is no empty index, return -1
            return -1;
        }
        int vfsIndex = vfs.Open(deviceString);
        if (vfsIndex == -1) { //call VFS open(), if the result is -1, there is no empty index in VFS so return -1
            return -1;
        }
        runningProcess.setOpenDevices(emptyIndex, vfsIndex); //if there is an empty index in VFS, set the empty KernelLandProcess index to the VFS ID
        System.out.println("The empty index found in KernelLandProcess array is: " + emptyIndex + ", ID " + vfsIndex + " was placed inside it");
        return emptyIndex; //return the index of the new Device to the process that called Open()
    }

    @Override
    public void Close(int id) {
        KernelLandProcess runningProcess = scheduler.GetCurrentlyRunning();
        int[] openDevices = runningProcess.getOpenDevices(); //get the array of KernelLandProcess
        System.out.println("Closing KernelLandProcess Device with ID: " + id);
        vfs.Close(openDevices[id]); //get the VFS ID that is in the KernelLandProcess index passed in, call VFS Close() with the ID stored
        runningProcess.setOpenDevices(id, -1);
        ; //set the index in KernelLandProcess to -1, clearing it
    }

    @Override
    public byte[] Read(int id, int size) {
        KernelLandProcess runningProcess = scheduler.GetCurrentlyRunning();
        int[] openDevices = runningProcess.getOpenDevices(); //get the array of KernelLandProcess
        return vfs.Read(openDevices[id], size); //call VFS Read() with the VFS ID stored in the KernelLandProcess index passed in
    }

    @Override
    public void Seek(int id, int to) {
        KernelLandProcess runningProcess = scheduler.GetCurrentlyRunning();
        int[] openDevices = runningProcess.getOpenDevices(); //get the array of KernelLandProcess
        vfs.Seek(openDevices[id], to); //call VFS Seek() with the VFS ID stored in the KernelLandProcess index passed in
    }

    @Override
    public int Write(int id, byte[] data) {
        KernelLandProcess runningProcess = scheduler.GetCurrentlyRunning();
        int[] openDevices = runningProcess.getOpenDevices(); //get the array of KernelLandProcess
        return vfs.Write(openDevices[id], data); //call VFS Write() with the VFS ID stored in the KernelLandProcess index passed in
    }

    public int GetPid() {
        return scheduler.GetPid(); //call schedulers GetPid
    }

    public int GetPidByName(String name) {
        return scheduler.GetPidByName(name); //call schedulers GetPidByName
    }

    public void SendMessage(KernelMessage originalMessage) {
        KernelMessage messageCopy = new KernelMessage(originalMessage); //create a copy of the original message
        messageCopy.setSenderPid(GetPid()); //set the sender pid of the message copy for security reasons
        KernelLandProcess targetProcess = scheduler.GetPidToProcess(messageCopy.getTargetPid()); //get the KernelLandProcess referenced by the target pid
        targetProcess.addMessage(messageCopy); //add the message copy to the message queue of the target KernelLandProcess
        if (scheduler.CheckIfWaiting(targetProcess)) { //check if the targetProcess is waiting for a message
            scheduler.FinishWaiting(targetProcess); //put the waiting process onto the correct runnable queue if it is waiting
        }
    }

    public KernelMessage WaitForMessage() {
        return scheduler.DecideWaitAction(); //the scheduler performs all functionality, it will either immediately return a message or place the process on wait queue
    }

    public void GetMapping(int virtualPageNumber) {
        KernelLandProcess runningProcess = scheduler.GetCurrentlyRunning();
        byte[] physicalMemory = UserlandProcess.getMemory();
        Device fakeFileSystem = vfs.getDeviceLookup("file"); //reference to the fakeFileSystem
        VirtualToPhysicalMapping runningProcessPhysicalMapping = runningProcess.getMemoryMap()[virtualPageNumber];
        if (runningProcessPhysicalMapping == null) { //if the virtual to physical page mapping was not found, kill the process trying to access foreign memory
            scheduler.KillCurrentProcess();
        }
        if (runningProcessPhysicalMapping.physicalPageNumber == -1) { //if the physicalPageNumber is -1, first attempt to allocate physical memory
            for (int i = 0; i < inUsePhysicalPageList.length; i++) { //iterate through the physical page list and attempt to find a free physical page
                if (inUsePhysicalPageList[i] == false) {
                    runningProcessPhysicalMapping.physicalPageNumber = i;
                    inUsePhysicalPageList[i] = true; //if a free physical page is found, allocate it to the current process and mark it as not free
                    break;
                }
            }
            if (runningProcessPhysicalMapping.physicalPageNumber == -1) { //if no free physical pages are found, proceed to borrow physical memory from an existing process
                while (runningProcessPhysicalMapping.physicalPageNumber == -1) {
                    KernelLandProcess victimProcess = scheduler.getRandomProcess(); //get a random process from the list of processes
                    VirtualToPhysicalMapping[] victimProcessMemory = victimProcess.getMemoryMap(); //get its memory map
                    for (int i = 0; i < victimProcessMemory.length; i++) { //iterate through the victim process memory map find a page that has physical memory allocated
                        VirtualToPhysicalMapping victimProcessMemoryMapping = victimProcessMemory[i];
                        if (victimProcessMemoryMapping != null) {
                            if (victimProcessMemoryMapping.physicalPageNumber != -1) { // if a page with physical memory is found
                                int borrowedPhysicalPageNumber = victimProcessMemoryMapping.physicalPageNumber;
                                if (victimProcessMemoryMapping.diskPageNumber == -1) { //if the victim process does not have a disk page number, assign it one
                                    victimProcessMemoryMapping.diskPageNumber = nextDiskPageNumber;
                                    nextDiskPageNumber++; //increment the next disk page number to be 1024 bytes higher than the newly assigned one
                                }
                                byte[] victimProcessData = new byte[1024];
                                int byteCount = 0;
                                for (int j = borrowedPhysicalPageNumber * 1024; j < (borrowedPhysicalPageNumber * 1024 + 1024); j++) { //store the contents of the borrowed physical page to a byte array
                                    victimProcessData[byteCount] = physicalMemory[j];
                                    byteCount++;
                                }
                                fakeFileSystem.Seek(swapFile, victimProcessMemoryMapping.diskPageNumber * 1024); //move to the correct page in the swap file (the disk page number assigned to the victim process)
                                fakeFileSystem.Write(swapFile, victimProcessData); //write the contents of the borrowed physical page to the swap file
                                runningProcessPhysicalMapping.physicalPageNumber = victimProcessMemoryMapping.physicalPageNumber; //assign the victims physical page to the current process
                                victimProcessMemoryMapping.physicalPageNumber = -1; //clear the victim processes physical page mapping
                                break;
                            }
                        }
                    }
                }
            }
            int physicalPageNumber = runningProcessPhysicalMapping.physicalPageNumber;
            if (runningProcessPhysicalMapping.diskPageNumber != -1) { //check if the current process has data written to disk, if it does
                fakeFileSystem.Seek(swapFile, runningProcessPhysicalMapping.diskPageNumber * 1024); //move to the current processes' page on the swap file
                byte[] dataOnSwapFile = fakeFileSystem.Read(swapFile, 1024); //load the data stored on the swap file to a byte array
                int byteCount = 0;
                for (int i = physicalPageNumber * 1024; i < (physicalPageNumber * 1024 + 1024); i++) {
                    physicalMemory[i] = dataOnSwapFile[byteCount]; //write the data from the swap file to the newly assigned/reassigned physical page
                    byteCount++;
                }
            } else { //if the current process does not have data written to disk, populate the physical memory assigned to it with 0's
                for (int i = physicalPageNumber * 1024; i < (physicalPageNumber * 1024 + 1024); i++) {
                    physicalMemory[i] = 0; //clear the physical memory if not loading
                }
            }
        } //if a physical page mapping already existed, all the above is skipped and the mapping is directly written to the tlb
        UserlandProcess.setTlbEntry(virtualPageNumber, runningProcessPhysicalMapping.physicalPageNumber); //update a random tlb entry with the virtual to physical page mapping
    }

    public int AllocateMemory(int size) {
        KernelLandProcess runningProcess = scheduler.GetCurrentlyRunning();
        VirtualToPhysicalMapping[] runningProcessMemoryMap = runningProcess.getMemoryMap();
        int numPagesNeeded = size / 1024; //keep track of the number of pages needed
        int emptyVirtualMemoryCount = 0; //keep track of the number of free consecutive virtual pages found
        int[] freeVirtualMemoryIndices = new int[numPagesNeeded];
        for (int i = 0; i < runningProcessMemoryMap.length; i++) { //iterate through the virtual memory and try to find consecutive free pages
            if (runningProcessMemoryMap[i] == null) {
                freeVirtualMemoryIndices[emptyVirtualMemoryCount] = i; //if a page is free, store its index
                emptyVirtualMemoryCount++;
                if (emptyVirtualMemoryCount == numPagesNeeded) { //if the required number of consecutive free virtual memory is found
                    for (int j = 0; j < numPagesNeeded; j++) {
                        runningProcessMemoryMap[freeVirtualMemoryIndices[j]] = new VirtualToPhysicalMapping(); //initialize each virtual page
                    }
                    return freeVirtualMemoryIndices[0] * 1024; //return the starting virtual address
                }
            } else if (emptyVirtualMemoryCount != 0) {
                emptyVirtualMemoryCount = 0; //if a non-empty index is found, make sure that the count of consecutive free virtual memory is reset
            }
        }
        return -1; //if the required amount of empty virtual memory is not found, return -1 for failure
    }

    public boolean FreeMemory(int pointer, int size) {
        KernelLandProcess runningProcess = scheduler.GetCurrentlyRunning();
        VirtualToPhysicalMapping[] runningProcessMemoryMap = runningProcess.getMemoryMap();
        int virtualPageToFree = pointer / 1024; //convert the starting virtual address to the virtual page
        int amountToFree = size / 1024; //find the number of pages to free
        for (int i = 0; i < amountToFree; i++) { //for every page to free
            VirtualToPhysicalMapping physicalPageToFree = runningProcessMemoryMap[virtualPageToFree]; //get the physical page to be freed
            if (physicalPageToFree != null) {
                if (physicalPageToFree.physicalPageNumber != -1) {
                    inUsePhysicalPageList[physicalPageToFree.physicalPageNumber] = false; //mark the physical space free
                }
                runningProcessMemoryMap[virtualPageToFree] = null; //clear the virtual to physical page mapping in the current process
            }
            virtualPageToFree++; //increment the index of the next virtual page to be freed
        }
        UserlandProcess.clearTlb(); //make sure to clear tlb to remove any old mappings
        return true;
    }
}
