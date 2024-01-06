import java.util.HashMap;

public class VFS implements Device{

    private DeviceAndID[] storedDevices;
    private HashMap<String, Device> deviceLookup = new HashMap<>(); //hashmap that stores an instance of every type of Device

    public VFS(){
        storedDevices = new DeviceAndID[10];
        deviceLookup.put("random", new RandomDevice()); //store the RandomDevice inside the deviceTypes hashmap
        deviceLookup.put("file", new FakeFileSystem()); //store the FakeFileSystem inside the deviceTypes hashmap
    }

    @Override
    public int Open(String deviceString){
        int emptyIndex = -1;
        for(int i = 0; i < storedDevices.length; i++){ //look for an empty index in the VFS array
            if(storedDevices[i] == null){
                emptyIndex = i;
                break;
            }
        }
        if(emptyIndex == -1){ //if there is no empty index in the VFS array, return -1
            return -1;
        }
        int indexOfParse = deviceString.indexOf(" "); //parse the string passed in
        String deviceType = deviceString.substring(0, indexOfParse); //the first part of the string is the device type
        String deviceInput = deviceString.substring(indexOfParse + 1); //the second part of the string is the device-related input
        int deviceID = deviceLookup.get(deviceType).Open(deviceInput); //get the device according to the device type, call Open()
        if(deviceID == -1){ //if there is no empty index in the device array, return -1
            return -1;
        } //create a new entry in the VFS array at an empty index, store the device type and device id returned from Open() inside the VFS entry
        DeviceAndID vfsEntry = new DeviceAndID(deviceLookup.get(deviceType), deviceID);
        storedDevices[emptyIndex] = vfsEntry;
        System.out.println("The empty index found in VFS array is: "+emptyIndex+", ID "+deviceID+" was placed inside it");
        return emptyIndex; //return the index of the newly added VFS entry
    }

    @Override
    public void Close(int id){
        storedDevices[id].getDevice().Close(storedDevices[id].getID()); //get the device at the ID that was passed in, call Close() on that device
        storedDevices[id] = null; //set the Closed() device index to null
    }

    @Override
    public byte[] Read(int id, int size) {
        return storedDevices[id].getDevice().Read(storedDevices[id].getID(), size); //get the device at the ID that was passed in, call Read() on that device
    }

    @Override
    public void Seek(int id, int to) {
        storedDevices[id].getDevice().Seek(storedDevices[id].getID(), to); //get the device at the ID that was passed in, call Seek() on that device
    }

    @Override
    public int Write(int id, byte[] data) {
        return storedDevices[id].getDevice().Write(storedDevices[id].getID(), data); //get the device at the ID that was passed in, call Write() on that device
    }

    public Device getDeviceLookup(String deviceType) {
        return deviceLookup.get(deviceType);
    }
}
