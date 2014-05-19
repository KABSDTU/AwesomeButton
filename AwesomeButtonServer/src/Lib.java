
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;


public class Lib {
	public static Map<String,Sound> SOUNDS = new TreeMap<String, Sound>();
	
	public static boolean init(InputStream stream) {
		if (stream == null) return false;
		
		SOUNDS = new TreeMap<String, Sound>();
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
	
	private static void putSound(String id, String filename) {
		SOUNDS.put(id, new Sound(id, filename+".wav", filename));
	}
}
