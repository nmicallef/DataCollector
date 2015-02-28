package aexp.sensorsfeedback;

import android.hardware.Sensor;

class SensorItem {
    SensorItem( String sensorName ) {
        this.sensorName = sensorName;
        this.sampling = false;
        this.checkboxstate = false;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String name) {
        this.sensorName = name;
    }

    void setSampling( boolean sampling ) {
    	this.sampling = sampling;
    }

    boolean getSampling() {
    	return sampling;
    }
    
    void setCheckboxState( boolean state ) {
    	this.checkboxstate = state;
    }

    boolean getCheckboxState() {
    	return checkboxstate;
    }

    private String sensorName;
    private boolean sampling;
    private boolean checkboxstate;
}
