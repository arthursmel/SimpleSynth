
/**
Enum that represents the parameters (attack, decay, sustain, release)
and their minimum and maximum values
**/

public enum EnvelopeParameter {
	
	ATTACK (0.0, 10.0), // Time (Seconds)
	DECAY (0.0, 10.0), // Time (Seconds)
	SUSTAIN (0.0, 100.0),// Relative amplitude
	RELEASE (0.0, 10.0); // Time (Seconds)
	
	private final double maxValue;
	private final double minValue;
	
	/**
	Constructor for a parameter 
	@param minValue Minimum value that the parameter is allowed have
	@param maxValue Maximum value that the parameter is allowed have
	**/
	EnvelopeParameter(double minValue, double maxValue) {
		this.maxValue = maxValue;
		this.minValue = minValue;
	}
	
	// Getters
	
	/**
	@return The maximum value that the parameter is allowed have
	**/
	public double getMaxValue() {
		return this.maxValue;
	}
	
	/**
	@return The minimum value that the parameter is allowed have
	**/
	public double getMinValue() {
		return this.minValue;
	}
	
	
}
