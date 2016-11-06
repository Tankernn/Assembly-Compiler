package eu.tankernn.assembly.output;

import com.pi4j.io.gpio.GpioController;

public interface OutputMethod {
	public void init(GpioController GPIO);

	public void output(byte b);
}
