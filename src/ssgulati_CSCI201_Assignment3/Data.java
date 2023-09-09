package ssgulati_CSCI201_Assignment3;

import java.util.List;
import java.util.Collections;

public class Data{

	private List<Datum> data;
	
	public List<Datum> getData() {
		return data;
	}
	
	public void setData(List<Datum> data) {
		this.data = data;
	}
	
	public Datum datumAtIndex(int i)
	{
		return data.get(i);
	}
	
	public int getSize()
	{
		return data.size();
	}
	
	public void add(Datum d)
	{
		data.add(d);
	}
	
	public void remove(Datum d)
	{
		data.remove(d);
	}
	
	public void sort(int direction)
	{
		//sort normal if A to Z
		if (direction == 1)
		{
			Collections.sort(data);
		}
		
		//sort reverse if Z to A
		else if (direction == 2)
		{
			Collections.sort(data, Collections.reverseOrder());
		}
	}
}
