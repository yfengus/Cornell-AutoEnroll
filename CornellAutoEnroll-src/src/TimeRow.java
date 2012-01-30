
public class TimeRow implements Comparable<TimeRow>{
	String time;
	String ID;
	
	TimeRow (String _time, String _ID) {
		time = _time;
		ID = _ID;
	}
	
	public String toString() {
		return ID + ": " + time;
	}

	public int compareTo(TimeRow other) {
		return ID.compareTo(other.ID);
	}
	
	
}
