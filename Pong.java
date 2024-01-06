import java.nio.charset.StandardCharsets;

public class Pong extends UserlandProcess {
    @Override
    public void run() {
        int messagePurpose = 0;
        int targetPid = -1;
        while (targetPid == -1) {
            targetPid = OS.GetPidByName("Ping"); //get the pid belonging to Ping
        }
        System.out.println("This is PONG, ping = " + targetPid);
        while (true) {
            KernelMessage pongMessage = new KernelMessage(targetPid, messagePurpose, "PONG".getBytes(StandardCharsets.UTF_8));
            KernelMessage responseMessage = OS.WaitForMessage(); //wait for a message from Ping
            OS.SendMessage(pongMessage); //send a pong message to Ping
            System.out.println(responseMessage.toString()); //print the message from Ping
            messagePurpose++;
            OS.Sleep(50);
        }
    }

}
