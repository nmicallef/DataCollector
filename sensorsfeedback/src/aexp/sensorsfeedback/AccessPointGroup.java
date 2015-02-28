package aexp.sensorsfeedback;

public class AccessPointGroup {
	
	private String name;
	private String date;
	private String hours;
	private String minutes;
	private String sound_file;
	private String fingerprint_file;
	private String spectogram_image;
	private String wav_file;
	
	
	
	public AccessPointGroup(String name, String date, String hours,String minutes, String sound_file, String fingerprint_file, String spectogram_image, String wav_file){
		this.name= name;
		this.date = date;
		this.hours = hours;
		this.minutes = minutes;
		this.sound_file = sound_file;
		this.fingerprint_file = fingerprint_file;
		this.spectogram_image = spectogram_image;
		this.wav_file = wav_file;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String n){
		this.name = n;
	}
	
	public String getDate(){
		return this.date;
	}
	
	public void setDate(String i){
		this.date = i;
	}
	
	public String getHours(){
		return this.hours;
	}
	
	public void setHours(String s){
		this.hours = s;
	}
	
	public String getMinutes(){
		return this.minutes;
	}
	
	public void setMinutes(String s){
		this.minutes = s;
	}
	
	public String getSound(){
		return this.sound_file;
	}
	
	public void setSound(String n){
		this.sound_file = n;
	}
	
	public String getFingerprint(){
		return this.fingerprint_file;
	}
	
	public void setFingerprint(String n){
		this.fingerprint_file = n;
	}
	
	public String getSpectogram(){
		return this.spectogram_image;
	}
	
	public void setSpectogram(String n){
		this.spectogram_image = n;
	}
	
	public String getWavFile(){
		return this.wav_file;
	}
	
	public void setWavFile(String n){
		this.wav_file = n;
	}
	
	public String toString(){
		return this.name+","+this.date+","+this.hours+","+this.minutes+","+this.sound_file+","+this.fingerprint_file+","+this.spectogram_image+","+this.wav_file;
	}
}
