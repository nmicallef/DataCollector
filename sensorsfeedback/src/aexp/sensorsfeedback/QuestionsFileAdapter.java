package aexp.sensorsfeedback;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class QuestionsFileAdapter extends BaseAdapter {
	
	public QuestionsFileAdapter(Context context,List<QuestionsFileItem> f ) {
		inflater = LayoutInflater.from( context );
		this.context = context;
		this.files = f;
	}

	public int getCount() {
		return files.size();
	}

	public Object getItem(int arg0) {
		return files.get(arg0);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		QuestionsFileItem item = files.get(position);
        View v = null;
        if( convertView != null ){
        	v = convertView;
        }else{
        	v = inflater.inflate( R.layout.questionsfile_row, parent, false);
        }
        String day = item.getDay();
        TextView dayTV = (TextView)v.findViewById( R.id.questionsday);
        dayTV.setText( day);
        String fileName = item.getDate();
        TextView fileNameTV = (TextView)v.findViewById( R.id.questionsfilename);
        fileNameTV.setText( fileName );
        String fileStatus = item.getStatus();
        TextView fileStatusTV = (TextView)v.findViewById( R.id.questionsfilestatus);
        fileStatusTV.setText( fileStatus );
        return v;
	}
	
	private Context context;
    private List<QuestionsFileItem> files;
	private LayoutInflater inflater;

}
