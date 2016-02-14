package com.wenyun.web;

import java.io.Console;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
	@Path("/{psname}/{dsname}")
	public Response getDevice(@PathParam("psname") String psname,@PathParam("dsname") String dsname) {
		JSONParser jParser = new JSONParser();
		String jsonText ="{}";
		try {
			InputStream pscfgStream = this.getClass().getClassLoader().getResourceAsStream("/wenyun.json");
			Object confobj = jParser.parse( new InputStreamReader(pscfgStream));
            JSONObject confjsonObject =  (JSONObject) confobj;
            String pumpJsonLoc = (String)confjsonObject.get("conf_loc") +"/ps_conf.json";
            Object obj = jParser.parse( new FileReader(pumpJsonLoc));
            JSONObject jsonObject =  (JSONObject) obj;
            JSONArray existstations = (JSONArray) jsonObject.get("PumpStations");
            for (int i=0; i < existstations.size();i++)
            {
            	JSONObject tempObject = (JSONObject) existstations.get(i);
            	String tempPumpName = (String)tempObject.get("stationName");
                if (tempPumpName.compareTo(psname)==0)
                {
                	JSONArray existdevices = (JSONArray) tempObject.get("devices");
                	for (int j=0; j < existdevices.size();j++)
                    {
                		JSONObject tempDevObject = (JSONObject) existdevices.get(j);
                    	String tempDeviceName = (String)tempDevObject.get("deviceName");
                    	if (tempDeviceName.compareTo(dsname)==0)
                        {
                    		StringWriter out = new StringWriter(); 
                            tempDevObject.writeJSONString(out); 
                            jsonText = out.toString(); 
                            return Response.status(200).entity(jsonText).build();	
                        }
                    	
                    }
                }
            }
		}catch (FileNotFoundException e) {
			return Response.status(500).entity(e.getMessage()).build();
        } catch (IOException e) {
        	return Response.status(500).entity(e.getMessage()).build();
        } catch (ParseException e) {
        	return Response.status(500).entity(e.getMessage()).build();
        }
		return Response.status(200).entity(jsonText).build();
	}

	@PUT
	@Path("/{psname}/{dsname}")
	public Response dsUpdate(@PathParam("psname") String psname,@PathParam("dsname") String dsname, String payload) {
		JSONParser jParser = new JSONParser();
		String jsonText ="{}";
		try {
			InputStream pscfgStream = this.getClass().getClassLoader().getResourceAsStream("/wenyun.json");
			Object confobj = jParser.parse( new InputStreamReader(pscfgStream));
            JSONObject confjsonObject =  (JSONObject) confobj;
            String pumpJsonLoc = (String)confjsonObject.get("conf_loc") +"/ps_conf.json";
            Object obj = jParser.parse( new FileReader(pumpJsonLoc));
            JSONObject jsonObject =  (JSONObject) obj;
            JSONArray existstations = (JSONArray) jsonObject.get("PumpStations");
            for (int i=0; i < existstations.size();i++)
            {
            	JSONObject tempObject = (JSONObject) existstations.get(i);
            	String tempPumpName = (String)tempObject.get("stationName");
                if (tempPumpName.compareTo(psname)==0)
                {
                	JSONArray existdevices = (JSONArray) tempObject.get("Devices");

                    JSONObject payloadjson = (JSONObject) jParser.parse(payload);
                    if (!this.verifyDSPUTPayload(payloadjson))
                    {
                    	return Response.status(500).entity("Input pyload lack of fields!").build();
                    }
                    JSONObject tempDevObject = null;
                    for (int j=0; j < existdevices.size();j++)
                    {
                		tempDevObject = (JSONObject) existdevices.get(j);
                    	String tempDeviceName = (String)tempDevObject.get("deviceName");
                    	if (tempDeviceName.compareTo(dsname)==0)
                        {
                    		existdevices.remove(j);
                    		
                        	Iterator<String> it = payloadjson.keySet().iterator(); 
                        	while (it.hasNext()) {
                        		String key = it.next();
                        		tempDevObject.put(key, payloadjson.get(key));
                        	}
                        	existdevices.add(tempDevObject);
                        	break;
                       	
                        }                       
            		}
                    tempObject.put("Devices", existdevices);
                    System.out.println(tempObject.toString());
                    StringWriter out = new StringWriter(); 
                    tempDevObject.writeJSONString(out);
                    
                    jsonText = out.toString(); 
                    FileWriter fileout = new FileWriter(pumpJsonLoc);
                    jsonObject.writeJSONString(fileout);
                    fileout.flush();
                    fileout.close();
                    return Response.status(200).entity(jsonText).build();
//                  return Response.status(200).entity("Station Device does not exists").build();
                }
            }            
            return Response.status(200).entity("Pump Station does not exists").build();
		}
        catch (FileNotFoundException e) {
        return Response.status(500).entity(e.getMessage()).build();
        } catch (IOException e) {
        return Response.status(500).entity(e.getMessage()).build();
        } catch (ParseException e) {
        return Response.status(500).entity(e.getMessage()).build();
        }
    }

	@POST
	@Path("/{psname}/devices")
	public Response dsCreateDevice(@PathParam("psname") String psname, String payload) {
 
		JSONParser jParser = new JSONParser();
		JSONArray pumpStationsTags = new JSONArray();
		String jsonText = "{}";
		try {
			InputStream pscfgStream = this.getClass().getClassLoader().getResourceAsStream("/wenyun.json");
			Object confobj = jParser.parse( new InputStreamReader(pscfgStream));
            JSONObject confjsonObject =  (JSONObject) confobj;
            String pumpJsonLoc = (String)confjsonObject.get("conf_loc") +"/ps_conf.json";
            Object obj = jParser.parse( new FileReader(pumpJsonLoc));
            JSONObject jsonObject =  (JSONObject) obj;
            JSONArray existstations = (JSONArray) jsonObject.get("PumpStations");
            
            JSONObject payloadjson = (JSONObject) jParser.parse(payload);
            if (!this.verifyDSPOSTPayload(payloadjson))
            {
            	return Response.status(500).entity("Input pyload lack of fields!").build();
            }
            for (int i=0; i < existstations.size();i++)
            {
            	JSONObject tempObject = (JSONObject) existstations.get(i);
            	String tempPumpName = (String)tempObject.get("stationName");
                if (tempPumpName.compareTo(psname)==0)
                {
                	JSONArray existdevices = (JSONArray) tempObject.get("devices");
                    JSONObject tempDevObject = null;
                    String newDevName = (String) payloadjson.get("deviceName");
                    for (int j=0; j < existdevices.size();j++)
                    {
//                		System.out.println(existdevices.get(j).toString());
                    	tempDevObject = (JSONObject) existdevices.get(j);
                    	String tempDeviceName = (String)tempDevObject.get("deviceName");
                    	
                        if (tempDeviceName.compareTo(newDevName)==0)
                        {
                        	               	
                        	return Response.status(500).entity("The pump station already exsit! Please use put instead of POST!").build();
                        }
                    }
                    existdevices.add(payloadjson);
                    tempObject.put("devices", existdevices);
                    System.out.println(tempObject.toString());
                    FileWriter fileout = new FileWriter(pumpJsonLoc);
                    jsonObject.writeJSONString(fileout);
                    fileout.flush();
                    fileout.close();
                    return Response.status(200).entity("Station Device Created!").build();
        		}
                
            }
            return Response.status(200).entity("Pump Station does not exists").build();
		
		} catch (FileNotFoundException e) {
			return Response.status(500).entity(e.getMessage()).build();
	    } catch (IOException e) {
	    	return Response.status(500).entity(e.getMessage()).build();
	    } catch (ParseException e) {
	    	return Response.status(500).entity(e.getMessage()).build();
	    }

	}

	@GET
	@Path("/{psname}/{dsname}/tags")
	public Response dsGetTags(@PathParam("psname") String psname,@PathParam("dsname") String dsname) {
 
		JSONParser jParser = new JSONParser();
		JSONArray pumpStationsTags = new JSONArray();
		String jsonText=null;
		try {
			InputStream pscfgStream = this.getClass().getClassLoader().getResourceAsStream("/wenyun.json");
			Object confobj = jParser.parse( new InputStreamReader(pscfgStream));
            JSONObject confjsonObject =  (JSONObject) confobj;
            String pumpJsonLoc = (String)confjsonObject.get("conf_loc") +"/ps_conf.json";
            Object obj = jParser.parse( new FileReader(pumpJsonLoc));
            JSONObject jsonObject =  (JSONObject) obj;
            JSONArray existstations = (JSONArray) jsonObject.get("PumpStations");
            for (int i=0; i < existstations.size();i++)
            {
            	JSONObject tempObject = (JSONObject) existstations.get(i);
            	String tempStationName = (String)tempObject.get("stationName");
                if (tempStationName.compareTo(psname)==0)
                {
                	JSONArray existdevices = (JSONArray) tempObject.get("devices");
                	for (int j=0; j < existdevices.size();j++)
                    {
                		JSONObject tempDevObject = (JSONObject) existdevices.get(j);
                		String tempDevName = (String)tempDevObject.get("deviceName");
//                		System.out.println(tempDevName);
                		if (tempDevName.compareTo(dsname)==0)
                		{
                			
                			if(tempDevObject.containsKey("tags"))
                			{
                    		pumpStationsTags = (JSONArray) tempDevObject.get("tags");
                			}
                		}
                    }
                }
            }
            StringWriter out = new StringWriter(); 
            pumpStationsTags.writeJSONString(out); 
            jsonText = out.toString(); 
            return Response.status(200).entity(jsonText).build();
		}catch (FileNotFoundException e) {
			return Response.status(500).entity(e.getMessage()).build();
        } catch (IOException e) {
        	return Response.status(500).entity(e.getMessage()).build();
        } catch (ParseException e) {
        	return Response.status(500).entity(e.getMessage()).build();
        }
	}
	
	@POST
	@Path("/{psname}/{dsname}/tags")
	public Response dsCreateTag(@PathParam("psname") String psname,@PathParam("dsname") String dsname, String payload) {
 
		JSONParser jParser = new JSONParser();
		JSONArray pumpStationsTags = new JSONArray();
		String jsonText = "{}";
		try {
			InputStream pscfgStream = this.getClass().getClassLoader().getResourceAsStream("/wenyun.json");
			Object confobj = jParser.parse( new InputStreamReader(pscfgStream));
            JSONObject confjsonObject =  (JSONObject) confobj;
            String pumpJsonLoc = (String)confjsonObject.get("conf_loc") +"/ps_conf.json";
            Object obj = jParser.parse( new FileReader(pumpJsonLoc));
            JSONObject jsonObject =  (JSONObject) obj;
            JSONArray existpumps = (JSONArray) jsonObject.get("PumpStations");
            
            JSONArray payloadjson = (JSONArray) jParser.parse(payload);
            
            JSONObject tempObject = null;
            for (int i=0; i < existpumps.size();i++)
            {
            	tempObject = (JSONObject) existpumps.get(i);
            	String tempStationName = (String)tempObject.get("stationName");
            	
                if (tempStationName.compareTo(psname)==0)
                {
                	JSONArray existdevices = (JSONArray) tempObject.get("devices");
                	for (int j=0; j < existdevices.size();j++)
                    {
                		JSONObject tempDevObject = (JSONObject) existdevices.get(j);
                		String tempDevName = (String)tempDevObject.get("deviceName");
//                		System.out.println(tempDevName);
                		if (tempDevName.compareTo(dsname)==0)
                		{
               			
                        	if(tempObject.containsKey("tags"))
                        	{
                        		pumpStationsTags = (JSONArray) tempDevObject.get("tags");
                        	}  
                        	for(int k=0; k< payloadjson.size();k++ )
                        	{
                        		if(!pumpStationsTags.contains((String)payloadjson.get(k)))
                        		{
                        			pumpStationsTags.add((String)payloadjson.get(k));
                        		}
                        	}
                        	tempObject.put("tags", pumpStationsTags);
                        	existdevices.remove(j);
                        	existdevices.add(tempDevObject);
//                        	jsonObject.put("PumpStations", existdevices);
                            FileWriter fileout = new FileWriter(pumpJsonLoc);
                            jsonObject.writeJSONString(fileout);
                            fileout.flush();
                            fileout.close();
                        	return Response.status(200).entity("Device tags added!").build();
                		}
                    }
                	
                	

                }
               
            }
        	return Response.status(200).entity("No pump station found!").build();
		}catch (FileNotFoundException e) {
			return Response.status(500).entity(e.getMessage()).build();
        } catch (IOException e) {
        	return Response.status(500).entity(e.getMessage()).build();
        } catch (ParseException e) {
        	return Response.status(500).entity(e.getMessage()).build();
        }
	}
	
	private Boolean verifyDSPUTPayload(JSONObject payloadjson)
	{
		String validKeys[] = {"TYPE","id"};

		List<String> keylist = Arrays.asList(validKeys);  
       		
		Iterator<String> it = payloadjson.keySet().iterator(); 
    	while (it.hasNext()) {
    		String key = it.next();
    		if (!keylist.contains(key))
    		{
    			return false;
    		}
    	}
    	return true;
	}
	private Boolean verifyDSPOSTPayload(JSONObject payloadjson)
	{
		String validKeys[] = {"id","deviceName","TYPE"};
		List<String> keylist = Arrays.asList(validKeys); 
		
		for (int i=0; i<validKeys.length;i++)
		{
			if(! payloadjson.containsKey(validKeys[i]))
			{
				return false;
			}
		}
		Iterator<String> it = payloadjson.keySet().iterator(); 
    	while (it.hasNext()) {
    		String key = it.next();
    		if (!keylist.contains(key))
    		{
    			return false;
    		}
    	}
    	return true;
	}

}
