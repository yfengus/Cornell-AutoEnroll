
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;


public class GUI {

	private JFrame CAEgui;
	private JComboBox deptComboBox;
	private JComboBox courseComboBox;
//	private JComboBox lecComboBox;
//	private JComboBox disComboBox;
//	private JComboBox labComboBox;
	private JScrollPane classTableScrollPane;
	private JButton addButton;
	private JButton delButton;
	private JButton clrButton;
	private JButton actionButton;
	private StartActionListener startActionListener = new StartActionListener();
	private StopActionListener stopActionListener = new StopActionListener();
	
	private static String program_Name_Version = "Cornell AutoEnroll V0.3";   
	
	/*
	 * V0.1: original version, no time interval
	 * V0.2: added time interval for retrying to enroll class 
	 * V0.3: fixed "Scroll to View" issue on Windows by using Selenium 2.14 instead of 2.15
	 */
	
	//need initialization every time
	private JComboBox[] timeComboBox = new JComboBox[3];
	private String curDept;
	private String curCourseNum;
	private String[] sectionTimes = new String[3];
	private String[] sectionTypes = new String[3];
	private int listCnt = 0;
	
	String[] columnNames = {"Priority", 
			"Course",
            "Section Time",
            "Section Time",
            "Section Time",
            "Status"};
	private ArrayList<ClassRow> classList = null;
	Runner runner = null;
	DefaultTableModel classTableModel = new DefaultTableModel(null, columnNames);
//	classTableModel.setValueAt("haha", 0, 0);
//	ArrayList<String> newRowS = new ArrayList<String>();
//	newRowS.add();
//	Vector newRow = new Vector(newRowS);
	final JTable classTable = new JTable(classTableModel);
	Timer timer;
	JOptionPane popMsgPane = null;
	JDialog popMsgDialog = null;
	
	
	
	
	private JButton upButton;
	private JButton downButton;
	private JLabel noteLabel;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		//EventQueue.invokeLater(new Runnable() {
			//public void run() {
				//try {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (UnsupportedLookAndFeelException e) {
						e.printStackTrace();
					}
					GUI window = new GUI();
					window.CAEgui.setVisible(true);
				//	System.out.println(Character.isLetterOrDigit(' '));
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
		//	}
	//	});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}

	private void initDataFields() {
		for (int i=0; i<3; i++) {
			timeComboBox[i].setModel(new DefaultComboBoxModel());
			sectionTimes[i] = "";
			sectionTypes[i] = "";
		}
		listCnt = 0;
		curCourseNum = "";
	}
	private void initialize() {
		try {
		CAEgui = new JFrame();
		CAEgui.setTitle(program_Name_Version);
		CAEgui.setResizable(false);
		CAEgui.setBounds(100, 100, 796, 408);
		CAEgui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		CAEgui.getContentPane().setLayout(null);
		
		timeComboBox[0] = new JComboBox();
		timeComboBox[0].setBounds(6, 35, 234, 27);
		timeComboBox[0].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				sectionTimes[0] = (String)timeComboBox[0].getSelectedItem();
				System.out.println(sectionTypes[0]+" : "+sectionTimes[0]);
			}
		});
		
		timeComboBox[1] = new JComboBox();
		timeComboBox[1].setBounds(237, 35, 234, 27);
		timeComboBox[1].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				sectionTimes[1] = (String)timeComboBox[1].getSelectedItem();
				System.out.println(sectionTypes[1]+" : "+sectionTimes[1]);
			}
		});
		
		timeComboBox[2] = new JComboBox();
		timeComboBox[2].setBounds(468, 35, 234, 27);
		timeComboBox[2].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				sectionTimes[2] = (String)timeComboBox[2].getSelectedItem();
				System.out.println(sectionTypes[2]+" : "+sectionTimes[2]);
			}
		});
		
		CAEgui.getContentPane().add(timeComboBox[0]);
		CAEgui.getContentPane().add(timeComboBox[1]);
		CAEgui.getContentPane().add(timeComboBox[2]);
		
		
		//6, 11, 322, 22
		deptComboBox = new JComboBox(getDeptList());
		deptComboBox.setBounds(6, 11, 322, 22);
		deptComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				initDataFields();	
				courseComboBox.setModel(new DefaultComboBoxModel(getClassList("")));
				String selected = (String)deptComboBox.getSelectedItem();
				if (!(selected.equals("Select Department"))) {
						curDept = selected.split(" - ")[0];
						courseComboBox.setModel(new DefaultComboBoxModel(getClassList(curDept)));
				}
			}
		});
		CAEgui.getContentPane().add(deptComboBox);
		
		courseComboBox = new JComboBox(getClassList(""));
		courseComboBox.setBounds(324, 11, 378, 22);
		courseComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				
				initDataFields();
				
				String selected = (String)courseComboBox.getSelectedItem();
				if (!(selected.equals("Select Course"))) {
					curCourseNum = selected.split(":")[0].split(" ")[1];
					System.out.println(curCourseNum);
					HashMap<String, Vector<String>> curTimeLists = getTimeLists(curDept, curCourseNum);
					
					for (String type : curTimeLists.keySet()) {
						if (type.equals("LEC")) {
							timeComboBox[listCnt].setModel(new DefaultComboBoxModel(curTimeLists.get(type)));
							sectionTypes[listCnt] = type;
							listCnt++;
						}
					}
					for (String type : curTimeLists.keySet()) {
						if (type.equals("DIS")) {
							timeComboBox[listCnt].setModel(new DefaultComboBoxModel(curTimeLists.get(type)));
							sectionTypes[listCnt] = type;
							listCnt++;
						}
					}
					for (String type : curTimeLists.keySet()) {
						if (type.equals("LAB")) {
							timeComboBox[listCnt].setModel(new DefaultComboBoxModel(curTimeLists.get(type)));
							sectionTypes[listCnt] = type;
							listCnt++;
						}
					}
					for (String type : curTimeLists.keySet()) {
						if (type.equals("SEM")) {
							timeComboBox[listCnt].setModel(new DefaultComboBoxModel(curTimeLists.get(type)));
							sectionTypes[listCnt] = type;
							listCnt++;
						}
					}
					for (String type : curTimeLists.keySet()) {
						if (type.equals("IND")) {
							timeComboBox[listCnt].setModel(new DefaultComboBoxModel(curTimeLists.get(type)));
							sectionTypes[listCnt] = type;
							listCnt++;
						}
					}
					for (String type : curTimeLists.keySet()) {
						if (type.equals("RSC")) {
							timeComboBox[listCnt].setModel(new DefaultComboBoxModel(curTimeLists.get(type)));
							sectionTypes[listCnt] = type;
							listCnt++;
						}
					}
				}
					
			}
		});
		CAEgui.getContentPane().add(courseComboBox);
		
		
		
		
		
		
		
		classTableScrollPane = new JScrollPane(classTable);
		classTableScrollPane.setBounds(10, 83, 688, 294);
		
		CAEgui.getContentPane().add(classTableScrollPane);
		
		addButton = new JButton("Add");
		addButton.setBounds(702, 9, 88, 52);
		addButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
