package eu.tankernn.assembly.output;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public class ParallelOutput implements OutputMethod {
	
	private Pin[] rawPins;
	
	private GpioPinDigitalOutput[] dataOutputPins;
	
	public ParallelOutput(Pin[] pins) {
		rawPins = pins;
		dataOutputPins = new GpioPinDigitalOutput[rawPins.length];
	}
	
	@Override
	public void init(GpioController GPIO) {
		for (int i = 0; i < dataOutputPins.length; i++) {
			dataOutputPins[i] = GPIO.provisionDigitalOutputPin(rawPins[i], "Data pin " + i, PinState.LOW);
			dataOutputPins[i].setShutdownOptions(true, PinState.LOW);
		}
	}

	@Override
	public void output(byte b) {
		for (int i = 0; i < dataOutputPins.length; i++) {
			dataOutputPins[i].setState(((b >> i) & 1) == 1);
		}
	}

}
