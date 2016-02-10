package com.wenyun.web;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Path("/pss")
public class PumpStationService {
	
	@GET
	@Path("/test")
	public Response psTest() {
 
		String output = "This is pump station service test function";
 
		return Response.status(200).entity(output).build();
 
	}
	
	@GET
	@Path("/{psname}")
	public Response psGet(@PathParam("psname") String psname) {
 
		JSONParser jParser = new JSONParser();
		String jsonText ="{}";
		try {
			InputStream pscfgStream = this.getClass().getClassLoader().getResourceAsStream("/wenyun.json");
			Object confobj = jParser.parse( new InputStreamReader(pscfgStream));
            JSONObject confjsonObject =  (JSONObject) confobj;
            String pumpJsonLoc = (String)confjsonObject.get("conf_loc") +"/ps_conf.json";
            Object obj = jParser.parse( new FileReader(pumpJsonLoc));
            JSONObject jsonObject =  (JSONObject) obj;
            JSONArray existpumps = (JSONArray) jsonObject.get("PumpStations");
            for (int i=0; i < existpumps.size();i++)
            {
            	JSONObject tempObject = (JSONObject) existpumps.get(i);
            	String tempPumpName = (String)tempObject.get("stationName");
                if (tempPumpName.compareTo(psname)==0)
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
	
	@PUT
	@Path("/{psname}")
	public Response psUpdate(@PathParam("psname") String psname, String payload) {
 
		JSONParser jParser = new JSONParser();
		String jsonText = "{}";
		try {
			InputStream pscfgStream = this.getClass().getClassLoader().getResourceAsStream("/wenyun.json");
			Object confobj = jParser.parse( new InputStreamReader(pscfgStream));
            JSONObject confjsonObject =  (JSONObject) confobj;
            String pumpJsonLoc = (String)confjsonObject.get("conf_loc") +"/ps_conf.json";
            Object obj = jParser.parse( new FileReader(pumpJsonLoc));
            JSONObject jsonObject =  (JSONObject) obj;
            JSONArray existpumps = (JSONArray) jsonObject.get("PumpStations");
            
            JSONObject payloadjson = (JSONObject) jParser.parse(payload);
            if (!this.verifyPSPayload(payloadjson))
            {
            	return Response.status(500).entity("Input pyload lack of fields!").build();
            }
            for (int i=0; i < existpumps.size();i++)
            {
            	JSONObject tempObject = (JSONObject) existpumps.get(i);
            	String tempPumpName = (String)tempObject.get("stationName");
                if (tempPumpName.compareTo(psname)==0)
                {
                	existpumps.remove(i);
                	existpumps.add(payloadjson);
                	break;
                }
            }
            jsonObject.put("PumpStations", existpumps);
            StringWriter out = new StringWriter(); 
            payloadjson.writeJSONString(out);
            
            jsonText = out.toString(); 
            FileWriter fileout = new FileWriter(pumpJsonLoc);
            jsonObject.writeJSONString(fileout);
            fileout.flush();
            fileout.close();
            return Response.status(200).entity(jsonText).build();
		}catch (FileNotFoundException e) {
			return Response.status(500).entity(e.getMessage()).build();
        } catch (IOException e) {
        	return Response.status(500).entity(e.getMessage()).build();
        } catch (ParseException e) {
        	return Response.status(500).entity(e.getMessage()).build();
        }
	}
	
	private Boolean verifyPSPayload(JSONObject payloadjson)
	{
		if (payloadjson.containsKey("stationName") && payloadjson.containsKey("kwareurl")
				&& payloadjson.containsKey("ifxhost")
				&& payloadjson.containsKey("ifxport")
				&& payloadjson.containsKey("ifxprotocal")
				&& payloadjson.containsKey("ifx_username")
				&& payloadjson.containsKey("ifx_password")
				&& payloadjson.containsKey("ifx_db"))
		{
			return true;
		}
		else
		{return false;}
	}


}
