package ssgulati_CSCI201_Assignment3;

public class SharedBalance {
	private static int balance;
	private static int rejected;
	
	//constructor to set balance
	public static void setBalance(int amount) {
		balance = amount;
	}
	
	public static int getBalance()
	{
		return balance;
	}
		
	public static int getNumRejected()
	{
		return rejected;
	}
	
	synchronized public static void reject()
	{
		rejected++;
	}
	
	//function to add to balance, synchronized so can thread safe update
	synchronized public static int add(int amount)
	{
		balance += amount;
		return balance;
	}
	
	//make synchronized so function is updated as needed
	synchronized public static int remove(int amount)
	{
		//if have enough in balance, go through with transaction and return as such
		if (balance >= amount)
		{
			balance -= amount;
		}
		
		return balance;
	}
	
	

}
