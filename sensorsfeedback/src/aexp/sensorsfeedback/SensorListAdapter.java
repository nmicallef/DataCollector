package aexp.sensorsfeedback;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import java.util.List;

public class SensorListAdapter extends BaseAdapter {

    public SensorListAdapter(Context context,
						List<SensorItem> sensors ) {
		inflater = LayoutInflater.from( context );
        this.context = context;
        this.sensors = sensors;
    }

    public int getCount() {
        return sensors.size();
    }

    public Object getItem(int position) {
        return sensors.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
    	
    	SensorItem item = sensors.get(position);
        View v = null;
        if( convertView != null )
        	v = convertView;
        else
        	v = inflater.inflate( R.layout.sensor_row, parent, false);
        String sensorName = item.getSensorName();
        TextView sensorNameTV = (TextView)v.findViewById( R.id.sensorname);
        sensorNameTV.setText( sensorName );
        boolean sampling = item.getSampling();
    	TextView samplingStatusTV = (TextView)v.findViewById( R.id.samplingstatus );
    	if( sampling )
    		samplingStatusTV.setVisibility( View.VISIBLE);
    	else
    		samplingStatusTV.setVisibility( View.INVISIBLE);
    	
    	
    	CheckBox cb =(CheckBox)v.findViewById(R.id.checkBox);
    	cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton cb, boolean flag) {
            	int getPosition = (Integer) cb.getTag(); 
                SensorItem si =getSensorItem(getPosition);
                si.setCheckboxState(flag);
            }
        });
       	cb.setTag(position); 
    	boolean cbstate = item.getCheckboxState();
    	cb.setChecked(cbstate);
    	
    	return v;
    }
    
    SensorItem getSensorItem(int position) {
        return ((SensorItem) getItem(position));
    }
    

    private Context context;
    private List<SensorItem> sensors;
	private LayoutInflater inflater;
	

}
