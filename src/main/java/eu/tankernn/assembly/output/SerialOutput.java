package eu.tankernn.assembly.output;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class SerialOutput implements DataOutputMethod {
	private static final Pin DATA_PIN = RaspiPin.GPIO_01;
	private static final Pin DATA_CLOCK_PIN = RaspiPin.GPIO_02;
	private GpioPinDigitalOutput dataOutputPin;
	private GpioPinDigitalOutput dataClockPin;
	
	@Override
	public void init(GpioController GPIO) {
		dataOutputPin = GPIO.provisionDigitalOutputPin(DATA_PIN, "Data pin", PinState.LOW);
		dataClockPin = GPIO.provisionDigitalOutputPin(DATA_CLOCK_PIN, "Data clock pin", PinState.LOW);
	}

	@Override
	public void output(byte b) {
		for (int i = 0; i < 8; i++) {
			dataOutputPin.setState((b & 1 << i) == 1);
			dataClockPin.pulse(10);
		}
	}

}
