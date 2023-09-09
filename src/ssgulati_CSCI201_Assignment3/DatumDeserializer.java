package ssgulati_CSCI201_Assignment3;

import java.lang.reflect.Type;
import java.time.LocalDate;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
 
public class DatumDeserializer implements JsonDeserializer<Datum> 
{  
    @Override
    public Datum deserialize(JsonElement json, Type type, 
          JsonDeserializationContext context) throws JsonParseException 
    {
        JsonObject jsonObject = json.getAsJsonObject();
        LocalDate localDate = null;
        
        //try creating the localDate, and if is incorrect format throw exception (default parsable format for LocalDate is yyyy-MM-DD)
        localDate = LocalDate.parse(jsonObject.get("localDate").getAsString());       
        

        /*System.out.println(
        jsonObject.get("name").getAsString() + " "  +
        jsonObject.get("tour").getAsString() + " " +
        localDate + " " +
        jsonObject.get("venue").getAsString());*/
        
        //make new datum and return it
        Datum d = new Datum(
                jsonObject.get("name").getAsString(), 
                jsonObject.get("tour").getAsString(), 
                localDate, 
                jsonObject.get("venue").getAsString(),
        		jsonObject.get("price").getAsInt());
        
        return d;
    }

}