package ssgulati_CSCI201_Assignment3;

import java.lang.reflect.Type;
import java.time.LocalDate;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LocalDateSerializer implements JsonSerializer<LocalDate>{
	@Override
	public JsonElement serialize(LocalDate arg0, Type arg1, JsonSerializationContext arg2) {
		// TODO Auto-generated method stub
		return new JsonPrimitive(arg0.toString());
	}

}
