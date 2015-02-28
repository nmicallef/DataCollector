package aexp.sensorsfeedback;

import java.util.ArrayList;
import java.util.Arrays;

public class Statistics {
	static ArrayList<Double> data;
    static double size;    

    public Statistics(ArrayList data) 
    {
        this.data = data;
        size = data.size();
    }   

    double getMean()
    {
        double sum = 0.0;
        for(double a : data)
            sum += a;
            return sum/size;
    }
    
    double getMinimum()
    {
        double min = 1000.0;
        for(double a : data){
        	if (a < min){
        		min = a;
        	}
        }
        return min;
    }
    double getMaximum()
    {
        double max = -1000;
        for(double a : data){
        	if (a > max){
        		max = a;
        	}
        }
        return max;
    }
    
    double getRange()
    {
        double max = -1000;
        double min = 1000;
        for(double a : data){
        	if (a > max){
        		max = a;
        	}
        	if (a < min){
        		min = a;
        	}
        }
        return max-min;
    }
    

    double getVariance()
    {
        double mean = getMean();
        double temp = 0;
        for(double a :data)
           temp += (mean-a)*(mean-a);
           return temp/size;
    }

    double getStdDev()
    {
    	return Math.sqrt(getVariance());
    }
    
    double getMode() {
        int maxCount = -1;
        double maxValue= -1;
        for (int i = 0; i < data.size(); ++i) {
            int count = 0;
            for (int j = 0; j < data.size(); ++j) {
                if ((double)data.get(j) == (double)data.get(i)) ++count;
            }
            if (count > maxCount) {
                maxCount = count;
                maxValue = (double) data.get(i);
            }
        }
        return maxValue;
    }

    public static double median() 
    {
        try{
        	if (data.size() > 0){
        		double[] b = new double[data.size()];
        		for (int i=0; i < data.size(); i++){
        			b[i] = data.get(i);
        		}
        		Arrays.sort(b);
        		if (data.size() % 2 == 0) 
        		{
        			return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
        		} 
        		else 
        		{
        			return b[b.length / 2];
        		}
        	}else{
        		return -1;
        	}
        }catch(Exception e){e.printStackTrace();return -1;}
    }
}
