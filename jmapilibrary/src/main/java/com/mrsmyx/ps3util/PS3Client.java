package com.mrsmyx.ps3util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.mrsmyx.JMAPI;
import com.mrsmyx.utils.Response;

public class PS3Client {

	protected String ip;
	protected int port;
	protected Socket client;

	protected boolean Connect(String ip, int port) {
		this.ip = ip;
		this.port = port;
		if (client == null) {
			client = new Socket();
		}
		if (!client.isConnected() || client.isClosed()) {
			try {
				client = new Socket(ip, port);
				BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
				Response first = parseResponse(Response.Build(true, br.readLine()));
				if(first.getSuccess() && first.getResponseCode() == JMAPI.PS3MAPI_RESPONSECODE.PS3MAPICONNECTED){
					Response second = parseResponse(Response.Build(true, br.readLine()));
					if(second.getSuccess() && second.getResponseCode() == JMAPI.PS3MAPI_RESPONSECODE.PS3MAPICONNECTEDOK){
						System.out.println("Connection Established!");
						return true;
					}
					return true;
				}
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	protected JMAPI.PS3MAPI_RESPONSECODE findResponse(int value){
		for(JMAPI.PS3MAPI_RESPONSECODE r : JMAPI.PS3MAPI_RESPONSECODE.values()){
			if(r.getValue() == value){
				return r;
			}
		}
		return null;
	}

	protected Response parseResponse(Response response){
		if(response.getSuccess()){
			String res = response.getResponse();
			if(res == null) return response;
			int responseCode = Integer.valueOf(res.substring(0, 3)).intValue();
			String buffer = res.substring(4).replace("\r", "").replace("\n", "");	
			if(buffer.contains("OK: ")){
				buffer = buffer.replace("OK: ", "");
			}
			response.setResponse(buffer);
			response.setResponseCode(findResponse(responseCode));
		}
		return response;
	}
	
	protected Response Send(String data) {
		try {
			PrintWriter pw = new PrintWriter(client.getOutputStream());
			pw.println(data);
			pw.flush();
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String z = br.readLine();
			return parseResponse(Response.Build(true, z));
		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.Build(false, "Could not send command");
		}
	}
	
	protected void Close() throws IOException{
		if(client.isConnected()){
			client.close();
		}
		client = null;
	}
}
