package sowbreira.f1mane.entidades;

public class Pausa {
	private long pausaIniMilis;
	private long pausaFimMilis;

	public long getPausaIniMilis() {
		return pausaIniMilis;
	}

	public void setPausaIniMilis(long pausaIniMilis) {
		this.pausaIniMilis = pausaIniMilis;
	}

	public long getPausaFimMilis() {
		return pausaFimMilis;
	}

	public void setPausaFimMilis(long pausaFimMilis) {
		this.pausaFimMilis = pausaFimMilis;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (pausaFimMilis ^ (pausaFimMilis >>> 32));
		result = prime * result
				+ (int) (pausaIniMilis ^ (pausaIniMilis >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pausa other = (Pausa) obj;
		if (pausaFimMilis != other.pausaFimMilis)
			return false;
		if (pausaIniMilis != other.pausaIniMilis)
			return false;
		return true;
	}

}
