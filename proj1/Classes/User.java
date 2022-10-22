package Classes;

import java.io.*;

public class User implements Serializable {
    public String username;
    public String password;
    public String university;
    public String phone;
    public String address;
    public String[] cc;
    public String dir;
    
    public User(String username, String password, String university, String phone, String address, String[] cc, String dir) {
        this.username = username;
        this.password = password;
        this.university = university;
        this.phone = phone;
        this.address = address;
        this.cc = cc;
        this.dir=dir;
    }
}