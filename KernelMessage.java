import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class KernelMessage {

    private int senderPid;
    private int targetPid;
    private int messagePurpose;
    private byte[] data;


    public KernelMessage(int targetPid, int messagePurpose, byte[] data){
        this.targetPid = targetPid; //set the target pid, messagePurpose, and data. Do not set the senderPid since Kernel will set it instead
        this.messagePurpose = messagePurpose;
        this.data = data;
    }

    public KernelMessage(KernelMessage message){ //copy the KernelMessage, used inside the Kernel when sending a message
        this.senderPid = message.senderPid;
        this.targetPid = message.targetPid;
        this.messagePurpose = message.messagePurpose;
        this.data = Arrays.copyOf(message.data, message.data.length); //create a deep copy of the byte array since it is an object itself
    }

    @Override
    public String toString() {
        String dataPayload = new String(data, StandardCharsets.UTF_8); //convert the data byte array to string and return it with other information
        return dataPayload + ": from: " + senderPid + " to " + targetPid + " what: " + messagePurpose;
    }

    public void setSenderPid(int senderPid) {
        this.senderPid = senderPid;
    }

    public int getTargetPid() {
        return targetPid;
    }

    public byte[] getData() {
        return data;
    }


}
