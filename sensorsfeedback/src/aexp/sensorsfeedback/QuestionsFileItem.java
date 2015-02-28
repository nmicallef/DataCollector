package aexp.sensorsfeedback;


public class QuestionsFileItem {
	QuestionsFileItem( String d, String s, String da ) {
        this.status = s;
        this.date = d;
        this.day = da;
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
    
    void setDay( String da ) {
    	this.day = da;
    }

    public String getDay() {
    	return this.day;
    }
    
    private String day;
    private String date;
    private String status;


}
