
public class SineOscillator extends Oscillator{
	
	public SineOscillator(int sampleRate) {
		super(sampleRate);
	}
	
	public SineOscillator(int sampleRate, double freq, double amplitude) {
		super(sampleRate, freq, amplitude);
	}
	
	@Override
	public byte createWave(double t) {
		return super.sineWave(t, DEFAULT_SINE_CONSTANT);
	}



}
