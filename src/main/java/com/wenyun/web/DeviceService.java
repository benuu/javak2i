package com.wenyun.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Path("/ds")
public class DeviceService {
	
	@GET
	@Path("/test")
	public Response psTest() {
 
		String output = "This is device service test function";
 
		return Response.status(200).entity(output).build();
 
	}
	
	@GET
	@Path("/{dsname}")
	public Response getDeviceByName(@PathParam("dsname") String dsname) {
		JSONParser jParser = new JSONParser();
		String jsonText ="{}";
		try {
			InputStream pscfgStream = this.getClass().getClassLoader().getResourceAsStream("/ds_conf.json");
			Object obj = jParser.parse( new InputStreamReader(pscfgStream));

            JSONObject jsonObject =  (JSONObject) obj;
            JSONArray existdevices = (JSONArray) jsonObject.get("Devices");
            for (int i=0; i < existdevices.size();i++)
            {
            	JSONObject tempObject = (JSONObject) existdevices.get(i);
            	String tempPumpName = (String)tempObject.get("deviceName");
                if (tempPumpName.compareTo(dsname)==0)
                {
                	
                    StringWriter out = new StringWriter(); 
                    tempObject.writeJSONString(out); 
                    jsonText = out.toString(); 
                    return Response.status(200).entity(jsonText).build();
                }
            }
		}catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
		return Response.status(200).entity(jsonText).build();
	}

}
