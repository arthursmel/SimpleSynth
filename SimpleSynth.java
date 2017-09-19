import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SimpleSynth {

	private static final int SAMPLE_RATE = 44100;
	
	public static void main(String[] args) throws LineUnavailableException {
	    
		final AudioFormat af = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
		final SourceDataLine line = AudioSystem.getSourceDataLine(af);
		line.open(af, SAMPLE_RATE);
		line.start();
		
		final SquareOscillator osc = new SquareOscillator(SAMPLE_RATE);	
		SynthNote note = new SynthNote(osc, 206.0, 60, 0.5, 0.1, 40, 0.5, new SynthNote.OnBufferFullListener() {

			@Override
			public void onFull(byte[] buffer) {
				line.write(buffer, 0, buffer.length);
			}

			@Override
			public void onDrain(byte[] buffer) {
				line.write(buffer, 0, buffer.length);
			}
			
		});
		note.start();
		

		Scanner sc = new Scanner(System.in);

		if (sc.nextInt() == 1) {
			note.release();
		}
		

		
		/*
		for (int t = 0; t < SAMPLE_RATE; t++) {
			buffer[0] = osc.squareWave(t);
			line.write(buffer, 0, buffer.length);
		}
		
		for (int t = 0; t < SAMPLE_RATE; t++) {
			buffer[0] = osc.sawtoothWave(t);
			line.write(buffer, 0, buffer.length);
		}*/

		
		line.drain();
		line.close(); 
	}

}
