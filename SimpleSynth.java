import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SimpleSynth {

	// Contants
	private static final int SAMPLE_RATE = 44100;
	private static final double DEFAULT_MAX_AMPLITUDE = 50;
	private static final int DEFAULT_OCTAVE = 0;
	private static final int OCTAVE_COUNT = 10;
	
	// Amplitude constants
	private static final double DEFAULT_SQUARE_AMPLITUDE = 0;
	private static final double DEFAULT_SINE_AMPLITUDE = 0;
	private static final double DEFAULT_SAWTOOTH_AMPLITUDE = 0;
	
	private static final String TITLE = "SimpleSynth";
	
	// GUI	dimensions 
	private static final int ROW_COUNT = 2;
	private static final int COLUMN_COUNT = 2;
	private static final int DEFAULT_H_GAP = 1;
	private static final int DEFAULT_V_GAP = 1;
	private static final int DEFAULT_ROW_COUNT = 1;
	private static final int DEFAULT_COLUMN_COUNT = 1;
	private static final int WINDOW_WIDTH = 550;
	private static final int WINDOW_HEIGHT = 200;
	
	private static final int SCROLL_BAR_MAX = 99; 
	private static final int SCROLL_BAR_MIN = 0;
	// The height of the adjuster within the scroll bar
	private static final int SCROLL_BAR_VISIBLE = 1; 

	// JPanels for each GUI element
	private JPanel keysPanel;
	private JPanel ADSRPanel;
	private JPanel oscPanel;
	private JPanel octavePanel;
	
	// Envelope values
	private double a; // attack time 
	private double d; // decay time
	private double s; // sustain amplitude
	private double r; // release time
	
	// Amplitude values
	private double maxAmplitude; // Current amplitude
	private double sineAmplitude;
	private double sawtoothAmplitude;
	private double squareAmplitude;
	
	private int currentOctave;
	private AdditiveOscillator osc; 
	
	private SynthNote currentNote; // Current note playing 
	private final SourceDataLine line;
	private final AudioFormat af;
	
	/**
	Creates a thread which creates the bytes which need to be written to the 
	speaker. The bytes are created by specified oscillator, with the specified parameters.
	The bytes are outputted to the buffer, when the thread is run, and the onFull callback function is
	called each time the buffer is full. When the note has finished, the remaining 
	bytes left in the buffer are outputted to the speaker
	@param freq Is the frequency the oscillator will be at 
	@return Returns the thread in order to start the note playing
	**/
	public SynthNote createSynthNote(double freq) {
		
		// Creating synth note with current oscillator, frequency, max amplitude,
		// attack time, decay time, sustain amplitude, releases time
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
	
	/**
	Starts a SynthNote thread in order to play the note once the user has clicked the 
	key.
	@param note The note thread which needs to be run
	@throws LineUnavailableException
	**/
	public void playSynthNote(SynthNote note) throws LineUnavailableException{
		
		// If the is currently a note playing, cancel the note to stop it playing
		if (currentNote != null) currentNote.cancel();

		// Creating reference to new note in case it needs to be cancelled in the future
    	currentNote = note;
    	
    	// Removing the bytes from the previous note from the line
    	if (line.isOpen()) {
	    	line.stop();
	    	line.flush();
	    	line.close();
    	}
    	
		line.open(af);
	    line.start();
	    line.drain();
	    
	    // Starting the new note thread
	    currentNote.start();
	}
	

	/**
	Creates a listener for the key which plays the synth note if the button has been pressed
	by the user, and releases the synth note if the button has been released by the user.
	@param note The musical note the key plays.
	@return The listener which reacts to user events that should be associated with 
	the button of the musical note specified.
	**/
	MouseListener createKeyPressListener(final Note note) {
		
		MouseListener keyPressListener = new MouseListener() {

		    @Override
		    public void mousePressed(MouseEvent e) {
		    	// When the user has pressed the button
		    	// Create the synth note with frequency associated with the button
		    	// relative to the current octave, and start playing the synth note
		    	try { playSynthNote(
		    			createSynthNote(
		    				note.getFreq(note, currentOctave)	
		    			)
		    		);
		    	} catch (LineUnavailableException error) {
		    		System.err.println(error);
		    	}
		    }

			@Override
			public void mouseReleased(MouseEvent e) {
				// When the user has released the key
				currentNote.release();
			}

			@Override public void mouseClicked(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
		};
		
		return keyPressListener;
		
	}
	
	
	/**
	Calculates the new parameter value based on the current value of the 
	scroll bar associated with the parameter.
	@param param The parameter to be changed.
	@param newValue The current value of the scroll bar.
	@return The relative value from the scrollbar, the new value the 
	envelope parameter should be.
	**/
	public static double calculateNewParamValue(EnvelopeParameter param, int newValue) {
		// The value of the scrollbar (top = 0, bottom = 99) relative to the maximum value of the parameter 
		// The value is inverted as the scroll bar min value is at the top, max value at bottom, however
		// This is usually inverted on synths
		return param.getMaxValue()
		- (((double) newValue / (double) SCROLL_BAR_MAX)
		* param.getMaxValue());	
	}
	
	
	/**
	Creates the gui elements for the keys panel. Used to represent a piano keyboard
	for the synth. Each key is given a listener which creates a note from the assigned frequency.
	The keys cover 1 octave.
	**/
	public void createKeys() {
		
		// A temp value for the number of musical notes that need to be represented
		int noteCount = Note.values().length;
		// An array of buttons to store each key
		JButton[] keys = new JButton[noteCount];
		
		// For each note value
		for (Note note : Note.values()) {
			// Decrement number of notes needed to be represented
			// And add button for key to array
			keys[--noteCount] = new JButton();
			keys[noteCount].setPreferredSize(new Dimension(40, 40));
			
			// If the note is sharp, there will be a # on the button
			keys[noteCount].setText(note.isSharp() ? "#" : "");

			// Creating and adding listener to respond to user's actions
			keys[noteCount].addMouseListener(createKeyPressListener(note));
			
			// Adding key to panel
			keysPanel.add(keys[noteCount]);
		}
		
	}
	

	/**
	Creates a new scroll bar gui element which will adjust the assigned parameter
	value.
	@param param The parameter of the envelope which should be adjusted i.e a/d/s/r
	@return A scrollbar element for the envelope parameter with an adjustment
	listener to adjust the parameter's value
	**/
	public Scrollbar createNewParameterScrollbar(EnvelopeParameter param) {
		
		Scrollbar sb = new Scrollbar(Scrollbar.VERTICAL);
		sb.addAdjustmentListener(
				// Creating & adding listener for parameter
				createEnvelopeAdjustmentListener(sb, param) 
		);
		sb.setValues(SCROLL_BAR_MAX, 1, SCROLL_BAR_MIN, SCROLL_BAR_MAX);
		return sb;
		
	}
	
	
	/**
	Creates an adjustment listener for a scroll bar with an associated envelope parameter and 
	changes the corresponding envelope parameter on the synth to the value specified by the user
	@param scrollBar The scroll bar that the adjustment listener should be assigned to listen to.
	@param param The envelope parameter which should be changed on adjustment.
	@return An adjustment listener which changes the envelope parameter value to the value specified.
	**/
	AdjustmentListener createEnvelopeAdjustmentListener(final Scrollbar scrollBar, final EnvelopeParameter param) {

		AdjustmentListener listener = new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// The user has adjusted the value of the scroll bar
				// Getting the relative value from the scrollbar
				double newValue = calculateNewParamValue(param, scrollBar.getValue());

				// Deciding which param should be changed 
				switch(param) {
				
				case ATTACK:
					a = newValue; // Updating attack value
					break;
				case DECAY:
					d = newValue; // Updating decay value
					break;
				case SUSTAIN:
 					s = newValue; // Updating sustain value
					break;
				case RELEASE:
					r = newValue; // Updating release value
				}
			}
		};
		
		return listener;
	}
	
	
	/**
	Creating the adsr envelope panel for the GUI which includes scrollbars to 
	adjust the values of attack/decay/sustain/release. The scollbars are then
	added to the adsr panel.
	**/
	public void createADSR() {
	
		ADSRPanel.add(createNewParameterScrollbar(EnvelopeParameter.ATTACK)); 
		ADSRPanel.add(createNewParameterScrollbar(EnvelopeParameter.DECAY));
		ADSRPanel.add(createNewParameterScrollbar(EnvelopeParameter.SUSTAIN));
		ADSRPanel.add(createNewParameterScrollbar(EnvelopeParameter.RELEASE));
		
	}
	
	
	/**
	Creates the oscillator panel for the GUI which includes scrollbars to adjust
	the amplitude for the sine/square/sawtooth waves. The scrollbars are then added to
	the oscillator panel. 
	**/
	public void createOsc() {
		
		// Creating sine amplitude scroll bar
		final Scrollbar sineAmp = new Scrollbar(Scrollbar.VERTICAL);
		sineAmp.setValues(SCROLL_BAR_MAX, SCROLL_BAR_VISIBLE, SCROLL_BAR_MIN, SCROLL_BAR_MAX);
		sineAmp.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// When the scroll bar has been adjusted, invert the value and set it as
				// the new sine amplitude
				sineAmplitude = SCROLL_BAR_MAX - sineAmp.getValue();
				osc.setIndivdualAmplitudes(squareAmplitude, sawtoothAmplitude, sineAmplitude);
			}
		});
		oscPanel.add(sineAmp);
		
		// Creating sawtooth amplitude scroll bar
		final Scrollbar sawAmp = new Scrollbar(Scrollbar.VERTICAL);
		sawAmp.setValues(SCROLL_BAR_MAX, SCROLL_BAR_VISIBLE, SCROLL_BAR_MIN, SCROLL_BAR_MAX);
		sawAmp.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// When the scroll bar has been adjusted, invert the value and set it as
				// the new sawtooth amplitude
				sawtoothAmplitude = SCROLL_BAR_MAX - sawAmp.getValue();
				osc.setIndivdualAmplitudes(squareAmplitude, sawtoothAmplitude, sineAmplitude);
			}
		});
		oscPanel.add(sawAmp);
		
		// Creating square amplitude scroll bar
		final Scrollbar squareAmp = new Scrollbar(Scrollbar.VERTICAL);
		squareAmp.setValues(SCROLL_BAR_MAX, SCROLL_BAR_VISIBLE, SCROLL_BAR_MIN, SCROLL_BAR_MAX);
		squareAmp.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// When the scroll bar has been adjusted, invert the value and set it as
				// the new square amplitude
				squareAmplitude = SCROLL_BAR_MAX - squareAmp.getValue();
				osc.setIndivdualAmplitudes(squareAmplitude, sawtoothAmplitude, sineAmplitude);
			}
		});
		oscPanel.add(squareAmp);
		
	}
	
	
	/**
	Creates a dropdown list for selecting the current octave of the synth
	The default octave is 0, user has the choice of octaves between 
	0 and Octave count (10) - 1. The dropdown is added to the octaveChoice panel
	**/
	public void createOctaveChoice() {
		
		// Creating a new "choice" element (dropdown selection)
		final Choice octaveChoice = new Choice();
		
		// Adding the options 0 to OCTAVE_COUNT - 1 to the choice
		for (int o = 0; o < OCTAVE_COUNT; o++) {
			octaveChoice.add(String.valueOf(o));
		}
		
		// Creating a listener for when a new choice is selected
		octaveChoice.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// When item selected, the octave of the synth is changed
				// to the index of the choice value 
				currentOctave = Integer.valueOf(octaveChoice.getSelectedItem());
			}
			
		});
		
		octavePanel.add(octaveChoice);
		
	}
	
	
	/**
	Creates the main GUI and panels
	**/
	public void createGUI() {
		
		JFrame frame = new JFrame(TITLE); 
		frame.setLayout(new GridLayout(ROW_COUNT, COLUMN_COUNT, DEFAULT_H_GAP, DEFAULT_V_GAP));
		
		GridLayout defaultLayout = new GridLayout(DEFAULT_ROW_COUNT, DEFAULT_COLUMN_COUNT,
				DEFAULT_H_GAP, DEFAULT_V_GAP);
		
		// Creating serperate panels for each GUI element
		keysPanel = new JPanel();
		keysPanel.setLayout(defaultLayout);
		
		oscPanel = new JPanel();
		oscPanel.setLayout(defaultLayout);
		
		ADSRPanel = new JPanel();
		ADSRPanel.setLayout(defaultLayout);
		
		octavePanel = new JPanel();
		octavePanel.setLayout(defaultLayout);
		
		frame.add(oscPanel, BorderLayout.CENTER);
		frame.add(ADSRPanel, BorderLayout.CENTER);
		frame.add(keysPanel, BorderLayout.CENTER);
		frame.add(octavePanel, BorderLayout.CENTER);
		
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
	
	}
	
	
	/**
	Constructor which initialises instance variables and creates GUI
	@param line output line
	@param af audio format
	**/
	public SimpleSynth(final SourceDataLine line, final AudioFormat af){
	
		this.line = line;
		this.af = af;
		this.currentNote = null;
		
		// Creating adsr envelopes
		this.a = calculateNewParamValue(EnvelopeParameter.ATTACK, SCROLL_BAR_MAX - 1);
		this.d = calculateNewParamValue(EnvelopeParameter.DECAY, SCROLL_BAR_MAX - 1);
		this.s = calculateNewParamValue(EnvelopeParameter.SUSTAIN, SCROLL_BAR_MAX - 1);
		this.r = calculateNewParamValue(EnvelopeParameter.RELEASE, SCROLL_BAR_MAX - 1);
		
		this.maxAmplitude = DEFAULT_MAX_AMPLITUDE;
		this.currentOctave = DEFAULT_OCTAVE;
		
		// Setting up oscillator
		this.osc = new AdditiveOscillator(SAMPLE_RATE);
		this.osc.setIndivdualAmplitudes(DEFAULT_SQUARE_AMPLITUDE,
				DEFAULT_SAWTOOTH_AMPLITUDE, DEFAULT_SINE_AMPLITUDE);
		
		// Creating GUI elements
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
			e.printStackTrace();
		}

	}

}
