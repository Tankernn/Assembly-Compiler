package eu.tankernn.assembly.output;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class GPIOHandler {

	static final Pin[] ADDRESS_PINS = { RaspiPin.GPIO_22, RaspiPin.GPIO_23, RaspiPin.GPIO_24, RaspiPin.GPIO_25 };
	static final Pin WRITE_PIN = RaspiPin.GPIO_26;
	private static DataOutputMethod dom;

	static GpioPinDigitalOutput[] addressOutputPins = new GpioPinDigitalOutput[8];
	static GpioPinDigitalOutput writePin;

	// create GPIO controller instance
	static final GpioController GPIO = GpioFactory.getInstance();

	static byte currentAddress;

	public static void init(DataOutputMethod method) {
		dom = method;
		dom.init(GPIO);

		for (int i = 0; i < addressOutputPins.length; i++)
			addressOutputPins[i] = GPIO.provisionDigitalOutputPin(ADDRESS_PINS[i], "Address pin " + i, PinState.LOW);

		writePin = GPIO.provisionDigitalOutputPin(WRITE_PIN, "Write pin", PinState.HIGH);
	}

	public static void writeData(Byte[] bytes) {
		for (byte b : bytes) {
			// Output address
			for (int i = 0; i < addressOutputPins.length; i++) {
				addressOutputPins[i].setState((currentAddress & 1 << i) == 1);
			}
			currentAddress++;

			// Output data
			dom.output(b);

			// Pulse write pin
			writePin.pulse(10);
		}
	}
}
