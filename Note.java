
/**
Enum that represents musical notes and their frequencies
**/

public enum Note {

	// Notes and their frequencies 
	C(16.351, false),
	C_sharp(17.324, true),
	D(18.354, false),
	D_sharp(19.445, true),
	E(20.601, false),
	F(21.827, false),
	F_sharp(23.124, true),
	G(24.499, false),
	G_sharp(25.956, true),
	A(27.5, false),
	A_sharp(29.135, true),
	B(30.868, false);
	
	private final double freq;
	private final boolean isSharp;
	
	
	/**
	Constructor 
	@param freq frequency of the note
	@param isSharp true if the note is X#
	**/
	Note(double freq, boolean isSharp) {
		this.freq = freq;
		this.isSharp = isSharp;
	}
	
	/**
	@return true if the note is X#
	**/
	boolean isSharp() {
		return this.isSharp;
	}
	
	
	/**
	Returns the frequency of the note relative to the current octave
	@param note the note we want the frequency of
	@param octave the current octave of the synth
	@return the notes frequency relative to the current octave
	**/
	double getFreq(Note note, int octave) {
		return note.freq * (Math.pow(2, octave));
	}
}