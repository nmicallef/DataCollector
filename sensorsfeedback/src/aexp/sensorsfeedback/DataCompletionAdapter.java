package aexp.sensorsfeedback;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DataCompletionAdapter extends BaseAdapter {
		
		public DataCompletionAdapter(Context context,List<DataCompletionItem> f ) {
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
			DataCompletionItem item = files.get(position);
	        View v = null;
	        if( convertView != null ){
	        	v = convertView;
	        }else{
	        	v = inflater.inflate( R.layout.datacompletion_row, parent, false);
	        }
	        String date = item.getDate();
	        TextView dateTV = (TextView)v.findViewById( R.id.completionday);
	        dateTV.setText( date);
	        String percentage = item.getPercentage();
	        TextView percentageTV = (TextView)v.findViewById( R.id.completionpercentage);
	        percentageTV.setText( percentage );
	        String fileStatus = item.getStatus();
	        TextView fileStatusTV = (TextView)v.findViewById( R.id.completionstatus);
	        fileStatusTV.setText( fileStatus );
	        return v;
		}
		
		private Context context;
	    private List<DataCompletionItem> files;
		private LayoutInflater inflater;

}