//				System.out.println(getSecInfo(sectionTypes[2], sectionTimes[2])[0]+" "+getSecInfo(sectionTypes[2], sectionTimes[2])[1]+" "+getSecInfo(sectionTypes[2], sectionTimes[2])[2]);
				ClassRow curClassRow = new ClassRow(curDept, curCourseNum, 
						getSecInfo(sectionTypes[0], sectionTimes[0])[0], getSecInfo(sectionTypes[0], sectionTimes[0])[1], getSecInfo(sectionTypes[0], sectionTimes[0])[2],
						getSecInfo(sectionTypes[1], sectionTimes[1])[0], getSecInfo(sectionTypes[1], sectionTimes[1])[1], getSecInfo(sectionTypes[1], sectionTimes[1])[2],
						getSecInfo(sectionTypes[2], sectionTimes[2])[0], getSecInfo(sectionTypes[2], sectionTimes[2])[1], getSecInfo(sectionTypes[2], sectionTimes[2])[2]);
//				classList.put(classRowCnt, curClassRow);
				String[] curClassString = curClassRow.getTableString();
				curClassString[0] = Integer.toString(classTableModel.getRowCount()+1);
				classTableModel.addRow(curClassString);
				
			}
		});
		CAEgui.getContentPane().add(addButton);
		
		
		upButton = new JButton("↑");
		upButton.setBounds(702, 81, 44, 52);
		upButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (classTable.getSelectedRow()!=0) {
					classTableModel.moveRow(classTable.getSelectedRow(), classTable.getSelectedRow(), classTable.getSelectedRow()-1);
					classTable.setRowSelectionInterval(classTable.getSelectedRow()-1, classTable.getSelectedRow()-1);
					for (int i=0; i<classTable.getRowCount(); i++){
						classTableModel.setValueAt(Integer.toString(i+1), i, 0);
					}
				}
			}
		});
		CAEgui.getContentPane().add(upButton);
		
		downButton = new JButton("↓");
		downButton.setBounds(746, 81, 44, 52);
		downButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (classTable.getSelectedRow()!=(classTable.getRowCount()-1)){
					classTableModel.moveRow(classTable.getSelectedRow(), classTable.getSelectedRow(), classTable.getSelectedRow()+1);
					classTable.setRowSelectionInterval(classTable.getSelectedRow()+1, classTable.getSelectedRow()+1);
					for (int i=0; i<classTable.getRowCount(); i++){
						classTableModel.setValueAt(Integer.toString(i+1), i, 0);
					}
				}
			}
		});
		CAEgui.getContentPane().add(downButton);
		
		
		delButton = new JButton("Delete");
		delButton.setBounds(702, 134, 88, 52);
		delButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (classTable.getSelectedRow()==-1) {
					JOptionPane.showMessageDialog(null, "No row is selected!");
				} else {
					classTableModel.removeRow(classTable.getSelectedRow());
				}
			}
		});
		CAEgui.getContentPane().add(delButton);
		
		clrButton = new JButton("Clear All");
		clrButton.setBounds(702, 187, 88, 52);
		clrButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
