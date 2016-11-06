package eu.tankernn.assembly.output;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import eu.tankernn.assembly.compiler.Assemble;
import eu.tankernn.assembly.compiler.Util;

public class GPIOHandler {
	private static final Logger LOG = LogManager.getLogger();
	
	static final Pin WRITE_PIN = RaspiPin.GPIO_26;
	static GpioPinDigitalOutput writePin;
	
	private static OutputMethod dataOutput, addressOutput;
	static GpioController GPIO;

	static byte currentAddress;

	public static void init(OutputMethod dataMethod, OutputMethod addressMethod) {
		// create GPIO controller instance
		GPIO = GpioFactory.getInstance();
		
		dataOutput = dataMethod;
		dataOutput.init(GPIO);
		
		addressOutput = addressMethod;
		addressOutput.init(GPIO);
		
		writePin = GPIO.provisionDigitalOutputPin(WRITE_PIN, "Write pin", PinState.HIGH);
		writePin.setShutdownOptions(true, PinState.HIGH);
	}

	public static void writeData(Byte[] bytes) {
		for (byte b : bytes) {
			// Output address
			byte address = Assemble.isBigEndianAddress() ? Util.reverseByte(new Byte(currentAddress), 4) : currentAddress;
			addressOutput.output(address);
			currentAddress++;

			// Output data
			dataOutput.output(b);

			// Pulse write pin
			writePin.pulse(100, PinState.LOW, true);

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				LOG.catching(e);
				return;
			}
		}
	}

	public static void cleanUp() {
		GPIO.shutdown();
	}
}
