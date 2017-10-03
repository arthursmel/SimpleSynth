
/**
SynthNote creates a thread in which the attack decay sustain release envelopes
are applied on the oscillator selected. The thread uses a buffer to write the bytes to.
When the buffer is full, the listener's callback function is used, where the bytes then can
be written to the line out. When the SynthNote has completed, the remaining bytes in the
buffer are drained. The SynthNote can also be cancelled as this is a monophonic synth.
**/

public class SynthNote extends Thread {
		
	private Buffer buffer;
	private Oscillator osc;
	
	private static final int DEFAULT_SAMPLE_RATE = 44100;
	private static final int DEFUALT_BUFFER_SIZE = 256;
	
	private double sustainAmplitude;
	private double maxAmplitude;
	private double currentAmplitude;
	
	private double attackTime;
	private double decayTime;
	private double releaseTime;
	private double currentTime;
	
	/* Notes may be in the pressed state
	where the note will sustain, the release state,
	where the note will release and eventually finish,
	or the cancelled state, where the note is immediately
	finished */
	private final int NOTE_PRESSED = 0;
	private final int NOTE_RELEASED = 1;
	private final int NOTE_CANCELLED = 2;
	private int noteState; // Current note state
	
	private double frequency; // Current frequency of the oscillator
	
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
		
		// Initallising the buffer 
		this.buffer = new Buffer(DEFUALT_BUFFER_SIZE, listener);
		this.osc = osc;
		
		// Initiallising the current amplitude & time
		this.currentAmplitude = 0;
		this.currentTime = 0;
		
		this.maxAmplitude = maxAmplitude;
		this.frequency = freq;
		
		// attackTime, decayTime, releaseTime are calculated relative to the sampleRate
		this.attackTime = a * sampleRate;
		this.decayTime = d * sampleRate;
		this.releaseTime = r * sampleRate;
		
		// The sustainAmplitude is calculated relative to the maxAmplitude
		this.sustainAmplitude = s * (maxAmplitude / 100.0);
		
	}
	

	public void run() {
		 
		this.osc.setFreq(this.frequency); // Setting osc freq to the SynthNote freq
		this.noteState = NOTE_PRESSED; // User is pressing the key currently
		
		// While the note's time is still in the attack envelope, 
		// and the key is still pressed
		while (this.currentTime <= this.attackTime && 
				this.noteState == NOTE_PRESSED) {
			// Continue with the attack envelope
			attackEnv();
			this.incrementTime();
		}
		// The attack envelope has finished
		
		// Calculating the slope and c value for the decay envelope 
		double m = (this.sustainAmplitude - this.maxAmplitude) / this.decayTime; // m = y2-y1/x2-x1
		double c = (-m * this.attackTime) + (this.maxAmplitude); // c = -mx + y
		
		// While the note's time is still in the decay envelope 
		// and the key is still pressed
		while (this.currentTime <= this.attackTime + this.decayTime && 
				// attackTime + decayTime is the end of the decay envelope
				this.noteState == NOTE_PRESSED) {
			// Continue with the decay envelope 
			decayEnv(m, c);
			this.incrementTime();
		}
		// The decay envelope has finished 
		
		// While the user is still pressing the key
		while (this.noteState == NOTE_PRESSED) {
			// Continue sustaining the note
			sustainEnv();
			this.incrementTime();
		}
		// The user has now let go of the key
		
		// Calculating the slope and c value for the release envelope
		// The sustain has finished, so we know the current time is sustainEnd
		double sustainEnd = this.currentTime;
		// The end of the release envelope is the end of the sustain envelope + the release time
		double releaseEnd = this.currentTime + this.releaseTime; 
		m = -this.currentAmplitude / this.releaseTime; // m = y2-y1/x2-x1
		c = -m * (sustainEnd + this.releaseTime);  // c = -mx + y
		
		// While the note has not gotten to the end of the release envelope
		// and the note has not been cancelled
		while (this.currentTime <= releaseEnd && this.noteState != NOTE_CANCELLED) {
			// Continue releasing the note
			releaseEnv(m, c);
			this.incrementTime();
		}
		// Release has finished
		
		this.buffer.drain(); // Output any remaining bytes in the buffer
	}
	
	
	/**
	Calculates the amolitude value for the attack envelope and adds the 
	frequency to the output buffer
	**/
	private void attackEnv() {
		
		this.currentAmplitude = (this.currentTime) /
				(this.attackTime / this.maxAmplitude); // amp = m(time)
		
		osc.setAmplitude(this.currentAmplitude);
		// Add the byte from wave at time t to the buffer
		this.buffer.add(osc.createWave(this.currentTime)); 
	}
	
	
	/**
	Calculates the amplitude value for the decay envelope and adds the 
	frequency to the output buffer
	@param m slope of decay
	@param c c value of decay
	**/
	private void decayEnv(double m, double c) {
		
		this.currentAmplitude = (m * this.currentTime) + c;	 // amp = m(time) + c

		osc.setAmplitude(this.currentAmplitude);
		// Add the byte from wave at time t to the buffer
		this.buffer.add(osc.createWave(this.currentTime));
		
	}
	
	
	/**
	Adds the frequency with the constant sustain amplitude to the output buffer
	**/
	private void sustainEnv() {
		
		// No need for sleop or c as the suatin amplitude is constant
		this.currentAmplitude = this.sustainAmplitude;
		
		osc.setAmplitude(this.currentAmplitude);
		// Add the byte from wave at time t to the buffer
		this.buffer.add(osc.createWave(this.currentTime));
		
	}
	
	
	/**
	Calculates the amplitude value for the release envelope and adds the 
	frequency to the output buffer
	@param m slope of release
	@param c c value of release
	**/
	private void releaseEnv(double m, double c) {
		
		this.currentAmplitude = (m * this.currentTime) + c; // amp = m(time) + c
		
		osc.setAmplitude(this.currentAmplitude);
		// Add the byte from wave at time t to the buffer
		this.buffer.add(osc.createWave(this.currentTime));
		
	}
	
	
	/**
	Increments the current relative time of the SynthNote
	**/
	private void incrementTime() {
		this.currentTime += 1;
	}
	
	
	// Public methods
	
	/**
	Notifies the SynthNote thread that the key has been released
	by the user
	**/
	public void release() {
		this.noteState = NOTE_RELEASED;
	}
		
	
	/**
	Notifies the SynthNote thread that the note has been cancelled
	by the user as they have pressed another key
	**/
	public void cancel() {
		this.noteState = NOTE_CANCELLED;
	}
	

	/**
	Private buffer class used to output bytes to the line
	**/
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
