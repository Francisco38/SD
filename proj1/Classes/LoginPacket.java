package Classes;

import java.io.*;

public class LoginPacket implements Serializable {
	public String username;
	public String password;

	public LoginPacket(String username, String password) {
		this.username = username;
		this.password = password;
	}

}