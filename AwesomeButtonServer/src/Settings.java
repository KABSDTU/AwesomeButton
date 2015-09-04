import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class Settings implements Serializable {
	private static final long serialVersionUID = -1566695152415046220L;
	private static final String SETTINGSFILE = "settings.cfg";
	public static final int STANDARD_DELAY = 30000;
	
	private String SjPath = null;
	private int blockDelay = STANDARD_DELAY;
	private int minVol = 50;
	private int maxVol = 255;

	public static Settings loadSettings() {
		File file = new File(SETTINGSFILE);
		if (!file.exists()) return new Settings();

		try {
			FileInputStream is = new FileInputStream(file);
			ObjectInputStream restore = new ObjectInputStream(is);
			
			Object obj = restore.readObject();
			
			is.close();
			restore.close();
			if (obj != null && obj instanceof Settings) {				
				return (Settings) obj;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new Settings();
	}
	
	private void saveSettings() {
		File file = new File(SETTINGSFILE);
		try {
			file.createNewFile();
			FileOutputStream fo = new FileOutputStream(file);
			ObjectOutputStream save = new ObjectOutputStream(fo);
			
			save.writeObject(this);
			
			fo.close();
			save.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setSjPath(String sjPath) {
		this.SjPath = sjPath;
		saveSettings();
	}

	public void setBlockDelay(int blockDelay) {
		this.blockDelay = blockDelay;
		saveSettings();
	}
	
	public void setMaxVol(int maxVol) {
		this.maxVol = maxVol;
		saveSettings();
	}
	
	public void setMinVol(int minVol) {
		this.minVol = minVol;
		saveSettings();
	}
	
	public int getBlockDelay() {
		return blockDelay;
	}
	
	public String getSjPath() {
		return SjPath;
	}
	
	public int getMinVol() {
		return minVol;
	}
	
	public int getMaxVol() {
		return maxVol;
	}
	
	public boolean hasSj() {
		return SjPath != null;
	}
}
