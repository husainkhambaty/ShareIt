package core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ShareItConnection {
	
	private Socket connection = null;
	
	private String host = null;
	private int port = ShareItService.SERVICE_DEFAULT_PORT;
	
	private DataOutputStream dataOut = null;
	private BufferedOutputStream out = null;
	
	public ShareItConnection() {
		
	}
	
	public ShareItConnection(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	private void connect() {
		try {
			this.connection = new Socket(this.host, this.port);
			this.connection.setKeepAlive(true);
			
			this.out = new BufferedOutputStream(this.connection.getOutputStream());
			this.dataOut = new DataOutputStream(this.out);
			
			System.out.println("Connected to server " + this.connection);
			
		
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.disconnect();
		} 
	}
	
	private void disconnect() {
		try {
			if (this.dataOut != null) {
				this.dataOut.writeByte(ShareItService.SEND_END_CONNECTION);
				this.dataOut.close();
			}
			if (this.connection != null) 
				this.connection.close();
			
			System.out.println("Disconnected from server");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private synchronized final boolean send(File file) throws IOException {
		
		System.out.println("Sending file " + file.getName() + "(" + file.length() + " bytes)");
		
		BufferedInputStream bis = null;
		boolean ret = false;
		
		try {
			
			this.dataOut.writeUTF(file.getName());
			this.dataOut.writeLong(file.length());
			
			this.dataOut.flush();
			
			bis = new BufferedInputStream(new FileInputStream(file));
			int fileByte = -1;
			
			while ((fileByte = bis.read()) != -1) { 
				//System.out.print(fileByte + " ");
				this.out.write(fileByte);
			}
			System.out.println("");
			
			this.out.flush();
			bis.close();
			ret = true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret = false;
		} finally {
			if (bis != null) bis.close();
			System.out.println("File sent\n------------------------------------");
			
		}
		return ret;
		
	}
	
	private final void sendDirectory(File dir) throws IOException {
		File[] files = dir.listFiles();
		
		this.sendMultipleFiles(files);
		
	}
	
	private final void sendMultipleFiles(File[] files) throws IOException {
		
		
		// send the command that needs to be performned
		this.sendCommand(ShareItService.SEND_MULTIPLE_FILES);
		
		// send the files that need to be sent
		this.dataOut.writeInt(files.length);
		
		// off you go
		this.dataOut.flush();
		
		// send the individual files
		for (File file: files) {
			
			this.send(file);
			
		}
		
		
	}
	
	private final void sendFile(File file) throws IOException {
		this.sendCommand(ShareItService.SEND_FILE);
		this.dataOut.flush();
	}
	
	private void sendMessage(String message) {
		
	}
	
	private void sendCommand(int command) throws IOException {
		this.dataOut.write(command);
	}
	
	public static void main(String[] args) {
		
		String root = "/Users/apple/Scripts";
		ShareItConnection conn = null;
		try {
		
			conn = new ShareItConnection();
			conn.connect();
			
			//conn.sendFile(new File(root + "/test1/file2.pdf"));
			conn.sendDirectory(new File(root + "/test1"));
			conn.sendDirectory(new File(root + "/test2"));
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			conn.disconnect();
		}
	}
}
