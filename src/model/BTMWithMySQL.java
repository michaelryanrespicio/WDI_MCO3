package model;

	import java.sql.Connection;
	import java.sql.PreparedStatement;
	import java.sql.ResultSet;
import java.util.ArrayList;

import bitronix.tm.BitronixTransactionManager;
	import bitronix.tm.TransactionManagerServices;
	import bitronix.tm.resource.jdbc.PoolingDataSource;

	/**
	 * @author Bikash
	 *
	 */
	public class BTMWithMySQL {

		private static final String DATABASE = "wdi";
		private static final String USER_NAME = "root";
		private static final String PASSWORD = "root";
		private static final String URL = "jdbc:mysql://localhost:3306/" + DATABASE;
		
		private static final String INSERT_QUERY =
				"INSERT INTO databyyear(Countrycode, seriescode, yearc, data) VALUES(?, ?, ?, ?);";
		private static final String DELETE_QUERY = 
				"DELETE FROM databyyear WHERE countrycode = ? AND seriescode = ? AND yearc = ? AND data = ?";
		private static final String UPDATE_QUERY = 
				"UPDATE databy	year SET data = ? WHERE countrycode = ? AND yearc = ? AND seriescode = ? AND data = ?";
		private static final String SELECT_QUERY =
				"SELECT * FROM databyyear WHERE countrycode = \"ABW\"";
		private static final String SELECT_QUERY_FIRST =
				"SELECT DBY.countrycode, DBY.yearc, DBY.data-SecondaryEnrollment.data as Not_Enrolled_in_Secondary_School" + 
						   "\nFROM databyyear DBY, (SELECT * " 
							+ "FROM databyyear DBY WHERE seriescode = \"SE.SEC.ENRL\") SecondaryEnrollment" 
							+ "\nWHERE DBY.seriescode = \"SE.PRM.ENRL\" AND SecondaryEnrollment.countrycode = DBY.countrycode"
							+ "\nAND SecondaryEnrollment.yearc = DBY.yearc AND DBY.data > SecondaryEnrollment.data";
		private static final String SELECT_QUERY_SECOND =
				"SELECT DBY.countrycode, DBY.yearc, TRUNCATE(DBY.data, 0) as Enrolled_in_Primary, UnenrolledMA.Unenrolled_Male+UnenrolledMA.Unenrolled_Female as Out_Of_School_Primary FROM databyyear DBY, (SELECT DBY.countrycode, DBY.yearc, TRUNCATE(DBY.data, 0) as Unenrolled_Male, TRUNCATE(UnenrolledFE.data, 0) as Unenrolled_Female"
						+ " FROM databyyear DBY, (SELECT * FROM databyyear DBY WHERE seriescode = \"SE.PRM.UNER.FE\") UnenrolledFE WHERE DBY.seriescode = \"SE.PRM.UNER.MA\""
						+ " AND UnenrolledFE.countrycode = DBY.countrycode AND UnenrolledFE.yearc = DBY.yearc) UnenrolledMA"
						+ " WHERE DBY.seriescode = \"SE.PRM.ENRL\" AND UnenrolledMA.countrycode = DBY.countrycode AND UnenrolledMA.yearc = DBY.yearc";
		private static final String SELECT_QUERY_THIRD =
				"SELECT DBY.countrycode, DBY.yearc, TRUNCATE(DBY.data, 3) as Male_LiteracyRate, TRUNCATE(LiteracyFE.data, 3) as Female_LiteracyRate FROM databyyear DBY, (SELECT * FROM databyyear DBY WHERE seriescode = \"SE.ADT.LITR.FE.ZS\") LiteracyFE" 
						  + " WHERE DBY.seriescode = \"SE.ADT.LITR.MA.ZS\" AND LiteracyFE.countrycode = DBY.countrycode AND LiteracyFE.yearc = DBY.yearc";
		private static final String SELECT_QUERY_FOURTH =
				"SELECT DBY.countrycode, DBY.yearc, DBY.data FROM databyyear DBY, (SELECT DBY.countrycode, DBY.yearc, EnrollmentInPrimaryEd.data/DBY.data as ActualPupilTeacherRatio" 
	                      + " FROM databyyear DBY, (SELECT DBY.countrycode, DBY.yearc, DBY.data FROM databyyear DBY WHERE seriescode = \"SE.PRM.ENRL\") EnrollmentInPrimaryEd"
	                      + " WHERE seriescode = \"SE.PRM.TCHR\" AND DBY.countrycode = EnrollmentInPrimaryEd.countrycode AND DBY.yearc = EnrollmentInPrimaryEd.yearc) ActualRatio"
	                      + " WHERE DBY.seriescode = \"SE.PRM.ENRL.TC.ZS\" AND DBY.countrycode = ActualRatio.countrycode AND DBY.yearc = ActualRatio.yearc";
		private static final String SELECT_QUERY_FIFTH = 
				"SELECT DBY.countrycode, DBY.yearc, TRUNCATE(DBY.data, 0) as YrsComEd, income FROM databyyear DBY, countryincome CI"
						   +" WHERE seriescode = \"SE.COM.DURS\" AND DBY.countrycode = CI.countrycode ORDER BY YrsComEd";
		
		private static final String SELECT_QUERY_SIXTH = 
				"SELECT FemalePercENRPRM.countrycode, FemalePercENRPRM.yearc, TRUNCATE(TotalEnrolledPrimary.data*FemalePercENRPRM.data/100, 0) as FemalePrimary, TRUNCATE(TotalEnrolledPrimary.data - ( TotalEnrolledPrimary.data*FemalePercENRPRM.data/100), 0) as MalePrimary"
						  + " FROM (SELECT DBY.countrycode, DBY.yearc, DBY.data FROM databyyear DBY WHERE seriescode = \"SE.PRM.ENRL\") TotalEnrolledPrimary, (SELECT DBY.countrycode, DBY.yearc, DBY.data FROM databyyear DBY WHERE seriescode = \"SE.PRM.ENRL.FE.ZS\") FemalePercENRPRM"
						  + " WHERE FemalePercENRPRM.countrycode = TotalEnrolledPrimary.countrycode AND FemalePercENRPRM.yearc = TotalEnrolledPrimary.yearc #AND seriescode = \"SE.PRM.ENRL.FE.ZS\"";
		
		private static final String SELECT_QUERY_SEVENTH = 
				"SELECT * FROM databyyear WHERE seriescode = \"SE.PRM.ENRL.FE.ZS\"";
		
		private static final String SELECT_QUERY_EIGHT = 
				"SELECT * FROM databyyear WHERE seriescode = \"SE.PRM.ENRL\"";
		
		
		private static PoolingDataSource mySQLDS = new PoolingDataSource();
		private static BitronixTransactionManager btm;
		
		public static ArrayList<String> getAllQueries(){
			ArrayList<String> list = new ArrayList<String>();
			list.add(INSERT_QUERY);
			list.add(DELETE_QUERY);
			list.add(SELECT_QUERY);
			list.add(UPDATE_QUERY);
			return list;
		}
		public BTMWithMySQL(){
			mySQLDS.setClassName
			("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
			mySQLDS.setUniqueName("mySQLBtm");
			mySQLDS.setMaxPoolSize(3);
			mySQLDS.getDriverProperties().setProperty("databaseName",  DATABASE);
			mySQLDS.getDriverProperties().setProperty
			("url", URL);
			mySQLDS.getDriverProperties().setProperty("user", "root");
			mySQLDS.getDriverProperties().setProperty("password", "root");
			mySQLDS.init();

			btm = TransactionManagerServices.getTransactionManager();
		}	
		/*
		 * QUERIES THAT INSERTS TO DATABASE
		 */
		
		public static boolean insertFirstEUAM(){
			boolean result = false;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(INSERT_QUERY);
				pstmt.setString(1, "" + "ABW");
				pstmt.setString(2, "" + "SE.PRM.ENRL");
				pstmt.setString(3, "" + "2016 [YR2016]");
				pstmt.setString(4, "" + "10902");
				pstmt.executeUpdate();
				pstmt.close();
				connection.close();
				btm.commit();
				result = true;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					result = false;
					e.printStackTrace();
				}
			}
			//mySQLDS.close();
			//btm.shutdown();
			return result;
		}
		
		public static boolean insertSecondEUAM(){
			boolean result = false;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(INSERT_QUERY);
				pstmt.setString(1, "" + "ABW");
				pstmt.setString(2, "" + "SE.PRM.ENRL.FE.ZS");
				pstmt.setString(3, "" + "2016 [YR2016]");
				pstmt.setString(4, "" + "50");
				pstmt.executeUpdate();
				pstmt.close();
				connection.close();
				btm.commit();
				result = true;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					result = false;
					e.printStackTrace();
				}
			}
			//mySQLDS.close();
			//btm.shutdown();
			return result;
		}
		
		public static boolean insertFirstASAF(){
			boolean result = false;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(INSERT_QUERY);
				pstmt.setString(1, "" + "AFG");
				pstmt.setString(2, "" + "SE.PRM.ENRL");
				pstmt.setString(3, "" + "2016 [YR2016]");
				pstmt.setString(4, "" + "10902");
				pstmt.executeUpdate();
				pstmt.close();
				connection.close();
				btm.commit();
				result = true;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					result = false;
					e.printStackTrace();
				}
			}
			//mySQLDS.close();
			//btm.shutdown();
			return result;
		}
		
		public static boolean insertSecondASAF(){
			boolean result = false;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(INSERT_QUERY);
				pstmt.setString(1, "" + "AFG");
				pstmt.setString(2, "" + "SE.PRM.ENRL.FE.ZS");
				pstmt.setString(3, "" + "2016 [YR2016]");
				pstmt.setString(4, "" + "50");
				pstmt.executeUpdate();
				pstmt.close();
				connection.close();
				btm.commit();
				result = true;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					result = false;
					e.printStackTrace();
				}
			}
			//mySQLDS.close();
			//btm.shutdown();
			return result;
		}
		
		/*
		 * QUERIES THAT DELETES FROM THE DATABASE
		 */
		
		public static boolean deleteFirstEUAM(){
			boolean result = false;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(DELETE_QUERY);
				pstmt.setString(1, "" + "ABW");
				pstmt.setString(2, "" + "SE.PRM.ENRL");
				pstmt.setString(3, "" + "2016 [YR2016]");
				pstmt.setString(4, "" + "10902");
				pstmt.executeUpdate();
				pstmt.close();
				connection.close();
				btm.commit();
				result = true;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					result = false;
					e.printStackTrace();
				}
			}
			//mySQLDS.close();
			//btm.shutdown();
			return result;
		}
		
		public static boolean deleteSecondEUAM(){
			boolean result = false;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(DELETE_QUERY);
				pstmt.setString(1, "" + "ABW");
				pstmt.setString(2, "" + "SE.PRM.ENRL.FE.ZS");
				pstmt.setString(3, "" + "2016 [YR2016]");
				pstmt.setString(4, "" + "50");
				pstmt.executeUpdate();
				pstmt.close();
				connection.close();
				btm.commit();
				result = true;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					result = false;
					e.printStackTrace();
				}
			}
			//mySQLDS.close();
			//btm.shutdown();
			return result;
		}
		
		public static boolean deleteFirstASAF(){
			boolean result = false;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(DELETE_QUERY);
				pstmt.setString(1, "" + "AFG");
				pstmt.setString(2, "" + "SE.PRM.ENRL");
				pstmt.setString(3, "" + "2016 [YR2016]");
				pstmt.setString(4, "" + "10902");
				pstmt.executeUpdate();
				pstmt.close();
				connection.close();
				btm.commit();
				result = true;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					result = false;
					e.printStackTrace();
				}
			}
			//mySQLDS.close();
			//btm.shutdown();
			return result;
		}
		
		public static boolean deleteSecondASAF(){
			boolean result = false;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(DELETE_QUERY);
				pstmt.setString(1, "" + "AFG");
				pstmt.setString(2, "" + "SE.PRM.ENRL.FE.ZS");
				pstmt.setString(3, "" + "2016 [YR2016]");
				pstmt.setString(4, "" + "50");
				pstmt.executeUpdate();
				pstmt.close();
				connection.close();
				btm.commit();
				result = true;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					result = false;
					e.printStackTrace();
				}
			}
			//mySQLDS.close();
			//btm.shutdown();
			return result;
		}
		
		/*
		 * QUERIES THAT UPDATES THE DATABASE
		 */
		
		public static boolean updateFirstEUAM(){
			boolean result = false;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(UPDATE_QUERY);
				pstmt.setString(1, "" + "51.382");
				pstmt.setString(2, "" + "ABW");
				pstmt.setString(3, "" + "2016 [YR2016]");
				pstmt.setString(4, "" + "SE.PRM.ENRL.FE.ZS");
				pstmt.setString(5, "" + "48.857");
				pstmt.executeUpdate();
				pstmt.close();
				connection.close();
				btm.commit();
				result = true;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					result = false;
					e.printStackTrace();
				}
			}
			//mySQLDS.close();
			//btm.shutdown();
			return result;
		}
		
		public static boolean updateSecondEUAM(){
			boolean result = false;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(UPDATE_QUERY);
				pstmt.setString(1, "" + "48.857");
				pstmt.setString(2, "" + "ABW");
				pstmt.setString(3, "" + "2016 [YR2016]");
				pstmt.setString(4, "" + "SE.PRM.ENRL.FE.ZS");
				pstmt.setString(5, "" + "51.382");
				pstmt.executeUpdate();
				pstmt.close();
				connection.close();
				btm.commit();
				result = true;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					result = false;
					e.printStackTrace();
				}
			}
			//mySQLDS.close();
			//btm.shutdown();
			return result;
		}
		
		public static boolean updateFirstASAF(){
			boolean result = false;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(UPDATE_QUERY);
				pstmt.setString(1, "" + "47.857");
				pstmt.setString(2, "" + "BEL");
				pstmt.setString(3, "" + "2016 [YR2016]");
				pstmt.setString(4, "" + "SE.PRM.ENRL.FE.ZS");
				pstmt.setString(5, "" + "48.648");
				pstmt.executeUpdate();
				pstmt.close();
				connection.close();
				btm.commit();
				result = true;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					result = false;
					e.printStackTrace();
				}
			}
			//mySQLDS.close();
			//btm.shutdown();
			return result;
		}
		
		public static boolean updateSecondASAF(){
			boolean result = false;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(UPDATE_QUERY);
				pstmt.setString(1, "" + "48.648");
				pstmt.setString(2, "" + "BEL");
				pstmt.setString(3, "" + "2016 [YR2016]");
				pstmt.setString(4, "" + "SE.PRM.ENRL.FE.ZS");
				pstmt.setString(5, "" + "47.857");
				pstmt.executeUpdate();
				pstmt.close();
				connection.close();
				btm.commit();
				result = true;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					result = false;
					e.printStackTrace();
				}
			}
			//mySQLDS.close();
			//btm.shutdown();
			return result;
		}
		
		
		/*
		 * QUERIES THAT SELECTS FROM THE DATABASE
		 */
		public static String selectDidntReach(){
			String resulting_query = null;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(SELECT_QUERY_FIRST);
				ResultSet rs = pstmt.executeQuery();
				resulting_query = "Number of primary students that didn't reach secondary school (for both sexes)\n";
				while(rs.next()){
					System.out.println("DataByYear: " + 
					rs.getString("countrycode") + " " +
					rs.getString("yearc") + " " +
					rs.getString("Not_Enrolled_in_Secondary_School"));
					resulting_query += ""+ rs.getString("countrycode") + " " + rs.getString("yearc") + " " + rs.getString("Not_Enrolled_in_Secondary_School")+ "\n";
				}
				rs.close();
				pstmt.close();
				connection.close();
				btm.commit();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
//			mySQLDS.close();
//			btm.shutdown();
			if(resulting_query == null)
				resulting_query = "error";
			return resulting_query;
		}
		
		public static String selectOutSchool(){
			String resulting_query = null;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(SELECT_QUERY_SECOND);
				ResultSet rs = pstmt.executeQuery();
				resulting_query = "Number of enrolled primary students and out of school primary (for both sexes)\n";
				while(rs.next()){
					System.out.println("DataByYear: " + 
					rs.getString("countrycode") + " " +
					rs.getString("yearc") + " " +
					rs.getString("Enrolled_in_Primary") + " " +
					rs.getString("Out_Of_School_Primary"));
					resulting_query += ""+ rs.getString("countrycode") + " " + rs.getString("yearc") + " " + rs.getString("Enrolled_in_Primary")+ " " + rs.getString("Out_Of_School_Primary")+ "\n";
				}
				rs.close();
				pstmt.close();
				connection.close();
				btm.commit();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
//			mySQLDS.close();
//			btm.shutdown();
			if(resulting_query == null)
				resulting_query = "error";
			return resulting_query;
		}
		
		public static String selectLiteracy(){
			String resulting_query = null;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(SELECT_QUERY_THIRD);
				ResultSet rs = pstmt.executeQuery();
				resulting_query = "Male versus Female Literacy Rate";
				while(rs.next()){
					System.out.println("DataByYear: " + 
					rs.getString("countrycode") + " " +
					rs.getString("yearc") + " " +
					rs.getString("Male_LiteracyRate") + " " +
					rs.getString("Female_LiteracyRate"));
					resulting_query += ""+ rs.getString("countrycode") + " " + rs.getString("yearc") + " " + rs.getString("Male_LiteracyRate")+ " " + rs.getString("Female_LiteracyRate")+ "\n";
				}
				rs.close();
				pstmt.close();
				connection.close();
				btm.commit();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
//			mySQLDS.close();
//			btm.shutdown();
			if(resulting_query == null)
				resulting_query = "error";
			return resulting_query;
		}
		
		public static String selectRatio(){
			String resulting_query = null;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(SELECT_QUERY_FOURTH);
				ResultSet rs = pstmt.executeQuery();
				resulting_query = "Pupil-Teacher ratio versus actual Pupil-Teacher count in primary education\n";
				while(rs.next()){
					System.out.println("DataByYear: " + 
					rs.getString("countrycode") + " " +
					rs.getString("yearc") + " " +
					rs.getString("ActualPupilTeacherRatio"));
					resulting_query += ""+ rs.getString("countrycode") + " " + rs.getString("yearc") + " " + rs.getString("ActualPupilTeacherRatio") + "\n";
				}
				rs.close();
				pstmt.close();
				connection.close();
				btm.commit();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
//			mySQLDS.close();
//			btm.shutdown();
			if(resulting_query == null)
				resulting_query = "error";
			return resulting_query;
		}
		
		public static String selectRequired(){
			String resulting_query = null;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(SELECT_QUERY_FIFTH);
				ResultSet rs = pstmt.executeQuery();
				resulting_query = "Required number of educational years in relation with the country's income";
				while(rs.next()){
					System.out.println("DataByYear: " + 
					rs.getString("countrycode") + " " +
					rs.getString("yearc") + " " +
					rs.getString("YrsComEd") + " " +
					rs.getString("income"));
					resulting_query += ""+ rs.getString("countrycode") + " " + rs.getString("yearc") + " " + rs.getString("YrsComEd")+ " " + rs.getString("income")+ "\n";
				}
				rs.close();
				pstmt.close();
				connection.close();
				btm.commit();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
//			mySQLDS.close();
//			btm.shutdown();
			if(resulting_query == null)
				resulting_query = "error";
			return resulting_query;
		}
		
		public static String selectVSPrimary(){
			String resulting_query = null;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(SELECT_QUERY_SIXTH);
				ResultSet rs = pstmt.executeQuery();
				resulting_query = "Female VS Male Primary Students";
				while(rs.next()){
					System.out.println("DataByYear: " + 
					rs.getString("FemalePercENRPRM.countrycode") + " " +
					rs.getString("FemalePercENRPRM.yearc") + " " +
					rs.getString("FemalePrimary") + " " +
					rs.getString("MalePrimary"));
					resulting_query += ""+ rs.getString("FemalePercENRPRM.countrycode") + " " + rs.getString("FemalePercENRPRM.yearc") + " " + rs.getString("FemalePrimary")+ " " + rs.getString("MalePrimary")+ "\n";
				}
				rs.close();
				pstmt.close();
				connection.close();
				btm.commit();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
//			mySQLDS.close();
//			btm.shutdown();
			if(resulting_query == null)
				resulting_query = "error";
			return resulting_query;
		}
		
		public static String selectPE(){
			String resulting_query = null;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(SELECT_QUERY_SEVENTH);
				ResultSet rs = pstmt.executeQuery();
				resulting_query = "Primary education,  pupils (% female)\n";
				while(rs.next()){
					System.out.println("DataByYear: " + 
					rs.getString("countrycode") + " " +
					rs.getString("yearc") + " " +
					rs.getString("data"));
					resulting_query += ""+ rs.getString("countrycode") + " " + rs.getString("yearc") + " " + rs.getString("data")+ "\n";
				}
				rs.close();
				pstmt.close();
				connection.close();
				btm.commit();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
//			mySQLDS.close();
//			btm.shutdown();
			if(resulting_query == null)
				resulting_query = "error";
			return resulting_query;
		}
		
		public static String selectPEEnrollment(){
			String resulting_query = null;
			try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(SELECT_QUERY_EIGHT);
				ResultSet rs = pstmt.executeQuery();
				resulting_query = "Enrolment in primary education,  both sexes (number)\n";
				while(rs.next()){
					System.out.println("DataByYear: " + 
					rs.getString("countrycode") + " " +
					rs.getString("yearc") + " " +
					rs.getString("data"));
					resulting_query += ""+ rs.getString("countrycode") + " " + rs.getString("yearc") + " " + rs.getString("data")+ "\n";
				}
				rs.close();
				pstmt.close();
				connection.close();
				btm.commit();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
//			mySQLDS.close();
//			btm.shutdown();
			if(resulting_query == null)
				resulting_query = "error";
			return resulting_query;
		}
		
		
