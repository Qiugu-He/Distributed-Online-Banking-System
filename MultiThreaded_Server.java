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
import java.sql.*;

public class SevMT{

	 public static void main(String[] args) {// The method run when the
	// server is started from the command line
	ServerSocket sock = null;    // server's master socket
	InetAddress addr = null;     // address of server
	Socket cli = null;           // client socket returned from accept	
	String DBName = null ;
	String userID = null;
	String userPw = "";

	if(args.length == 3)
	{
		DBName = args[0];
		userID =args[1];
		userPw = args[2];
	}
	else if(args.length == 2)
	{	
		boolean gotUps = false;
		while(!gotUps)
		{
			System.out.println("Please provide your user password: ");
			Scanner scan = new Scanner (System.in);
			userPw = scan.nextLine().trim();
			if(!userPw.equals(""))
			{
				gotUps = true;
			}
		}//while
	}
	else
	{
		System.out.println("Please run again with correct DBname, userID and user userPw: ");
		System.exit(0);
	}

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
	    new Thread(new myThread(cli, DBName, userID, userPw)).start();
	} // end while - accept

	// Will never get here - its a server!

     } //end main

}


//Threads class, actuall do the work
class myThread implements Runnable {

    Socket cliSock = null;  // socket for each client
							// *** This is instantiated per client whenever
							// a new Thread is create
    String DBName;
    String userID;
    String userPw;
    myThread(Socket csocket, String DBName, String userID, String userPw) {// constructor called by server for each client
	this.cliSock=csocket;
	this.DBName = DBName;
	this.userID = userID;
	this.userPw = userPw;
    } // end constructor

