package scraper;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Scraper {
	private static Connection _connection;
	private static final String[] GROUP_ONE = { "AL", "AR", "CA" };
	private static final String[] GROUP_TWO = { "AK", "AZ", "MI", "NY" };
	private static final String[] GROUP_THREE = { "CO", "GA", "PA", "UT" };
	private static final String[] GROUP_FOUR = { "MD", "ME", "NM", "TX" };
	private static final String[] GROUP_FIVE = { "CT", "IA", "MS", "VA", "WA", "WY" };
	private static final String[] GROUP_SIX = { "DC", "MO", "MT", "NC" , "ND", "NE", "NH" };
	private static final String[] GROUP_SEVEN = { "DE", "FL", "HI", "IL", "KS", "RI" };
	private static final String[] GROUP_EIGHT = { "ID", "IN", "KY", "LA", "MA", "OR" };
	private static final String[] GROUP_NINE = { "MN", "NJ", "NV", "OK", "PR", "SC", "SD", "VT" };
	private static final String[] GROUP_TEN = { "OH", "TV", "WI", "WV" };
	private static String _directoryName;

	/**
	 * @title main
	 * @param args<String[]>
	 * @return
	 * @desc Main function
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Error: Incorrect usage.");
			System.out.println("Correct usage: ./scraper <Selection (all,1-10)> <directory_name>");
			System.exit(1);
		}

		_connection = getConnection();

		String selection = args[0];
		_directoryName = args[1];
		if (selection.equals("all")) {
			scrapeStates(GROUP_ONE);
			scrapeStates(GROUP_TWO);
			scrapeStates(GROUP_THREE);
			scrapeStates(GROUP_FOUR);
			scrapeStates(GROUP_FIVE);
			scrapeStates(GROUP_SIX);
			scrapeStates(GROUP_SEVEN);
			scrapeStates(GROUP_EIGHT);
			scrapeStates(GROUP_NINE);
			scrapeStates(GROUP_TEN);
		} else if (selection.equals("1")) {
			scrapeStates(GROUP_ONE);
		} else if (selection.equals("2")) {
			scrapeStates(GROUP_TWO);
		} else if (selection.equals("3")) {
			scrapeStates(GROUP_THREE);
		} else if (selection.equals("4")) {
			scrapeStates(GROUP_FOUR);
		} else if (selection.equals("5")) {
			scrapeStates(GROUP_FIVE);
		} else if (selection.equals("6")) {
			scrapeStates(GROUP_SIX);
		} else if (selection.equals("7")) {
			scrapeStates(GROUP_SEVEN);
		} else if (selection.equals("8")) {
			scrapeStates(GROUP_EIGHT);
		} else if (selection.equals("9")) {
			scrapeStates(GROUP_NINE);
		} else if (selection.equals("10")) {
			scrapeStates(GROUP_TEN);
		} else if (selection.length() == 2) {
			scrapeState(selection);
		} else {
			System.out.println("Invalid argument passed in. Valid arguments: all, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10");
			System.out.println("Application exiting.");
			System.exit(1);
		}
		System.out.println("Scraping completed (100%).");

		_connection.close();
	}

	/**
	 * @title scrapeStates
	 * @param states<String[]>
	 * @return
	 * @desc Calls scrapeState() on each state<String> in states<String[]>
	 */
	public static void scrapeStates(String[] states) throws Exception {
		try {
			for (String state : states) {
				scrapeState(state);
			}
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * @title scrapeState
	 * @param state<String>
	 * @return
	 * @desc Calls parseAndSaveDataFromFile for each html file in
	 *       /home/jtan021/airbnb/pagesources/state<String>
	 */
	public static void scrapeState(String state) throws Exception {
		try {
			System.out.println("Scraping " + state);
//			 String directoryPath = "C:/Users/jonat/Desktop/DatabaseResearch/Airbnb/Airbnb_Crawler/pagesources/" + _directoryName + "/" + state + "/";
			String directoryPath = "/home/jtan021/airbnb/pagesources/" + _directoryName + "/" + state + "/";
			File directory = new File(directoryPath);

			if (directory != null) {
				File[] textFiles = directory.listFiles();

				if (textFiles != null) {
					System.out.println("Number of files to parse: " + textFiles.length);

					for (File textFile : textFiles) {
						if (textFile.isFile() && textFile.getName().endsWith(".txt")) {
							parseAndSaveDataFromFile(textFile, state);
						}
					}
				}
			}
			System.out.println("Finished scraping state " + state);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * @title parseAndSaveDataFromFile
	 * @param file<File>
	 * @return
	 * @desc Parses file<File> to get airbnb data and saves data to database
	 */
	public static void parseAndSaveDataFromFile(File file, String state) throws Exception {
		String fileName = file.getName();
		System.out.println("Parsing through file " + fileName);

		String[] fileNameParts = fileName.split("_");
		if (fileNameParts.length != 4) {
			System.out.println("ERROR: FILE NAME INCONSISTENT.");
			return;
		} else {
			int zipcode = convertToInt(fileNameParts[0]);
			String city = fileNameParts[1]; 
			int month = convertToInt(fileNameParts[2]);
			int year = convertToInt(fileNameParts[3]);
			
			String pageSource = FileUtils.readFileToString(file, "UTF-8");
			Document document = Jsoup.parse(pageSource);
			
			if (isCorrectPageSource(city, state, pageSource)) {
				System.out.println("File " + fileName + " contains correct page source.");
				Calendar calendar = new GregorianCalendar(year, month - 1, 1);
				int numDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
				String checkInDate = year + "-" + month + "-01";
				String checkOutDate = year + "-" + month + "-" + numDays;
				String url = "https://www.airbnb.com/s/" + zipcode + "/homes?checkin=" + checkInDate + "&checkout="
						+ checkOutDate;
				String modifiedCity = city.replaceAll("-", " ");

				Airbnb airbnb = new Airbnb();
				airbnb.setCrawlTime(getCurrentTimestamp());
				airbnb.setZipcode(zipcode);
				airbnb.setCity(modifiedCity);
				airbnb.setUrl(url);
				airbnb.setMonth(month);
				airbnb.setYear(year);

				airbnb.setAveragePrice(getAveragePriceFromDocumentComments(document));
				airbnb.setIsMonthlyPriceType(foundMonthlyPriceTypeFromDocumentComments(document));
				
				airbnb.print();
				
				saveAirbnbToDatabase(airbnb);
			} else {
				System.out.println("File " + fileName + " contains incorrect page source.");
			}
		}
		System.out.println("Finished parsing " + fileName);
	}

	/**
	 * @title getAveragePriceFromDocumentComments
	 * @param document<Node>
	 * @return averagePrice<int> if found in document<Node>, -1 otherwise
	 */
	private static int getAveragePriceFromDocumentComments(Document document) {
		int averagePrice = -1;

		Elements scripts = document.getElementsByTag("script");

		for (int i = 0; i < scripts.size() && averagePrice < 0; ++i) {
			List<DataNode> scriptDataNodes = scripts.get(i).dataNodes();
			for (int j = 0; j < scriptDataNodes.size() && averagePrice < 0; ++j) {
				averagePrice = getAveragePriceFromText(scriptDataNodes.get(j).getWholeData());
			}
		}

		return averagePrice;
	}

	/**
	 * @title getAveragePriceFromText
	 * @param text<String>
	 * @return averagePrice<int> if found in text<String>, -1 otherwise
	 */
	public static int getAveragePriceFromText(String text) {
		Matcher m = Pattern.compile("\"average_price\":[0-9]+[,]").matcher(text);

		int averagePrice = -1;
		while (m.find()) {
			averagePrice = convertToInt(m.group());
			System.out.println("AveragePrice: " + averagePrice);
			if (averagePrice > 0) {
				break;
			}
		}

		return averagePrice;
	}
	
	/**
	 * @title foundMonthlyPriceTypeFromDocumentComments
	 * @param document<Node>
	 * @return true if "price_type":"monthly" found in document<Node>, false otherwise
	 */
	private static boolean foundMonthlyPriceTypeFromDocumentComments(Document document) {
		boolean priceType = false;
		Elements scripts = document.getElementsByTag("script");

		for (int i = 0; i < scripts.size() && !priceType; ++i) {
			List<DataNode> scriptDataNodes = scripts.get(i).dataNodes();
			for (int j = 0; j < scriptDataNodes.size() && !priceType; ++j) {
				priceType = foundPriceTypeMonthlyFromText(scriptDataNodes.get(j).getWholeData());
			}
		}
		
		return priceType;
	}
	
	/**
	 * @title foundPriceTypeMonthlyFromText
	 * @param text<String>
	 * @return true if "price_type":"monthly" found in text, false otherwise
	 */
	public static boolean foundPriceTypeMonthlyFromText(String text) {
		String searchText = "\"price_type\":\"monthly\"";
		
		return text.contains(searchText);
	}

	/**
	 * @title saveAirbnbToDatabase
	 * @param airbnb<Airbnb>
	 * @throws Exception
	 * @desc Inserts airbnb's data to database
	 */
	public static void saveAirbnbToDatabase(Airbnb airbnb) throws Exception {
		System.out.println("Entered saveAirbnbToDatabase");

		String sqlStatement = "INSERT INTO airbnb2(zipcode, city, state, average_price, month, year, url, crawl_time) VALUES(?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = _connection.prepareStatement(sqlStatement);
		// 1
		preparedStatement.setInt(1, airbnb.getZipcode());
		// 2
		preparedStatement.setString(2, airbnb.getCity());
		// 3
		preparedStatement.setString(3, airbnb.getState());
		// 4
		if ((airbnb.getAveragePrice() <= 0) || !airbnb.isMonthlyPriceType()) {
			preparedStatement.setNull(4, java.sql.Types.INTEGER);
		} else {
			preparedStatement.setInt(4, airbnb.getAveragePrice());
		}
		// 5
		preparedStatement.setInt(5, airbnb.getMonth());
		// 6
		preparedStatement.setInt(6, airbnb.getYear());
		// 7
		preparedStatement.setString(7, airbnb.getUrl());
		// 8
		preparedStatement.setTimestamp(8, airbnb.getCrawlTime());
		preparedStatement.executeUpdate();
		preparedStatement.close();
	}

	/**
	 * @title getNumericalCharacters
	 * @param value<String>
	 * @return value<String> with numerical characters only
	 */
	public static String getNumericalCharacters(String value) {
		return value.replaceAll("[^0-9]", "");
	}

	/**
	 * @title convertToInt
	 * @param value<String>
	 * @return Converts value<String> to an integer after removing all
	 *         non-numerical characters
	 */
	public static int convertToInt(String value) {
		String filteredValue = getNumericalCharacters(value);

		if (filteredValue.length() > 0) {
			return Integer.parseInt(filteredValue);
		} else {
			return 0;
		}
	}
	
	/**
	 * @title isCorrectPageSource
	 * @param city<String>,
	 *            state<String>, pageSource<String>
	 * @return True if page source pertains to correct city, state. False
	 *         otherwise.
	 */
	public static boolean isCorrectPageSource(String city, String state, String pageSource) {
		Document document = Jsoup.parse(pageSource);
		Elements metaLinks = document.select("meta[id=english-canonical-url]");

		if (metaLinks.size() > 0) {
			Element metaLink = metaLinks.first();
			String stringToCheck = metaLink.toString();
			String searchString = city + "--" + state + "?";
			System.out.println("SearchString: " + searchString);
			return stringToCheck.contains(searchString);
		} else {
			return false;
		}
	}

	/**
	 * @title getConnection
	 * @param
	 * @return connection<Connection> to MySQL database where data will be
	 *         stored
	 */
	private static Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		String urldb = "jdbc:mysql://localhost/homeDB";
		String user = "jonathan";
		String password = "password";

//		 String urldb = "jdbc:mysql://localhost/databaselabs";
//		 String user = "root";
//		 String password = "A895784e1!";
		Connection connection = DriverManager.getConnection(urldb, user, password);
		return connection;
	}

	/**
	 * @title getCurrentTimestamp
	 * @param
	 * @return currentTimestamp<Timestamp>
	 */
	private static Timestamp getCurrentTimestamp() {
		System.out.println("Entered getCurrentTimestamp");

		Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
		return currentTimestamp;
	}
}
