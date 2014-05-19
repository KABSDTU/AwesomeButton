package mktange.awesomebutton;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class MobileLib {
	public final static String SOUNDFILE = "sounds.dat";
	
	private final static String DEFAULT_IP = "192.168.0.1";
	private final static String FAVPREF = "saved_favorites";
	private final static String IPPREF = "connect_to";
	
	
	public static Map<String,Sound> SOUNDS = new TreeMap<String, Sound>();
	public static Set<Sound> FAVORITES = new TreeSet<Sound>();
	public static String SERVERIP = DEFAULT_IP;
	
	private static SharedPreferences prefs;

	public static void init(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SERVERIP = prefs.getString(IPPREF, DEFAULT_IP);
		try {
			initSounds(context.openFileInput(SOUNDFILE));
			initFavorites();
		} catch (Exception e) { }
	}

	private static boolean initSounds(InputStream stream) {
		SOUNDS = new TreeMap<String, Sound>();
		if (stream == null) return false;

		BufferedReader br = null; 
		try {
			br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

			String line;
			String[] parts;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("//")) continue;

				parts = line.split("//");
				if (parts.length == 2)
					putSound(parts[0], parts[1]);
			}

			br.close();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void initFavorites() {
		FAVORITES = new TreeSet<Sound>();
		for (String s : prefs.getString(FAVPREF, null).split("//")) {
			FAVORITES.add(SOUNDS.get(s));
		}
	}

	private static void putSound(String id, String desc) {
		SOUNDS.put(id, new Sound(id, desc));
	}

	public static void clearData(Context context) {
		MobileLib.SOUNDS = new TreeMap<String, Sound>();
		try {
			context.openFileOutput(MobileLib.SOUNDFILE, Context.MODE_PRIVATE).close();
		} catch (Exception e) {	}
	}

	public static void saveFavorites() {
		prefs.edit().putString(FAVPREF, implodeFavs(FAVORITES)).commit();
	}
	
	public static void setIp(String ip) {
		SERVERIP = ip;
		prefs.edit().putString(IPPREF, SERVERIP).commit();
	}
	
	public static String implodeFavs(Collection<Sound> sounds) {
		if (sounds.isEmpty()) return null;
		
		String result = "";
		for (Sound s : sounds) {
			result = result+"//"+s.id;
		}
		
		return result.substring(2);
	}
}
