import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FakeFileSystem implements Device {

    private RandomAccessFile[] randomAccessFiles;

    public FakeFileSystem() {
        randomAccessFiles = new RandomAccessFile[10];
    }

    @Override
    public int Open(String fileName) {
        if (fileName == null || fileName.isEmpty()) { //if the fileName is empty or null, return -1
            return -1;
        }
        int emptyIndex = -1;
        RandomAccessFile openedFile;
        for(int i = 0; i < randomAccessFiles.length; i++){//look for an empty index in the device array
            if(randomAccessFiles[i] == null){
                emptyIndex = i;
                break;
            }
        }
        if(emptyIndex == -1){ //if there is no empty index, return -1
            return -1;
        }
        File file = new File(fileName);
        try {
            openedFile = new RandomAccessFile(file, "rws"); //try opening the file with the passed in filename
        } catch (FileNotFoundException e) {
            System.out.println("There was an error opening the file");
            e.printStackTrace();
            return -1; //return -1 if the file is not found
        }
        randomAccessFiles[emptyIndex] = openedFile; //store the opened RandomAccessFile in the empty index
        //System.out.println("The empty index found in FakeFileSystem array is: "+emptyIndex);
        return emptyIndex; //return the index of the newly created RandomAccessFile
    }

    @Override
    public void Close(int id) {
        try {
            randomAccessFiles[id].close(); //close the RandomAccessFile at the given index
        } catch (IOException e) {
            System.out.println("There was an error closing the file");
            e.printStackTrace();
        }
        randomAccessFiles[id] = null; //set the index of the closed RandomAccessFile to null to clear it
    }

    @Override
    public byte[] Read(int id, int size) {
        byte[] readBytes = new byte[size];
        try {
            randomAccessFiles[id].read(readBytes); //read the number of bytes (determined by size) from the RandomAccessFile at the given index
        } catch (IOException e) {
            System.out.println("There was an error reading the file");
            e.printStackTrace();
        }
        return readBytes;
    }

    @Override
    public void Seek(int id, int to) {
        try {
            randomAccessFiles[id].seek(to); //perform seek on the RandomAccessFile at the given index
        } catch (IOException e) {
            System.out.println("There was an error using seek on the file");
            e.printStackTrace();
        }
    }

    @Override
    public int Write(int id, byte[] data) {
        try {
            randomAccessFiles[id].write(data); //write the data given to the RandomAccessFile at the given index
        } catch (IOException e) {
            System.out.println("There was an error writing to the file");
            e.printStackTrace();
            return -1; //return -1 if Write() was unsuccessful
        }
        return 0; //return 0 for success
    }
}
