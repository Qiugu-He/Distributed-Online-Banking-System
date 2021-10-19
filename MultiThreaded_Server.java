// ----------------------------------------------
//    REMARK: SevMT.java
//	- This is the stream server code which accepts connections from clients
//	  and echos the response message sent over the connection to the screen until
//	  a E is received over the connection. The server support mutiple threads, which 
//	  allows mutiple client connection to the server. Noted that in order to 
//	  avoid concurrence, this class us synchornize key works to allowed that only
//	  one client can access the shared data.
//-----------------------------------------------

import java.net.*;
import java.io.*;
import java.util.*;

public class SevMT{

	 public static void main(String[] args) {// The method run when the
	// server is started from the command line
	ServerSocket sock = null;    // server's master socket
	InetAddress addr = null;     // address of server
	Socket cli = null;           // client socket returned from accept	
	System.out.println("Server starting.");

	// Create main ServerSocket
	try {
	    addr = InetAddress.getLocalHost();
	    sock = new ServerSocket(13254,3,addr); // create server socket: // put your own port number 
	} catch (Exception e) {
	    System.err.println("Creation of ServerSocket failed.");
	    System.exit(1);
	} // end try-catch

	// Loop forever accepting client connections
	while (1==1) {
	    // Accept a connection
	    try {
		cli = sock.accept(); // accept a connection from client,
		// returns socket to client
	    } catch (Exception e) {
		System.err.println("Accept failed.");
		System.exit(1);
	    } // end try-catch
	    
	    // Create a thread for this client (which starts our run() method
	    new Thread(new myThread(cli)).start();
	} // end while - accept

	// Will never get here - its a server!

     } //end main

}


//Threads class, actuall do the work
class myThread implements Runnable {

    Socket cliSock = null;  // socket for each client
    // *** This is instantiated per client whenever
    // a new Thread is created

   private static Hashtable <String, Integer>table = new Hashtable<String, Integer>();

    myThread(Socket csocket) {// constructor called by server for each client
	this.cliSock=csocket;
    } // end constructor

    public void run() {
    	InputStreamReader strm = null;
		BufferedReader strmIn = null;
		OutputStream out = null;

    	//read stream from Client
    	try{
    		strm = new InputStreamReader(cliSock.getInputStream());
    		strmIn = new BufferedReader(strm);

    		//write message to Client
    		out = cliSock.getOutputStream();

    	} catch(Exception e){
    		System.out.println("Couldn't create socket input stream.");
		    System.exit(1);
    	}

    	//Read request from the connection and print them
    	String request = "";
    	//String ecoMsg = "";
    	String reqSplit[];
    	boolean close = false;
    	do{
    		try{
    			while((request = strmIn.readLine())!= null)
    			{
    				System.out.println("Request received: " + request);

    				reqSplit = request.split("<|,|>");

    				processOperation(request, reqSplit, out, close);

    			}//while
    		}catch (Exception e) {
		       System.out.println("Socket input failed.");
		       System.exit(1);
		   }
    	}while(!close);

		// close the socket
		try {
		    cliSock.close();
		    //cli.close();
		    strm.close();
		    out.close();
		    strmIn.close();
		} catch (Exception e) {
		    System.err.println("couldn't close client socket.");
		    System.exit(1);
		} // end try-catch

    }// end run 


