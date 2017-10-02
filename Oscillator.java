
class Oscillator {

	protected final double DEFAULT_SINE_CONSTANT = 2.0;
	
	protected int sampleRate;
	protected double freq;
	protected double amplitude;
	
	
	protected Oscillator (int sampleRate) {
		//frequency default is middle C
		this(sampleRate, 261.6, 100);
	}
	
	protected Oscillator (int sampleRate, double freq, double amplitude) {
		this.sampleRate = sampleRate;
		this.freq 		= freq;
		this.amplitude 	= amplitude;
	}
	
	
	protected byte createWave(double t) {
		return 0;
	}
	
	protected byte sineWave(double t, double constant) {

		double period 	= (double) this.sampleRate / this.freq;
	    double angle 	= constant * Math.PI * (t / period);
	    byte output 	= (byte) (Math.sin(angle) * amplitude); 
	       
	    return output;
	}
	   
		
	// Setter methods
	
	public void setFreq(double freq) {
		this.freq = freq;
	}
	
	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}

	
	public double getAmplitude() {
		return this.amplitude;
	}
	
}
