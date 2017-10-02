
public enum EnvelopeParameter {
	
	ATTACK (0.0, 10.0),
	DECAY (0.0, 10.0),
	SUSTAIN (0.0, 100.0),
	RELEASE (0.0, 10.0);
	
	private final double maxValue;
	private final double minValue;
	
	EnvelopeParameter(double minValue, double maxValue) {
		this.maxValue = maxValue;
		this.minValue = minValue;
	}
	
	public double maxValue() {
		return this.maxValue;
	}
	
	public double minValue() {
		return this.minValue;
	}
	
	
}
