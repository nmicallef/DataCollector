package aexp.sensorsfeedback;

public class DataCompletionItem {
	
	DataCompletionItem( String d, String p, String st ) {
        this.date = d;
        this.percentage = p;
        this.status = st;
    }

    public String getStatus() {
        return status;
    }

    void setStatus(String s) {
        this.status =s;
    }

    void setDate( String d ) {
    	this.date = d;
    }

    public String getDate() {
    	return this.date;
    }
    
    void setPercentage( String p ) {
    	this.percentage = p;
    }

    public String getPercentage() {
    	return this.percentage;
    }
    
    private String date;
    private String percentage;
    private String status;
}
