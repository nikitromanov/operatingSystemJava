public class Main {

    public static void main(String[] args) throws InterruptedException {
        testMemory();
        //testPingPong();
        /*
        testRandomDevice();
        testFakeFileSystem();
        testBothDevices();
        testDeviceOverflow();
        */
        /*
        testProbabilisticModel();
        testSleep();
        testDemotion();
        */

    }
    //Run Processes that will test memory allocation, writing and reading, extending memory, and killing processes that attempt to access foreign memory
    private static void testMemory() throws InterruptedException {
        OS.Startup(new TestSwapFile(0, "This is test process 0: ")); // This is "Piggy" it will grab 100 physical pages of memory to fill up the memory
        for(int i = 0; i < 19; i++) { //instantiate "Piggy" 20 times
            OS.CreateProcess(new TestSwapFile(i + 1, "This is test process " + (i + 1) + ": "));
        }
        Thread.sleep(1500);
        OS.CreateProcess(new TestWriteReadMemory(2, 2, "This is test process 20", 100));
        OS.CreateProcess(new TestKillProcess("This is test process 2, it attempts to access foreign memory, and is killed"));
        OS.CreateProcess(new TestWriteReadMemory(2, 5, "This is test process 21",300));
        OS.CreateProcess(new TestAllocateMemory(10, 50, "This is test process 22, it tries to allocate memory in the gaps that other processes leave behind"));
        OS.CreateProcess(new TestWriteReadMemory(4, 99, "This is test process 23",50));
        OS.CreateProcess(new TestFreeMemory(10, 5, "This is test process 24, it frees its memory after writing"));
        OS.CreateProcess(new TestWriteReadMemory(2, 7, "This is test process 25", 1000));
        OS.CreateProcess(new TestWriteReadMemory(4, 88, "This is test process 26", 500));
        OS.CreateProcess(new TestExtendAndFreeMemory(2, 5, "This is test process 27, it extends its allocated memory and then frees memory in the middle, then allocates memory into the created gap in virtual memory"));
    }

    //Run Processes that will test Ping and Pong, as well as run HelloWorld and GoodbyeWorld alongside them
    private static void testPingPong() throws InterruptedException {
        OS.Startup(new HelloWorld());
        OS.CreateProcess(new GoodbyeWorld());
        OS.CreateProcess(new Ping());
        OS.CreateProcess(new Pong());
    }

    //Run Processes that will test all functionalities of RandomDevice, the test will call RandomDevice Open(), Read(), Seek(), Write(), and Close()
    private static void testRandomDevice() throws InterruptedException {
        OS.Startup(new TestRandomDevice("This is test process 1", 10));
        OS.CreateProcess(new TestRandomDevice("This is test process 2", 200), KernelLandProcess.Priority.Background);
        OS.CreateProcess(new TestRandomDevice("This is test process 3", 2));
    }

    /*
    Run Processes that will test all functionalities of FakeFileSystem, the test will call FakeFileSystem Open(), Read(), Seek(), Write(), and Close()
    Each process will open a testingFile1 and testingFile2, call Read() to read a sentence located in testingFile1, and write that sentence in testingFile2
     */
    private static void testFakeFileSystem() throws InterruptedException {
        OS.CreateProcess(new TestFakeFileSystem("This is test process 4", 0));
        OS.CreateProcess(new TestFakeFileSystem("This is test process 5", 23));
        OS.CreateProcess(new TestFakeFileSystem("This is test process 6", 46));
    }

    /*
    Run a Processes that will test both the FakeFileSystem and RandomDevice classes simultaneously, while manually closing each device after use.
    The process create a RandomDevice, calls Read() and then closes the device to avoid VFS array overflow. Next, testingFile1 and testingFile2
    are opened, and testingFile1 is once again written to testingFile 2, both files are closed after use. Finally, a RandomDevice is opened
    to make sure that the original RandomDevice was closed properly, and the memory was freed correctly.
    */
    private static void testBothDevices() throws InterruptedException {
        OS.CreateProcess(new TestCloseDevices("This is test process 7"));
    }
    /*
    Run a Process that will attempt to overflow the RandomDevice array. The process opens 11 RandomDevices and checks that -1 is returned.
    The process makes sure that all devices are closed once the process receives a -1 output.
    */
    private static void testDeviceOverflow() throws InterruptedException {
        OS.CreateProcess(new TestDeviceOverflow("This is test process 8"));
    }

    //Run Processes that will test the probabilistic model, prints out the random number that determines the next process
    private static void testProbabilisticModel() throws InterruptedException {
        OS.Startup(new TestProbabilisticModel("This is test process 1"));
        OS.CreateProcess(new TestProbabilisticModel("This is test process 2"), KernelLandProcess.Priority.Interactive);
        OS.CreateProcess(new TestProbabilisticModel("This is test process 3"), KernelLandProcess.Priority.Background);
        OS.CreateProcess(new TestProbabilisticModel("This is test process 4"), KernelLandProcess.Priority.RealTime);
        OS.CreateProcess(new TestProbabilisticModel("This is test process 5"), KernelLandProcess.Priority.Background);
    }

    //Run processes that will call sleep to avoid being demoted
    private static void testSleep() throws InterruptedException {
        OS.CreateProcess(new TestSleep("This is test process 6", 2000), KernelLandProcess.Priority.RealTime);
        OS.CreateProcess(new TestSleep("This is test process 7", 3000), KernelLandProcess.Priority.RealTime);
        OS.CreateProcess(new TestSleep("This is test process 8", 100), KernelLandProcess.Priority.RealTime);
        OS.CreateProcess(new TestSleep("This is test process 9", 200), KernelLandProcess.Priority.Background);
        OS.CreateProcess(new TestSleep("This is test process 10", 500), KernelLandProcess.Priority.Interactive);
    }

    //Run processes that will never call sleep in order to be demoted
    private static void testDemotion() throws InterruptedException {
        OS.CreateProcess(new TestDemotion("This is test process 11"), KernelLandProcess.Priority.RealTime);
        OS.CreateProcess(new TestDemotion("This is test process 12"), KernelLandProcess.Priority.RealTime);
        OS.CreateProcess(new TestDemotion("This is test process 13"), KernelLandProcess.Priority.Interactive);
        OS.CreateProcess(new TestDemotion("This is test process 14"), KernelLandProcess.Priority.Interactive);
        OS.CreateProcess(new TestDemotion("This is test process 15"), KernelLandProcess.Priority.Background);
    }

}



