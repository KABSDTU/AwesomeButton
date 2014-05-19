package mktange.awesomebutton;

public class Sound implements Comparable<Sound> {
	final String id;
	final String desc;

	public Sound(String id, String desc) {
		this.id = id;
		this.desc = desc;
	}

	@Override
	public int compareTo(Sound other) {
		return desc.compareTo(other.desc);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Sound)) return false;
		Sound s = (Sound) o;
		return id.equals(s.id) && desc.equals(s.desc);
	}

}