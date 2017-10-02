import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SimpleSynth {

	private static final int SAMPLE_RATE = 44100;
	private static final int KEY_COUNT = 5;
	private static final int DEFAULT_OCTAVE = 0;
	private static final int OCTAVE_COUNT = 10;
	
	// GUI	
	private static final int ROW_COUNT = 2;
	private static final int COLUMN_COUNT = 2;
	
	private static final int SCROLL_BAR_MAX = 99; 
	private static final int SCROLL_BAR_MIN = 0;

	private JPanel keysPanel;
	private JPanel ADSRPanel;
	private JPanel oscPanel;
	private JPanel octavePanel;
	
	// Envelope
	private double a; // attack time 
	private double d; // decay time
	private double s; // sustain amplitude
	private double r; // release time
	
	private double maxAmplitude; // Current amplitude
	private double sineAmplitude;
	private double sawtoothAmplitude;
	private double squareAmplitude;
	
	private int currentOctave;
	
	private AdditiveOscillator osc; 
	
	private SynthNote currentNote = null; // reference to current note playing 
	private final SourceDataLine line;
	private final AudioFormat af;
	
	/*
	 * Creates and returns a thread with the current specified adsr values,
	 * freq, osc, maxAmp
	 */
	public SynthNote createSynthNote(double freq) {
		
		SynthNote note = new SynthNote(this.osc, freq, this.maxAmplitude,
				this.a, this.d, this.s, this.r, new SynthNote.OnBufferFullListener() {

			@Override
			public void onFull(byte[] buffer) {
				
				// Write the output from the oscillator to the buffer
				// Written each time the buffer is full
				line.write(buffer, 0, buffer.length);

			}

			@Override
			public void onDrain(byte[] buffer) {
				
				// Writes the last bytes from the buffer, as
				// we can't assume the buffer will be full at the end
				line.write(buffer, 0, buffer.length);
			}
			
		});
		return note;
		
	}
	
	public void playSynthNote(SynthNote note) throws LineUnavailableException{
		
		if (currentNote != null) currentNote.cancel();

    	System.out.println("cancelling");
    	currentNote = note;
    	
    	if (line.isOpen()) {
	    	line.stop();
	    	line.flush();
	    	line.close();
    	}

		line.open(af);
	    line.start();
	    line.drain();
	    	
	    currentNote.start();
	}
	

	
	MouseListener createKeyPressListener(final Note note) {
		
		MouseListener keyPressListener = new MouseListener() {

		    @Override
		    public void mousePressed(MouseEvent e) {
		    	try { 	playSynthNote(
		    				createSynthNote(
		    					note.getFreq(note, currentOctave)	
		    				)
		    			);
		    		}
		    	catch (LineUnavailableException error) {}
		    }

			@Override
			public void mouseReleased(MouseEvent e) {
				System.out.println("released");
				currentNote.release();
			}

			@Override public void mouseClicked(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
		};
		
		return keyPressListener;
		
	}
	
	
	public static double calculateNewParamValue(EnvelopeParameter param, int newValue) {
		return param.maxValue()
		- (((double) newValue / (double) SCROLL_BAR_MAX)
		* param.maxValue());	
	}
	
	
	AdjustmentListener createEnvelopeAdjustmentListener(final Scrollbar scrollBar, final EnvelopeParameter param) {

		
		AdjustmentListener listener = new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {

				double newValue = calculateNewParamValue(param, scrollBar.getValue());
				System.out.println(scrollBar.getValue());
				
				switch(param) {
				
				case ATTACK:
					a = newValue;
					System.out.println(a);
					break;
				case DECAY:
					d = newValue;
					System.out.println(d);
					break;
				case SUSTAIN:
 					s = newValue;
					System.out.println(s);
					break;
				case RELEASE:
					r = newValue;
					System.out.println(r);
				
				}
			}
		};
		
		return listener;
	}
	
	
	
	public void createKeys() {
		
		int noteCount = Note.values().length;
		JButton[] keys = new JButton[noteCount];
		
		for (Note note : Note.values()) {
			keys[--noteCount] = new JButton();
			keys[noteCount].setPreferredSize(new Dimension(40, 40));
			
			keys[noteCount].setText(note.isSharp() ? "#" : "");

			keys[noteCount].addMouseListener(createKeyPressListener(note));
			keysPanel.add(keys[noteCount]);
		}
	}
	
	public Scrollbar createNewParameterScrollbar(EnvelopeParameter param) {
		
		Scrollbar sb = new Scrollbar(Scrollbar.VERTICAL);
		sb.addAdjustmentListener(
				createEnvelopeAdjustmentListener(sb, param)
		);
		sb.setValues(SCROLL_BAR_MAX, 1, SCROLL_BAR_MIN, SCROLL_BAR_MAX);
		return sb;
		
	}
	
	public void createADSR() {
	
		ADSRPanel.add(createNewParameterScrollbar(EnvelopeParameter.ATTACK));
		ADSRPanel.add(createNewParameterScrollbar(EnvelopeParameter.DECAY));
		ADSRPanel.add(createNewParameterScrollbar(EnvelopeParameter.SUSTAIN));
		ADSRPanel.add(createNewParameterScrollbar(EnvelopeParameter.RELEASE));
		
	}
	
	
	public void createOsc() {
		
		final Scrollbar sineAmp = new Scrollbar(Scrollbar.VERTICAL);
		sineAmp.setValues(SCROLL_BAR_MAX, 1, SCROLL_BAR_MIN, SCROLL_BAR_MAX);
		sineAmp.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				sineAmplitude = SCROLL_BAR_MAX - sineAmp.getValue();
				osc.setIndivdualAmplitudes(squareAmplitude, sawtoothAmplitude, sineAmplitude);
			}
		});
		oscPanel.add(sineAmp);
		
		final Scrollbar sawAmp = new Scrollbar(Scrollbar.VERTICAL);
		sawAmp.setValues(100, 1, 0, 100);
		sawAmp.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				sawtoothAmplitude = SCROLL_BAR_MAX - sawAmp.getValue();
				osc.setIndivdualAmplitudes(squareAmplitude, sawtoothAmplitude, sineAmplitude);
			}
		});
		oscPanel.add(sawAmp);
		
		final Scrollbar squareAmp = new Scrollbar(Scrollbar.VERTICAL);
		squareAmp.setValues(100, 1, 0, 100);
		squareAmp.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				squareAmplitude = SCROLL_BAR_MAX - squareAmp.getValue();
				osc.setIndivdualAmplitudes(squareAmplitude, sawtoothAmplitude, sineAmplitude);
			}
		});
		oscPanel.add(squareAmp);
		
	}
	
	public void createOctaveChoice() {
		
		final Choice octaveChoice = new Choice();
		
		for (int o = 0; o < OCTAVE_COUNT; o++) {
			octaveChoice.add(String.valueOf(o));
		}
		
		octaveChoice.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				currentOctave = Integer.valueOf(octaveChoice.getSelectedItem());
			}
			
		});
		
		octavePanel.add(octaveChoice);
		
	}
	
	public void createGUI() {
		
		JFrame frame = new JFrame("SimpleSynth");
		frame.setLayout(new GridLayout(ROW_COUNT, COLUMN_COUNT, 1, 1));
		
		keysPanel = new JPanel();
		keysPanel.setLayout(new GridLayout(1, KEY_COUNT, 1, 1));
		
		oscPanel = new JPanel();
		oscPanel.setLayout(new GridLayout(1, 3, 1, 1));
		
		ADSRPanel = new JPanel();
		ADSRPanel.setLayout(new GridLayout(1, 4, 1, 1));
		
		octavePanel = new JPanel();
		octavePanel.setLayout(new GridLayout(1, 1, 1, 1));
		
		frame.add(oscPanel, BorderLayout.CENTER);
		frame.add(ADSRPanel, BorderLayout.CENTER);
		frame.add(keysPanel, BorderLayout.CENTER);
		frame.add(octavePanel, BorderLayout.CENTER);
		
		
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setSize(550,200);
		
	}
	
	
	public SimpleSynth(final SourceDataLine line, final AudioFormat af){
	
		this.line = line;
		this.af = af;
		
		this.a = calculateNewParamValue(EnvelopeParameter.ATTACK, SCROLL_BAR_MAX - 1);
		this.d = calculateNewParamValue(EnvelopeParameter.DECAY, SCROLL_BAR_MAX - 1);
		this.s = calculateNewParamValue(EnvelopeParameter.SUSTAIN, SCROLL_BAR_MAX - 1);
		this.r = calculateNewParamValue(EnvelopeParameter.RELEASE, SCROLL_BAR_MAX - 1);
		
		this.maxAmplitude = 100;
		this.currentOctave = DEFAULT_OCTAVE;
		
		this.osc = new AdditiveOscillator(SAMPLE_RATE);
		this.osc.setIndivdualAmplitudes(0, 0, 0);
		
		createGUI();
		createKeys();
		createADSR();
		createOsc();
		createOctaveChoice();
	}
	

	public static void main(String[] args) {

		try {
			
			AudioFormat af = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
			final SourceDataLine line = AudioSystem.getSourceDataLine(af);
	
			SimpleSynth synth = new SimpleSynth(line, af);		
			
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
