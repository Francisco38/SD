//package hello;
//package sockets;

import java.net.*;
import java.net.UnknownHostException;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.util.Scanner; 

import Classes.LoginPacket;
import Classes.User;

import java.util.ArrayList;

/**
 * Class that represents the data base via which all changes are made 
 * 
 * @author Francisco Faria
 * @author Davide Areias
 * @author Iago Bebiano
 */
class db {
    //array where all the directorys that need to be replicatad are located
    public ArrayList<String> needUpdateDir;
    
    //array with all the users
    public ArrayList<User> usersInfo;
    
    //maximum number of missed pings
    public int maxFailedRounds;
    
    //time between each ping
    public int period;
    
    //variable that is used to check is user.txt is already in the needupdateDir array
    public int needUserupdate;
    
    //variable that is used to control access to the array needUpdateDir and needUserUpdate
    public int t = 1;

    /**
     * Constructor
     * @throws RemoteException 
     */
    public db() throws RemoteException {
        usersInfo = new ArrayList<User>();
        needUpdateDir = new ArrayList<String>();
    }

    /**
     * Add user to array usersInfo
     * updates users.txt via function setusers
     * @param user new user
     */
    public synchronized void addUser(User user) {
        usersInfo.add(user);
        setUsers();
    }

    /**
     * Switchs the users directory
     * updates users.txt via function setusers
     * @param currentUser user that needs to change directory
     * @param dir new directory
     */
    public synchronized void changeDir(User currentUser, String dir) {
        //takes the bd out od the dir
        dir=dir.substring(2);
        
        //find coorect user
        for (User user : this.usersInfo) {
            if (user.equals(currentUser)) {
                //change dir
                user.dir = dir;
            }
        }
        
        setUsers();
    }

    /**
     * Switch the users password
     * updates users.txt via function setusers
     * @param currentUser user that wants to change password
     * @param pass new password
     */
    public synchronized void changePassword(User currentUser, String pass) {
        //find coorect user
        for (User user : this.usersInfo) {
            if (user.equals(currentUser)) {
                //change password
                user.password = pass;
            }
        }
        setUsers();
    }

    /**
     * Switch values associated with ping
     * @param v1 new value for maxfailedrouds
     * @param v2 new value for period
     */
    public synchronized void changePingValues(int v1, int v2) {
        this.maxFailedRounds = v1;
        this.period = v2;
    }

    /**
     * Updates the file users.txt
     * updates users.txt via function setusers
     */
    public synchronized void setUsers() {
        boolean first = true;
        for (User user : this.usersInfo) {
            // update users file
            try {
                //define string with all the data
                String data = user.username + ":"
                        + user.password + ":"
                        + user.university + ":"
                        + user.phone + ":"
                        + user.address + ":"
                        + user.cc[0] + ":"
                        + user.cc[1] + ":"
                        + user.dir
                        + "\n";

                //open file
                File usersFile = new File("bd\\users.txt");
                if (!usersFile.exists()) {
                    usersFile.createNewFile();
                }

                //write data
                if (first) {
                    FileWriter fileWritter = new FileWriter(usersFile, false);
                    fileWritter.write(data);
                    fileWritter.close();
                    first = false;
                } else {
                    FileWriter fileWritter = new FileWriter(usersFile, true);
                    BufferedWriter bw = new BufferedWriter(fileWritter);
                    bw.write(data);
                    bw.close();
                }

            } catch (Exception e) {
                System.out.println("Exception in updating users File: " + e);
            }
        }
        
        needupdate("bd\\users.txt", 1);
    }

    /**
     * Adds dir to needUpdateDir array
     * @param dir directory that needs to be replicated
     * @param v value that indicates is it is user.txt that needs update
     */
    public synchronized void needupdate(String dir, int v) {
        try {
            //waits to have acess
            while (t == 0) {
                wait();
            }
            
            //blocks other from entering
            t = 0;
            
            //checks if it is users.txt
            if (v == 1) {
                //if users.txt not already in array add and leaves
                if (needUserupdate != 1) {
                    needUpdateDir.add(dir);
                }
                t = 1;
                notifyAll();
            } else {
                //adds dir to array
                needUpdateDir.add(dir);
                t = 1;
                notifyAll();
            }
        } catch (InterruptedException e) {
            System.out.println("interruptedException caught");
        }
    }
    
