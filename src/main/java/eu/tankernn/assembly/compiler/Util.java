package eu.tankernn.assembly.compiler;

public class Util {
	public static byte reverseByte(byte x, int wordSize) {
		byte y = 0;
		for (int i = 0; i < wordSize; i++) {
			y <<= 1;
			y |= (x & 1);
			x >>= 1;
		}
		return y;
	}
}
