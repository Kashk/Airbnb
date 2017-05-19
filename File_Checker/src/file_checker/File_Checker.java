package file_checker;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class File_Checker {
	private static final String[] GROUP_ONE = { "AL", "AR", "CA" };
	private static final String[] GROUP_TWO = { "AK", "AZ", "MI", "NY" };
	private static final String[] GROUP_THREE = { "CO", "GA", "PA", "UT" };
	private static final String[] GROUP_FOUR = { "MD", "ME", "NM", "TX" };
	private static final String[] GROUP_FIVE = { "CT", "IA", "MS", "VA", "WA", "WY" };
	private static final String[] GROUP_SIX = { "DC", "MO", "MT", "NC", "ND", "NE", "NH" };
	private static final String[] GROUP_SEVEN = { "DE", "FL", "HI", "IL", "KS", "RI" };
	private static final String[] GROUP_EIGHT = { "ID", "IN", "KY", "LA", "MA", "OR" };
	private static final String[] GROUP_NINE = { "MN", "NJ", "NV", "OK", "PR", "SC", "SD", "VT" };
	private static final String[] GROUP_TEN = { "OH", "TN", "WI", "WV" };
	private static String _directory = "";
	private static int _count = 0;
	private static int _total = 0;

	/**
	 * @title main
	 * @param args<String[]>
	 * @return
	 * @desc Main function
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Error: Not enough arguments passed in.");
			System.out.println("Correct usage: ./file_checker <selection> <directory after pagesources>");
			System.exit(1);
		}

		String selection = args[0];
		_directory = args[1];
		_count = 0;

		if (selection.equals("all")) {
			checkStates(GROUP_ONE);
			checkStates(GROUP_TWO);
			checkStates(GROUP_THREE);
			checkStates(GROUP_FOUR);
			checkStates(GROUP_FIVE);
			checkStates(GROUP_SIX);
			checkStates(GROUP_SEVEN);
			checkStates(GROUP_EIGHT);
			checkStates(GROUP_NINE);
			checkStates(GROUP_TEN);
		} else if (selection.equals("1")) {
			checkStates(GROUP_ONE);
		} else if (selection.equals("2")) {
			checkStates(GROUP_TWO);
		} else if (selection.equals("3")) {
			checkStates(GROUP_THREE);
		} else if (selection.equals("4")) {
			checkStates(GROUP_FOUR);
		} else if (selection.equals("5")) {
			checkStates(GROUP_FIVE);
		} else if (selection.equals("6")) {
			checkStates(GROUP_SIX);
		} else if (selection.equals("7")) {
			checkStates(GROUP_SEVEN);
		} else if (selection.equals("8")) {
			checkStates(GROUP_EIGHT);
		} else if (selection.equals("9")) {
			checkStates(GROUP_NINE);
		} else if (selection.equals("10")) {
			checkStates(GROUP_TEN);
		} else if (selection.length() == 2) {
			checkState(selection);
		} else {
			System.out.println("Invalid argument passed in. Valid arguments: all, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10");
			System.out.println("Application exiting.");
			System.exit(1);
		}
		System.out.println("Crawling completed (100%).");
		System.out.println("Total zipcodes: " + _total);
		System.out.println("# of incorrect zipcodes: " + _count);
	}

	public static void checkStates(String[] states) throws Exception {
		try {
			for (String state : states) {
				checkState(state);
			}
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void checkState(String state) throws Exception {
		try {
			System.out.println("Checking " + state);

			String directoryPath = "./pagesources/" + _directory + "/" + state + "/";
			File directory = new File(directoryPath);

			if (directory != null) {
				File[] textFiles = directory.listFiles();

				if (textFiles != null) {
					System.out.println("Number of files to parse: " + textFiles.length);

					for (File textFile : textFiles) {
						if (textFile.isFile() && textFile.getName().endsWith(".txt")) {
							checkIfFileIsCorrect(state, textFile);
							_total++;
						}
					}
				}
			}
			System.out.println("Finished checking state " + state + ".");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void checkIfFileIsCorrect(String state, File file) throws Exception {
		String fileName = file.getName();
		String[] fileNameParts = fileName.split("_");

		if (fileNameParts.length != 4) {
			System.out.println("Invalid file: " + fileName);
		} else {
			String city = fileNameParts[1];
			
			String pageSource = FileUtils.readFileToString(file, "UTF-8");

			if (!isCorrectPageSource(city, state, pageSource)) {
				System.out.println(fileName);
				_count++;
			}
		}
	}

	public static boolean isCorrectPageSource(String city, String state, String pageSource) {
		Document document = Jsoup.parse(pageSource);
		// meta id="english-canonical-url"
		// content="/s/Moody--AL?sublets=monthly">
		Elements metaLinks = document.select("meta[id=english-canonical-url]");

		if (metaLinks.size() > 0) {
			Element metaLink = metaLinks.first();
			String stringToCheck = metaLink.toString();
			String searchString = city + "--" + state + "?";

			return stringToCheck.contains(searchString);
		} else {
			return false;
		}
	}
}
