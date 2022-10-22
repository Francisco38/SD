//package hello;

import java.io.File;
import java.rmi.*;

import Classes.LoginPacket;
import Classes.User;

public interface ucDrive extends Remote {
	public String register(User userData) throws java.rmi.RemoteException;

	public File[] listFiles(String user_id) throws java.rmi.RemoteException;

	public long storageInfo(String user_id) throws java.rmi.RemoteException;
        
        public int changePing(String v1,String v2) throws RemoteException;
        
        public int getMaxFailed() throws RemoteException;
        
        public int getPeriod() throws RemoteException;
        
        public String checkReplication() throws RemoteException;
}
