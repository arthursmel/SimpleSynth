
public enum Note {

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
	
	Note(double freq, boolean isSharp) {
		this.freq = freq;
		this.isSharp = isSharp;
	}
	
	boolean isSharp() {
		return this.isSharp;
	}
	
	double getFreq(Note note, int octave) {
		return note.freq * (Math.pow(2, octave));
	}

}
