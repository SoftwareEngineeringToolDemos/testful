package testful.coverage.whiteBox;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import testful.coverage.CoverageInformation;

public class CoverageDefExp implements CoverageInformation {
	private static final long serialVersionUID = 456863110951663193L;

	public static String KEY = "de";
	public static String NAME = "Def-Exposition";

	@Override
	public String getKey() { return KEY; }

	@Override
	public String getName() { return NAME; }

	private final Map<Stack, Set<DataAccess>> defExpo;
	private int quality;

	public CoverageDefExp(Map<Stack, Set<DataAccess>> defExpo) {
		this.defExpo = defExpo;

		updateQuality();
	}

	private void updateQuality() {
		quality = 0;
		for (Set<DataAccess> e : defExpo.values())
			quality += e.size();
	}

	@Override
	public boolean contains(CoverageInformation other) {
		if(!(other instanceof CoverageDefExp)) return false;
		final CoverageDefExp o = (CoverageDefExp) other;

		if(quality < o.quality) return false;

		for (Stack deKey : o.defExpo.keySet()) {
			Set<DataAccess> tde = defExpo.get(deKey);
			if(tde == null) return false;

			Set<DataAccess> ode = o.defExpo.get(deKey);
			if(!tde.containsAll(ode)) return false;
		}

		return true;
	}

	@Override
	public CoverageInformation createEmpty() {
		return new CoverageDefExp(new LinkedHashMap<Stack, Set<DataAccess>>());
	}

	@Override
	public float getQuality() {
		return quality;
	}

	@Override
	public CoverageDefExp clone() {
		return new CoverageDefExp(defExpo);
	}

	@Override
	public void merge(CoverageInformation other) {
		if(other instanceof CoverageDefExp) {
			for (Entry<Stack, Set<DataAccess>> e : ((CoverageDefExp) other).defExpo.entrySet()) {
				Set<DataAccess> de = defExpo.get(e.getKey());
				if(de == null) {
					de = new LinkedHashSet<DataAccess>();
					defExpo.put(e.getKey(), de);
				}
				de.addAll(e.getValue());
			}
			updateQuality();
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Entry<Stack, Set<DataAccess>> e : defExpo.entrySet()) {
			sb.append("stack:").append(e.getKey().toString()).append("\ndefs:").append(e.getValue().toString()).append("\n");
		}

		return sb.toString();
	}
}
