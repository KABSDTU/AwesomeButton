import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

@SuppressWarnings("serial")
public class AwesomeButton {
	public final static String SOUNDFOLDER = "sounds/";
	public final static String SOUNDFILE = SOUNDFOLDER+"sounds.txt";
	private final static int PORT = 1990;

	private DatagramSocket socket;
	private boolean running = true;

	private Set<InetAddress> blocked;
	private boolean currentlyPlaying = true;
	private Object mutex = new Object();

	public Settings settings = Settings.loadSettings();
	private AwesomeButtonGUI GUI;
	
	public AwesomeButton() throws Exception {
		this.GUI = new AwesomeButtonGUI(this);

		if (!Lib.init(new FileInputStream(new File(SOUNDFILE))))
			GUI.println("Failed to initialize sounds.");
		this.blocked = Collections.synchronizedSet(new HashSet<InetAddress>());

		(new FileRequestServer()).start();
		start();
	}

	private void start() throws Exception {
		byte[] in = new byte[1024];
		GUI.println("Listening on "+InetAddress.getLocalHost()+":"+PORT);

		DatagramPacket p = new DatagramPacket(in, in.length);

		while (running && socket != null) {
			try {
				socket.receive(p);
			} catch (Exception e) {	continue; }


			handleInput(p);
		}

		socket.close();
	}

	private void handleInput(DatagramPacket p) throws Exception {
		String m = new String(p.getData(), 0, p.getLength());

		if (m.startsWith("delay ")) { // Change delay
			String[] parts = m.split(" ");
			if (parts.length > 0) {
				int delay = Integer.parseInt(parts[1]);
				if (delay >= 0) {
					GUI.println("Set blocking time to "+delay);
					settings.setBlockDelay(delay);
				}
			}
		
		} else if (m.startsWith("min ")) { // Change delay
			String[] parts = m.split(" ");
			if (parts.length > 0) {
				int minVol = Integer.parseInt(parts[1]);
				if (minVol >= 0 && minVol <= 255) {
					GUI.println("Set min volume to "+minVol);
					settings.setMinVol(minVol);
				}
			}
		} else if (m.startsWith("max ")) { // Change delay
			String[] parts = m.split(" ");
			if (parts.length > 0) {
				int maxVol = Integer.parseInt(parts[1]);
				if (maxVol >= 0 && maxVol <= 255) {
					GUI.println("Set max volume to "+maxVol);
					settings.setMaxVol(maxVol);
				}
			}
		} else if (m.equals("abort")) { // Stop the program
			running = false;
		} else {
		
			GUI.println("Received \""+m+"\" from "+p.getAddress().toString());
			if (Lib.SOUNDS.containsKey(m)) { // Play a sound
				requestSound(p.getAddress(), Lib.SOUNDS.get(m));
			}
		}
	}

	private void requestSound(InetAddress ip, Sound sound) throws Exception {
		if (!blocked.contains(ip) && playSound(sound)) {
			block(ip);
		}
	}

	private boolean playSound(Sound sound) throws Exception {
		// Check if the audio file exists
		File audioFile = getAudioFile(sound);
		if (!audioFile.exists()) {
			GUI.println("Sound not found: "+sound.filename);
			return false;
		}

		// Prepare and play the sound
		prepareSound();
		AudioInputStream as = AudioSystem.getAudioInputStream(audioFile);
		final Clip clip = AudioSystem.getClip();
		clip.open(as);
		clip.start();

		// Set up listener for when song ends
		clip.addLineListener(new LineListener() {
			@Override
			public void update(LineEvent myLineEvent) {
				if (myLineEvent.getType() == LineEvent.Type.STOP) {
					clip.close();
					endSound();
				}
			}
		});
		return true;
	}

	public synchronized void block(InetAddress ip) {
		this.blocked.add(ip);
		(new Blocker(this, ip, settings.getBlockDelay())).start();
	}

	public synchronized void unblock(InetAddress ip) {
		this.blocked.remove(ip);
	}

	private void prepareSound() {
		synchronized (mutex) {
			currentlyPlaying = true;
			if (settings.hasSj() && currentlyPlaying) {
				setSjVol(settings.getMinVol());
			}
		}
	}

	private void endSound() {
		synchronized (mutex) {
			currentlyPlaying = false;
			if (settings.hasSj() && !currentlyPlaying) {
				try {
					setSjVol(settings.getMaxVol());				
				} catch (Exception e) {	}
			}
		}
	}
	
	private void setSjVol(int vol) {
		if (vol < 0 || vol > 255) return;
		String command = String.format(
				"--execute=\"player.volume=%d\"", 
				vol);
		String[] args = { settings.getSjPath(), command };
		try {
			Runtime.getRuntime().exec(args);
		} catch (Exception e) {	}
	}

	private File getAudioFile(Sound sound) {
		return new File(SOUNDFOLDER+sound.filename);
	}
	
		public void shutdown() {
		running = false;
		this.socket.close();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {	}
		System.exit(0);
	}
	
	public void removeSj() {
		settings.setSjPath(null);
	}
	
	public void setSjPath(String path) {
		settings.setSjPath(path);
	}

	public static void main(String[] args) throws Exception {
		new AwesomeButton();
	}
}
