import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SimpleSynth {

	private static final int SAMPLE_RATE = 44100;
	
	public static void main(String[] args) throws LineUnavailableException {
	       
		Oscillator osc = new Oscillator(SAMPLE_RATE, 500, 100);
		
		AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
		SourceDataLine line = AudioSystem.getSourceDataLine(af);
		line.open(af, SAMPLE_RATE);
		line.start();
	
	
		byte [] buffer = new byte[1];
	   
		
		
		for (int t = 0; t < SAMPLE_RATE; t++) {
			buffer[0] = osc.sineWave(t);
			line.write(buffer, 0, buffer.length);
		}
		
		for (int t = 0; t < SAMPLE_RATE; t++) {
			buffer[0] = osc.squareWave(t);
			line.write(buffer, 0, buffer.length);
		}
		
		for (int t = 0; t < SAMPLE_RATE; t++) {
			buffer[0] = osc.sawtoothWave(t);
			line.write(buffer, 0, buffer.length);
		}

	  
		line.drain();
		line.close();
	}

}
