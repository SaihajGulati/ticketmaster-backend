package ssgulati_CSCI201_Assignment3;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

public class InfoAPI {


	/*public static void main(String[] args)
	{	
		String ID = "vvG1iZ94EbSeQ";
		Datum bye = getEventData(ID);
	}*/
		
	public static Datum getEventData(String ID)
	{
		//open scanner to read through file
		Gson gson = new GsonBuilder()
		        .registerTypeAdapter(Datum.class, new DatumDeserializer())
		        .create();
		String eventInfo = getEvent(ID);	
		
		//check if couldn't retreive event from api (if couldn't, no need to do stuff below
		if (eventInfo == null)
		{
			return null;
		}
		
		//try to add data from json into data object, catch exception if not and keep null for outside while loop
		try {
			Datum data = gson.fromJson(eventInfo, Datum.class);
			return data;
		}
		//correct printout
		catch (JsonSyntaxException s)
		{
			System.out.println("The event cannot be parsed.");
			System.out.println();
		}
		
		catch (JsonParseException | NullPointerException l)
		{
			System.out.println("The event has missing parameters.");
			System.out.println();
		}
		catch (Exception e) {
			  System.out.println("The event is not formatted properly.");
			  System.out.println();
		}
		return null;
	}
	
	
	public static String getEvent(String ID)
	{
	    try {
	      // Create URL object with the API endpoint URL
	      URL url = new URL("https://us-west2-csci201-376723.cloudfunctions.net/events/" + ID);
	
	      // Create a HttpURLConnection object to connect to the URL
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	
	      // Set the request method to GET
	      conn.setRequestMethod("GET");
	      
	      // Get the response code
	      int responseCode = conn.getResponseCode();
	
	      // If the response code is OK (200), read the response body
	      if (responseCode == HttpURLConnection.HTTP_OK) {
		      BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		      String response = in.readLine();
		      
		      in.close();
		
		      // return the response body (comma separated list)
		      return response;
	        
	      } 
	      
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    
	    return null;
	    
	  }

}