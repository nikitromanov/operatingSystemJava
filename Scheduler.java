import java.time.Clock;
import java.util.*;

public class Scheduler {
    private Map<KernelLandProcess.Priority, List<KernelLandProcess>> jobQueues;
    private Timer timer;
    private KernelLandProcess currentProcess;
    private Clock currentTime;
    private List<KernelLandProcess> sleepingProcesses;
    private final Object lockCurrentProcess;
    private Kernel kernel;
    private Map<Integer, KernelLandProcess> pidToProcess;
    private Map<Integer, KernelLandProcess> waitForMessageQueue;

    public Scheduler(Kernel kernel) {
        lockCurrentProcess = new Object(); //A lock in order to lock the currentProcess variable and make it thread-safe
        jobQueues = new HashMap<>(); //Holds the 3 synchronized lists for the 3 types of processes
        for (KernelLandProcess.Priority enumValue : KernelLandProcess.Priority.values()) {
            List<KernelLandProcess> synchronizedList = Collections.synchronizedList(new LinkedList<>());
            jobQueues.put(enumValue, synchronizedList);
        }
        sleepingProcesses = Collections.synchronizedList(new LinkedList<>());
        currentTime = Clock.systemDefaultZone(); //Have a clock that keeps track of the current time
        timer = new Timer();
        Interupt interupt = new Interupt();
        timer.scheduleAtFixedRate(interupt, 250, 250); //schedule the interrupt for every 250ms
        this.kernel = kernel;
        pidToProcess = Collections.synchronizedMap(new HashMap());
        waitForMessageQueue = Collections.synchronizedMap(new HashMap());
    }

    public int CreateProcess(UserlandProcess up, KernelLandProcess.Priority priority) {
        KernelLandProcess nextProcess = new KernelLandProcess(up, priority);
        jobQueues.get(priority).add(nextProcess);
        pidToProcess.put(nextProcess.getPid(), nextProcess); //add the process onto the pid to KernelLandProcess map
        if (currentProcess == null) { //if nothing is running, call SwitchProcess
            SwitchProcess();
        }
        return nextProcess.getPid();
    } //creates a new process and adds it to the list of processes, if it is the first process call SwitchProcess

    public synchronized void SwitchProcess() {
        Wakeup();
        synchronized (lockCurrentProcess) {
            if (currentProcess != null) { //if a process is running, stop it
                //System.out.println("Finished running Process: " + currentProcess.getPid() + " with priority: " + currentProcess.getPriority());
                KernelLandProcess.Priority processPriority = currentProcess.getPriority();
                if (processPriority == KernelLandProcess.Priority.RealTime || processPriority == KernelLandProcess.Priority.Interactive) {
                    currentProcess.setDemotionCounter(currentProcess.getDemotionCounter() + 1); //increment the demotion counter by 1
                }
                if (currentProcess.getDemotionCounter() > 5) { //if the demotion counter reaches 6, demote the current process
                    Demote(currentProcess);
                }
                currentProcess.stop();
                if (!currentProcess.isDone()) {//if the process is not done running, add it to the end of the processes list
                    jobQueues.get(currentProcess.getPriority()).add(currentProcess);
                } else { //if the process is done, iterate through the process device array and close each device stored
                    int[] processDevices = currentProcess.getOpenDevices();
                    for (int i = 0; i < processDevices.length; i++) {
                        if (processDevices[i] != -1) { //if a device is open at an index, close it
                            kernel.Close(i);
                        }
                    }
                    kernel.FreeMemory(0, 100 * 1024); //free the memory of the finished process
                    pidToProcess.remove(currentProcess.getPid()); //remove the pid to KernelLandProcess mapping of the finished process
                }
                currentProcess = null;
            }
            determineNextProcess(); //determine the next process to run
            UserlandProcess.clearTlb(); //clear the tlb on task switch
            if (currentProcess != null) {
                currentProcess.run();
            }
        }
    }