     //simply check is the input string is integer
    public static boolean isInteger(String s) {
    return isInteger(s,10);
	}//end isInteger
	public static boolean isInteger(String s, int radix) {
		if(s.isEmpty()) return false;
		for(int i = 0; i < s.length(); i++) {
	    if(i == 0 && s.charAt(i) == '-') {
	        if(s.length() == 1) return false;
	        else continue;
	    }
	    if(Character.digit(s.charAt(i),radix) < 0) return false;
	}
	return true;
	}//end isInteger



// ----------------------------------------------
// processOperation
// Parameter: request, the requst line from client
//            out, the outputstream that response to client
//
// Remark: this methods process the request operation from 
//		   client and send the response message back to client
//-----------------------------------------------
	public static synchronized boolean processOperation(String request, String[] reqSplit, OutputStream out, boolean close)
	{	
		boolean toReturn = close;
		String ecoMsg = "";

		try{
			 //Command E
	      	if(request.equals("E"))
	      	{
	      		synchronized(myThread.class)
	      		{
				close = true;
				ecoMsg = "Client Terminated. ";
	      		out.write(ecoMsg.getBytes());
      			}
	      	}
	      	//Command C and R
	      	else if(reqSplit.length == 2)
	      	{	
	      		boolean check = isInteger(reqSplit[1]);
	      		if(check)
	      		{	
	      			synchronized(myThread.class)
	      			{
	      			//C Command
		      		if(reqSplit[0].equals("C"))
		      		{
		      			//ecoMsg = "Creating Account. ";
		      			//out.write(ecoMsg.getBytes());

		      			//seatch from table check if it existed
		      			if(table.containsKey(reqSplit[1]))
		      			{
		      				ecoMsg = "Creation Failed, account number is already exist. ";
		      				out.write(ecoMsg.getBytes());
		      			}
		      			else
		      			{	
		      				table.put(reqSplit[1], 0);
		      				ecoMsg = "Creating Account " +reqSplit[1] + " Successfully. Balance: "+ table.get(reqSplit[1]) ;
		      				out.write(ecoMsg.getBytes());
		      			}
		      		}

		      		//R Command
		      		else if(reqSplit[0].equals("R"))
		      		{
		      			if(table.containsKey(reqSplit[1]))
		      			{
		      				ecoMsg = "Retrieving Account " +reqSplit[1]+" successfully. Balance: "+ table.get(reqSplit[1]) + " ";
		      				out.write(ecoMsg.getBytes());
		      			}
		      			else
		      			{
		      				ecoMsg = "Retrieving Failed. Account "+ reqSplit[1] + " does not existed.";
		      				out.write(ecoMsg.getBytes());
		      			}
		      		}
		      		else
		      		{
		      			ecoMsg =  "Invalid command format, plase enter again.";
		      			out.write(ecoMsg.getBytes());
		      		}
		      	}
		      	}
		      	else
		      	{
		      		ecoMsg = "Invalid command format, plase enter again.";
		      		out.write(ecoMsg.getBytes());
		      	}

	      	}
	      	//D and W
	      	else if(reqSplit.length == 3)
	      	{
	      		boolean check = isInteger(reqSplit[1]);
	      		boolean check2 = isInteger(reqSplit[2]);
	      		if(check && check2)
	      		{	
	      			synchronized(myThread.class)
	      			{
	      			if(reqSplit[0].equals("D") && !reqSplit[2].equals("0"))
	      			{
		      			if(table.containsKey(reqSplit[1]))
		      			{
		      				int val = table.get(reqSplit[1]);
		      				table.put(reqSplit[1],val + Integer.parseInt(reqSplit[2]));
		      				ecoMsg = "Deposit to Account: "+ reqSplit[1]+ " Successfully. New balance: "+ table.get(reqSplit[1]);
		      				out.write(ecoMsg.getBytes());
		      			}
		      			else
		      			{
		      				ecoMsg = "Deposit Failed, Account " + reqSplit[1] + " does not exist.";
		      				out.write(ecoMsg.getBytes());
		      			}
	      			}
	      			else if(reqSplit[0].equals("W") && !reqSplit[2].equals("0"))
	      			{
	      				//ecoMsg = "Withdraw from Account: "+ reqSplit[1] + " with amount "+reqSplit[2];
		      			//out.write(ecoMsg.getBytes());
		      			if(table.containsKey(reqSplit[1]))
		      			{
		      				int val = table.get(reqSplit[1]);
		      				if(Integer.parseInt(reqSplit[2]) > val)
		      				{
		      					ecoMsg = "Withdraw from account: " + reqSplit[1]+" failed, does not enough money. Current blanace: "+ table.get(reqSplit[1]);
		      					out.write(ecoMsg.getBytes());
		      				}
		      				else
		      				{
		      					table.put(reqSplit[1],val-Integer.parseInt(reqSplit[2]) );
		      					ecoMsg = "Withdraw from account: " + reqSplit[1]+ " successfully. New balance: "+ table.get(reqSplit[1]);
		      					out.write(ecoMsg.getBytes());
		      				}
		      				
		      			}
		      			else
		      			{
		      				ecoMsg = "Withdraw Faile, Account " + reqSplit[1] + " does not exist.";
		      				out.write(ecoMsg.getBytes());
		      			}
	      			}
	      			else
	      			{
	      				ecoMsg =  "Invalid command format, plase enter again.";
		      			out.write(ecoMsg.getBytes());
	      			}
	      			}

	      		}
	      		else
	      		{
	      			ecoMsg = "Invalid command format, plase enter again.";
		      		out.write(ecoMsg.getBytes());
	      		}
	      	}
	      	else
	      	{
	      		ecoMsg = "Invalid Command, Please enter again. ";
	      		out.write(ecoMsg.getBytes());
	      	}


      	request += '\n';
      	//echo back to Client
      	String nl = "\n";
      	out.write(nl.getBytes());
      }
      catch (Exception e) {
		    System.out.println("Server couldn't close a socket.");
		    System.exit(1);
	}

     return toReturn;

	}//end processOperation

} // end class:SERVERCLASS
