public class OS {

    private static Kernel kernel;

    public static void Startup(UserlandProcess init) {
        kernel = new Kernel();
        UserlandProcess.Initialize();
        CreateProcess(init); //create the first process passed in as input
    }

    public static int CreateProcess(UserlandProcess up, KernelLandProcess.Priority priority){
        return kernel.CreateProcess(up, priority); //call kernels CreateProcess
    }

    public static int CreateProcess(UserlandProcess up){
        return kernel.CreateProcess(up, KernelLandProcess.Priority.Interactive); //call kernels CreateProcess
    }

    public static void Sleep(int milliseconds){
        kernel.Sleep(milliseconds); //call kernels Sleep
    }

    public static int Open(String s) {
        return kernel.Open(s); //call kernels Open
    }

    public static void Close(int id) {
        kernel.Close(id); //call kernels Close
    }

    public static byte[] Read(int id, int size) {
        return kernel.Read(id, size); //call kernels Read
    }

    public static void Seek(int id, int to) {
        kernel.Seek(id, to); //call kernels Seek
    }

    public static int Write(int id, byte[] data) {
        return kernel.Write(id, data); //call kernels Write
    }

    public static int GetPid(){
        return kernel.GetPid(); //call kernels GetPid
    }

    public static int GetPidByName(String name){
        return kernel.GetPidByName(name); //call kernels GetPidByName
    }

    public static void SendMessage(KernelMessage km){
        kernel.SendMessage(km); //call kernels SendMessage
    }

    public static KernelMessage WaitForMessage(){
        return kernel.WaitForMessage(); //call kernels WaitForMessage
    }

    public static void GetMapping(int virtualPageNumber){
        kernel.GetMapping(virtualPageNumber);
    }

    public static int AllocateMemory(int size){
        if (size % 1024 == 0) { //check if the size is a multiple of 1024
            return kernel.AllocateMemory(size);
        }
        else{
            return -1;
        }
    }
    public static boolean FreeMemory(int pointer, int size){
        if (size % 1024 == 0 && pointer % 1024 == 0) { //make sure the size and pointer are multiples of 1024
            return kernel.FreeMemory(pointer, size);
        }
        else {
            return false;
        }
    }
}
