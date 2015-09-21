package core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;



/**
 * Service to help share files and messages
 * Help sync folders
 * @author Husain Khambaty
 * @version 1.0
 *
 */
public final class ShareItService {
	
	/* === CONSTANTS === */
	private static final long SLEEP_TIMEOUT = 1000;
	protected static final int SERVICE_DEFAULT_PORT = 10052;
	
	protected static final int SEND_FILE = 1;
	protected static final int SEND_MULTIPLE_FILES = 2;
	protected static final int SEND_MESSAGE = 5;
	protected static final int SEND_END_CONNECTION = 30;
	
	private static final String DEFAULT_SERVER_STORE = "/Users/apple/Scripts/output";
	
	// service connection
	private ServerSocket server = null;
	
	// set the default port
	private int port = ShareItService.SERVICE_DEFAULT_PORT;
	private File store = null;
	
	private ArrayList<ShareItConnectionThread> clientList = null;
	
	private boolean isConnected = false;
	private boolean acceptConnections = false;
	
	/**
	 * Default Constructor
	 */
	public ShareItService() {
		
	}
	
	/**
	 * Constructor
	 * @param port
	 */
	public ShareItService(int port) {
		super();
		this.port = port;
	}
	
	private void setStore(String storeName) {
		
		File store = new File(storeName);
		
		// check if the store exists and if its a dir
		if (store.exists() && store.isDirectory()) {
			this.store = store;
		} else {
			// else set the default - assuming this location is available
			this.store = new File(ShareItService.DEFAULT_SERVER_STORE);
		}
	} 
	
	protected File getStore() {
		return this.store;
	}
	
	
	/**
	 * Start the ShareIt service
	 */
	private void startService() {
		try {
			this.server = new ServerSocket(this.port);
			this.clientList = new ArrayList<>();
			this.isConnected = true;
			this.acceptConnections = true;
			
			// while we're online
			while (this.isConnected) {
				
				// while we're accepting requests
				while (this.acceptConnections) {
					
					System.out.println("Waiting for connections.... ");
					
					// accept a new connection and add it to the client list. Also initiate the client thread
					this.clientList.add(new ShareItConnectionThread(this.server.accept()));
					
				}
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			this.isConnected = false;
			this.acceptConnections = false;
			
			System.err.println("Exception while trying to start ShareIt service");
			e.printStackTrace();
			
		} finally {
		
			// gradually stop the service
			this.stopService();
		}
	}
	
	private void stopService() {
		if (isConnected) {
			try {
				this.server.close();
				this.isConnected = false;
				this.acceptConnections = false;
				System.out.println("Current connetions " + this.clientList.size());
				
				this.clientList.clear();
				this.clientList = null;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Exception while trying to stop ShareIt service");
				e.printStackTrace();
			}
			
		}
	}
	
	
	private void restartService() {
		
	}
	
	private void acceptConnections() { this.acceptConnections = true; }
	private void denyConnections() { this.acceptConnections = false; }
	
	
	/**
	 * Connection Thread class to help accept multiple connections
	 * @author Husain Khambaty
	 *
	 */
	private class ShareItConnectionThread extends Thread {
		
		private Socket connection = null;
		private DataInputStream dataIn = null;
		//private DataOutputStream dataOut = null;
		
		private BufferedInputStream in = null;
		
		private boolean keepAlive = false;
		
		private static final String DEFAULT_SERVER_STORE = "/Users/apple/Scripts/output"; // TODO: temp. Later configuration will replace this.
		
		/**
		 * ConnectionThread Constructor
		 * @param connection
		 * @throws IOException 
		 */
		public ShareItConnectionThread(Socket connection) throws IOException {
			this.connection = connection;
			
			this.in = new BufferedInputStream(this.connection.getInputStream());
			
			this.dataIn = new DataInputStream(this.in);
			//this.dataOut = new DataOutputStream(this.connection.getOutputStream());
			
			System.out.println("Connected to " + this.connection);
			this.keepAlive = true;
			this.start();
		}
		
		public void run() {
			
			try {
				
				while (this.keepAlive) {
					
					int command = dataIn.read();
					String filename = null;
					long length = 0;
					
					switch(command) {
						case (ShareItService.SEND_FILE):
							
							filename = dataIn.readUTF();
							length = dataIn.readLong();
							
							this.receiveFile(filename, length);
							
							// TODO: tell the connection ALL_IS_GOOD
						
							break;
						
						case (ShareItService.SEND_MULTIPLE_FILES):
							
							int fileCount = dataIn.readInt();
							
							System.out.println("Receiving Multiple files " + fileCount);
						
							for (int i = 0; i < fileCount; i++) {
								
								filename = dataIn.readUTF();
								length = dataIn.readLong();
								
								this.receiveFile(filename, length);
							}
							
							// TODO: tell the connection ALL_IS_GOOD
							
							break;
						
						case (ShareItService.SEND_MESSAGE):
							
							String message = dataIn.readUTF();
							System.out.println("MESSAGE: " + message);
							
							break;
						case (ShareItService.SEND_END_CONNECTION):
							this.keepAlive = false;
							break;
						
						default:
					}
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			} catch (Exception e) {
				System.err.println("Nasty error");
				e.printStackTrace();
				
			} finally {
				try {
					if (dataIn != null) dataIn.close();
					//if (dataOut != null) dataOut.close();
					if (this.connection.isConnected()) this.connection.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void receiveFile(String filename, long length) throws IOException {
			
			BufferedOutputStream bos = null;
			
			try {
				
				File file = new File(ShareItConnectionThread.DEFAULT_SERVER_STORE + "/" + filename);
				System.out.println("Receiving file " + file.getAbsolutePath() + "(" + length + " bytes)");
				
				bos = new BufferedOutputStream(new FileOutputStream(file));
				
				for (int i = 0; i < length; i++) {
					bos.write(this.in.read());
				}
				bos.close();
				System.out.println("File received");
				
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (bos != null) bos.close();
				System.out.println("Buffer closed\n------------------------------------");
			}
			
		}
		
	}
	
	public static void main(String[] args) {
		ShareItService service = new ShareItService();
		service.startService();
		
	}
}
