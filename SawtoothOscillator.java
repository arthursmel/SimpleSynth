
public class SawtoothOscillator extends Oscillator{

	
	public SawtoothOscillator(int sampleRate, double freq, double amplitude) {
		super(sampleRate, freq, amplitude);
	}

	public SawtoothOscillator(int sampleRate) {
		super(sampleRate);
	}
	
	@Override
	public byte createWave(double t) {
		   
		double period 	= (double) this.sampleRate / this.freq;
		byte output   	= (byte) (this.amplitude * 2 * ( 
				   			(t / period) - Math.floor((1 / 2) + (t / period))
				   			));
		return output;
	}
	   
}
