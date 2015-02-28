package aexp.sensorsfeedback;

public class AccessPoint {
	private String name;
	private String id;
	private int signal;
	private String groupname;
	
	
	public AccessPoint(String name, String id, int sig, String gname){
		this.name= name;
		this.id = id;
		this.signal = sig;
		this.groupname = gname;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String n){
		this.name = n;
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setId(String i){
		this.id = i;
	}
	
	public int getSignal(){
		return this.signal;
	}
	
	public void setSignal(int s){
		this.signal = s;
	}
	
	public String getGroupname(){
		return this.groupname;
	}
	
	public void setGroupname(String n){
		this.groupname = n;
	}
	
	public String toString(){
		return this.name+","+this.id+","+this.signal+","+this.groupname;
	}
	
}
