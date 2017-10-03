
/**
SawtoothOscillator extends on the Oscillator class, 
It acts as a sawtooth oscillator
**/

public class SawtoothOscillator extends Oscillator{

	// Constructors
	
	/**
	Constructor for SawtoothOscillator by default sets freq to middle c, and amplitude to 100
	@param sampleRate sets the sample rate of the oscillator
	**/
	public SawtoothOscillator(int sampleRate) {
		super(sampleRate);
	}
	
	
	/**
	Constructor for SawtoothOscillator
	@param sampleRate sets the sample rate of the oscillator
	@param freq sets the frequency of the oscillator
	@param amplitude sets the amplitude of the oscillator
	**/
	public SawtoothOscillator(int sampleRate, double freq, double amplitude) {
		super(sampleRate, freq, amplitude);
	}


	/**
	Returns the frequency as a byte at time t on the sawtooth wave
	@param t is the time at which the wave is at (1 sec => t=sampleRate)
	@return The byte created by the sawtooth oscillator at time t
	**/
	@Override
	public byte createWave(double t) {
		   
		double period = (double) this.sampleRate / this.freq;
		// returning 2*(t/a - floor(1/2 + t/a)) where a is the period
		return (byte) 
				(this.amplitude * 2 * 
				((t / period) - Math.floor((1 / 2) + (t / period))
				));
	}
	   
}
