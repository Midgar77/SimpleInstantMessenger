import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


public class Client extends JFrame{

	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	
	
	public Client(String host){
		super("Client");
		this.setSize(400, 400);
		serverIP = host;
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						sendMessage(event.getActionCommand());
						userText.setText("");
					}
				}
				);
		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		setVisible(true);
	}
	
	//connect to server
	public void startRunning(){
		try{
			connectToServer();
			setupStreams();
			whileChatting();
		}catch(EOFException eof){
			showMessage("\n Client terminated the connection");
		}catch(IOException io){
			System.out.println("start running");
			io.printStackTrace();
		}finally{
			closeStreams();
		}
	}
	
	
	//connect to server
	private void connectToServer() throws IOException{
		showMessage("Attempting connection...");
		connection = new Socket(InetAddress.getByName(serverIP), 6789);     //6789 was the same number server used. Must get IP from server
		showMessage("Connected to: " + connection.getInetAddress().getHostName());   //Shows IP address of server we are connecting to
	}
	
	
	//Sets up streams to send and receive messages
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are now set up! \n");
	}
	
	
	//while chatting with the server
	public void whileChatting() throws IOException{
		ableToType(true);
		do{
			
			try{
				message = (String) input.readObject();
				showMessage("\n" + message);
			}catch(ClassNotFoundException e){
				showMessage("\n I don't know that object type");
			}
			
		}while(!message.equalsIgnoreCase("SERVER - END"));
	}
	
	//close streams and sockets after you are done chatting
		public void closeStreams(){
			showMessage("\n Closing connections... \n");
			ableToType(false);
			
			try{
				output.close();
				input.close();
				connection.close();
			}catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
		
		//send messages to server
		private void sendMessage(String text){
			try{
				output.writeObject("CLIENT - " + text);  //Actually sends the message using the ObjectOutputStream
				output.flush();
				showMessage("\nCLIENT - " + text);   //Shows the message in the text area box
			}catch(IOException exception){
				chatWindow.append("\n ERROR: Can't send that message");
			}
		}
		
		
		//updates messages in the chat window
		public void showMessage(final String text){
			SwingUtilities.invokeLater(
					new Runnable(){
						public void run(){
							chatWindow.append(text);
						}
					}
					);
		}
		
		//let the user type in their box
		private void ableToType(final boolean ableToType){
			SwingUtilities.invokeLater(
					new Runnable(){
						public void run(){
							userText.setEditable(ableToType);
						}
					}
					);
		}
		
		
		
		
		
		public static void main(String[] args) {
			
			//A JOptionPane asks you for your IP Address
			String IPAddress = JOptionPane.showInputDialog("Enter the IP Address of the Client Program.", "IP Address"); 
			Client client = new Client(IPAddress);
			client.startRunning();
			
		}
		
	
}
