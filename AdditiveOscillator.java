
/**
AdditiveOscillator extends on the Oscillator class, 
it contains a sine, a sawtooth, and a square wave oscillator,
The output will be the waveform from each oscillator added together
**/

public class AdditiveOscillator extends Oscillator{
	
	// Instances of each oscillator type
	private SquareOscillator sqOsc;
	private SawtoothOscillator stOsc;
	private Oscillator siOsc;
	
	// Amplitudes for each oscillator
	private double sqAmp; // Amplitude for squaretooth oscillator
	private double stAmp; // Amplitude for sawtooth oscillator
	private double siAmp; // Amplitude for sine oscillator
	

	/**
	Constructor creates new square/saw/sine oscillators
	@param sampleRate is the sample rate used for the oscillators
	**/
	public AdditiveOscillator(int sampleRate) {
		super(sampleRate);
		sqOsc = new SquareOscillator(sampleRate);
		stOsc = new SawtoothOscillator(sampleRate);
		siOsc = new Oscillator(sampleRate);
	}


	/**
	Creates the frequency as a byte where at time t
	@param t is the time at which the wave is at (1 sec => t=sampleRate)
	@return The byte created by adding the individual oscillator waves
			together at time t
	**/
	@Override
	public byte createWave(double t) {
		return (byte) (siOsc.createWave(t) + sqOsc.createWave(t) + stOsc.createWave(t));
	}
	

	/**
	Sets the frequency of the oscillator
	@param freq the new frequency of the oscillator
	**/
	@Override
	public void setFreq(double freq) {
		// Setting frequency of the individual oscillators
		sqOsc.setFreq(freq);
		stOsc.setFreq(freq);
		siOsc.setFreq(freq);
	}
	

	/**
	Sets the amplitude of the oscillator by setting the relative amplitude of each
	individual oscillator
	@param amplitude is the value of the amplitude that will be set
	**/
	@Override
	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
		
		// Setting the amplitude of each synth relative to its current amplitude
		sqOsc.setAmplitude(amplitude * (sqAmp / 100.0));
		stOsc.setAmplitude(amplitude * (stAmp / 100.0));
		siOsc.setAmplitude(amplitude * (siAmp / 100.0));
	}
	

	/**
	Sets the amplitude of each individual oscillator
	@param 	sqAmp the amplitude of the square wave oscillator
	@param 	stAmp the amplitude of the sawtooth wave oscillator
	@param 	siAmp the amplitude of the sine wave oscillator
	**/
	public void setIndivdualAmplitudes(double sqAmp, double stAmp, double siAmp) {
		
		this.sqAmp = sqAmp;
		this.stAmp = stAmp;
		this.siAmp = siAmp;
		
		sqOsc.setAmplitude(sqAmp);
		stOsc.setAmplitude(stAmp);
		siOsc.setAmplitude(siAmp);
	}
}