    public void determineNextProcess() {
        Random random = new Random();
        double randomProbability = random.nextDouble(); // Generates a random number between 0 (inclusive) and 1 (exclusive)
        //System.out.println("The random number is: " + randomProbability);
        if (!jobQueues.get(KernelLandProcess.Priority.RealTime).isEmpty()) { //If there is a RealTime process
            if (randomProbability < 0.6) { //There is a 6/10 chance the next process is RealTime
                currentProcess = jobQueues.get(KernelLandProcess.Priority.RealTime).remove(0);
            } else if (randomProbability < 0.9) { //There is a 3/10 chance the next process is Interactive
                if (!jobQueues.get(KernelLandProcess.Priority.Interactive).isEmpty()) {
                    currentProcess = jobQueues.get(KernelLandProcess.Priority.Interactive).remove(0);
                }
            } else { //There is a 1/10 chance the next process is Background
                if (!jobQueues.get(KernelLandProcess.Priority.Background).isEmpty()) {
                    currentProcess = jobQueues.get(KernelLandProcess.Priority.Background).remove(0);
                }
            }
            if (currentProcess == null) { //If there is no Interactive or Background processes, run a RealTime one
                currentProcess = jobQueues.get(KernelLandProcess.Priority.RealTime).remove(0);
            }
        } else if (!jobQueues.get(KernelLandProcess.Priority.Interactive).isEmpty()) {//If there are no RealTime processes
            if (randomProbability < 0.75) {//There is a 3/4 chance to run an Interactive process
                currentProcess = jobQueues.get(KernelLandProcess.Priority.Interactive).remove(0);
            } else {//There is a 1/4 chance to run a Background process
                if (!jobQueues.get(KernelLandProcess.Priority.Background).isEmpty()) {
                    currentProcess = jobQueues.get(KernelLandProcess.Priority.Background).remove(0);
                } else {//If there are no Background processes, run an Interactive process
                    currentProcess = jobQueues.get(KernelLandProcess.Priority.Interactive).remove(0);
                }
            }//If there are no RealTime or Interactive processes but there are Background ones, run a Background process
        } else if (!jobQueues.get(KernelLandProcess.Priority.Background).isEmpty()) {
            currentProcess = jobQueues.get(KernelLandProcess.Priority.Background).remove(0);
        }
    }

    public void Sleep(int milliseconds) {
        var tmp = currentProcess;
        synchronized (lockCurrentProcess) {
            currentProcess.setMinWakeupTime(currentTime.millis() + milliseconds); //Set the wakeup time for the process
            currentProcess.setDemotionCounter(0); //reset the demotion counter
            currentProcess = null;
            sleepingProcesses.add(tmp); //add the current process to the sleeping queue
            //System.out.println("Sleeping Process: " + tmp.getPid());
            //iterate through the sleeping queue and sort the processes according to their wakeup time in ascending order
            synchronized (sleepingProcesses) {
                Collections.sort(sleepingProcesses, Comparator.comparingLong(KernelLandProcess::getMinWakeupTime));
            }
        }
        SwitchProcess(); //run a new process and stop the current one
        tmp.stop();
    }

    public void Demote(KernelLandProcess processToDemote) {
        if (processToDemote.getPriority() == KernelLandProcess.Priority.RealTime) { //If the process is RealTime, make it Interactive
            processToDemote.setPriority(KernelLandProcess.Priority.Interactive);
        } else {
            processToDemote.setPriority(KernelLandProcess.Priority.Background); //If the process is Interactive, make it Background
        }
        processToDemote.setDemotionCounter(0); //reset the demotion counter
    }

    public void Wakeup() {
        // Check if any processes are ready to be awakened, while there are processes to be awoken, keep waking them up
        synchronized (sleepingProcesses) {
            while (!sleepingProcesses.isEmpty() && sleepingProcesses.get(0).getMinWakeupTime() <= currentTime.millis()) {
                KernelLandProcess awakenedProcess = sleepingProcesses.remove(0); // Remove the element from the queue
                //System.out.println("Waking up Process: " + awakenedProcess.getPid());
                jobQueues.get(awakenedProcess.getPriority()).add(awakenedProcess); // Get the process with the earliest wake-up time
            }
        }
    }

