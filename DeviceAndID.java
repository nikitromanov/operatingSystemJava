public class DeviceAndID {

    private Device device;
    private int ID;

    public DeviceAndID(Device device, int ID){ //data structure for the device/id combination that is used and stored in VFS
        this.device = device;
        this.ID = ID;
    }

    public Device getDevice() {
        return device;
    }

    public int getID() {
        return ID;
    }
}
