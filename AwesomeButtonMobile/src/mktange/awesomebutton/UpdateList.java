package mktange.awesomebutton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;

public class UpdateList extends AsyncTask<String, Void, Boolean> {
	private MainActivity activity;
	private AlertDialog mDialog;
	
	public UpdateList(MainActivity activity) {
		this.activity = activity;
	}
	
	@Override
	protected Boolean doInBackground(String... args) {
		String ip = args[0];
		
		try {
			InetAddress IP = InetAddress.getByName(ip);
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(IP, 1991), 1000);
			
			OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
			InputStream is = socket.getInputStream();
			writer.write("update_list\n");
			writer.flush();
			
			OutputStream os = activity.openFileOutput(MobileLib.SOUNDFILE, Context.MODE_PRIVATE);
			copy(is, os);
			
			is.close();
			os.close();
			writer.close();
			socket.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	@Override
	protected void onPreExecute() {
		mDialog = new ProgressDialog(activity);
        mDialog.setMessage("Loading...");
        mDialog.setCancelable(false);
        mDialog.show();
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		mDialog.dismiss();
		if (!result) {
			mDialog = new AlertDialog.Builder(activity).create();
			mDialog.setTitle("Failed");
			mDialog.setMessage("Unable to get sound list from server");
			mDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					mDialog.dismiss();
				}
			});
			mDialog.show();
		}
		
		MobileLib.init(activity);
		activity.updateGUI();
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] data = new byte[8192];
		int len = 0;
		while ((len = in.read(data)) != -1) {
			out.write(data, 0, len);
		}
	}
}
