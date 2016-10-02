package eu.tankernn.assembly.compiler;

import java.util.ArrayList;
import java.util.List;

public class LineParser {

	public static class OPCodes {
		public static final byte NOP = 0x0, LDA = 0x1, ADD = 0x2, SUB = 0x3, AND = 0x4, OR = 0x5, NOT_B = 0x6,
				STO = 0x7, OUT = 0x8, JMP = 0x9, HLT = 0xA;
	}

	String line;
	int lineIndex;
	ArrayList<Byte> list = new ArrayList<Byte>();
	String[] instruction;
	int pos = 0;

	public LineParser(String line, int lineIndex) {
		this.line = line;
		this.lineIndex = lineIndex;
		instruction = line.split("\\s+");
	}

	public Byte[] parse() throws SyntaxErrorException {

		// Comment
		if (line.startsWith(";")) {
			return null;
		}

		// Add a label
		if (instruction[0].endsWith(":")) {
			Assemble.addLabel(instruction[0].substring(0, instruction[0].length() - 2));
			pos++;
		}

		String command = instruction[pos].toUpperCase();

		switch (command) {
		case "NOP":
			addWithoutAddress(OPCodes.NOP);
			break;
		case "LDA":
			addWithAddress(OPCodes.LDA);
			break;
		case "ADD":
			addWithAddress(OPCodes.ADD);
			break;
		case "SUB":
			addWithAddress(OPCodes.SUB);
			break;
		case "AND":
			addWithAddress(OPCodes.AND);
			break;
		case "OR":
			addWithAddress(OPCodes.OR);
			break;
		case "NOT":
		case "NOTB":
			addWithAddress(OPCodes.NOT_B);
			break;
		case "STO":
			addWithoutAddress(OPCodes.STO);
			break;
		case "OUT":
			addWithoutAddress(OPCodes.OUT);
			break;
		case "JMP":
			pos++;
			switch (instruction[pos]) {
			case "":

			}
			addWithoutAddress(OPCodes.JMP);
			break;
		case "HLT":
			addWithoutAddress(OPCodes.HLT);
			break;
		default:
			// Try adding as byte before throwing
			try {
				byte b = parseToByte(instruction[pos], false);
				list.add(b);
				return listToByteArray(list);
			} catch (NumberFormatException ex) {
				throw new SyntaxErrorException("Commmand not found: " + command + " At line: " + lineIndex);
			}
		}

		return listToByteArray(list);
	}

	private static Byte[] listToByteArray(List<Byte> list) {
		return list.toArray(new Byte[list.size()]);
	}

	/**
	 * Converts a string representation of a byte value into a byte.
	 * 
	 * @param s
	 *            A string with the format <code>$base:value</code>
	 * @return The byte value represented by the string
	 */
	private Byte parseToByte(String s, boolean isAddress) {
		byte b;
		if (s.contains("$")) {
			String[] sArr = instruction[pos].replace("$", "").split(":");
			b = (byte) Integer.parseInt(sArr[1], Integer.parseInt(sArr[0]));
		} else {
			b = (byte) Integer.parseInt(s);
		}
		return Assemble.bigEndianData ? reverseByte(b, isAddress ? 4 : 8) : b;

	}

	private byte reverseByte(byte x, int wordSize) {
		System.out.print("Original: " + Assemble.byteToBinaryString(x, wordSize, false));
		byte y = (byte) (((x & 0xff) >>> 0) << 24);
		System.out.println(", Reversed: " + Assemble.byteToBinaryString(y, wordSize, false));
		return y;
	}

	private void addWithAddress(byte OPCode) throws SyntaxErrorException {
		pos++;
		byte address;
		try {
			address = parseToByte(instruction[pos], true);
		} catch (IndexOutOfBoundsException ex) {
			throw new SyntaxErrorException("Missing address." + " At line: " + lineIndex);
		}
		list.add((byte) ((OPCode << 0x4) | address));
	}

	private void addWithoutAddress(byte OPCode) {
		list.add((byte) ((OPCode << 0x4)));
	}
}
