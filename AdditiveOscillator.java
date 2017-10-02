
public class AdditiveOscillator extends Oscillator{
	
	private SquareOscillator sqOsc;
	private SawtoothOscillator stOsc;
	private SineOscillator siOsc;
	
	private double sqAmp;
	private double stAmp;
	private double siAmp;
	
	public AdditiveOscillator(int sampleRate) {
		super(sampleRate);
		sqOsc = new SquareOscillator(sampleRate);
		stOsc = new SawtoothOscillator(sampleRate);
		siOsc = new SineOscillator(sampleRate);
	}


	@Override
	public byte createWave(double t) {
		return (byte) (siOsc.createWave(t) + sqOsc.createWave(t) + stOsc.createWave(t));
	}
	
	@Override
	public void setFreq(double freq) {
		sqOsc.setFreq(freq);
		stOsc.setFreq(freq);
		siOsc.setFreq(freq);
	}
	
	@Override
	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
		
		sqOsc.setAmplitude(amplitude * (sqAmp / 100.0));
		stOsc.setAmplitude(amplitude * (stAmp / 100.0));
		siOsc.setAmplitude(amplitude * (siAmp / 100.0));
	}
	
	public void setIndivdualAmplitudes(double sqAmp, double stAmp, double siAmp) {
		
		this.sqAmp = sqAmp;
		this.stAmp = stAmp;
		this.siAmp = siAmp;
		
		sqOsc.setAmplitude(sqAmp);
		stOsc.setAmplitude(stAmp);
		siOsc.setAmplitude(siAmp);
	}
}
