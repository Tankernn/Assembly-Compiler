package eu.tankernn.assembly.output;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import static com.pi4j.io.gpio.RaspiPin.*;

public class ParallelOutput implements DataOutputMethod {

	private static final Pin[] DATA_PINS = { GPIO_07, GPIO_00, GPIO_01, GPIO_02, GPIO_03, GPIO_04, GPIO_05, GPIO_06 };

	private GpioPinDigitalOutput[] dataOutputPins = new GpioPinDigitalOutput[8];

	@Override
	public void init(GpioController GPIO) {
		for (int i = 0; i < dataOutputPins.length; i++)
			dataOutputPins[i] = GPIO.provisionDigitalOutputPin(DATA_PINS[i], "Data pin " + i, PinState.LOW);
	}

	@Override
	public void output(byte b) {
		for (int i = 0; i < 8; i++)
			dataOutputPins[i].setState((b & 1 << i) == 1);
	}

}
