package ssgulati_CSCI201_Assignment3;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.TimeZone;

public class AgentClient extends Thread{
	
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private int profit;
	
	Socket s;
	
	//will help keep time figured out correctly across threads
	private static long startTime = -1;
	
	
	public static void main(String[] args)
	{
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Welcome to SalTickets v2.0!");
		System.out.println("Enter the server hostname: ");
		String hostname = sc.next();
		
		System.out.println("Enter the server port:");
		
		boolean validResponse = false;
		int port = 0;
		
		//keep looping through and asking until get valid int
		while (!validResponse)
		{
			try {
				port = sc.nextInt(); //get nextInt
				sc.nextLine(); //clear dangling newLine character in scanner
				validResponse = true;
			} catch(InputMismatchException e)
			{
				sc.nextLine(); //clear stuff in scanner
				validResponse = false;
				System.out.println("That is not a valid port number. Please try again.");
			}
		}
		
		@SuppressWarnings("unused")
		AgentClient ac = new AgentClient(hostname, port);
		
		sc.close();
		
	}
	public AgentClient (String hostname, int port)
	{
		/*this.num = hostname;s
		this.balance = balance;*/
		
		//create socket to connect to server thread
		
		profit = 0;
		
		try {
			s = new Socket(hostname, port);
			
			//setting input and output streams
			output = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
			output.flush();
			input = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
			
			int numLeft = (int) (input.readObject());
			
			
			while (numLeft > 0)
			{
				if (numLeft == 1) //special case so it says 1 more agent instead of agents
				{
					System.out.println(numLeft + " more agent is needed before the service can begin.");
					System.out.println("Waiting...");
				}
				
				else //still is above 0, but not 1	
				{
					System.out.println(numLeft + " more agents are needed before the service can begin.");
					System.out.println("Waiting...");
				}
				numLeft = (int)input.readObject();
			}
			
			//only gets here if numLeft is 0
			System.out.println("All agents have arrived!");
			System.out.println("Starting service.");
			this.start();
			
		} catch (IOException ioe) {
			System.out.println("ioe in AgentClient constructor: " + ioe.getMessage());	
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			System.out.println("cnfe in AgentClient constructor: " + cnfe.getMessage());
			cnfe.printStackTrace();
		}	
		
	}
	
	public static long setStartTime()
	{
		startTime = System.currentTimeMillis();
		return startTime;
	}
	
	public void run()
	{
		try
		{
			while (true)
			{
				@SuppressWarnings("unchecked")
				ArrayList<Trade> tradeList = (ArrayList<Trade>)(input.readObject());
				
				if (tradeList.get(0) == null) //if list is sent through, it's not empty, so null in first element is only when the incomplete trade list is being sent through
				{
					System.out.print(getTimeString(System.currentTimeMillis()) + " Incomplete Trades: ");
					if (tradeList.size() > 1)
					{
						//start at 1 since first element is null to indicate this is incomplete list
						for (int i = 1; i < tradeList.size(); i++)
						{
							Trade t = tradeList.get(i);
						
							System.out.print("(" + t.getTime() + ", " + t.getApiID() + " [" + t.getArtist() + "], " + t.getAction() + ", " + getEndDateTime() + ") ");
						}
					}
					else //this means the only thing in the list the first element which is null to indicate it's the end (so no inomplete trades)
					{
						System.out.print("NONE");
					}
					
					//new line after list of incompletes (couldn't do above because when printing, not new line each time)
					System.out.println();
					
					output.writeObject("done");
					output.flush();
					
					System.out.println("Total Profit Earned: $" + profit + ".00");
					
					System.out.println();
					
					System.out.println("Processing complete.");
					//end program
					break;
				}
				
				//if get here, then the thing sent through is not the incompleteList as it does not have null as the first element
				
				//loop through trades sent through and print properly at correct times for assignment
				for (Trade t : tradeList)
				{
					String time;
					
					//if is default value still, need to set to 0
					if (startTime == -1)
					{
						time = getTimeString(setStartTime());
					}
					else
					{
						time = getTimeString(System.currentTimeMillis());
					}
					
					int action = t.getAction();
					int price = t.getPrice();
					
					if (action < 0) //which means a sale
					{
						System.out.println(time + " Assigned sale of " + Math.abs(action) + " tickets(s) of " + t.getArtist() + 
								". Total gain estimate = " + price + " * " + Math.abs(action) + " = " + (price * Math.abs(action)) + ".00.");
					}
					else //action is greater than or equal to 0 (is a purchase)
					{
						System.out.println(time + " Assigned purchase of " + action + " tickets(s) of " + t.getArtist() + 
								". Total cost estimate = " + price + " * " + action + " = " + (price * action) + ".00.");
					}
				}
				
				//loop through trades assigned and now doing, printing properly aat correct times
				for (Trade t : tradeList)
				{
					int action = t.getAction();
					int price = t.getPrice();
					
					
					if (action < 0) //which means a sale
					{
						System.out.println(getTimeString(System.currentTimeMillis()) + " Starting sale of " + Math.abs(action) + " tickets(s) of " + t.getArtist() + 
								". Total gain = " + price + " * " + Math.abs(action) + " = " + (price * Math.abs(action)) + ".00.");
					}
					else //action is greater than or equal to 0 (is a purchase)
					{
						System.out.println(getTimeString(System.currentTimeMillis()) + " Starting purchase of " + action + " tickets(s) of " + t.getArtist() + 
								". Total cost = " + price + " * " + action + " = " + (price * action) + ".00.");
					}
					//one second to do transaction
					Thread.sleep(1000);
					
					if (action < 0) //which means a sale
					{
						System.out.println(getTimeString(System.currentTimeMillis()) + " Finished sale of " + Math.abs(action) + " tickets(s) of " + t.getArtist() + ".");
						profit += Math.abs(action) * price;
					}
					else //action is greater than or equal to 0 (is a purchase)
					{
						System.out.println(getTimeString(System.currentTimeMillis()) + " Finished purchase of " + action + " tickets(s) of " + t.getArtist() + ".");
					}
					
				}
				
				//at end of list, send back message that finished
				output.writeObject("free");
				output.flush();
				
			}
		}
		catch (IOException ioe)
		{
			System.out.println("ioe in AgentClient.run(): " + ioe.getMessage());
			ioe.printStackTrace();
			
		} catch (ClassNotFoundException cnfe) {
			System.out.println("cnfe in AgentClient.run(): " + cnfe.getMessage());
			cnfe.printStackTrace();
		} catch (InterruptedException ie) {
			System.out.println("ie in AgentClient.run(): " + ie.getMessage());
			ie.printStackTrace();
		}
	}

		
	public static String getTimeString(long now)
	{
		// Creating date format
        DateFormat simple = new SimpleDateFormat("HH:mm:ss.SSS");
        simple.setTimeZone(TimeZone.getTimeZone("UTC"));
        
       long programTime = now - startTime;
       return "[" + simple.format(new Date(programTime)) + "]";
	}
	
	public static String getEndDateTime()
	{
		DateFormat simple = new SimpleDateFormat("yyyy/LL/dd HH:mm:ss");
        simple.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        return simple.format(new Date(System.currentTimeMillis()));
	}
}

