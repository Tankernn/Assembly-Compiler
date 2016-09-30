package eu.tankernn.assembly.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import eu.tankernn.assembly.output.GPIOHandler;
import eu.tankernn.assembly.output.ParallelOutput;

public class Assemble {
	static PrintStream out;
	static BufferedReader in;

	static HashMap<String, Byte> labels = new HashMap<String, Byte>();
	static HashMap<String, Byte> variables = new HashMap<String, Byte>();
	static byte currentAddress = 0;
	
	static boolean outputToGPIO = false;
	static boolean bigEndianData = true;
	static boolean bigEndianAddress = true;

	public static void main(String[] args) {
		if (outputToGPIO)
			GPIOHandler.init(new ParallelOutput());
		
		File fileIn;

		if (args.length > 0) {
			fileIn = new File(args[0]);
		} else {
			fileIn = new File("data/in.ta");
		}

		try {
			in = new BufferedReader(new FileReader(fileIn));
		} catch (FileNotFoundException e) {
			log("Could not find file " + fileIn.getAbsolutePath());
			return;
		}

		if (args.length > 1) {
			File fileOut = new File(args[1]);
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

		try {
			startCompile();
		} catch (IOException ex) {
			log("The compiler encountered an error: ");
			ex.printStackTrace();
		}
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
		for (int i = 0; i < bytes.length; i++)
			currentAddress++;

		if (outputToGPIO)
			GPIOHandler.writeData(bytes);
		else
			for (Byte b : bytes)
				out.println(byteToBinaryString(currentAddress, 4, bigEndianAddress) + " : " + byteToBinaryString(b, 8, false));
	}
	
	private static String byteToBinaryString(int b, int wordLength, boolean reverse) {
		int template = (int) Math.pow(2, wordLength) - 1;
		StringBuilder result = new StringBuilder();
		result.append(Integer.toBinaryString((b & template) + template + 0x1).substring(1));
		if (reverse)
			result.reverse();
		return result.toString();
	}
}
