package ssgulati_CSCI201_Assignment3;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

public class TradeThread extends Thread{
	
	//list of trades to happen at this time
	private ArrayList<Trade> tradeList;
	
	//reference to object
	private MainServer ms;
	
	//static variable to hold info of serverThreads to send info and do work as needed
	private static List<ServerThread> serverThreads;
	
	private boolean end;
	
	//constructor that is called when thread is created
	public TradeThread(ArrayList<Trade> tradeList, MainServer ms, boolean end) {
		this.tradeList = tradeList;
		this.ms = ms;
		this.end = end;
	}
	
	
	//set the serverThreads static variable
	public static void setServerThreads(List<ServerThread> sts)
	{
		serverThreads = sts;
	}
	
	
	public static String getTimeString(long now)
	{
		// Creating date format
        DateFormat simple = new SimpleDateFormat("HH:mm:ss.SSS");
        simple.setTimeZone(TimeZone.getTimeZone("UTC"));
        
       return "[" + simple.format(new Date(now)) + "]";
	}
	
	public void run()
	{
		int tradeIndex = 0;
		HashSet<ServerThread> notPossible = new HashSet<ServerThread>();
		while (tradeIndex < tradeList.size())
		{
			//empty list to hold agents that are free right now
			ArrayList<ServerThread> freeAgents = new ArrayList<ServerThread>();
			
			//creates list of agents available at this momemt in time
			
			synchronized (serverThreads)
			{
				for (ServerThread st : serverThreads)
				{
					if (st.isFree())
					{
						freeAgents.add(st);
					}
				}
			}
			
			for (ServerThread st : freeAgents)
			{
				//while has enough balance and therefore assigns
				while (tradeIndex < tradeList.size() && st.assignTradeAttempt(tradeList.get(tradeIndex)))
				{
					//move on to try next trade
					tradeIndex++;
					
					//reset notPossible list as moving onto new trade
					notPossible = new HashSet<ServerThread>();
				}
				
				//when get here, this trade was too expensive for the this agent, so add entry to current notPossible list
				notPossible.add(st);
			}
			
			
			//now that have assigned all to free agents, run them
			for (ServerThread st : freeAgents)
			{
				st.doTrades();
				
			}
			
			/*if (end)
			{
				System.out.println(tradeIndex);
				System.out.println(tradeList.size());
				System.out.println(notPossible.size());
			}*/
			//if get here, and for this tradeIndex the notPossible list which doesn't add duplicates is as big as all agents (so have checked all agents if they have enough balance)
			if (notPossible.size() == serverThreads.size() && tradeIndex < tradeList.size())
			{
				//add it to the incomplete list and move on (also reset notPossible list)
				ms.addIncomplete(tradeList.get(tradeIndex));
				tradeIndex++;
				notPossible = new HashSet<ServerThread>();
			}
		}
		
		//if is last tradeThread, initialize end process
		if (end)
		{
			synchronized(serverThreads)
			{
				for (ServerThread st : serverThreads)
				{
					st.endGame();
				}
				
			}
		}
		
	}
	
}
