package ssgulati_CSCI201_Assignment3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread{

	private ObjectInputStream input;
	private ObjectOutputStream output;
	private MainServer ms;
	private int balance;
	private boolean free;
	private int profit;
	private ArrayList<Trade> assignedTrades;
	private int serialNum;
	boolean end;
	Socket s;
	
	public ServerThread(Socket s, MainServer ms, Integer bal, int serial) {
		try 
		{
			//main searver to broadcast/communicate across server/client connections
			this.ms = ms;
			this.balance = bal;
			profit = 0;
			this.s = s;
			end = false;
			
			output = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
			output.flush();
			input = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
			
			this.free = true;
			this.assignedTrades = new ArrayList<Trade>();
			
			this.setSerialNum(serial);
			
			//input (to get information from client / get input from socket)
			//br = new BufferedReader(snew InputStreamReader(s.getInputStream()));
			
			this.start();
		}
		catch (IOException ioe)
		{
			System.out.println("ioe in ServerThread constructor: " + ioe.getMessage());
			ioe.printStackTrace();
		}
	}
	
	//to send info to agent about what trades it's been assigned or how many agents left, etc
	public void sendMessage(Object o)
	{
		try {
			//add information to output stream
			output.writeObject(o);
			//System.out.println("Sending " + o);
			//flush it / send it across socket to the client
			output.flush();
		} catch (IOException ioe) {
			System.out.println("ioe in ServerThread sendMessage: " + ioe.getMessage());
			ioe.printStackTrace();
		}
	}
	
	public void endGame()
	{		
		sendMessage(ms.getIncompleteList());
	}
	
	public void run()
	{
		try {
			//only do this while not something that represents being done isn't called
			String line = "";
			while (!(line.equals("done")))
			{
				//blocks and waits for input from connected client
				line = (String)input.readObject();

				if (line.equals("free"))
				{
					free = true;
				}
				//sends the information from this client to broadcast method of mainServer to have info sent to rest
				//ms.broadcast(line, this);s
			}
			ms.done(this);
		} catch (IOException ioe)
		{
			System.out.println("ioe in ServerThread.run(): " + ioe.getMessage());
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			System.out.println("cnfe ServerThread.run(): " + cnfe.getMessage());
			cnfe.printStackTrace();
		}
	}
	
	//return the balance (can be called from TradeThreads
	public int getBalance()
	{
		return balance;
	}
	
	//returns true is the associated client is free, returns false if is busy
	public boolean isFree()
	{
		return free;
	}
	
	public void makeBusy()
	{
		free = false;
	}
	
	//to change the balance once client is done
	public void removeBalance(int changeBy)
	{
		balance -= changeBy;
	}
	
	//returns sum of all sales done by this agent
	public int getProfit()
	{
		return profit;
	}
	
	public void increaseProfit(int changeBy)
	{
		profit += changeBy;
	}
	
	//function called to attempt to assign trade, only when know this agent has enough balance
	public boolean assignTradeAttempt(Trade t) {
		
		//if get here, then are not busy
		
		//if is a selling transaction
		if (t.getAction() < 0)
		{
			profit += t.getPrice() * Math.abs(t.getAction());
			assignedTrades.add(t);	
			return true;
		}
		
		
		//if have enough balance to do purchase, then assign to purchase it and move on
		else if ((balance - (t.getPrice() * t.getAction())) >= 0) 
		{
			//update balance, add to assigned trades, move on to check next trade, and therefore set the numChecks counter to 0 for that trade
			balance -= (t.getPrice() * t.getAction());
			assignedTrades.add(t);
			return true;
		}
		
		//if get here, do not have enough balance
		else
		{
			return false;
		}
		
	}

	public void doTrades()
	{
		//if have trades to do, go at it
		if (assignedTrades.size() > 0)
		{
			//send list of assigned trades to client 
			sendMessage(assignedTrades);
			
			//set to busy
			free = false;
			
			//clear assignedTrades
			assignedTrades = new ArrayList<Trade>();
		}
		//if have nothing assigned, don't do anything
	}
	
	//setter for balance
	public void setBalance(int balance) {
		this.balance = balance;
		
	}

	public int getSerialNum() {
		return serialNum;
	}

	public void setSerialNum(int serialNum) {
		this.serialNum = serialNum;
	}

}
