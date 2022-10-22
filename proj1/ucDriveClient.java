import java.util.Scanner;
import java.io.*;
import Classes.LoginPacket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class ucDriveClient extends Thread {

    private static Scanner scanner = new Scanner(System.in);
    private static DataInputStream in;
    private static DataOutputStream out;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;

    private static String currentServerDir;
    private static String currentLocalDir;
    private static String minDir;

    private static int serversocket = 6000;
    private static int serversocket2 = 7000;

    private static String arg2;
    private static int v1;
    private static int v2;

    /**
     * Change Password
     *
     * @return void
     */
    private static void changePass() {
        Scanner scan = scanner;
        try {
            System.out.print("new password:");
            String new_password = scan.nextLine();
            new_password = scan.nextLine();

            // sends object to server using ObjectOutputStream
            oos.writeObject(new_password);

            // receive if the operation is sucessfull or not
            int res = in.readInt();

            if (res == 0) {
                System.out.println("An error ocureed. (new-password)");
            } else {
                System.out.println("Password changed Succefully!");
                login();
            }

        } catch (IOException e) {
            System.out.println("An error ocureed while trying to change the password.\n Error:" + e.getMessage());
        }

    }

    /**
     * Change Local Directory
     * 
     * @return void
     */
    private static void changeLocalDir() {
        Scanner scan = scanner;
        System.out.print(currentLocalDir + "\\");
        String new_path = scan.nextLine();
        new_path = scan.nextLine();
        if (new_path.equals("--")) {
            String info[] = currentLocalDir.split("\\\\");
            currentLocalDir = minDir;
            for (int i = 4; i < info.length - 1; i++) {
                currentLocalDir += "\\" + info[i];
            }
            return;
        }
        String currentLocalDirTemp = currentLocalDir + "\\" + new_path;
        File file = new File(currentLocalDirTemp);

        if (file.exists()) {
            currentLocalDir = currentLocalDirTemp;
        } else {
            System.out.println("File not Found");
        }
    }

    /**
     * Change Server Directory
     *
     * @return void
     */
    private static void changeServerDir() {
        Scanner scan = scanner;
        try {
            System.out.print(currentServerDir + "\\");
            String new_path = scan.nextLine();
            new_path = scan.nextLine();

            // sends object to server using ObjectOutputStream
            oos.writeObject(new_path);

            // receive if the operation is sucessfull or not
            String res = in.readUTF();
            if (!res.equals("Folder Not Found")) {
                currentServerDir = res;
            } else {
                System.out.println("Created folder\n");
                currentServerDir = currentServerDir + "\\" + new_path;
            }

        } catch (IOException e) {
            System.out
                    .println("An error ocureed while trying to change the server directory.\n Error:" + e.getMessage());
        }

    }

    /**
     * List Local Files
     *
     * @param path Path of files to List
     * @return List of Files
     * @apiNote Files
     * @apiNote [Folders]
     */
    public static String getFiles(String path) {
        File file = new File(path);
        File[] filesList = file.listFiles();
        String list = "";
        try {
            for (File f : filesList) {
                if (f.isDirectory()) {
                    list += "[" + f.getName() + "]" + "\n";
                }
                if (f.isFile()) {
                    list += f.getName() + "\n";
                }

            }
        } catch (Exception e) {
            System.out.println("Directory not Found");
        }

        return list;
    }

    /**
     * List Files In Local Directory
     * <p>
     * Uses: {@link #getFiles(String) ListFiles}
     * 
     * @return void
     */
    private static void listLocalDir() throws IOException {
        String fileList = getFiles(currentLocalDir);
        System.out.println("\n--- " + " Files" + " ---\n");
        System.out.println(fileList);
    }

    /**
     * List Server Files
     *
     * @return void
     */
    private static void listServerDir() throws IOException {

        String fileList = in.readUTF();
        System.out.println("\n--- " + " Files" + " ---\n");
        System.out.println(fileList);
    }

    /**
     * Download File
     * 
     * @return void
     */
    private static void downloadFile() {
        Scanner scan = scanner;
        System.out.print("File name:");
        scan.nextLine();
        final String file_name = scan.nextLine();
        System.out.print("New file name:");
        String file_name2 = scan.nextLine();
        Thread t1 = new Thread() {
            public void run() {
                try (Socket s2 = new Socket(arg2, serversocket2)) {
                    DataInputStream in2 = new DataInputStream(s2.getInputStream());
                    ObjectOutputStream oos2 = new ObjectOutputStream(s2.getOutputStream());

                    oos2.writeObject(file_name);
                    int res = 0;
                    res = in2.readInt();
                    if (res == 0) {
                        System.out.println("File not Found");
                    } else {
                        int bytes = 0;
                        FileOutputStream fos = new FileOutputStream(currentLocalDir + "\\" + file_name2);
                        long size = in2.readLong(); // read file size
                        byte[] buffer = new byte[4 * 1024];
                        while (size > 0 && (bytes = in2.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                            fos.write(buffer, 0, bytes);
                            size -= bytes; // read upto file size
                        }
                        fos.close();
                    }

                } catch (IOException e) {
                    System.out.println("An error ocureed while downloading file.\n Error:" + e.getMessage());
                } catch (Exception e) {
                    File temp = new File(file_name2);
                    if (temp.exists()) {
                        temp.delete();
                    }
                    System.out.println("Failed download Error:" + e.getMessage());
                }
            }
        };
        t1.run();
    }

    /**
     * Upload File
     * 
     * @return void
     */
    private static void uploadFile() {
        Scanner scan = scanner;
        System.out.print("Local file name:");
        scan.nextLine();
        final String file_name = scan.nextLine();
        System.out.print("server file name:");
        final String new_file = scan.nextLine();
        Thread t5 = new Thread() {
            public void run() {
                try (Socket s2 = new Socket(arg2, serversocket2)) {
                    DataOutputStream out2 = new DataOutputStream(s2.getOutputStream());
                    ObjectOutputStream oos2 = new ObjectOutputStream(s2.getOutputStream());

                    String file_path = currentLocalDir + "\\" + file_name;
                    File file = new File(file_path);

                    if (file.exists()) {
                        out2.writeInt(1);
                        oos2.writeObject(new_file);

                        FileInputStream fis = new FileInputStream(file);

                        out2.writeLong(file.length());
                        // break file into chunks
                        int bytes = 0;
                        byte[] buffer = new byte[4 * 1024];
                        while ((bytes = fis.read(buffer)) != -1) {
                            out2.write(buffer, 0, bytes);
                            out2.flush();
                        }
                        fis.close();
                    } else {
                        out2.writeInt(0);
                        System.out.println("File doesnt exist");
                    }

                } catch (IOException e) {
                    System.out.println("An error ocureed while downloading file.\n Error:" + e.getMessage());
                }
            }
        };
        t5.run();
    }

    /**
     * Menu with all the features available:
     * <p>
     * Uses:
     * <p>
     * &bull; {@link #changePass() 1 - Change Password}
     * <p>
     * &bull; {@link #changeLocalDir() 2 - Change Local Directory}
     * <p>
     * &bull; {@link #changeServerDir() 3 - Change Server Directory}
     * <p>
     * &bull; {@link #listLocalDir() 4 - List Files In Local Directory}
     * <p>
     * &bull; {@link #listServerDir() 5 - List Files In Server Directory}
     * <p>
     * &bull; {@link #downloadFile() 6 - Download File}
     * <p>
     * &bull; {@link #uploadFile() 7 - Upload File}
     * <p>
     * &bull; 0 - Exit
     * 
     * @return void
     */
    private static void menu() {
        int option = 0;
        Scanner scan = scanner;
        try {
            do {
                v1 = in.readInt();
                v2 = in.readInt();

                System.out.println("\n\n     * ucDrive - CLIENT CONSOLE *");
                System.out.println("--------------------------------------");
                System.out.println("     1 - Change Password");
                System.out.println("     2 - Change Local Directory");
                System.out.println("     3 - Change Server Directory");
                System.out.println("     4 - List Files In Local Directory");
                System.out.println("     5 - List Files In Server Directory");
                System.out.println("     6 - Download File");
                System.out.println("     7 - Upload File");
                System.out.println("     0 - Exit");
                System.out.println("---------------------------------------\n");

                System.out.print("Option -> ");
                option = scan.nextInt();
                System.out.print("\n");

                // Informs the server of the operation to execute
                out.writeInt(option);
                System.out.println(option);
                switch (option) {
                    case 1 ->
                        changePass();
                    case 2 ->
                        changeLocalDir();
                    case 3 ->
                        changeServerDir();
                    case 4 ->
                        listLocalDir();
                    case 5 ->
                        listServerDir();
                    case 6 ->
                        downloadFile();
                    case 7 ->
                        uploadFile();
                    case 0 ->
                        System.out.println("Closing...");
                    default ->
                        System.out.println("Chose a correct option");
                }

            } while (option != 0);
        } catch (Exception e) {
            System.out.println("Main server is currently switching to secondary server please wait...");
            try {
                Thread.sleep((v1 * (v2 + 1000)) + 1000);
            } catch (Exception e2) {
                System.out.println("Error in sleep (menu function):" + e2.getMessage());
            }
            scan.nextLine();
            connectSocket();
            System.out.println("Chose a correct option");
        }
    }

    /**
     * Login function only exits when login is successful
     *
     * @return void
     */
    private static void login() {
        try {
            LoginPacket loginData = new LoginPacket("", "");

            Scanner scan = scanner;
            int res = 0;
            do {
                System.out.print("username:");
                loginData.username = scan.nextLine();
                System.out.print("password:");
                loginData.password = scan.nextLine();

                // sends object to server using ObjectOutputStream
                oos.writeObject(loginData);

                // receive if the operation is sucessfull or not
                res = in.readInt();
                if (res == 0) {
                    System.out.println("Wrong login information.Try again.");
                    oos.reset();
                } else {
                    System.out.println("Welcome " + loginData.username + "\n");
                    minDir = System.getProperty("user.home");
                    currentLocalDir = minDir;
                    currentServerDir = (String) ois.readObject();
                    v1 = in.readInt();
                    v2 = in.readInt();
                    return;
                }
            } while (res == 0);
        } catch (Exception e) {
            System.out.println("An error ocureed in login function.\n Error: " + e.getMessage());
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
    public static void connectSocket() {
        try (Socket s = new Socket(arg2, serversocket)) {
            System.out.println("SOCKET=" + s);

            // 2o passo
            in = new DataInputStream(s.getInputStream());
            out = new DataOutputStream(s.getOutputStream());
            oos = new ObjectOutputStream(s.getOutputStream());
            ois = new ObjectInputStream(s.getInputStream());

            login();
            menu();
        } catch (java.net.UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        }
    }

    /**
     * Main
     * <p>
     * Uses:
     * <p>
     * &bull; {@link #connectSocket() Connect Socket}
     * 
     * @return void
     */
    public static void main(String args[]) {

        /*
         * System.getProperties().put("java.security.policy", "policy.all");
         * System.setSecurityManager(new RMISecurityManager());
         */

        if (args.length < 1) {
            System.out.println("java -cp . ucDriceClient <address>");
            System.exit(0);
        }
        arg2 = args[0];
        connectSocket();
    }
}