import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;


@SuppressWarnings("serial")
public class AwesomeButton extends JFrame {
	public final static String SOUNDFOLDER = "sounds/";
	public final static String SOUNDFILE = SOUNDFOLDER+"sounds.txt";
	private final static int PORT = 1990;

	private DatagramSocket socket;
	private boolean running = true;
	private StyledDocument doc;
	private JTextPane textPane;
	private final JFileChooser fc = new JFileChooser();

	private Set<InetAddress> blocked;
	private int currentlyPlaying = 0;
	private Object mutex = new Object();

	private Settings settings = Settings.loadSettings();

	public AwesomeButton() throws Exception {
		super("AwesomeButton");
		setupGUI();

		if (!Lib.init(new FileInputStream(new File(SOUNDFILE))))
			println("Failed to initialize sounds.");
		this.blocked = Collections.synchronizedSet(new HashSet<InetAddress>());

		(new FileRequestServer()).start();
		start();
	}

	private void start() throws Exception {
		byte[] in = new byte[1024];
		println("Listening on "+InetAddress.getLocalHost()+":"+PORT);

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
					println("Set blocking time to "+delay);
					settings.setBlockDelay(delay);
				}
			}
		
		} else if (m.startsWith("min ")) { // Change delay
			String[] parts = m.split(" ");
			if (parts.length > 0) {
				int minVol = Integer.parseInt(parts[1]);
				if (minVol >= 0 && minVol <= 255) {
					println("Set min volume to "+minVol);
					settings.setMinVol(minVol);
				}
			}
		} else if (m.startsWith("max ")) { // Change delay
			String[] parts = m.split(" ");
			if (parts.length > 0) {
				int maxVol = Integer.parseInt(parts[1]);
				if (maxVol >= 0 && maxVol <= 255) {
					println("Set max volume to "+maxVol);
					settings.setMaxVol(maxVol);
				}
			}
		} else if (m.equals("abort")) { // Stop the program
			running = false;
		} else {
		
			println("Received \""+m+"\" from "+p.getAddress().toString());
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
			println("Sound not found: "+sound.filename);
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
			currentlyPlaying++;
			if (settings.hasSj() && currentlyPlaying == 1) {
				setSjVol(settings.getMinVol());
			}
		}
	}

	private void endSound() {
		synchronized (mutex) {
			currentlyPlaying--;
			if (settings.hasSj() && currentlyPlaying == 0) {
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



	/**
	 * GUI setup
	 * @throws Exception
	 */
	private void setupGUI() throws Exception {
		this.socket = new DatagramSocket(PORT);

		JPanel panel = new JPanel(new BorderLayout());
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				running = false;
				socket.close();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {	}
				System.exit(0);
			}
		});
		
		
		final JButton removeSjButton = new JButton("Remove SilverJuke");
		removeSjButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				settings.setSjPath(null);
				removeSjButton.setEnabled(false);
			}
		});
		if (!settings.hasSj()) removeSjButton.setEnabled(false);

		JButton sjButton = new JButton("Find Silverjuke");
		sjButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int res = fc.showOpenDialog(AwesomeButton.this);

				if (res == JFileChooser.APPROVE_OPTION) {
					String path = fc.getSelectedFile().getAbsolutePath();
					if (!path.endsWith("Silverjuke.exe")) {
						JOptionPane.showMessageDialog(AwesomeButton.this, 
								"Invalid file chosen.\nPlease find 'Silverjuke.exe'.");
					} else {
						settings.setSjPath(path);
						removeSjButton.setEnabled(true);
						println("Saved the path to SilverJuke!");
					}
				}
			}
		});
		
		JPanel bottom = new JPanel();
		bottom.setLayout(new GridLayout(2, 1));
		JPanel silverjuke = new JPanel(new GridLayout(1, 2));
		silverjuke.add(sjButton);
		silverjuke.add(removeSjButton);
		bottom.add(silverjuke);
		bottom.add(close);
		
		this.textPane = new JTextPane();
		this.doc = textPane.getStyledDocument();
		JScrollPane scrollPane = new JScrollPane(this.textPane);

		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(bottom, BorderLayout.SOUTH);
		this.setPreferredSize(new Dimension(400,200));
		this.add(panel);
		this.setVisible(true);
		this.pack();
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		if (settings.hasSj()) {
			File file = new File(settings.getSjPath());
			if (file.exists()) {
				fc.setSelectedFile(file);
			}
		}
		fc.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Silverjuke.exe";
			}

			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().endsWith("Silverjuke.exe");
			}
		});
	}


	private synchronized void println(String message) {
		try {
			doc.insertString(doc.getLength(), message+"\n", null);
			textPane.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {}
	}


	public static void main(String[] args) throws Exception {
		new AwesomeButton();
	}
}