    /**
     * Define needUserUpdate as 0
     */
    public synchronized void userUpdate() {
        try {
            //waits for access
            while (t == 0) {
                wait();
            }
            
            //bloks others
            t = 0;
            
            //changes need user update
            needUserupdate = 0;
            t = 1;
            notifyAll();
        } catch (InterruptedException e) {
            System.out.println("interruptedException caught");
        }
    }

    /**
     * cicle that stops allows for a non-active wait
     */
    public synchronized void checkWhenNedded() {
        try {
            //waits untill array has elements in array
            while (this.needUpdateDir.isEmpty()) {
                wait();
            }
        } catch (InterruptedException e) {
            System.out.println("interruptedException caught");
        }
    }

    /**
     * Update ping values
     */
    public synchronized void getPingInfo() {
        try {
            //opens file
            File usersFileObj = new File("heartBeats.txt");
            Scanner reader = new Scanner(usersFileObj);
            String info = reader.nextLine();
            
            //gets info
            String[] info2 = info.split(":");
            
            //updates info
            maxFailedRounds = Integer.parseInt(info2[0]);
            period = Integer.parseInt(info2[1]);
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error Reading primary file");
            e.printStackTrace();
        }
    }
}

/**
 * Class that implemetns all admin functions
 * 
 * @author Davide Areias
 * @author Francisco Faria
 * @author Iago Bebiano
 */
class ucDriveServer_admin extends UnicastRemoteObject implements ucDrive {
    //Data base
    db db;

    /**
     * Constructor
     * @param dbInput database 
     * @throws RemoteException 
     */
    public ucDriveServer_admin(db dbInput) throws RemoteException {
        db = dbInput;
    }
    