//				System.out.println(classTableModel.getRowCount());
				int numOfRows = classTableModel.getRowCount();
				for(int i=0; i<numOfRows; i++) {
					classTableModel.removeRow(0);
				}
			}
		});
		CAEgui.getContentPane().add(clrButton);
		
		//"Start" mode
		actionButton = new JButton("Start!");
		actionButton.setBounds(702, 325, 88, 52);
		actionButton.addActionListener(startActionListener);
		
		CAEgui.getContentPane().add(actionButton);
		
		noteLabel = new JLabel("Note: please add the most important classes first.");
		noteLabel.setBounds(10, 64, 692, 13);
		CAEgui.getContentPane().add(noteLabel);
		
		} catch (Exception e) {
			String errMsg = "";
			for (StackTraceElement elem : e.getStackTrace()) {
				errMsg = errMsg + elem.toString();
			}
			JOptionPane.showMessageDialog(null, errMsg);
		}
	}
	
	//Action listener for "Start" button
	class StartActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e){
			classList = new ArrayList<ClassRow>();
			
			for (int i=0; i<classTableModel.getDataVector().size(); i++) {
//				System.out.println(classTableModel.getDataVector().get(i).toString());
				String curClassStr = classTableModel.getDataVector().get(i).toString().substring(classTableModel.getDataVector().get(i).toString().indexOf(", ")+2);
				curClassStr = curClassStr.substring(0, curClassStr.length()-1);
//				System.out.println(curClassStr);
				/*ClassRow (String _deptName, String _courseNum, 
				String _secType0, String _secNum0, String _secTime0, 
				String _secType1, String _secNum1, String _secTime1, 
				String _secType2, String _secNum2, String _secTime2)
				*/
				String deptNameStr = curClassStr.split(" ")[0]; 
				String courseNumStr = curClassStr.split(", ")[0].split(" ")[1];  
				
				//handle section 1
				String secType0Str = curClassStr.split(", ")[1].split(" ")[0]; 
				String secNum0Str = curClassStr.split(", ")[1].split(" ")[1]; 
				String secTime0Str = curClassStr.split(", ")[1].split(" ",3)[2]; 
				
				//handle section 2
				String sec1Str = curClassStr.split(", ")[2];
				String secType1Str = ""; 
				String secNum1Str = ""; 
				String secTime1Str = ""; 
				for (int j=0; j<sec1Str.length(); j++) {
					if (Character.isLetterOrDigit(sec1Str.charAt(j))) {
						secType1Str = sec1Str.split(" ")[0]; 
						secNum1Str = sec1Str.split(" ")[1]; 
						secTime1Str = sec1Str.split(" ",3)[2]; 
						break;
					}
				}
				
				//handle section 3
				String sec2Str = curClassStr.split(", ")[3];
				String secType2Str = ""; 
				String secNum2Str = ""; 
				String secTime2Str = "";
				for (int k=0; k<sec2Str.length(); k++) {
					if (Character.isLetterOrDigit(sec2Str.charAt(k))) {
						secType2Str = sec2Str.split(" ")[0]; 
						secNum2Str = sec2Str.split(" ")[1]; 
						secTime2Str = sec2Str.split(" ",3)[2]; 
						break;
					}
				}
				
				ClassRow curClassRow = new ClassRow(deptNameStr, courseNumStr, 
						secType0Str, secNum0Str, secTime0Str, 
						secType1Str, secNum1Str, secTime1Str, 
						secType2Str, secNum2Str, secTime2Str);
				classList.add(curClassRow);
			}
			System.out.println("Class list:");
			for (ClassRow classRow : classList) {
				System.out.println(classRow.toString());
			}
			System.out.println("Start is pressed");
			actionButton.setText("Stop");
			actionButton.removeActionListener(startActionListener);
			actionButton.addActionListener(stopActionListener);
			
			popMsgPane = null;
			
			Timer popMsgTimer = new Timer(10000, new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (popMsgPane == null) {
						popMsgPane = new JOptionPane();
						popMsgDialog = popMsgPane.createDialog("Please wait...");
						popMsgDialog.setVisible(true);
//						popMsgPane.setVisible(true);
					}
					else {
						popMsgDialog.setVisible(false);
					}
				}
			});
			popMsgTimer.setInitialDelay(0);
			popMsgTimer.start();

			
			//TODO: classList is what needs to be passed to runner
			runner = new Runner(classList);
			runner.start();
			
			timer = new Timer(200, new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					for (ClassRow curClass : classList) {
						for (int i=0; i<classTableModel.getRowCount(); i++) {
							if (classTableModel.getValueAt(i, 1).equals(curClass.deptName+" "+curClass.courseNum)) {
//								System.out.println("Class matched");
								classTableModel.setValueAt(curClass.status, i, 5);
								classTableModel.fireTableDataChanged();
								break;
							}
							
						}
					}
			    }
			});
			timer.start();
			
			}
		
	}
	
	class StopActionListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			//TODO: add something
			
			System.out.println("Stop is pressed");
			actionButton.setText("Start!");
			actionButton.removeActionListener(stopActionListener);
			actionButton.addActionListener(startActionListener);
			
			try {
				runner.dr.quit();
			} catch (Exception e) {
				e.printStackTrace();
			}
			timer.stop();
			
		}
		
	}
	
	public HashMap<String, Vector<String>> getTimeLists(String deptName, String courseNum){
		HashMap<String, TreeSet<TimeRow>> retTSet = new HashMap<String, TreeSet<TimeRow>>();
//		retTSet.put("LEC", new TreeSet<TimeRow>());
//		retTSet.put("DIS", new TreeSet<TimeRow>());
//		retTSet.put("LAB", new TreeSet<TimeRow>());
//		retTSet.put("SEM", new TreeSet<TimeRow>());
//		retTSet.put("IND", new TreeSet<TimeRow>());
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("DeptClassList//"+deptName+"//"+deptName+" "+courseNum+".txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		Scanner sc = new Scanner(br);
		String curLine = sc.nextLine();
		curLine.replace(",,", ", ,");
		System.out.println(curLine);
		while (curLine.split(",").length>1) {
			String[] curElems = curLine.split(",", -1);
			if (!(retTSet.keySet().contains(curElems[1]))) {
				System.out.println("New type: " + curElems[1]);
				retTSet.put(curElems[1], new TreeSet<TimeRow>());
			}
			retTSet.get(curElems[1]).add(new TimeRow(curElems[2]+","+curElems[3], curElems[0]));
			if (sc.hasNextLine()) {
				curLine = sc.nextLine();
				curLine.replace(",,", ", ,");
			}
			else break;
			System.out.println(curLine);
		}
		
		HashMap<String, Vector<String>> retVec = new HashMap<String, Vector<String>>();
		for (String type : retTSet.keySet()){
			retVec.put(type, new Vector<String>());
			retVec.get(type).add("Select "+type+" time");
			for (TimeRow curTimeRow : retTSet.get(type)) {
				retVec.get(type).add(curTimeRow.toString());
			}
		}
		return retVec;
	}
	
	public Vector<String> getDeptList(){
		ArrayList<String> retList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("DeptClassList//DeptList.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Scanner sc = new Scanner(br);
		String curLine = "";
		String[] curElems = null;
		while (sc.hasNextLine()){
			curLine = sc.nextLine();
			curElems = curLine.split(",");
			retList.add(curElems[0]+" - "+curElems[1]);
		}
		
		Vector<String> retVec = new Vector<String>();
		retVec.add("Select Department");
		for (String deptName : retList) {
			retVec.add(deptName);
		}
		return retVec;	    
	}
	
	public Vector<String> getClassList(String deptName){
		if (deptName.equals("")) {
			Vector<String> retVec = new Vector<String>();
			retVec.add("Please select department first.");
			return retVec;
		}
		ArrayList<String> retList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("DeptClassList//"+deptName+"//"+deptName+"_ClassList.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Scanner sc = new Scanner(br);
		String[] curElems = null;
		String curLine = "";
		curLine = sc.nextLine();
		while (curLine.length()>0){
			System.out.println(curLine);
			curElems = curLine.split(",");
			retList.add(curElems[0]+": "+curElems[1]);
			curLine = sc.nextLine();
		}
		Vector<String> retVec = new Vector<String>();
		retVec.add("Select Course");
		for (String className : retList) {
			retVec.add(className);
		}
		return retVec;
	}
	
	public Vector<String> getVec(String text){
		Vector<String> ret = new Vector<String>();
		ret.add(text);
		return ret;	
	}
	
	public String[] getSecInfo(String secType, String secTime) {
		String[] ret = new String[3];
		if (secType.equals("")) {
			ret[0] = "";
			ret[1] = "";
			ret[2] = "";
			return ret;
		} else {
			ret[0] = secType;
			ret[1] = secTime.split(": ")[0];
			ret[2] = secTime.split(": ")[1];
			return ret;
		}
	}
}
