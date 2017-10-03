
/**
SquareOscillator extends on the Oscillator class, 
It acts as a square wave oscillator
**/

public class SquareOscillator extends Oscillator{

	// Constructors
	
	/**
	Constructor for SquareOscillator by default sets freq to middle c, and amplitude to 100
	@param sampleRate sets the sample rate of the oscillator
	**/
	public SquareOscillator(int sampleRate) {
		super(sampleRate);
	}
	
	
	/**
	Constructor for SquareOscillator
	@param sampleRate sets the sample rate of the oscillator
	@param freq sets the frequency of the oscillator
	@param amplitude sets the amplitude of the oscillator
	**/
	public SquareOscillator(int sampleRate, double freq, double amplitude) {
		super(sampleRate, freq, amplitude);
	}

	
	/**
	Creates the frequency as a byte where at time t
	@param t is the time at which the wave is at (1 sec => t=sampleRate)
	@return The byte created by the square wave oscillator at time t
	**/
	@Override
	public byte createWave(double t) {
	   	// If sine wave at t is negative, return 0, otherwave return the amplitude
		return (byte) ((super.sineWave(t, DEFAULT_SINE_CONSTANT) <= 0)
							? 0 : this.amplitude);   
	}
}