    /**
     * Calculates and return the total space ocupied by a folder 
     * @param folder 
     * @return total ocuppied space
     */
    public long getFolderSize(File folder) {
        long length = 0;

        // contents of the given folder
        File[] files = folder.listFiles();

        int count = files.length;

        // loop for traversing the directory
        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            } else {
                length += getFolderSize(files[i]);
            }
        }
        return length;
    }

    /**
     * Add user to database
     * @param userData user to add
     * @return message that indicates the success of the operation
     * @throws RemoteException 
     */
    public String register(User userData) throws RemoteException {
        //checks if user exists
        for (User user : db.usersInfo) {
            if (user.cc[0].equals(userData.cc[0])) {
                return "User Alredy Exists";
            }
        }
        
        //defines user
        String cc[] = {userData.cc[0], userData.cc[1]};
        User user = new User(userData.username, userData.password, userData.university, userData.phone,
                userData.address, cc, "\\dir\\" + userData.cc[0]);

        //adds user to data base
        db.addUser(user);

        try {
            //creates forder for user
            File file = new File("bd\\dir\\" + userData.cc[0]);
            if (!file.exists()) {
                if (file.mkdir()) {
                    // System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }
            file = new File("bd2\\dir\\" + userData.cc[0]);
            if (!file.exists()) {
                if (file.mkdir()) {
                    // System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }
            return "Register User With Sucess";
        } catch (Exception e) {
            System.out.println("Exception in updating users File - register method: " + e);
        }
        return "A bug happen register method";
    }

    /**
     * Get all files from a user
     * @param user_id user from with we want files
     * @return file list
     * @throws RemoteException 
     */
    public File[] listFiles(String user_id) throws RemoteException {
        File file = new File("bd\\dir\\" + user_id);
        try {
            if (file.exists() && file.isDirectory()) {
                File subDir[] = file.listFiles();
                return subDir;
            }

        } catch (Exception e) {
            e.getStackTrace();
        }
        return null;
    }

    /**
     * Get total folder size
     * @param user_id from which we want the size
     * @return size os folder or -1 if file doesnt exit
     * @throws RemoteException 
     */
    public long storageInfo(String user_id) throws RemoteException {
        File file;
        if (user_id.equals("all")) {
            file = new File("bd\\dir");
        } else {
            file = new File("bd\\dir\\" + user_id);
        }
        if (file.exists() && file.isDirectory()) {
            return getFolderSize(file);
        }
        return -1;
    }

    /**
     * Switch values associated with ping for the parrams
     * @param v1 new value for maxfailedrouds
     * @param v2 new value for period
     * @return if operation was successful~ or nor
     * @throws RemoteException 
     */
    public int changePing(String v1, String v2) throws RemoteException {
        try {
            //open heartBeats file
            File tempFile = new File("heartBeats.txt");
            if (tempFile.exists()) {
                //reads and changes info
                db.changePingValues(Integer.parseInt(v1), Integer.parseInt(v2));
                FileWriter fileWritter = new FileWriter(tempFile, false);
                fileWritter.write(v1 + ":" + v2);
                fileWritter.close();
                return 0;
            }
        } catch (Exception e) {
            System.out.println("Exception in updating heartBeats File: " + e);
        }
        return 1;
    }

    /**
     * @return maxFailedRounds
     * @throws RemoteException 
     */
    public int getMaxFailed() throws RemoteException {
        return db.maxFailedRounds;
    }

    /**
     * 
     * @return period
     * @throws RemoteException 
     */
    public int getPeriod() throws RemoteException {
        return db.period;
    }
    
    public String checkReplication() throws RemoteException{
        String str="";
        for (String dir : db.needUpdateDir) {
            str=str+dir+"\n";
        }
        if(str.equals("")){
            str="all updated\n";
        }
        return str;
    }
}

/**
 * Class that implements all client functions
 * 
 * @author Davide Areias
 * @author Francisco Faria
 * @author Iago Bebiano
 */
class ucDriveServer_client extends Thread {
    //Comunication streams
    private DataInputStream in;
    private DataOutputStream out;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    Socket clientSocket;
    
    //number od thread
    int thread_number;
    
    //values used to keep the ping values on the client side updated
    int v1;
    int v2;
    
    //current user information 
    User currentUser;

    //current server directory
    String currentServerDir;

    //server port used for download/upload
    private int serverPort2 = 7000;

    //database pointer
    db db;

    /**
     * Contructor
     * @param aClientSocket 
     * @param number
     * @param dbInput 
     */
    public ucDriveServer_client(Socket aClientSocket, int number, db dbInput) {
        db = dbInput;
        thread_number = number;
        this.v1 = db.maxFailedRounds;
        this.v2 = db.period;
        try {
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());
            this.start();
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        }
    }

    /**
     * Work for current thread
     */
    public void run() {
        login();
        menu();
    }

    /**
     * login function that works infinitly until the login is sucessful
     */
    private void login() {
        int sucess = 0;
        try {
            while (sucess == 0) {

                // read objectfrom object input stream
                LoginPacket loginData = (LoginPacket) ois.readObject();

                // checks if information correct
                for (User user : db.usersInfo) {
                    if (user.username.equals(loginData.username) && user.password.equals(loginData.password)) {
                        sucess = 1;
                        out.writeInt(sucess);
                        // save current user
                        currentUser = user;
                        // save current server dir
                        currentServerDir = "bd"+user.dir;
                        oos.writeObject(currentServerDir );
                        out.writeInt(v1);
                        out.writeInt(v2);
                        return;
                    }
                }

                // informs client of success of the opetation
                out.writeInt(sucess);
            }
        } catch (Exception e) {
            System.out.println("Login Failed:" + e);
        }
    }
    
    /**
     * function that redirects thread in order to perform the different operations
     */
    private void menu() {
        int option = 0;
        try {
            do {
                // read int from input stream
                this.v1 = db.maxFailedRounds;
                this.v2 = db.period;
                out.writeInt(v1);
                out.writeInt(v2);
                option = in.readInt();
                
                //goes to specified function
                switch (option) {
                    case 1 ->
                        changePass();
                    case 2 ->
                        changeLocalDirec();
                    case 3 ->
                        changeServerDirec();
                    case 4 ->
                        ListDirectoryLocal();
                    case 5 ->
                        ListDirectoryServer();
                    case 6 ->
                        sendFile();
                    case 7 ->
                        uploadFile();
                    case 0 -> {
                        System.out.println("Closing...");
                        return;
                    }
                    default ->
                        System.out.println("Chose a correct option");
                }
            } while (option != 0);
        } catch (EOFException e) {
            System.out.println("EOF:" + e);
        } catch (IOException e) {
            System.out.println("IO:" + e);
        }
    }

    /**
     * gets a list of all files in path
     * @param path 
     * @return list with all the files
     * @throws RemoteException 
     */
    public String listFiles(String path) throws RemoteException {
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
            System.out.println("Directory not Found e:"+e);
        }

        return list;
    }

    /**
     * Change user password
     */
    private void changePass() {
        try {
            // read objectfrom object input stream
            String new_password = (String) ois.readObject();

            // update-password
            db.changePassword(currentUser, new_password);

            // informs client of success of the opetation
            out.writeInt(1);
            login();
        } catch (Exception e) {
            System.out.println("Changed Pass:" + e);
        }
    }

    /**
     * List all files in the current server directory
     * @throws IOException 
     */
    private void ListDirectoryServer() throws IOException {
        String list = listFiles(currentServerDir);
        out.writeUTF(list);
    }

    /**
     * Change server directory
     */
    private void changeServerDirec() {
        try {
            // read objectfrom object input stream
            String new_path = (String) ois.readObject();
            
            //going back
            if (new_path.equals("--")) {
                String info[] = currentServerDir.split("\\\\");
                currentServerDir = "bd\\dir\\" + info[2];
                for (int i = 3; i < info.length - 1; i++) {
                    currentServerDir += "\\" + info[i];
                }
                out.writeUTF(currentServerDir);
                db.changeDir(currentUser, currentServerDir);
                System.out.println(currentServerDir);
                return;
            }
            String currentServerDirTemp = currentServerDir + "\\" + new_path;

            File file = new File(currentServerDirTemp);
            String res = "";
            
            //if it doesnt exist create folder
            if (file.exists()) {
                res = currentServerDir;
            } else {
                res = "Folder Not Found";
                file.mkdir();
                String info[] = currentServerDirTemp.split("\\\\");
                String temp="bd2\\dir\\" + info[2];
                for (int i = 3; i < info.length; i++) {
                    temp += "\\" + info[i];
                }
                file = new File(temp);
                file.mkdir();
            }

            // Responds to client
            currentServerDir = currentServerDirTemp;
            db.changeDir(currentUser, currentServerDir);
            out.writeUTF(res);

        } catch (Exception e) {
            System.out.println("Changed Server Dir:" + e);
        }

    }

    /**
     * list all files in the current local directory
     * @throws IOException 
     */
    private void ListDirectoryLocal() throws IOException {

    }

    /**
     * Change local directory
     * @throws IOException 
     */
    private void changeLocalDirec() {

    }

    /**
     * Send file to client via a diferent socket and thread
     */
    private void sendFile() {
        Thread t1 = new Thread() {
            public void run() {
                try ( ServerSocket listenSocket2 = new ServerSocket(serverPort2)) {
                    System.out.println(serverPort2);
                    Socket clientSocket2 = listenSocket2.accept();

                    DataOutputStream out2 = new DataOutputStream(clientSocket2.getOutputStream());
                    ObjectInputStream ois2 = new ObjectInputStream(clientSocket2.getInputStream());
                    String file_name = (String) ois2.readObject();
                    String file_path = currentServerDir + "\\" + file_name;
                    File file = new File(file_path);
                    int res = 0;
                    if (file.exists()) {
                        res = 1;
                        
                        //tells client that file exists
                        out2.writeInt(res);
                        
                        FileInputStream fis = new FileInputStream(file);
                        
                        //send file length
                        out2.writeLong(file.length());
                        
                        // sends file in chuncks
                        int bytes = 0;
                        byte[] buffer = new byte[4 * 1024];
                        while ((bytes = fis.read(buffer)) != -1) {
                            out2.write(buffer, 0, bytes);
                            out2.flush();
                        }
                        fis.close();
                    } else {
                        res = 0;
                        out2.writeInt(res);
                    }

                } catch (Exception e) {
                    System.out.println("Failed to send file Error:" + e.getMessage());
                }
            }
        };
        t1.run();
    }

    /**
     * Receives file from client and adds to server directory
     */
    private void uploadFile() {
        Thread t1 = new Thread() {
            public void run() {
                try ( ServerSocket listenSocket2 = new ServerSocket(serverPort2)) {
                    Socket clientSocket2 = listenSocket2.accept();

                    DataInputStream in2 = new DataInputStream(clientSocket2.getInputStream());
                    ObjectInputStream ois2 = new ObjectInputStream(clientSocket2.getInputStream());
                    String file_name;
                    int r = in2.readInt();
                    if (r == 1) {
                        //receives file name from client
                        file_name = (String) ois2.readObject();
                        int bytes = 0;
                        FileOutputStream fos = new FileOutputStream(currentServerDir + "\\" + file_name);

                        long size = in2.readLong();     // read file size
                        byte[] buffer = new byte[4 * 1024];
                        while (size > 0 && (bytes = in2.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                            fos.write(buffer, 0, bytes);
                            size -= bytes;      // read upto file size
                        }
                        fos.close();
                        db.needupdate(currentServerDir + "\\" + file_name, 0);
                    }

                } catch (Exception e) {
                    System.out.println("Failed to upload file error:" + e.getMessage());
                }

            }
        };
        t1.run();
    }
}

/**
 * Main class from which all the other classes are 
 * 
 * @author Davide Areias
 * @author Francisco Faria
 * @author Iago Bebiano
 */
public class ucDriveServer {
    //server ports
    private static int serverPort = 6000;
    private static int otherServerPort = 6001;
    private static int udpServerPort = 6002;
    
    //mas buffer size for file udp
    private static final int bufsize = 4096;
    
    //max time that the udp connection waits for package
    private static int timeout = 1000;
    
    //variable used to define if this is the primary server
    static int isPrimary = 1;

    /**
     * Constructor
     * @throws RemoteException 
     */
    public ucDriveServer() throws RemoteException {
        super();
    }

    /**
     * gets all users from file users.txt
     * @return
     * @throws RemoteException 
     */
    private static db getUsers() throws RemoteException {
        db db = new db();
        try {
            //opens users file
            File usersFileObj = new File("bd\\users.txt");
            Scanner reader = new Scanner(usersFileObj);
            while (reader.hasNextLine()) {
                //gets user info 
                String data = reader.nextLine();
                String[] dataArray = data.split(":");
                
                //if there is no info leave
                if (dataArray.length == 1) {
                    break;
                }
                //inicializes user
                String[] cc = {dataArray[5], dataArray[6]};
                User user = new User(dataArray[0], dataArray[1], dataArray[2], dataArray[3], dataArray[4], cc, dataArray[7]);
                
                //adds to array
                db.addUser(user);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error Reading users file");
            e.printStackTrace();
        }
        return db;
    }

    /**
     * Gets what server is primary from primary.txt
     * @return 
     * @throws RemoteException 
     */
    private static int getPrimary() throws RemoteException {
        int primary = 0;
        try {
            //open primary.txt
            File usersFileObj = new File("primary.txt");
            Scanner reader = new Scanner(usersFileObj);
            
            //gets primary info
            primary = reader.nextInt();
            
            //leaves
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error Reading primary file");
            e.printStackTrace();
        }
        return primary;
    }

    /**
     * Makes this server the primary by changing value in file primary.txt
     * @throws RemoteException 
     */
    private static void setPrimary() throws RemoteException {
        try {
            //open primary file
            File usersFile = new File("primary.txt");
            if (!usersFile.exists()) {
                usersFile.createNewFile();
            }

            //switchs the information
            FileWriter fileWritter = new FileWriter(usersFile, false);
            fileWritter.write("0");
            fileWritter.close();
        } catch (Exception e) {
            System.out.println("Exception in updating users File - register method: " + e);
        }
    }

    /**
     * Code to be run if the server is primary
     * Is compossed by four threads one for the admin, another for client, another for the failover and the last one for the replication of the files
     * @param db
     * @throws InterruptedException 
     */
    public static void inCaseItsPrimary(db db) throws InterruptedException {
        //thread for client
        Thread t = new Thread() {
            public void run() {
                try {
                    // cria objecto para rmi
                    ucDriveServer_admin h = new ucDriveServer_admin(db);

                    // bind rmi
                    LocateRegistry.getRegistry(5000).rebind("login", h);

                    System.out.println("--SERVER READY--");
                } catch (RemoteException re) {
                    System.out.println("Exception in ucDriveServer_admin.main: " + re);
                }
            }
        };
        // thread for client
        Thread t2 = new Thread() {
            public void run() {
                int numero = 0;

                try ( ServerSocket listenSocket = new ServerSocket(serverPort)) {
                    System.out.println("A escuta no porto 6000");
                    System.out.println("LISTEN SOCKET=" + listenSocket);

                    //cicle that creates a thread for each client giving it access to the functions of class ucDriveServer_client
                    while (true) {
                        Socket clientSocket = listenSocket.accept();
                        System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                        numero++;
                        new ucDriveServer_client(clientSocket, numero, db);
                    }
                } catch (IOException e) {
                    System.out.println("Listen:" + e.getMessage());
                }
            }
        };
        // thread for failover
        Thread t3 = new Thread() {
            public void run() {
                try ( DatagramSocket ds = new DatagramSocket(otherServerPort)) {
                    while (true) {
                        byte buf[] = new byte[bufsize];
                        DatagramPacket dp = new DatagramPacket(buf, buf.length);
                        ds.receive(dp);
                        ByteArrayInputStream bais = new ByteArrayInputStream(buf, 0, dp.getLength());
                        DataInputStream dis = new DataInputStream(bais);
                        int count = dis.readInt();

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(baos);
                        dos.writeInt(count);
                        byte resp[] = baos.toByteArray();
                        DatagramPacket dpresp = new DatagramPacket(resp, resp.length, dp.getAddress(),
                                dp.getPort());
                        ds.send(dpresp);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        //thread for file duplication
        Thread t4 = new Thread() {
            public void run() {
                while (true) {
                    try ( DatagramSocket ds = new DatagramSocket()) {
                        db.checkWhenNedded();
                        InetAddress ia = InetAddress.getByName("localhost");
                        String temp = db.needUpdateDir.get(0);
                        System.out.println("temp:"+temp);
                        byte[] dir = temp.getBytes();
                        DatagramPacket dp = new DatagramPacket(dir, dir.length, ia, udpServerPort+1);
                        ds.send(dp);
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(temp));
                        int length = bis.available();
                        byte[] buf = new byte[length];
                        bis.read(buf);
                        dp = new DatagramPacket(buf, buf.length, ia, udpServerPort+1);
                        ds.send(dp);
                        db.needUpdateDir.remove(0);
                        if (temp.equals("bd\\users.txt")) {
                            db.userUpdate();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        // run thread t e t2 e t3
        t.start();
        t2.start();
        t3.start();
        t4.start();

        t.join();
        t2.join();
        t3.join();
        t4.join();
    }

    /**
     * Code to be run if the server is primary
     * Is compossed by two threads one for the failover and another to receive files from primary server
     * @param db
     * @throws RemoteException
     * @throws InterruptedException 
     */
    public static void secondaryWord(db db) throws RemoteException, InterruptedException, SocketException {
        // thread for failover
        Thread t = new Thread() {
            public void run() {
                int count = 1;
                try ( DatagramSocket ds = new DatagramSocket()) {
                    InetAddress ia = InetAddress.getByName("localhost");
                    ds.setSoTimeout(timeout);
                    int failedheartbeats = 0;
                    while (failedheartbeats < db.maxFailedRounds) {
                        try {
                            db.getPingInfo();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            DataOutputStream dos = new DataOutputStream(baos);
                            dos.writeInt(count++);
                            byte[] buf = baos.toByteArray();

                            DatagramPacket dp = new DatagramPacket(buf, buf.length, ia, otherServerPort);
                            ds.send(dp);

                            byte[] rbuf = new byte[bufsize];
                            DatagramPacket dr = new DatagramPacket(rbuf, rbuf.length);

                            ds.receive(dr);
                            failedheartbeats = 0;
                            ByteArrayInputStream bais = new ByteArrayInputStream(rbuf, 0, dr.getLength());
                            DataInputStream dis = new DataInputStream(bais);
                            int n = dis.readInt();
                            System.out.println("Got: " + n + ".");
                        } catch (IOException ste) {
                            failedheartbeats++;
                            System.out.println("Failed heartbeats: " + failedheartbeats);
                        }
                        Thread.sleep(db.period);
                    }
                } catch (InterruptedException | SocketException | UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        };

        //thread for file replication
        Thread t2 = new Thread() {
            public boolean allDone = false;
            public void run() {
                while (true) {
                    try (DatagramSocket ds = new DatagramSocket(udpServerPort)) {
                        byte[] buf = new byte[1024];
                        DatagramPacket dp = new DatagramPacket(buf, buf.length);
                        ds.receive(dp);
                        String tempdir = new String(dp.getData(), 0, dp.getLength());
                        String info[] = tempdir.split("\\\\");
                        String dir = "bd";
                        for (int i = 1; i < info.length; i++) {
                            dir += "\\" + info[i];
                        }
                        System.out.println("dir:" + dir);
                        
                        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(dir));

                        ds.receive(dp);
                        fos.write(dp.getData(), 0, dp.getLength());
                        fos.flush();
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
        t2.start();
        
        t.join();
        t2.stop();

        setPrimary();
        inCaseItsPrimary(db);
    }

    public static void main(String args[]) throws RemoteException, SocketException {
        db db = getUsers();
        db.getPingInfo();
        try {
            if (getPrimary() == 0) {
                inCaseItsPrimary(db);
            } else {
                secondaryWord(db);
            }
        } catch (InterruptedException e) {
            System.out.println("Main thread Interrupted");
        }
    }

}
