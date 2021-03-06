package eu.tankernn.assembly.compiler;

import static com.pi4j.io.gpio.RaspiPin.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pi4j.io.gpio.Pin;

import eu.tankernn.assembly.output.GPIOHandler;
import eu.tankernn.assembly.output.ParallelOutput;

public class Assemble {
	private static final Logger LOG = LogManager.getLogger();

	private static final Pin[] DATA_PINS = { GPIO_07, GPIO_00, GPIO_01, GPIO_02, GPIO_03, GPIO_04, GPIO_05, GPIO_06 };
	private static final Pin[] ADDRESS_PINS = { GPIO_22, GPIO_23, GPIO_24, GPIO_25 };

	static PrintStream out;
	static FileOutputStream binaryOut;
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
			GPIOHandler.init(new ParallelOutput(DATA_PINS), new ParallelOutput(ADDRESS_PINS));

		try {
			in = new BufferedReader(new FileReader(fileIn));
		} catch (FileNotFoundException e) {
			LOG.error("Could not find file {}", fileIn.getAbsolutePath());
			return;
		}

		if (fileOut != null) {
			if (fileOut.exists()) {
				LOG.error("File {} already exists, aborting.", fileOut.getAbsolutePath());
				return;
			} else {
				try {
					fileOut.createNewFile();
					binaryOut = new FileOutputStream(fileOut);
				} catch (IOException e) {
					LOG.error("Error creating/opening file " + fileOut.getAbsolutePath());
					LOG.catching(e);
				}
			}
		} else {
			out = System.out;
		}

		switch (FilenameUtils.getExtension(fileIn.getName())) {
		// Source file
		case "ta":
		case "tas":
			try {
				startCompile();
			} catch (IOException ex) {
				LOG.error("The compiler encountered an error.");
				LOG.catching(ex);
			}
			break;
		// Compiled binary file
		case "tac":
			try {
				outputBinary();
			} catch (IOException e) {
				LOG.error("The outputter encountered an error.");
				LOG.catching(e);
			}
			break;
		}

		if (outputToGPIO)
			GPIOHandler.cleanUp();
	}

	public static void startCompile() throws IOException {
		LOG.info("Compiling file: {}", fileIn.getName());
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

	public static void outputBinary() throws IOException {
		LOG.info("Outputting binary file: {}", fileIn.getName());
		output(ArrayUtils.toObject(Files.readAllBytes(fileIn.toPath())));
	}

	public static void addLabel(String label) {
		labels.put(label, (byte) currentAddress);
	}

	public static void output(Byte[] bytes) throws IOException {
		if (outputToGPIO)
			GPIOHandler.writeData(bytes);

		if (binaryOut != null) {
			binaryOut.write(ArrayUtils.toPrimitive(bytes));
		}

		for (Byte b : bytes) {
			out.println(byteToBinaryString(bigEndianAddress ? Util.reverseByte(currentAddress, 4) : currentAddress, 4)
					+ " : " + byteToBinaryString(b, 8));

			currentAddress++;
		}
	}

	public static String byteToBinaryString(int b, int wordLength) {
		int template = (int) Math.pow(2, wordLength) - 1;
		return Integer.toBinaryString((b & template) + template + 0x1).substring(1);
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

		Option bigEndData = new Option("D", "big-endian-data", false, "reverse the data before outputting");
		bigEndData.setRequired(false);
		options.addOption(bigEndData);

		Option bigEndAddress = new Option("A", "big-endian-address", false, "reverse the address before outputting");
		bigEndAddress.setRequired(false);
		options.addOption(bigEndAddress);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			formatter.printHelp("tankernn-assembly-compiler", options);

			System.exit(1);
			return;
		}

		outputToGPIO = cmd.hasOption(gpio.getOpt());
		bigEndianData = cmd.hasOption(bigEndData.getOpt());
		bigEndianAddress = cmd.hasOption(bigEndAddress.getOpt());

		try {
			fileIn = (File) cmd.getParsedOptionValue("input");
			if (cmd.hasOption("output"))
				fileOut = (File) cmd.getParsedOptionValue("output");
		} catch (ParseException e) {
			LOG.catching(e);
		}
	}

	public static boolean isBigEndianAddress() {
		return bigEndianAddress;
	}
}
