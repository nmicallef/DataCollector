package aexp.sensorsfeedback;

import java.util.ArrayList;

public class RelatedAccessPointGroups {
	private String name;
	private ArrayList list;
	
	
	public RelatedAccessPointGroups(String name){
		this.name= name;
		this.list = new ArrayList();
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String n){
		this.name = n;
	}
	
	public ArrayList getList(){
		return this.list;
	}
	
	public void setList(ArrayList i){
		this.list = i;
	}
	
	public void addtolist(String x){
		this.list.add(x);
	}
	
	public String toString(){
		return this.name+","+this.list.size();
	}
}
