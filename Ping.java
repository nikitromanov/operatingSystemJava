import java.nio.charset.StandardCharsets;

public class Ping extends UserlandProcess {
    @Override
    public void run() {
        int messagePurpose = 0;
        int targetPid = -1;
        while (targetPid == -1) {
            targetPid = OS.GetPidByName("Pong"); //get the pid belonging to Pong using GetPidByName
        }
        System.out.println("This is PING, pong = " + targetPid);
        while (true) {
            KernelMessage pingMessage = new KernelMessage(targetPid, messagePurpose, "PING".getBytes(StandardCharsets.UTF_8));
            OS.SendMessage(pingMessage); //send a ping message to Pong
            KernelMessage responseMessage = OS.WaitForMessage(); //wait for a message from Pong
            System.out.println(responseMessage.toString()); //print the message from Pong
            messagePurpose++;
            OS.Sleep(50);
        }
    }
}
