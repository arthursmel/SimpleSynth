import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class SynthNote extends Thread {
		
	private Buffer buffer;
	private Oscillator osc;
	
	private double frequency;
	
	private int sampleRate;
	private final static int DEFAULT_SAMPLE_RATE = 44100;
	
	private double sustainAmplitude;
	private double maxAmplitude;
	private double currentAmplitude = 0;
	
	private double attackTime;
	private double decayTime;
	private double releaseTime;
	private double currentTime = 0;
	
	private final int NOTE_PRESSED = 0;
	private final int NOTE_RELEASED = 1;
	private int noteState;
	
	public SynthNote(
			Oscillator osc,
			double freq, double maxAmplitude,
			double a, double d, double s, double r,
			OnBufferFullListener listener) {
		this(osc, DEFAULT_SAMPLE_RATE, freq, maxAmplitude, a, d, s, r, listener);
	}
	
	public SynthNote(
			Oscillator osc,
			int sampleRate, double freq, double maxAmplitude,
			double a, double d, double s, double r,
			OnBufferFullListener listener) {
		
		this.buffer = new Buffer(sampleRate, listener);
		this.osc = osc;
		
		this.maxAmplitude = maxAmplitude;
		this.frequency = freq;
		this.attackTime = a * sampleRate;
		this.decayTime = d * sampleRate;
		this.sustainAmplitude = s;
		this.releaseTime = r * sampleRate;
		this.sampleRate = sampleRate;
	}
	

	public void run() {
		 
		this.osc.setFreq(this.frequency);
		this.noteState = NOTE_PRESSED;
		
		while (this.currentTime <= this.attackTime) {
			attackEnv();
		}
	
		double m = (this.sustainAmplitude - this.maxAmplitude) / this.decayTime;
		double c = (-m * this.attackTime) + (this.maxAmplitude);
		
		while (this.currentTime <= this.attackTime + this.decayTime) {
			decayEnv(m, c);
		}
		
		while (this.noteState == NOTE_PRESSED) {
			sustainEnv();
		}
		
		double sustainEnd = this.currentTime;	
		double releaseEnd = this.currentTime + this.releaseTime;
		m = -this.sustainAmplitude / this.releaseTime;
		c = -m * (sustainEnd + this.releaseTime);
		
		while (this.currentTime <= releaseEnd) {
			releaseEnv(m, c);
		}
		
		this.buffer.drain();
		
	}
	
	
	private void attackEnv() {
		
		this.currentAmplitude = (this.currentTime) /
				(this.attackTime / this.maxAmplitude);
		
		this.incrementTime();
		
		osc.setAmplitude(this.currentAmplitude);
		this.buffer.add(osc.createWave(this.currentTime));
		
	}
	
	private void decayEnv(double m, double c) {
		
		this.currentAmplitude = (m * this.currentTime) + c;	
		
		this.incrementTime();
		
		osc.setAmplitude(this.currentAmplitude);
		this.buffer.add(osc.createWave(this.currentTime));
		
	}
	
	private void sustainEnv() {
				
		this.currentAmplitude = this.sustainAmplitude;
		this.incrementTime();
		
		osc.setAmplitude(this.currentAmplitude);
		this.buffer.add(osc.createWave(this.currentTime));
		
	}
	
	private void releaseEnv(double m, double c) {
		
		this.currentAmplitude = (m * this.currentTime) + c;
		
		this.incrementTime();
		osc.setAmplitude(this.currentAmplitude);
		this.buffer.add(osc.createWave(this.currentTime));
		
	}
	
	private void incrementTime() {
		this.currentTime += 1;
	}
	
	
	public void release() {
		this.noteState = NOTE_RELEASED;
	}
		
	public void cancel() {
		this.attackTime = 0;
		this.decayTime = 0;
		this.maxAmplitude = 0;
		this.releaseTime = 0;
	}
	

	
	private class Buffer {
		
		int size;
		int itemCount = 0;
		byte[] buffer;
		OnBufferFullListener listener;
		
		Buffer(int size, OnBufferFullListener listener) {
			this.size = size;
			this.listener = listener;
			this.buffer = new byte[size];
		}
		
		void add(byte b) {
			if (this.itemCount < this.size) {
				this.buffer[itemCount++] = b;
			} else {
				this.listener.onFull(this.buffer.clone());
				this.reset();
				this.buffer[itemCount++] = b;
			}
		}
		
		void reset() {
			this.buffer = new byte[size];
			this.itemCount = 0;
		}
		
		void drain() {
			this.listener.onDrain(this.buffer.clone());
		}
				
	}
	
	public interface OnBufferFullListener {
		void onFull(byte[] buffer);
		void onDrain(byte[] buffer);
	}
	
}