//		public static void main(String[] args) {
//			new BTMWithMySQL();
//			
//		}
			
/*			try{
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				
				PreparedStatement pstmt = 
				connection.prepareStatement(DELETE_QUERY);
					pstmt.executeUpdate();
				
				pstmt.close();

				connection.close();

				btm.commit();

			} catch (Exception ex) {
				ex.printStackTrace();
	            try {
					btm.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}*/
			
/*			try{
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				
				PreparedStatement pstmt = 
				connection.prepareStatement(DELETE_QUERY);
					pstmt.setString(1, "" + "2");
					pstmt.executeUpdate();
				
				pstmt.close();

				connection.close();

				btm.commit();

			} catch (Exception ex) {
				ex.printStackTrace();
	            try {
					btm.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}*/
			

			/*try {
				btm.begin();
				Connection connection = 
				mySQLDS.getConnection(USER_NAME, PASSWORD);
				PreparedStatement pstmt = 
				connection.prepareStatement(SELECT_QUERY);
				ResultSet rs = pstmt.executeQuery();
				while(rs.next()){
					System.out.println("Region: " + 
					rs.getString("countrycode") + " " +
					rs.getString("region"));
				}
				rs.close();
				pstmt.close();
				connection.close();
				btm.commit();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try {
					btm.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mySQLDS.close();
			btm.shutdown();
		}*/
//			try {
//				btm.begin();
//				Connection connection = 
//				mySQLDS.getConnection(USER_NAME, PASSWORD);
//				PreparedStatement pstmt = 
//				connection.prepareStatement(SELECT_QUERY);
//				ResultSet rs = pstmt.executeQuery();
//				while(rs.next()){
//					System.out.println("DataByYear: " + 
//					rs.getString("countrycode") + " " +
//					rs.getString("yearc") + " " +
//					rs.getString("data"));
//				}
//				rs.close();
//				pstmt.close();
//				connection.close();
//				btm.commit();
//			}
//			catch (Exception ex) {
//				ex.printStackTrace();
//				try {
//					btm.rollback();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			mySQLDS.close();
//			btm.shutdown();
//		}
	}

