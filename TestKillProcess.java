public class TestKillProcess extends UserlandProcess {

    private String message;

    public TestKillProcess(String message){
        this.message = message;
    }

    @Override
    public void run() { //This test process allocates memory and then tries to access foreign memory
        int startingAddress = OS.AllocateMemory(1024);
        System.out.println();
        System.out.println(message);
        System.out.println("The process allocated 1 page of memory starting from virtual address: " + startingAddress + " it now will attempt to read virtual address 1536");
        this.Read(1536);
        System.out.println("If this is reached, killProcess() failed");
    }
}
