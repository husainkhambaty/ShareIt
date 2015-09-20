package core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;

public final class ShareItService {
	
	private ServerSocket server = null;
	private int port = 10052;
	
	private DataInputStream dataIn = null;
	private DataOutputStream dataOut = null;
	
	private boolean isConnected = false;
	
	
	public ShareItService(int port) {
		super();
		this.port = port;
	}

	private void startService() {
		try {
			this.server = new ServerSocket(this.port);
			this.isConnected = true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			this.isConnected = false;
			System.err.println("");
			e.printStackTrace();
			
		}
	}
	
	private void stopService() {
		
	}
	
	private void restartService() {
		
	}
	
	
	
	public static void main(String[] args) {
		
	}
}
