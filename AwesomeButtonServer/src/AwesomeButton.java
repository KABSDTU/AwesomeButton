import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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

	private boolean currentlyPlaying = true;
	private Object mutex = new Object();

	private Blocker blocker = new Blocker(Settings.STANDARD_DELAY);
	public Settings settings = Settings.loadSettings();
	private AwesomeButtonGUI GUI;
	
	public AwesomeButton() throws Exception {
		this.GUI = new AwesomeButtonGUI(this);

		if (!Lib.init(new FileInputStream(new File(SOUNDFILE))))
			GUI.println("Failed to initialize sounds.");

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
			} catch (Exception e) { continue; }


			handleInput(p);
		}

		socket.close();
	}

	private void handleInput(DatagramPacket p) throws Exception {
		String msg = new String(p.getData(), 0, p.getLength());
		
		String command = msg.split(" ")[0];
		String params = msg.substring(command.length()).trim();
		
		/**
		  Commands to send to AwesomeButton:
			- delay <milisec>
			- abort
			- play / pause
			- song <song name>
		**/
		
		switch (command) {
			case "delay":
				setDelay( Integer.parseInt(params) );
				break;
			
			case "abort":
				this.running = false;
				break;
			
			case "play":
			case "pause":
				startStopSj(command);
				break;
			
			case "song":
				playSong(p.getAddress(), params);
				break;
			
			default: // backwards compatibility
				playSong(p.getAddress(), command);
				break;
		}
	}
	
	private void playSong(InetAddress ip, String soundString) {
		GUI.println("Received \""+soundString+"\" from "+ip.toString());
		try {
			requestSound(ip, soundString);
		} catch (Exception e) {
			GUI.println("Error while playing song: " + soundString + "\nError: " + e.getMessage());
		}
	}

	private void requestSound(InetAddress ip, String soundString) throws Exception {
		if (blocker.checkAndBlock(ip)) {
			if (!playSound(soundString))
				GUI.println("Could not play song: " + soundString + ".");
		} else {
			GUI.println("Could not play song: " + soundString + ". IP is blocked.");
		}
	}

	private boolean playSound(String soundString) throws Exception {
		Sound sound = null;
		if (Lib.SOUNDS.containsKey(soundString)) {
			sound = Lib.SOUNDS.get(soundString);
		} else {
			return false;
		}
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

	private void prepareSound() {
		synchronized (mutex) {
			currentlyPlaying = true;
			if (settings.hasSj()) {
				startStopSj("pause");
			}
		}
	}

	private void endSound() {
		synchronized (mutex) {
			currentlyPlaying = false;
			if ( settings.hasSj() ) {
				try {
					startStopSj("play");
				} catch (Exception e) {	}
			}
		}
	}
	
	private void startStopSj(String startStop) {
		String command = String.format("--%s", startStop);
		String[] args = { settings.getSjPath(), command };
		try {
			Runtime.getRuntime().exec(args);
		} catch (Exception e) {
			GUI.println("Could not " + startStop + " SilverJuke. Error: " + e.getMessage());
		}
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
	
	private void setDelay(int delay) {
		if (delay >= 0) {
			GUI.println("Set blocking time to " + delay);
			settings.setBlockDelay(delay);
		} else {
			GUI.println("Recieved an illigal delay: " + delay);
		}
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
