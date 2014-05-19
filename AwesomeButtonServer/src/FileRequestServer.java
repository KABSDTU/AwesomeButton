import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class FileRequestServer extends Thread {
	private ServerSocket socket;

	@Override
	public void run() {
		super.run();

		Socket newConnection;
		try {
			socket = new ServerSocket(1991);
			while (true) {
				newConnection = socket.accept();
				(new Connection(newConnection)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] data = new byte[8192];
		int len = 0;
		while ((len = in.read(data)) != -1) {
			out.write(data, 0, len);
		}
	}

	private class Connection extends Thread {
		private Socket socket;

		public Connection(Socket socket) {
			this.socket = socket;
			System.out.println("Received connection from "+socket.getInetAddress());
		}

		@Override
		public void run() {
			super.run();

			BufferedReader br;
			try {
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				System.out.println("waiting for request");
				if (br.readLine().equals("update_list")) {
					System.out.println("Sending file to "+socket.getInetAddress());
					performTransfer();
				}
				
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		private void performTransfer() {
			try {
				InputStream is = new FileInputStream(AwesomeButton.SOUNDFILE);
				OutputStream os = socket.getOutputStream();
				copy(is, os);
				is.close();
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
