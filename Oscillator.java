
public class Oscillator {

	private static final double DEFAULT_SINE_CONSTANT = 2.0;
	
	private int 	sampleRate;
	private double 	freq;
	private int 	amplitude;
	
	
	public Oscillator (int sampleRate) {
		//frequency default is middle C
		this(sampleRate, 261.6, 100);
	}
	
	public Oscillator (int sampleRate, double freq, int amplitude) {
		this.sampleRate = sampleRate;
		this.freq 		= freq;
		this.amplitude 	= amplitude;
	}
	
	public byte sawtoothWave(int t) {
		   
		double period 	= (double) this.sampleRate / this.freq;
		byte output   	= (byte) (this.amplitude * 2 * ( 
				   			(t / period) - Math.floor((1 / 2) + (t / period))
				   			));
		return output;
	}
	   
	public byte sineWave(int t) {
		return sineWave(t, DEFAULT_SINE_CONSTANT);
	}
	   
	public byte sineWave(int t, double constant) {

		double period 	= (double) this.sampleRate / this.freq;
	    double angle 	= constant * Math.PI * (t / period);
	    byte output 	= (byte) (Math.sin(angle) * amplitude); 
	       
	    return output;
	}
	   
	public byte squareWave(int t) {
		   	   
		byte output 	= (byte) ((sineWave(t, DEFAULT_SINE_CONSTANT) >= 0)
							? 0 : this.amplitude);   
		return output;
	}
	
	
	// Setter methods
	
	public void setFreq(double freq) {
		this.freq = freq;
	}
	
	public void setAmplitude(int amplitude) {
		this.amplitude = amplitude;
	}
	
}
