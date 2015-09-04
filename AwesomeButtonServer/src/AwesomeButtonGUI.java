import java.io.File;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

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
public class AwesomeButtonGUI extends JFrame {
	private StyledDocument doc;
	private JTextPane textPane;
	private final JFileChooser fc = new JFileChooser();
	
	private AwesomeButton main;
	
	public AwesomeButtonGUI(AwesomeButton main) throws Exception {
		super("AwesomeButton");
		this.main = main;
		setupGUI();
	}
	
	private void setupGUI() throws Exception {
		JPanel panel = new JPanel(new BorderLayout());
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				main.shutdown();
			}
		});
		
		
		final JButton removeSjButton = new JButton("Remove SilverJuke");
		removeSjButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				main.removeSj();
				removeSjButton.setEnabled(false);
			}
		});
		if (!main.settings.hasSj()) removeSjButton.setEnabled(false);

		JButton sjButton = new JButton("Find Silverjuke");
		sjButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int res = fc.showOpenDialog(AwesomeButtonGUI.this);

				if (res == JFileChooser.APPROVE_OPTION) {
					String path = fc.getSelectedFile().getAbsolutePath();
					if (!path.endsWith("Silverjuke.exe")) {
						JOptionPane.showMessageDialog(AwesomeButtonGUI.this, 
								"Invalid file chosen.\nPlease find 'Silverjuke.exe'.");
					} else {
						main.setSjPath(path);
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

		if (main.settings.hasSj()) {
			File file = new File(main.settings.getSjPath());
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
	
	public synchronized void println(String message) {
		System.out.println(message);
		try {
			doc.insertString(doc.getLength(), message+"\n", null);
			textPane.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {}
	}
}
