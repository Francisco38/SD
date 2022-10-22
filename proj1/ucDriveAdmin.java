import java.io.File;
import java.rmi.*;

import java.util.Scanner;

import Classes.User;
import java.net.MalformedURLException;

/**
 * 
 */
public class ucDriveAdmin {
    private static Scanner scanner = new Scanner(System.in);
    static int v1;
    static int v2;
    // Helpers
    /**
     * Gets User information from console to store in user object
     * @return User with all necessary information to register in the database
     */
    private static User registerScan() {
        Scanner scan = scanner;
        String[] cc = { "", "" };
        User user = new User("username", "password", "university", "phone", "address", cc,"dir");
        System.out.print("username:");
        user.username = scan.nextLine();
        user.username = scan.nextLine();
        System.out.print("password:");
        user.password = scan.nextLine();
        System.out.print("university:");
        user.university = scan.nextLine();
        System.out.print("phone:");
        user.phone = scan.nextLine();
        System.out.print("address:");
        user.address = scan.nextLine();
        System.out.print("cc number:");
        user.cc[0] = scan.nextLine();
        System.out.print("cc expiring date:");
        user.cc[1] = scan.nextLine();
        return user;
    }

    /**
     * Prints all files directories and sub_directories
     * @param fileList list of files to print
     * @param level how deep in sub-directories the function will go
     */
    private static void printDir(File[] fileList, int level) {
        for (File f : fileList) {
            // tabs for internal levels
            for (int i = 0; i < level; i++)
                System.out.print("\t");

            if (f.isFile())
                System.out.println(f.getName());

            else if (f.isDirectory()) {
                System.out.println("[" + f.getName() + "]");

                // recursion for sub-directories
                printDir(f.listFiles(), level + 1);
            }
        }
    }
    
    /**
     * Called when the failover needs to be changed
     * Asks for max number of failed pings and
     * time between pings.
     * @param h used to call methods from ucDrive 
     * @throws RemoteException
     */
    private static void changeFailover(ucDrive h) throws RemoteException {
        Scanner scan = scanner;
        System.out.print("Max number of failed pings:");
        scan.nextLine();
        String val1 = scan.nextLine();
        System.out.print("Time between pings:");
        String val2 = scan.nextLine();
        if(h.changePing(val1,val2)==0){
            System.out.println("Change successfull");
        }
        else{
            System.out.println("Not sucessFull");
        }
    }
    
    private static void checkReplication(ucDrive h) throws RemoteException {
        String message=h.checkReplication();
        System.out.println(message);
    }

    /**
     * Admin's console menu where can:
     * 1 - Register User : will call register() method
     * 2 - List Files: calls listFiles() 
     * 3 - Change failover values : calls changeFailover()
     * 4 - Storage Info : calls storageInfo()
     * 0 - Exit
     * @param h used to call methods from ucDrive 
     */
    private static void menu(ucDrive h) {
        int option = 0;
        Scanner scan = scanner;
        try {
            do {
                v1=h.getMaxFailed();
                v2=h.getPeriod();
                System.out.println("\n\n     * ucDrive - ADMIN CONSOLE *");
                System.out.println("--------------------------------------");
                System.out.println("     1 - Register User");
                System.out.println("     2 - List Files ");
                System.out.println("     3 - Change failover values ");
                System.out.println("     4 - Storage Info ");
                System.out.println("     5 - Check replication  ");
                System.out.println("     0 - Exit ");
                System.out.println("---------------------------------------\n");

                System.out.print("Option -> ");
                option=scan.nextInt();
                System.out.print("\n");
                switch (option) {
                    case 1 -> register(h);
                    case 2 -> listFiles(h);
                    case 3 -> changeFailover(h);
                    case 4 -> storageInfo(h);
                    case 5 -> checkReplication(h);
                    case 0 -> System.out.println("Closing...");
                    default -> System.out.println("Chose a correct option");
                }
            } while (option != 0);
        } catch (Exception e) {
            System.out.println("Main server failed switching to secondary server please wait!");
            try{
                Thread.sleep((v1*(v2+1000))+1000);
            }
            catch (Exception e2){
                System.out.println("Error:"+e2);
            }
            scan.nextLine();
            connectSocket();
            System.out.println("Chose a correct option");
        }
    }

    // Admin Functions
    /**
     * Gets user information with registerScan and calls h.register 
     * that stores the data in the database
     * @param h
     * @throws RemoteException
     */
    private static void register(ucDrive h) throws RemoteException {
        User user = registerScan();
        String message = h.register(user);
        System.out.println(message);
    }

    /**
     * Lists all files from chosen user
     * If leaved empty lists files from all users
     * @param h
     * @throws RemoteException
     */
    private static void listFiles(ucDrive h) throws RemoteException {
        Scanner scan = scanner;
        String user_id = "";
        System.out.print("user_id(empty for all): ");
        user_id = scan.nextLine();
        user_id = scan.nextLine();
        File[] fileList = h.listFiles(user_id);
        if (fileList != null) {
            System.out.println("\n--- " + user_id + " Files" + " ---\n");
            printDir(fileList, 0);
        } else {
            System.out.println("User: " + user_id + " not found");
        }
    }

    /**
     * Choose a user and prints how much space in storage its files ocuppate
     * @param h
     * @throws RemoteException
     */
    private static void storageInfo(ucDrive h) throws RemoteException {
        Scanner scan = scanner;
        String user_id = "";
        System.out.print("Write: \"all\" for all Storage Info\n");
        System.out.print("user_id: ");
        user_id = scan.nextLine();
        user_id = scan.nextLine();
        long size = h.storageInfo(user_id);
        if (size != -1) {
            System.out.println("\n--- " + user_id + " Storage Info" + " ---\n");
            int type = 0;
            if (size > 1024)
                type = 1;
            if (size > 1024 * 1024)
                type = 2;
            if (type == 0)
                System.out.println("size: " + size + "B");
            else if (type == 1)
                System.out.println("size: " + size / 1024 + "KB");
            else if (type == 2)
                System.out.println("size: " + size / (1024 * 1024) + "MB");

        } else {
            System.out.println("User: " + user_id + " not found");
        }
    }
    
    /**
     * Connect to server
     * <p>
     * Uses:
     * <p>
     * &bull; {@link #login() login}
     * <p>
     * &bull; {@link #menu() menu}
     * 
     * @return void
     */
    public static void connectSocket(){
        try {   
            ucDrive h = (ucDrive) Naming.lookup("rmi://localhost:5000/login");

            menu(h);
        } catch (MalformedURLException | NotBoundException | RemoteException e) {
            System.out.println("Exception in main: " + e);
        }
    }

    public static void main(String args[]) {
        connectSocket();
    }

}