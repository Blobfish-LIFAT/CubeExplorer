package it.unibo.csr.big.cubeload.schema;

/**
 * This method implements a cube's measure.
 * @author Luca Spadazzi
 * 
 */
public class Measure
{
	private String name;
	
	/**
	 * Class constructor.
	 * @param name The name of the measure.
	 */
	public Measure (String name)
	{
		this.name = name;
	}
	
	/**
	 * Getter method for the measure's name.
	 * @return The name of the measure.
	 */
	public String getName()
	{
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Measure)) {
			return false;
		}
		
		Measure other = (Measure) obj;
		
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		
		return true;
	}
}
