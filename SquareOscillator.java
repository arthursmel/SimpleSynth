
public class SquareOscillator extends Oscillator{

	
	public SquareOscillator(int sampleRate) {
		super(sampleRate);
	}
	
	public SquareOscillator(int sampleRate, double freq, double amplitude) {
		super(sampleRate, freq, amplitude);
	}

	@Override
	public byte createWave(double t) {
	   	   
		byte output 	= (byte) ((super.sineWave(t, DEFAULT_SINE_CONSTANT) >= 0)
							? 0 : this.amplitude);   
		return output;
	}
}
