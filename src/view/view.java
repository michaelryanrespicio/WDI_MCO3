package view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.JButton;
import java.awt.Choice;
import javax.swing.JList;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import model.BTMWithMySQL;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

public class view {
	
	
	private JFrame frmAdvandbMco;
	private JTextArea textArea;
	private JButton btnNewButton;
	private JButton btnNewButton_1;
	private JComboBox<String> comboBox;
	private static ArrayList<String> query_list;
	private JTextArea textArea_1;
 	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		new BTMWithMySQL();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					view window = new view();
					window.frmAdvandbMco.setVisible(true);
					int selected = 0;
					window.textArea.setText(query_list.get(0));
					if(window.comboBox.getSelectedIndex() != selected)
						selected = window.comboBox.getSelectedIndex();
					window.textArea.setText(query_list.get(selected));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public view() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		ArrayList<String> list = new ArrayList<String>();
		//Select queries
		/*0*/list.add("SELECT Number of primary students that didn't reach secondary school (for both sexes)");
		/*1*/list.add("SELECT Number of enrolled primary students and out of school primary (for both sexes)");
		/*2*/list.add("SELECT Male versus Female Literacy Rate");
		/*3*/list.add("SELECT Pupil-Teacher ratio versus actual Pupil-Teacher count in primary education.");
		/*4*/list.add("SELECT Required number od educational years in relation with the country's income.");
		/*5*/list.add("SELECT Female VS Male Primary Students");
		/*6*/list.add("SELECT Primary education,  pupils (% female)");
		/*7*/list.add("SELECT Enrolment in primary education,  both sexes (number)");
		//insert in EU & AM
		/*8*/list.add("INSERT EU & AM Enrolment in primary education,  both sexes (number)");
		/*9*/list.add("INSERT EU & AM Primary education,  pupils (% female)");
		//insert in AS & AF
		/*10*/list.add("INSERT AS & AF Enrolment in primary education,  both sexes (number)");
		/*11*/list.add("INSERT AS & AF Primary education,  pupils (% female)");
		//delete from EU & AM
		/*12*/list.add("DELETE EU & AM Enrolment in primary education,  both sexes (number)");
		/*13*/list.add("DELETE EU & AM Primary education,  pupils (% female)");
		//delete from AS & AF
		/*14*/list.add("DELETE AS & AF Enrolment in primary education,  both sexes (number)");
		/*15*/list.add("DELETE AS & AF Primary education,  pupils (% female)");
		//update
		/*16*/list.add("UPDATE EU & AM Enrolment in primary education,  both sexes (number)");
		/*17*/list.add("UPDATE EU & AM Primary education,  pupils (% female)");
		/*18*/list.add("UPDATE AS & AF Enrolment in primary education,  both sexes (number)");
		/*19*/list.add("UPDATE AS & AF Primary education,  pupils (% female)");
		
