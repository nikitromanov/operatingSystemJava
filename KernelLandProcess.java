import java.util.LinkedList;

public class KernelLandProcess {
    private String name;
    private static int nextPid;
    private int pid;
    private boolean threadStarted;
    private Thread thread;
    private Priority priority;
    private long minWakeupTime;
    private int demotionCounter;
    private LinkedList<KernelMessage> messageQueue;
    private int[] openDevices;
    private UserlandProcess userlandProcess;
    private VirtualToPhysicalMapping[] pageTable;

    KernelLandProcess(UserlandProcess up, Priority priority){
        if (nextPid == 0) {
            nextPid = 1; //set the pid of the first process to 1
        }
        this.priority = priority;
        thread = new Thread(up);
        pid = nextPid;
        nextPid++; //increment the nextpid for the next process
        openDevices = new int[10];
        for (int i = 0; i < openDevices.length; i++) {
            openDevices[i] = -1;
        }
        name = up.getClass().getSimpleName(); //populate the name variable of the KernelLandProcess in accordance to the name of the UserLandProcess
        messageQueue = new LinkedList<>(); //linked list to hold the processes messages
        pageTable = new VirtualToPhysicalMapping[100];
        userlandProcess = up;
    } /* creates the thread and sets pid */

    @SuppressWarnings({"deprecation", "removal"})
    void stop(){
        if(threadStarted == true) {
            thread.suspend();
        }
    } /* suspend the thread when stop is called and the thread has started*/

    boolean isDone(){
        if (threadStarted == true && thread.isAlive() == false)
            return true;
        else
            return false;
    } /* true if the thread started and not isAlive() */

    @SuppressWarnings({"deprecation", "removal"})
    void run(){
        if(threadStarted == true && !isDone()){ //if the thread has started and is not yet done, resume
            thread.resume();
        }
        else { //else start the thread and update threadStarted
            thread.start();
            threadStarted = true;
        }
    } /* resume() or start() and update “started” */

    public int getPid() {
        return pid;
    }

    public Priority getPriority() {
        return priority;
    }

    public long getMinWakeupTime() {
        return minWakeupTime;
    }

    public void setMinWakeupTime(long minWakeupTime) {
        this.minWakeupTime = minWakeupTime;
    }

    public int getDemotionCounter() {
        return demotionCounter;
    }

    public void setDemotionCounter(int demotionCounter) {
        this.demotionCounter = demotionCounter;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public int[] getOpenDevices() {
        return openDevices;
    }

    public void setOpenDevices(int index, int value) {
        this.openDevices[index] = value; //set the device at index
    }

    public void addMessage(KernelMessage message) {
        this.messageQueue.add(message); //add a message to the message queue
    }

    public LinkedList<KernelMessage> getMessageQueue() {
        return messageQueue;
    }

    public String getName() {
        return name;
    }

    public UserlandProcess getUserlandProcess() {
        return userlandProcess;
    }

    public VirtualToPhysicalMapping[] getMemoryMap() {
        return pageTable;
    }

    public enum Priority {
        RealTime,
        Interactive,
        Background

    }
}
