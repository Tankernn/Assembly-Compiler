package eu.tankernn.assembly.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;

import eu.tankernn.assembly.output.GPIOHandler;
import eu.tankernn.assembly.output.ParallelOutput;

public class Assemble {
	static PrintStream out;
	static BufferedReader in;

	static HashMap<String, Byte> labels = new HashMap<String, Byte>();
	static HashMap<String, Byte> variables = new HashMap<String, Byte>();
	static byte currentAddress = 0;

	static boolean outputToGPIO = false;
	static boolean bigEndianData = false;
	static boolean bigEndianAddress = false;

	static File fileIn;
	static File fileOut = null;

	public static void main(String[] args) {
		parseArguments(args);

		if (outputToGPIO)
			GPIOHandler.init(new ParallelOutput());

		try {
			in = new BufferedReader(new FileReader(fileIn));
		} catch (FileNotFoundException e) {
			log("Could not find file " + fileIn.getAbsolutePath());
			return;
		}

		if (fileOut != null) {
			if (fileOut.exists()) {
				log("File " + fileOut.getAbsolutePath() + " already exists, aborting.");
				return;
			} else {
				try {
					fileOut.createNewFile();
					out = new PrintStream(new FileOutputStream(fileOut));
				} catch (IOException e) {
					System.err.println("Error creating/opening file " + fileOut.getAbsolutePath());
					e.printStackTrace();
				}
			}
		} else {
			out = System.out;
		}

		System.out.println("Compiling file: " + fileIn.getName());

		try {
			startCompile();
		} catch (IOException ex) {
			log("The compiler encountered an error: ");
			ex.printStackTrace();
		}
		if (outputToGPIO)
			GPIOHandler.cleanUp();
	}

	public static void startCompile() throws IOException {
		String line;
		int index = 0;
		while ((line = in.readLine()) != null) {
			try {
				output(new LineParser(line, index++).parse());
			} catch (SyntaxErrorException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public static void addLabel(String label) {
		labels.put(label, (byte) currentAddress);
	}

	public static void log(String str) {
		System.out.println(str);
	}

	public static void output(Byte[] bytes) throws IOException {
		if (outputToGPIO)
			GPIOHandler.writeData(bytes);
		
		for (Byte b : bytes) {
			out.println(
					byteToBinaryString(currentAddress, 4, bigEndianAddress) + " : " + byteToBinaryString(b, 8, false));
			currentAddress++;
		}	
	}

	public static String byteToBinaryString(int b, int wordLength, boolean reverse) {
		int template = (int) Math.pow(2, wordLength) - 1;
		StringBuilder result = new StringBuilder();
		result.append(Integer.toBinaryString((b & template) + template + 0x1).substring(1));
		if (reverse)
			result.reverse();
		return result.toString();
	}

	private static void parseArguments(String[] args) {
		Options options = new Options();

		Option input = new Option("i", "input", true, "input file path");
		input.setRequired(true);
		input.setType(PatternOptionBuilder.EXISTING_FILE_VALUE);
		options.addOption(input);

		Option output = new Option("o", "output", true, "output file");
		output.setRequired(false);
		output.setType(PatternOptionBuilder.FILE_VALUE);
		options.addOption(output);

		Option gpio = new Option("g", "gpio", false, "output to GPIO");
		gpio.setRequired(false);
		options.addOption(gpio);

		Option bigEndData = new Option("D", "big-endian-data", false, "Flip the data before outputting");
		bigEndData.setRequired(false);
		options.addOption(bigEndData);

		Option bigEndAddress = new Option("A", "big-endian-address", false, "Flip the data before outputting");
		bigEndAddress.setRequired(false);
		options.addOption(bigEndAddress);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("utility-name", options);

			System.exit(1);
			return;
		}

		outputToGPIO = cmd.hasOption("gpio");
		bigEndianData = cmd.hasOption("big-endian-data");
		bigEndianAddress = cmd.hasOption("big-endian-address");

		try {
			fileIn = (File) cmd.getParsedOptionValue("input");
			if (cmd.hasOption("output"))
				fileOut = (File) cmd.getParsedOptionValue("output");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
