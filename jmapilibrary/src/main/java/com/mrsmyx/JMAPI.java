package com.mrsmyx;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.mrsmyx.JMAPI.JMAPIListener.PS3OP;
import com.mrsmyx.exceptions.JMAPIException;
import com.mrsmyx.ps3util.PS3Client;
import com.mrsmyx.ps3util.PS3Process;
import com.mrsmyx.ps3util.Temperature;
import com.mrsmyx.utils.Network;
import com.mrsmyx.utils.Response;


public class JMAPI extends PS3Client {
	
	public interface JMAPIListener{
		public enum PS3OP{
			IDPS,
			PSID,
			LED,
			DELHISTORY,
			BUZZ,
			DISSYSCALL,
			SYSCALL8MODE,
			CHECKSYSCALL,
			NETWORK_FOUND,
			DISCONNECTED,
			ERROR, FWTYPE, FWVERSION, SCMINVERSION, PSIDSET, IDPSSET, BOOT, SCVERSION
		}
		public void onJMAPIError(String error);
		public void onJMAPIResponse(PS3OP ps3Op, JMAPI.PS3MAPI_RESPONSECODE responseCode, String message);
		public void onJMAPIPS3Process(JMAPI.PS3MAPI_RESPONSECODE responseCode, List<com.mrsmyx.ps3util.PS3Process> processes);
		public void onJMAPITemperature(JMAPI.PS3MAPI_RESPONSECODE responseCode, com.mrsmyx.ps3util.Temperature temperature);
	}
	private JMAPIListener listener;
	private int port = 7887;
	private String ip;
	
	public enum PS3BOOT{
		REBOOT,
		SOFTREBOOT,
		HARDREBOOT,
		SHUTDOWN
	}
	
	public enum BUZZER{
		SINGLE,
		DOUBLE,
		TRIPLE
	}
	
	public enum LEDCOLOR{
		RED,
		GREEN,
		YELLOW
	}
	
	public enum LEDMODE{
		OFF,
		ON,
		BLINKFAST,
		BLINKSLOW
	}
	
	public enum DELHISTORY{
		EXCLUDE_DIR,
		INCLUDE_DIR
	}
	
	public enum SYSCALL8MODE{
		ENABLED,
		ONLY_COBRAMAMBA_AND_PS3API_ENABLED,
		ONLY_PS3MAPI_ENABLED,
		FAKEDISABLED,
		DISABLED
	}
	
	public enum PS3MAPI_RESPONSECODE {
	    DATACONNECTIONALREADYOPEN(125), 
	    MEMORYSTATUSOK(150),
	    COMMANDOK(200),
	    REQUESTSUCCESSFUL(226),
	    ENTERINGPASSIVEMOVE(227),
	    PS3MAPICONNECTED(220),
	    PS3MAPICONNECTEDOK(230),
	    MEMORYACTIONCOMPLETED(250),
	    MEMORYACTIONPENDING(350);

	    private int id;
	    PS3MAPI_RESPONSECODE(int id) { this.id = id; }
	    public int getValue() { return id; }
	}
	
	public JMAPI(String ip){
		this.ip = ip;
	}
	
	public JMAPI(){	}
	
	public JMAPI(boolean searchNetwork){
		if(searchNetwork){
			scanNetwork();
			System.out.println(this.ip);
		}
	}

	
	public JMAPI(String ip, JMAPIListener listener){
		this.ip = ip;
		this.listener = listener;
	}
	
	public JMAPI(JMAPIListener listener){ this.listener = listener;	}
	
	public JMAPI(boolean searchNetwork, JMAPIListener listener){
		this.listener = listener;
		if(searchNetwork){
			scanNetwork();
			System.out.println(this.ip);
		}
		
	}
	
	
	enum VERSION{
		CORE,
		SERVER,
	}
	
	public String getFwVersion(){
		String s = "PS3 GETFWVERSION";
		Response res = super.Send(s);
		if(this.listener!=null){
			String str = new StringBuilder(Integer.toHexString(Integer.valueOf(res.getResponse()))).insert(1,".").toString();
			this.listener.onJMAPIResponse(PS3OP.FWVERSION, res.getResponseCode(), str);
		}
		return res.getResponse();
	}
	
	public void disableSysCall(SYSCALL8MODE mode){
		String s = "PS3 DISABLESYSCALL " + String.valueOf(mode.ordinal());
		Response res = super.Send(s);
		res.getResponse();
		if(this.listener != null){
			this.listener.onJMAPIResponse(PS3OP.DISSYSCALL, res.getResponseCode(), res.getResponse());
		}
	}
	
