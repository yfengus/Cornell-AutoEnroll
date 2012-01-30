
public class ClassRow {
	String deptName = null;
	String courseNum = null;
	
	String[] sectionTypes = new String[3]; 
	String[] sectionNums = new String[3];
	String[] sectionTimes = new String[3];
//	String lecNum = null;
//	String lecTime = null;
//	String disNum = null;
//	String disTime = null;
//	String labNum = null;
//	String labTime = null;
	String status = "";
	
	ClassRow (String _deptName, String _courseNum, 
			String _secType0, String _secNum0, String _secTime0, 
			String _secType1, String _secNum1, String _secTime1, 
			String _secType2, String _secNum2, String _secTime2) {
		deptName = _deptName;
		courseNum = _courseNum;
		
		sectionTypes[0] = _secType0;
		sectionNums[0] = _secNum0;
		sectionTimes[0] = _secTime0;
		
		sectionTypes[1] = _secType1;
		sectionNums[1] = _secNum1;
		sectionTimes[1] = _secTime1;
		
		sectionTypes[2] = _secType2;
		sectionNums[2] = _secNum2;
		sectionTimes[2] = _secTime2;
	}
	
	public String toString() {
		return deptName + " " + courseNum + " " + sectionTypes[0] + ":" + sectionNums[0] + " " +sectionTimes[0] + "; " +
				sectionTypes[1] + ":" + sectionNums[1] + " " +sectionTimes[1] + "; " +
				sectionTypes[2] + ":" + sectionNums[2] + " " +sectionTimes[2];
	}
	
	public String[] getTableString() {
		String[] ret = new String[6];
		ret[0] = "1";
		ret[1] = deptName + " " + courseNum;
		ret[2] = sectionTypes[0] + " " + sectionNums[0] + " " + sectionTimes[0];
		ret[3] = sectionTypes[1] + " " + sectionNums[1] + " " + sectionTimes[1];
		ret[4] = sectionTypes[2] + " " + sectionNums[2] + " " + sectionTimes[2];
		ret[5] = "";
		return ret;
	}
	
	public String[] getRunnerString() {
		String[] ret = new String[4];
		ret[0] = deptName + " " + courseNum;
		ret[1] = sectionNums[0];
		ret[2] = sectionNums[1];
		ret[3] = sectionNums[2];
		return ret;
	}
	
	public void updateStatus(String newStatus) {
		status = newStatus;
	}
	
	public String getStatus() {
		return status;
	}
	
}