		query_list = new ArrayList<String>();
		query_list.add("SELECT DBY.countrycode, DBY.yearc, DBY.data-SecondaryEnrollment.data as Not_Enrolled_in_Secondary_School" + 
					   "\nFROM databyyear DBY, (SELECT * " 
						+ "FROM databyyear DBY WHERE seriescode = \"SE.SEC.ENRL\") SecondaryEnrollment" 
						+ "\nWHERE DBY.seriescode = \"SE.PRM.ENRL\" AND SecondaryEnrollment.countrycode = DBY.countrycode"
						+ "\nAND SecondaryEnrollment.yearc = DBY.yearc AND DBY.data > SecondaryEnrollment.data");
		query_list.add("SELECT DBY.countrycode, DBY.yearc, TRUNCATE(DBY.data, 0) as Enrolled_in_Primary, UnenrolledMA.Unenrolled_Male+UnenrolledMA.Unenrolled_Female as Out_Of_School_Primary FROM databyyear DBY, (SELECT DBY.countrycode, DBY.yearc, TRUNCATE(DBY.data, 0) as Unenrolled_Male, TRUNCATE(UnenrolledFE.data, 0) as Unenrolled_Female"
						+ " FROM databyyear DBY, (SELECT * FROM databyyear DBY WHERE seriescode = \"SE.PRM.UNER.FE\") UnenrolledFE WHERE DBY.seriescode = \"SE.PRM.UNER.MA\""
						+ " AND UnenrolledFE.countrycode = DBY.countrycode AND UnenrolledFE.yearc = DBY.yearc) UnenrolledMA"
						+ " WHERE DBY.seriescode = \"SE.PRM.ENRL\" AND UnenrolledMA.countrycode = DBY.countrycode AND UnenrolledMA.yearc = DBY.yearc");
		query_list.add("SELECT DBY.countrycode, DBY.yearc, TRUNCATE(DBY.data, 3) as Male_LiteracyRate, TRUNCATE(LiteracyFE.data, 3) as Female_LiteracyRate FROM databyyear DBY, (SELECT * FROM databyyear DBY WHERE seriescode = \"SE.ADT.LITR.FE.ZS\") LiteracyFE" 
						  + " WHERE DBY.seriescode = \"SE.ADT.LITR.MA.ZS\" AND LiteracyFE.countrycode = DBY.countrycode AND LiteracyFE.yearc = DBY.yearc");
		query_list.add("SELECT DBY.countrycode, DBY.yearc, DBY.data FROM databyyear DBY, (SELECT DBY.countrycode, DBY.yearc, EnrollmentInPrimaryEd.data/DBY.data as ActualPupilTeacherRatio" 
                      + " FROM databyyear DBY, (SELECT DBY.countrycode, DBY.yearc, DBY.data FROM databyyear DBY WHERE seriescode = \"SE.PRM.ENRL\") EnrollmentInPrimaryEd"
                      + " WHERE seriescode = \"SE.PRM.TCHR\" AND DBY.countrycode = EnrollmentInPrimaryEd.countrycode AND DBY.yearc = EnrollmentInPrimaryEd.yearc) ActualRatio"
                      + " WHERE DBY.seriescode = \"SE.PRM.ENRL.TC.ZS\" AND DBY.countrycode = ActualRatio.countrycode AND DBY.yearc = ActualRatio.yearc");
		query_list.add("SELECT DBY.countrycode, DBY.yearc, TRUNCATE(DBY.data, 0) as YrsComEd, income FROM databyyear DBY, countryincome CI"
					   +" WHERE seriescode = \"SE.COM.DURS\" AND DBY.countrycode = CI.countrycode ORDER BY YrsComEd");
		query_list.add("SELECT FemalePercENRPRM.countrycode, FemalePercENRPRM.yearc, TRUNCATE(TotalEnrolledPrimary.data*FemalePercENRPRM.data/100, 0) as FemalePrimary, TRUNCATE(TotalEnrolledPrimary.data - ( TotalEnrolledPrimary.data*FemalePercENRPRM.data/100), 0) as MalePrimary"
					  + " FROM (SELECT DBY.countrycode, DBY.yearc, DBY.data FROM databyyear DBY WHERE seriescode = \"SE.PRM.ENRL\") TotalEnrolledPrimary, (SELECT DBY.countrycode, DBY.yearc, DBY.data FROM databyyear DBY WHERE seriescode = \"SE.PRM.ENRL.FE.ZS\") FemalePercENRPRM"
					  + " WHERE FemalePercENRPRM.countrycode = TotalEnrolledPrimary.countrycode AND FemalePercENRPRM.yearc = TotalEnrolledPrimary.yearc #AND seriescode = \"SE.PRM.ENRL.FE.ZS\"");
		query_list.add("SELECT * FROM databyyear WHERE seriescode = \"SE.PRM.ENRL.FE.ZS\"");	
		query_list.add("SELECT * FROM databyyear WHERE seriescode = \"SE.PRM.ENRL\"");
		frmAdvandbMco = new JFrame();
		frmAdvandbMco.setBounds(100, 100, 700, 400);
		frmAdvandbMco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmAdvandbMco.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(102, 153, 102), 2, true));
		panel.setBounds(10, 86, 285, 237);
		frmAdvandbMco.getContentPane().add(panel);
		panel.setLayout(null);
		panel.setLayout(null);
		
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textArea.setBounds(0, 0, 286, 237);
		panel.add(textArea);
		
		btnNewButton = new JButton("Refresh Table");
		btnNewButton.setBounds(549, 26, 135, 23);
		frmAdvandbMco.getContentPane().add(btnNewButton);
		
		btnNewButton_1 = new JButton("Run Query");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnNewButton_1.setBounds(90, 334, 105, 23);
		frmAdvandbMco.getContentPane().add(btnNewButton_1);
		
		comboBox = new JComboBox<String>();
		comboBox.addItem(list.get(0));
		comboBox.addItem(list.get(1));
		comboBox.addItem(list.get(2));
		comboBox.addItem(list.get(3));
		comboBox.addItem(list.get(4));
		comboBox.addItem(list.get(5));
		comboBox.addItem(list.get(6));
		comboBox.addItem(list.get(7));
		comboBox.addItem(list.get(8));
		comboBox.addItem(list.get(9));
		comboBox.addItem(list.get(10));
		comboBox.addItem(list.get(11));
		comboBox.addItem(list.get(12));
		comboBox.addItem(list.get(13));
		comboBox.addItem(list.get(14));
		comboBox.addItem(list.get(15));
		comboBox.addItem(list.get(16));
		comboBox.addItem(list.get(17));
		comboBox.addItem(list.get(18));
		comboBox.addItem(list.get(19));
		
		
		comboBox.setToolTipText("Choose query");
		comboBox.setBounds(10, 55, 285, 20);
		frmAdvandbMco.getContentPane().add(comboBox);
		
		
		JLabel lblChooseQuery = new JLabel("Choose Query");
		lblChooseQuery.setBounds(10, 30, 141, 14);
		frmAdvandbMco.getContentPane().add(lblChooseQuery);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(305, 53, 379, 277);
		frmAdvandbMco.getContentPane().add(scrollPane);
		
		textArea_1 = new JTextArea();
		textArea_1.setLineWrap(true);
		textArea_1.setBounds(305, 53, 379, 277);
		scrollPane.setViewportView(textArea_1);
		
		
		
		frmAdvandbMco.setVisible(true);
		frmAdvandbMco.setTitle("ADVANDB MCO3");
		frmAdvandbMco.setResizable(false);
		
		comboBox.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				textArea.setText(query_list.get(comboBox.getSelectedIndex()));
			}
		});
		btnNewButton_1.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				int selected = comboBox.getSelectedIndex();
				switch(selected){
					case 0: textArea_1.setText(BTMWithMySQL.selectDidntReach());
					break;
					case 1:textArea_1.setText(BTMWithMySQL.selectOutSchool());
					break;
				}
				//textArea_1.setText(BTMWithMySQL.selectPE());
			}
			
		});
	}
}
