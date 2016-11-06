package eu.tankernn.assembly.output;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public class SerialOutput implements OutputMethod {
	private Pin dataPin;
	private Pin clockPin;
	
	private GpioPinDigitalOutput dataOutputPin;
	private GpioPinDigitalOutput clockOutputPin;
	
	public SerialOutput(Pin dataPin, Pin clockPin) {
		this.dataPin = dataPin;
		this.clockPin = clockPin;
	}

	@Override
	public void init(GpioController GPIO) {
		dataOutputPin = GPIO.provisionDigitalOutputPin(dataPin, "Data pin", PinState.LOW);
		clockOutputPin = GPIO.provisionDigitalOutputPin(clockPin, "Serial clock pin", PinState.LOW);
	}

	@Override
	public void output(byte b) {
		for (int i = 0; i < 8; i++) {
			dataOutputPin.setState((b & 1 << i) == 1);
			clockOutputPin.pulse(10);
		}
	}

}
