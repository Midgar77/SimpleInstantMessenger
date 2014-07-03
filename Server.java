import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Server extends JFrame{

	
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	
	
	public Server(){
		super("Instant Messenger");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
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
		//chatWindow.setEditable(false);
		add(new JScrollPane(chatWindow));
		setSize(400,400);
		setVisible(true);
	}
	
	
	public void startRunning(){
		try{
			server = new ServerSocket(6789, 100);
			while(true){
				try{
					//connect and have conversation
					waitForConnection();
					setupStreams();
					whileChatting();
				}catch(EOFException eofException){
					showMessage("\n Server ended the connection!");
				}finally{
					closeStreams();
				}
			}
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	//wait for connection, then display connection information
	private void waitForConnection() throws IOException{
		showMessage(" Waiting for someone to connect... \n");
		connection = server.accept();
		showMessage(" Now connected to " + connection.getInetAddress().getHostName());
	}
	
	//get stream tos end and receive data
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are now set up! \n");
	}
	
	//during the chat conversation
	private void whileChatting() throws IOException{
		String message = "You are now connected!";
		sendMessage(message);
		ableToType(true);
		
		do{
			try{
				message = (String) input.readObject();
				showMessage("\n" + message);
			}catch(ClassNotFoundException notFoundException){
				showMessage("\n I don't know what that user sent");
			}
		}while(!message.equals("CLIENT - END"));
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

	private void sendMessage(String text){
		try{
			output.writeObject("SERVER - " + text);  //Actually sends the message using the ObjectOutputStream
			output.flush();
			showMessage("\nSERVER - " + text);   //Shows the message in the text area box
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
	
	
	
	public static void main(String[] args) {
		Server server = new Server();
		server.startRunning();
	}
	
}
