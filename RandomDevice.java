import java.util.Random;

public class RandomDevice implements Device {

    private Random[] randomDevices;

    public RandomDevice() {
        randomDevices = new Random[10];
    }

    @Override
    public int Open(String seed) {
        if (seed == null || seed.isEmpty()) { //if the seed is null or empty, return -1
            return -1;
        }
        int emptyIndex = -1;
        for(int i = 0; i < randomDevices.length; i++){ //check for an empty index in the device array
            if(randomDevices[i] == null){
                emptyIndex = i;
                break;
            }
        }
        if(emptyIndex == -1){ //if there is no empty index, return -1
            return -1;
        }
        Random generator = new Random(Integer.parseInt(seed));
        randomDevices[emptyIndex] = generator; //create a new Random and place it in the empty index
        System.out.println("The empty index found in RandomDevice array is: "+emptyIndex);
        return emptyIndex; //return the index of the newly added Random
    }

    @Override
    public void Close(int id) {
        randomDevices[id] = null; //set the index at the passed in id to null to close
    }

    @Override
    public byte[] Read(int id, int size) {
        Random randomGenerator = randomDevices[id]; //get the Random at the passed in id
        byte[] randomBytes = new byte[size];
        randomGenerator.nextBytes(randomBytes); //generate a byte array of the passed in size
        return randomBytes;
    }

    @Override
    public void Seek(int id, int to) {
        byte[] randomBytes = Read(id, to); //call Read(), but do not return the byte array generated
        for(int i = 0; i < randomBytes.length; i++){ //print out the bytes for testing purposes
            System.out.print("  "+randomBytes[i]);
        }
    }

    @Override
    public int Write(int id, byte[] data) { //return 0 since this method does not make sense in the context of Random
        return 0;
    }
}