    public void run() {
    	InputStreamReader strm = null;
		BufferedReader strmIn = null;
		OutputStream out = null;


		 /* --------------------------------
			Make connection to JDBC
		-------------------------------------*/
		// First get a JDBC driver, in this case for a mySQL/MariaDB database
		System.out.println("Client accessing database... ");
		try {
	    	Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			System.out.println("Couldn't get the needed JDBC driver");
			e.printStackTrace();
			System.exit(44);
		} // end try-catch
		// Next, get a connection to the database
		Connection con=null;
		try {
			//String srvr = "silicon.cs.umanitoba.ca";
	    	String srvr = "127.0.0.1";
	    	String port = "3306";
	    	// construct URL address for database
	    	String db = DBName;
	    	String url = "jdbc:mysql://"+srvr+":"+port+"/"+db;
		
	    	String uid = userID;
	    	String pw = userPw;
	    	
	    	// make connection to database
	    	con = DriverManager.getConnection(url, uid, pw);
		} catch( Exception e ) {
			System.out.println("Connection failed");
	    	e.printStackTrace();
			System.exit(55);
		} // end try-catch
		System.out.println("Client connected to database successfully. ");


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

    				processOperation(request, reqSplit, out, close, con);

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
	public static synchronized boolean processOperation(String request, String[] reqSplit, OutputStream out, boolean close, Connection con)
	{	
		boolean toReturn = close;
		String ecoMsg = "";

		try{
			 //------------ E ------------
	      	if(request.equals("E"))
	      	{
				close = true;
				ecoMsg = "Client Terminated. ";
	      		out.write(ecoMsg.getBytes());
	      	}
	      	else if(reqSplit.length == 2)
	      	{	
	      		boolean check = isInteger(reqSplit[1]);
	      		if(check)
	      		{	
	      			//---------------C----------------------
		      		if(reqSplit[0].equals("C"))
		      		{
		      			CreateAcc(reqSplit,con, out);
		      		}

		      		//----------------R------------------------
		      		else if(reqSplit[0].equals("R"))
		      		{
		      			RetrivelAcc(reqSplit, con, out);
		      		}
		      		else
		      		{
		      			ecoMsg =  "Invalid command format, plase enter again.";
		      			out.write(ecoMsg.getBytes());
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
      				//--------------------D--------------------------
	      			if(reqSplit[0].equals("D") )
	      			{	
	      				if(!reqSplit[2].equals("0"))
	      				{
		      				DepositToAcc(reqSplit,con, out);
		      			}
		      			else
		      			{
		      				ecoMsg =  "Do you really want to deposit 0 to your account?";
		      				out.write(ecoMsg.getBytes());
		      			}
	      			}
	      			//--------------------W-------------------------
	      			else if(reqSplit[0].equals("W"))
	      			{	
	      				if(!reqSplit[2].equals("0"))
	      				{
	      					WdFromAcc(reqSplit, con, out);
	      				}
	      				else
		      			{
		      				ecoMsg =  "I really want to draw more money for you instead of 0..";
		      				out.write(ecoMsg.getBytes());
		      			}
	      			}
	      			else
	      			{
	      				ecoMsg =  "Invalid command format, plase enter again.";
		      			out.write(ecoMsg.getBytes());
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


	 //---------------------------------
    //	Create account
    //---------------------------------
	public static synchronized void CreateAcc(String [] reqSplit,  Connection con, OutputStream out)
	{
		 //check if account is already exit
		Statement st = null;
		String ecoMsg = "";
	    boolean validAcc =true;
	    int accN = Integer.parseInt(reqSplit[1]);
		try
		{
			st = con.createStatement();
			String sql ="SELECT accNum, balance FROM heq3456_BANKACCOUNTS";
			ResultSet rs = st.executeQuery(sql);
			while(rs.next())
			{
				//retrivel
				int accNumber = rs.getInt("accNum");
				if (accNumber == accN)
				{	
					validAcc = false;
					break;
				}
			}
			rs.close();
		}catch(SQLException se){
		se.printStackTrace();
		}

		//create account
		if(validAcc)
		{
			try
			{
				st = con.createStatement();

				String sql = "INSERT INTO heq3456_BANKACCOUNTS (accNum, balance) VALUES('"+ accN+"','"+ 0+"');";
				st.executeUpdate(sql);
				ecoMsg = "Creating Account " +accN + " Successfully. Current balance: 0.";
				try{
				out.write(ecoMsg.getBytes());
				}catch(Exception e)
				{
					System.out.println("Write to Client failed.");
				}

			}catch(SQLException se){
			se.printStackTrace();
			}
		}
		else
		{
			ecoMsg = "Creation Failed, account number is already exist. ";
			try{
			out.write(ecoMsg.getBytes());
			}catch(Exception e)
			{
				System.out.println("Write to Client failed.");
			}
		}

	}// end Create AAcc


	//---------------------------------
	//         Retrive Account
	//---------------------------------
	public static synchronized void RetrivelAcc(String [] reqSplit, Connection con, OutputStream out)
	{
		boolean validAcc = false;
		String ecoMsg = "";
	    int accN = Integer.parseInt(reqSplit[1]);
	    int amt = 0;
		Statement st = null;
		try
		{
			st = con.createStatement();
			String sql ="SELECT accNum, balance FROM heq3456_BANKACCOUNTS";
			ResultSet rs = st.executeQuery(sql);
			while(rs.next())
			{
				//retrivel
				int accNumber = rs.getInt("accNum");
				if (accNumber == accN)
				{	
					amt = rs.getInt("balance");
					validAcc = true;
					break;
				}
			}
			rs.close();
		}catch(SQLException se){
		se.printStackTrace();
		}

		if(validAcc)
		{
			ecoMsg = "Retriving account "+ accN+" successfully. Balance: "+ amt + " ";
			try{
		 	out.write(ecoMsg.getBytes());
		 	}catch(Exception e)
			{
				System.out.println("Write to Client failed.");
			}
		}
		else
		{
			ecoMsg = "Retriving failed. Account "+ accN + " does not existed.";
			try{
		 	out.write(ecoMsg.getBytes());
		 	}catch(Exception e)
			{
				System.out.println("Write to Client failed.");
			}	
		}

	}//end RetrivelAcc


	//-----------------------------
	//		Deposit to account
	//------------------------------
	public static synchronized void DepositToAcc(String [] reqSplit, Connection con, OutputStream out)
	{
		boolean validAcc = false;
		String ecoMsg = "";
	    int accN = Integer.parseInt(reqSplit[1]);
	    int amtDposit = Integer.parseInt(reqSplit[2]);
	  	int amt = 0;
	    int accNumber;
	    int newBalance = 0;
		Statement st = null;
		try
		{
			st = con.createStatement();
			String sql ="SELECT accNum, balance FROM heq3456_BANKACCOUNTS";
			ResultSet rs = st.executeQuery(sql);
			while(rs.next())
			{
				//retrivel
			    accNumber = rs.getInt("accNum");
				if (accNumber == accN)
				{	
					amt = rs.getInt("balance");
					validAcc = true;
					break;
				}
			}
			rs.close();
		}catch(SQLException se){
		se.printStackTrace();
		}

		if(validAcc)
		{	
			try
			{
				newBalance = amt + amtDposit;
				String sql = "UPDATE heq3456_BANKACCOUNTS " + 
							 " SET balance = ? WHERE accNum  = ?";
				PreparedStatement preparedSt = con.prepareStatement(sql);
				preparedSt.setInt(1, newBalance);
				preparedSt.setInt(2, accN);
				preparedSt.executeUpdate();
			}catch(SQLException se){
			se.printStackTrace();
			}

			ecoMsg = "Deposit to account "+ accN+" successfully. Now balance: "+ newBalance + ".";
			try{
		 	out.write(ecoMsg.getBytes());
		 	}catch(Exception e)
			{
				System.out.println("Write to Client failed.");
			}
		}
		else
		{
			ecoMsg = "Deposit to account failed. Account "+ accN + " does not existed.";
			try{
		 	out.write(ecoMsg.getBytes());
		 	}catch(Exception e)
			{
				System.out.println("Write to Client failed.");
			}
		}
	}// end DepositToAcc


	//-----------------------------------------
	//				WithDraw from account
	//-------------------------------------------
	public static synchronized void WdFromAcc(String [] reqSplit, Connection con, OutputStream out)
	{
		boolean validAcc = false;
		String ecoMsg = "";
	    int accN = Integer.parseInt(reqSplit[1]);
	    int amtWd = Integer.parseInt(reqSplit[2]);
	    int amt = 0;
	    int accNumber;
	    int newBalance = 0;
		Statement st = null;
		try
		{
			st = con.createStatement();
			String sql ="SELECT accNum, balance FROM heq3456_BANKACCOUNTS";
			ResultSet rs = st.executeQuery(sql);
			while(rs.next())
			{
				//retrivel
			    accNumber = rs.getInt("accNum");
				if (accNumber == accN)
				{	
					amt = rs.getInt("balance");
					validAcc = true;
					break;
				}
			}
			rs.close();
		}catch(SQLException se){
		se.printStackTrace();
		}

		if(validAcc)
		{	
			if(amtWd <= amt)
			{
				try
				{
					newBalance = amt - amtWd;
					String sql = "UPDATE heq3456_BANKACCOUNTS " + 
								 "SET balance = ? WHERE accNum =?";
					PreparedStatement preparedSt = con.prepareStatement(sql);
					preparedSt.setInt(1, newBalance);
					preparedSt.setInt(2, accN);
					preparedSt.executeUpdate();
				}catch(SQLException se){
				se.printStackTrace();
				}

				ecoMsg = "Withdraw from account "+ accN+" successfully. New balance: "+ newBalance + ".";
				try{
			 	out.write(ecoMsg.getBytes());
			 	}catch(Exception e)
				{
					System.out.println("Write to Client failed.");
				}
			}
			else
			{
				ecoMsg = "Withdraw from account failed. Account "+ accN + " does not have enough money. Current balance: "+amt;
				try{
			 	out.write(ecoMsg.getBytes());
			 	}catch(Exception e)
				{
					System.out.println("Write to Client failed.");
				}
			}
		}
		else
		{
			ecoMsg = "Deposit to account failed. Account "+ accN + " does not existed.";
			try{
		 	out.write(ecoMsg.getBytes());
		 	}catch(Exception e)
			{
				System.out.println("Write to Client failed.");
			}
		}
	}// end DepositToAcc

} // end class:SERVERCLASS

