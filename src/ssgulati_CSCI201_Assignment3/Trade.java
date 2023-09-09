package ssgulati_CSCI201_Assignment3;

import java.io.Serializable;

public class Trade implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int time;
	private String artist;
	private int action;
	private int price;
	private String apiID;
	
	public Trade (int time, String artist, int action, int price, String ID)
	{
		this.time = time;
		this.artist = artist;
		this.action = action;
		this.price = price;
		this.apiID = ID;
	}
	
	public int getTime() {
		return time;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public int getAction() {
		return action;
	}
	public void setAction(int action) {
		this.action = action;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}

	public String getApiID() {
		return apiID;
	}
	
	
	

}