    public KernelLandProcess GetCurrentlyRunning() {
        return currentProcess;
    }

    public int GetPid() {
        return currentProcess.getPid(); //return the pid of currentProcess
    }

    public int GetPidByName(String name) {
        synchronized (pidToProcess) { //needs to be manually synchronized since the method iterates over the map pidToProcess
            for (Map.Entry<Integer, KernelLandProcess> entry : pidToProcess.entrySet()) { //look through all existing  processes
                if (name.equals(entry.getValue().getName())) { //when the process with the specified name is found, return its pid
                    return entry.getKey();
                }
            }
        }
        return -1; //return -1 if the process does not exist
    }

    public KernelLandProcess GetPidToProcess(int pid) {
        return pidToProcess.get(pid); //get the process with the specified pid and return it
    }

    public KernelMessage DecideWaitAction() { //checks if the currentProcess has a message, if so return it. If not, place it on the wait queue
        var tmp = currentProcess;
        LinkedList<KernelMessage> messageQueue = tmp.getMessageQueue(); //get the message queue of the currently running process
        synchronized (lockCurrentProcess) { //needs to be synchronized because currentProcess is being set to null
            if (messageQueue.peek() != null) { //if the process has a message on its messageQueue
                return messageQueue.pop(); //pop the message off the queue and return it immediately
            } else { //if the process does not have a message on its messageQueue
                currentProcess = null;
                waitForMessageQueue.put(tmp.getPid(), tmp); //place the process that needs to wait for a message on to the waiting queue
            }
        }
        SwitchProcess(); //call switch process
        tmp.stop(); //stop the running process
        return messageQueue.pop(); //once the process is awakened, it will pop the message off the message queue and return
    }

    public boolean CheckIfWaiting(KernelLandProcess processToCheck) { //check if the process is waiting for a message
        if (waitForMessageQueue.containsValue(processToCheck)) { //if the process is on the wait queue, return true
            return true;
        } else {
            return false; //if the process is not on the wait queue return false
        }
    }

    public void FinishWaiting(KernelLandProcess processDoneWaiting) {
        KernelLandProcess awakenedProcess = waitForMessageQueue.remove(processDoneWaiting.getPid()); //get the pid of the process done waiting and remove the process off of the waiting queue
        jobQueues.get(awakenedProcess.getPriority()).add(awakenedProcess); //put the awakened process back on the appropriate runnable queue
    }

    public void KillCurrentProcess() {
        var tmp = currentProcess;
        kernel.FreeMemory(0, 100 * 1024); //free the processes memory
        synchronized (lockCurrentProcess) {
            currentProcess = null; //must be synchronized because currentProcess is being set to null, but it is used inside SwitchProcess()
        }
        int[] processDevices = tmp.getOpenDevices();
        for (int i = 0; i < processDevices.length; i++) {
            if (processDevices[i] != -1) { //if a device is open at an index, close it
                kernel.Close(i);
            }
        }
        pidToProcess.remove(tmp.getPid()); //remove the pid to KernelLandProcess mapping of the finished process
        SwitchProcess();
        tmp.stop();
    }

    public KernelLandProcess getRandomProcess(){
        Object[] existingProcesses = pidToProcess.values().toArray(); //get a list of existing processes
        Random random = new Random();
        int randomProcessID = random.nextInt(existingProcesses.length); //generate a random number from 0 to the total number of existing processes
        while(existingProcesses[randomProcessID] == currentProcess){ //make sure that the random process selected is not the same one that is currently running
            randomProcessID = random.nextInt(existingProcesses.length); //choose a different process if the process chosen is the same as the one running
        }
        return ((KernelLandProcess) existingProcesses[randomProcessID]); //return the randomly chosen process
    }

    private class Interupt extends TimerTask {
        @Override
        public void run() {
            SwitchProcess();
        }
    }

}
