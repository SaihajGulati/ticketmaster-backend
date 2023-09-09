package ssgulati_CSCI201_Assignment3;    

import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.File;

public class MainServer {

	private int port;

	//will hold serverThreads
	List<ServerThread> serverThreads;

	//to handle how many connections there are currently
	private int numConnections;
	
	//based on number of agents, how many connections max
	private int maxConnections;
	
	private Scanner sc;
	
	//map of serial number for agent and their balance
	private SortedMap<Integer, Integer> am;
	
	//map of times and trades to be initialized at that time
	private SortedMap<Integer, ArrayList<Trade>> ttm;
	
	private ArrayList<Trade> incompleteTrades;

	private List<ServerThread> doneThreads;
	
	//constructor
	public MainServer(int port) 
	{
		
		this.port = port;
		
		sc = new Scanner(System.in);
		
		incompleteTrades = new ArrayList<Trade>();
		
		doneThreads  = new ArrayList<ServerThread>();
		
		//this will indicate to client that this is the incomplete list
		incompleteTrades.add(null);
		
		//make list of trades after getting correct file and one that is properly formatted
		ArrayList<Trade> tradeList = makeTradeList();

		//group trades by start time
		ttm = makeTimeTradeMap(tradeList);
		
		//get list for agents after getting file for agents that exists and then get an agent file that is properly formatted
		am = makeAgentMap();
		 
		setupNetworking();
		
		System.out.println("Starting service.");
		

		for (Trade t : incompleteTrades)
		{
			if (t != null)
			{
				System.out.println("Invalid EventID (" + t.getApiID() + "). Discard this transaction and continue...");
			}
		}
		
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(ttm.size());
		
		int count = 0; 
		
		//loop through all times in map
		for (Integer i : ttm.keySet())
		{
			count++;
			boolean end;
			if (count == ttm.keySet().size())
			{
				end = true;
			}
			else
			{
				end = false;
			}
			if (i == 0) //time is 0
			{
				//pass in true so we know the time it's starting is 0
				TradeThread t = new TradeThread(ttm.get(i), this, end);
				
				//start execution immediately
				scheduler.execute(t);
			}
			else
			{
				//make trade thread that is passed list of trades at a certain time
				TradeThread t = new TradeThread(ttm.get(i), this, end);
				
				//schedule it to go after a delay of i seconds, which is startTime
				scheduler.schedule(t, i, TimeUnit.SECONDS);
			}
				
		}
		
		//say shutdown so does not accept any more tasks and works to finish what has
		scheduler.shutdown();
		
		//until done, keep on switching to next thread
		while (!scheduler.isTerminated())
		{
			Thread.yield();
		}
		
		
				
	}
	
	public void setupNetworking()
	{
		numConnections = 0;
		maxConnections = am.size();		
		
		//networking connections
		try
		{
			//wait for connection (print as such)
			System.out.println("Listening on port " + port + ".");
			ServerSocket ss = new ServerSocket(port);
			
			//create a synchronized list backed by an arrayList storing type serverThread
			serverThreads = Collections.synchronizedList(new ArrayList<ServerThread>()); 
			
			//set here at first because need to be able to access after while loop
			ServerThread st = null;
			
			//run for each agent in list(which is same as runninig this for the number of connections max)
			//the agents information will be presented in ascending order of serial number since it's a sorted map
			for (Entry<Integer, Integer> entry: am.entrySet())
			{
				//if don't have any agents yet, print appropiate message
				if (numConnections == 0)
				{
					System.out.println("Waiting for agents...");
				}
				//if don't have enough agents yet, but have at least 1, print correct message
				else if (numConnections < maxConnections)
				{
					System.out.println("Waiting for " + (maxConnections - numConnections) + " more agent(s)...");
				}				
				
				//the socket will use to communicate is blocking to accept the connection at a server socket
				Socket s = ss.accept();
				
				//now that have made connection between client socket and a server socket, pass this connection to a server thread
				numConnections++;
				
				//pass in socket, this as the class to broadcast from, and value from agent entry which is balance
				//pass in key of serverThread just for debugging purposes
				st = new ServerThread(s, this, entry.getValue(), entry.getKey());
				
				//add this server thread to our list of server threads
				serverThreads.add(st);
				
				//immediately send a message telling the server thread to tell the client how many connections are left to make
				st.sendMessage(maxConnections-numConnections);
				
				//tell every other serverThread agent how many are left so they can update
				broadcast(maxConnections-numConnections, st);
				
				//print out correct things to indicate
				System.out.println("Connection from: " + s.getInetAddress());
			}
			
			//set list of serverThreads in TradeThread class to have correct list
			TradeThread.setServerThreads(serverThreads);
			
			ss.close(); //done tryna make new connection, so close the serverSocket, which can bc socket is separate
			
		}
		catch (IOException ioe)
		{
			System.out.println("ioe in MainServer constructor: " + ioe.getMessage());
			ioe.printStackTrace();
			//release semaphore so someone else can try to do stuff
			//sem.release(); //only release if have error of some sort
		}
	}
	
	public static void main (String[] args)
	{
		@SuppressWarnings("unused")
		MainServer server = new MainServer(3456);

	}