	public boolean checkSysCall(int mode){
		String s = "PS3 CHECKSYSCALL " + String.valueOf(mode);
		Response res = super.Send(s);
		if(this.listener != null){
			this.listener.onJMAPIResponse(PS3OP.CHECKSYSCALL, res.getResponseCode(), res.getResponse());
		}
		return Boolean.valueOf(res.getResponse());
	}
	
	public SYSCALL8MODE partialCheckSysCall(){
		String s = "PS3 PCHECKSYSCALL8";
		Response res = super.Send(s);
		if(this.listener != null){
			this.listener.onJMAPIResponse(PS3OP.SYSCALL8MODE, res.getResponseCode(), SYSCALL8MODE.values()[Integer.valueOf(res.getResponse())].toString());
		}
		return SYSCALL8MODE.values()[Integer.valueOf(res.getResponse())];
	}
	
	
	
	public boolean deleteHistory(DELHISTORY mode){
		String s = "PS3 DELHISTORY";
		switch(mode){
			case EXCLUDE_DIR:
				//TODO nothing
				break;
			case INCLUDE_DIR:
				s += "+D";
				break;
		}
		Response res = super.Send(s);
		if(this.listener != null ){
			this.listener.onJMAPIResponse(PS3OP.DELHISTORY, res.getResponseCode(),  mode + " : " + res.getResponse());
		}
		if(res.getResponseCode() == PS3MAPI_RESPONSECODE.COMMANDOK){
			return true;
		}
		return false;
	}
	
	public void checkSysCall(){
		//TODO : not implemented yet
	}
	
	public List<PS3Process> getAllProcesses(){
		List<PS3Process> process = new ArrayList<PS3Process>();
		String text = "PROCESS GETALLPID";
		Response res = super.Send(text);
		for(String s : res.getResponse().split("\\|")){
			if(s.equals("0")) continue;
		
			Response r = super.Send("PROCESS GETNAME " + s);
			process.add(PS3Process.Build(r.getResponse(), s));
		}
		if(process.size() > 0 && this.listener != null){
			this.listener.onJMAPIPS3Process(JMAPI.PS3MAPI_RESPONSECODE.REQUESTSUCCESSFUL, process);
		}
		return process;
	}
	
	public String getFwType(){
		String s = "PS3 GETFWTYPE";
		Response res = super.Send(s);
		if(this.listener != null){
			this.listener.onJMAPIResponse(PS3OP.FWTYPE, res.getResponseCode(), res.getResponse());
		}
		return res.getResponse();
	}

	protected Response getVersion(VERSION version){
		String s = version + " GETVERSION";
		Response res = super.Send(s);
		if(this.listener != null){
			this.listener.onJMAPIResponse(PS3OP.SCVERSION, res.getResponseCode(), res.getResponse());
		}
		return res;
	}
	
	protected Response getMinVersion(VERSION version){
		String s = version + " GETMINVERSION";
		Response res = super.Send(s);
		if(this.listener!=null){
			this.listener.onJMAPIResponse(PS3OP.SCMINVERSION, res.getResponseCode(), res.getResponse());
		}
		return res;
	}
	
	public void buzzer(BUZZER buzz) throws JMAPIException{
		if(!isConnected()){
			throw new JMAPIException("Not connected to host.");
		}
		String buzzer =  "PS3 BUZZER" + String.valueOf(buzz.ordinal()+1);
		Response r = super.Send(buzzer);
		if(this.listener != null){
			this.listener.onJMAPIResponse(PS3OP.BUZZ, r.getResponseCode(), "A buzz was send to the ps3");
		}
		System.out.println(r.getResponse());
	}
	
	public void boot(PS3BOOT ps3boot) throws JMAPIException {
		if(!isConnected()){
			throw new JMAPIException("Not connected to host.");
		}
		String boot = "PS3 " + ps3boot;
		Response res = super.Send(boot);
		if(this.listener != null){
			switch (ps3boot){
				case REBOOT:
					this.listener.onJMAPIResponse(PS3OP.BOOT, res.getResponseCode(),"Rebooting console");
					break;
				case SHUTDOWN:
					this.listener.onJMAPIResponse(PS3OP.BOOT, res.getResponseCode(),"Shutting down console");
					break;
				case HARDREBOOT:
					this.listener.onJMAPIResponse(PS3OP.BOOT, res.getResponseCode(),"Hard Rebooting console");
					break;
				case SOFTREBOOT:
					this.listener.onJMAPIResponse(PS3OP.BOOT, res.getResponseCode(),"Soft rebooting console");
					break;
			}
		}
		System.out.println(res.getResponse());
	}
	
