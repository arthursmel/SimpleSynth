
/**
Oscillator class by default acts as a sine wave oscillator
**/

class Oscillator {

	// Constants
	protected final double DEFAULT_SINE_CONSTANT = 2.0;
	private final static double MIDDLE_C = 261.1; // Freq for middle c
	private final static double DEFAULT_AMPLITUDE = 100;
	
	protected int sampleRate;
	protected double freq;
	protected double amplitude;

	// Constructors

	/**
	Constructor for Oscillator by default sets freq to middle c, and amplitude to 100
	@param sampleRate sets the sample rate of the oscillator
	**/
	protected Oscillator (int sampleRate) {
		//frequency default is middle C
		this(sampleRate, MIDDLE_C, DEFAULT_AMPLITUDE);
	}
	
	
	/**
	Constructor for Oscillator
	@param sampleRate sets the sample rate of the oscillator
	@param freq sets the frequency of the oscillator
	@param amplitude sets the amplitude of the oscillator
	**/
	protected Oscillator (int sampleRate, double freq, double amplitude) {
		this.sampleRate = sampleRate;
		this.freq = freq;
		this.amplitude = amplitude;
	}
	
	
	/**
	Creates the frequency as a byte where at time t
	@param t is the time at which the wave is at (1 sec => t=sampleRate)
	@return The byte created by the sine oscillator at time t
	**/
	protected byte createWave(double t) {
		return sineWave(t, DEFAULT_SINE_CONSTANT);
	}
	
	
	/**
	Creates the frequency of the sine wave oscillator at time t, using the constant
	specified
	@param t the time of the sine wave to return the frequency at
	@param constant default is 2.0, can be used to create square waves using fourier series
	@return the byte containing the frequency of the sine wave at time t
	**/
	protected byte sineWave(double t, double constant) {
		double period = (double) this.sampleRate / this.freq;
	    double angle = constant * Math.PI * (t / period);
	    return  (byte) (Math.sin(angle) * amplitude); 
	}
	   
		
	// Setter methods
	
	/**
	Sets the frequency of the oscillator
	@param freq is the frequency the oscillator will be set to
	**/
	public void setFreq(double freq) {
		this.freq = freq;
	}
	
	
	/**
	Sets the amplitude of the oscillator
	@param amplitude is the amplitude the oscillator will be set to
	**/
	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}
}