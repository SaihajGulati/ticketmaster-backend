package ssgulati_CSCI201_Assignment3;


import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.google.gson.JsonParseException;

//implements comparable so can sort
public class Datum implements Comparable<Datum>{

	private String name;
	
	private String tour;
	
	private LocalDate localDate;
	
	private String venue;
	
	private int price;
	
	//private DateValidator validator;
	
	public Datum(String n, String t, LocalDate l, String v, int p)
	{
		//validator = new DateValidator("YYYY-MM-DD");
		name = n;
		tour = t;
		localDate = l;
		venue = v;
		price = p;
	}
	
	//default constructor
	public Datum() {}
	
	public int getPrice() {
		return price;
	}
	
	public void setPrice(int price) {
		this.price = price;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTour() {
		return tour.replace(" TOUR", "").replace(" Tour", "").concat(" Tour");
	}
	
	public void setTour(String tour) {
		this.tour = tour;
	}
	
	public String getLocalDate() {
		return localDate.toString();
	}
	
	public void setLocalDate(String localDate) {
		//try creating the localDate, and if is incorrect format throw exception (default parsable format for LocalDate is yyyy-MM-DD)
        try
        {
        	this.localDate = LocalDate.parse(localDate);
        }
        catch (DateTimeParseException e)
        {
        	throw new JsonParseException("biff");
        }
	}
	
	public String getVenue() {
		return venue;
	}
	
	public void setVenue(String venue) {
		this.venue = venue;
	}

	//comparator for sorting
	public int compareTo(Datum d)
	{
		return getName().toLowerCase().compareTo(d.getName().toLowerCase());
	}

}