	public void notify(String message) throws JMAPIException{
		if(!isConnected()){
			throw new JMAPIException("Not connected to host.");
		}
		String notify = "PS3 NOTIFY " + message;
		Response res = super.Send(notify);
		System.out.println(res.getResponse());
	}
	
	public void changeLed(LEDCOLOR color, LEDMODE mode) throws JMAPIException{
		if(!isConnected()){
			throw new JMAPIException("Not connected to host.");
		}
		String led = "PS3 LED " + String.valueOf(color.ordinal()) + " " + String.valueOf(mode.ordinal());
		Response res = super.Send(led);
		System.out.println(res.getResponseCode());
		if(this.listener != null){
			this.listener.onJMAPIResponse(PS3OP.LED, res.getResponseCode(), res.getResponse());
		}
	}
	
	public String getIDPS() throws JMAPIException{
		if(!isConnected()){
			throw new JMAPIException("Not connected to host.");
		}
		String idps = "PS3 GETIDPS";
		Response res = super.Send(idps);
		System.out.println(res.getResponseCode());
		if(this.listener != null){
			this.listener.onJMAPIResponse(PS3OP.IDPS, res.getResponseCode(), res.getResponse());
		}
		return res.getResponse();
	}
	
	public String getPSID() throws JMAPIException{
		if(!isConnected()){
			throw new JMAPIException("Not connected to host.");
		}
		String psid = "PS3 GETPSID";
		Response res = super.Send(psid);
		System.out.println(res.getResponseCode());
		if(this.listener != null){
			this.listener.onJMAPIResponse(PS3OP.PSID, res.getResponseCode(), res.getResponse());
		}
		return res.getResponse();
	}

	public Response setIDPS(String idps) throws JMAPIException{
		if(!isConnected()) throw new JMAPIException("Not connected to host.");
		String idps_cmd = "PS3 SETIDPS " + idps.substring(0,16) + " " + idps.substring(16);
		Response res = super.Send(idps_cmd);
		if(this.listener != null){
			this.listener.onJMAPIResponse(PS3OP.IDPSSET, res.getResponseCode(), res.getResponse());
		}
		return res;
	}


	public Response setPSID(String psid) throws JMAPIException{
		if(!isConnected()) throw new JMAPIException("Not connected to host.");
		String psid_cmd = "PS3 SETPSID " + psid.substring(0,16) + " " + psid.substring(16);
		Response res = super.Send(psid_cmd);
		if(this.listener != null){
			this.listener.onJMAPIResponse(PS3OP.PSIDSET, res.getResponseCode(), res.getResponse());
		}
		return res;

	}

	public boolean disconnect() {
		if(!isConnected()) return true; 
		try {
			super.Send("DISCONNECT");
		}catch (Exception ex){
			ex.printStackTrace();
		}
		try {
			super.Close();
			if(this.listener!=null){
				this.listener.onJMAPIResponse(PS3OP.DISCONNECTED, PS3MAPI_RESPONSECODE.COMMANDOK,"Disconnected from ps3");
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			if(this.listener!=null){
				this.listener.onJMAPIError("Something happened while disconnecting");
			}
			return false;
		}
	}
	
	public boolean isConnected(){
		if(super.client != null){
			return super.client.isConnected();
		}else{
			return false;
		}
	}
	
	public Temperature getTemp() throws JMAPIException{
		String psid = "PS3 GETTEMP";
		Response res = super.Send(psid);
		System.out.println(res.getResponse());
		if(!res.getResponse().contains(":")){
			String[] temp = res.getResponse().split("\\|");
			if(this.listener != null){
				this.listener.onJMAPITemperature(res.getResponseCode(), Temperature.instantiate(temp[0], temp[1]));
			}
			return Temperature.instantiate(temp[0], temp[1]);
		}else{
			throw new JMAPIException("Error: could not obtain temperature");
		}
	}
	
	public boolean scanNetwork(){
		try {
			this.ip = Network.getReachableHosts(Network.scanSubNets());
			if(ip == null) return false;
			this.connect();
			this.buzzer(BUZZER.DOUBLE);
			if(this.listener != null){
				this.listener.onJMAPIResponse(PS3OP.NETWORK_FOUND, PS3MAPI_RESPONSECODE.COMMANDOK, this.ip);
			}
			return true;
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		} catch (JMAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean connect(){
		if(super.Connect(ip, port)){
			return true;
		}else{
			return false;
		}
	}
	
	
}