	public SortedMap<Integer, ArrayList<Trade>> makeTimeTradeMap(ArrayList<Trade> tradeList) {
		
		//create hashmap to store times and all trades at that time
		SortedMap<Integer, ArrayList<Trade>> timeTrades = new TreeMap<Integer, ArrayList<Trade>>();
		
		//for loop to go through each thing in tradeList
		for (Trade trade : tradeList)
		{
			int time = trade.getTime();
			
			//the ccurrent list of trades at that time is the list returned by calling get on the hashmap
			ArrayList<Trade> currList = timeTrades.get(time);
			
			// if list does not exist, create it
		    if(currList == null) {
		    	
		    	//create list of trades at this time and add this trade to it
		    	currList = new ArrayList<Trade>();
		    	currList.add(trade);
		    	
		    	//put this list into the hashmap, with the time as the key
		    	timeTrades.put(time, currList);
		    } else {
		        // add if item is not already in list, will update in map because is return by reference since is an object
		        currList.add(trade);
		    }
		}
		
		return timeTrades;
	}
	
	public ArrayList<Trade> makeTradeList()
	{		
		//make array/list for storing trades in order
		ArrayList<Trade> tradeList = new ArrayList<Trade>();

		try {
			boolean validResponse = false;
			
			while (!validResponse)
			{
				//get valid file for agents (one that exists)
				String filename = getValidFile("schedule");
				//make empty line variable
				String line = "";
				BufferedReader br = new BufferedReader(new FileReader(filename));
				validResponse = true;
				
				//keep reading until reach end of file
				while ((line = br.readLine()) != null && validResponse) // returns a Boolean value
				{
					String[] ticketOp = line.split(","); //split line along commas since is csv
					//add to trades list
					try
					{
						//call the api get event method to get string of json formatted data
						Datum event = InfoAPI.getEventData(ticketOp[1]);
						
						//only do below if don't get null data entry (so actually have data to unpack)
						if (event != null)
						{
							//System.out.println(event.getName() + " | " + event.getPrice());
							//add a new trade object that uses data directly from the csv and from the json formatted data
							tradeList.add(new Trade(Integer.parseInt(ticketOp[0]), event.getName(), Integer.parseInt(ticketOp[2]), event.getPrice(), ticketOp[1]));
							validResponse = true;
						}
						else //error getting from file
						{
							incompleteTrades.add(new Trade(Integer.parseInt(ticketOp[0]), "N/A", Integer.parseInt(ticketOp[2]), 0, ticketOp[1]));
						}
					} catch (java.lang.NumberFormatException n)
					{
						System.out.println("The file " + filename + " is not the correct format.");
						System.out.println();
						validResponse = false;
					}
				}
				br.close();
			}
			System.out.println("The schedule file has been properly read.");
			System.out.println();
			
		} catch (IOException ioe) {
			System.out.println("ioe in Main makeTradeList: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		return tradeList;
	}
	
	//function to make list of agents
	public SortedMap<Integer,Integer> makeAgentMap()
	{		
		//make array/list for storing trades in order
		SortedMap<Integer,Integer> agentMap = new TreeMap<Integer,Integer>();

		try {
			boolean validResponse = false;
			
			while (!validResponse)
			{
				//get valid file for agents (one that exists)
				String filename = getValidFile("agents");
				//make empty line variable
				String line = "";
				BufferedReader br = new BufferedReader(new FileReader(filename));
				validResponse = true;
				
				//keep reading until reach end of file
				while ((line = br.readLine()) != null && validResponse) // returns a Boolean value
				{
					String[] agentsplit = line.split(","); //split line along commas since is csv
					//add to trades list
					try //to get integer values of what was in file
					{
						agentMap.put(Integer.valueOf(agentsplit[0]), Integer.valueOf(agentsplit[1]));
						validResponse = true;
					} catch (IllegalArgumentException n)
					{
						System.out.println("The file " + filename + " is not the correct format.");
						System.out.println();
						validResponse = false;
					}
				}
				br.close();
			}
			System.out.println("The agents file has been properly read.");
			System.out.println();
			
		} catch (IOException ioe) {
			System.out.println("ioe in Main makeAgentList: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		return agentMap;
	}
	
	public String getValidFile(String filetype)
	{
		String fileName = null;
		
		File f;
	    
		do //this loop if not correct filename
		{
			System.out.println("What is the path of the " + filetype + " file? ");
			//set up input and get filename
			fileName = sc.next();
		    f = new File(fileName);
		    if (!f.exists())
		    {
		    	System.out.println("The file " + fileName + " could not be found.");
		    	System.out.println();
		    }
		    
		    //try making file with this name to see if possible

		} while (!f.exists()); //got valid filename
		
		
		return fileName;
	}
	
	public int getConnectionsLeft()
	{
		return maxConnections - numConnections;
	}
	

	public void broadcast(Object message, ServerThread st) {
		// check is there is a message to send
		if (message != null)	
		{
			//System.out.println(message);
			//to make iterator threadsafe, use synched block
			synchronized (serverThreads)
			{
				for (ServerThread serverThread: serverThreads) //so will send to all server threads / clients
				{
					if (st != serverThread) //so that thread that sent message isn't resent it
					{
						serverThread.sendMessage(message);
					}
				}
			}
		}
	}

	//adds to list of incomplete trades
	public void addIncomplete(Trade trade) {
		incompleteTrades.add(trade);
	}
	
	public ArrayList<Trade> getIncompleteList()
	{
		return incompleteTrades;
	}
	
	public void done(ServerThread st)
	{
		doneThreads.add(st);
		
		//if all server/Clients are done
		if (doneThreads.size() == serverThreads.size())
		{
			//do this when the agents all are done
			System.out.println("Processing complete.");
		}
		
		//program should end
	}
	
}
