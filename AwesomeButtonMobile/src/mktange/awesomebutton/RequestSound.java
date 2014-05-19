package mktange.awesomebutton;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.os.AsyncTask;

public class RequestSound extends AsyncTask<String, Void, Void> {

	@Override
	protected Void doInBackground(String... args) {
		String ip = args[0];
		String message = args[1];
		
		try {
			DatagramSocket socket = new DatagramSocket();
			byte[] out = message.getBytes();
			InetAddress IP = InetAddress.getByName(ip);
			DatagramPacket p = new DatagramPacket(out, out.length, IP, MainActivity.SERVERPORT);

			System.out.println("Sending packet to "+IP+":"+MainActivity.SERVERPORT);
			socket.send(p);

			socket.close();
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

}
