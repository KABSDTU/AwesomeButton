package mktange.awesomebutton;

import java.util.TreeSet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.text.InputType;
import android.widget.EditText;


public class DialogCreator {

	public static AlertDialog getFavoritesDialog(final MainActivity activity) {
		final String[] ids = new String[MobileLib.SOUNDS.size()];
		CharSequence[] items = new CharSequence[MobileLib.SOUNDS.size()];
		boolean[] checked = new boolean[MobileLib.SOUNDS.size()];

		int i = 0;
		for (Sound sound : new TreeSet<Sound>(MobileLib.SOUNDS.values())) {
			ids[i] = sound.id;
			items[i] = sound.desc;
			checked[i] = MobileLib.FAVORITES.contains(sound);
			i++;
		}

		return new AlertDialog.Builder(activity)
		.setTitle("Favorites")
		.setMultiChoiceItems(items, checked, new OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if (isChecked) {
					MobileLib.FAVORITES.add(MobileLib.SOUNDS.get(ids[which]));
				} else {
					MobileLib.FAVORITES.remove(MobileLib.SOUNDS.get(ids[which]));
				}

			}
		})
		.setPositiveButton("Done", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MobileLib.saveFavorites();
				activity.updateGUI();
			}
		})
		.create();
	}

	public static AlertDialog getIpSelectDialog(final MainActivity activity) {
		final EditText field = new EditText(activity);
		field.setInputType(InputType.TYPE_CLASS_PHONE);
		field.setText(MobileLib.SERVERIP);
		field.setSelection(MobileLib.SERVERIP.length());
		
		return new AlertDialog.Builder(activity)
		.setTitle("Server IP")
		.setView(field)
		.setPositiveButton("OK", new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!validIP(field.getText().toString())) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("IP not saved");
                    builder.setMessage("Input has to be a valid IP, i.e. \"192.168.0.1\"");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                    activity.removeDialog(MainActivity.IPDIALOG);
                } else {
                	MobileLib.setIp(field.getText().toString());
                }
			}
		})
		.setNegativeButton("Cancel", new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				activity.removeDialog(MainActivity.IPDIALOG);
			}
		})
		.create();
	}

	private static boolean validIP(String input) {
		String[] parts = input.split("\\.");
		if (parts.length != 4) return false;
		
		int num;
		for (int i = 0; i < parts.length; i++) {
			num = Integer.parseInt(parts[i]);
			if (num < 0 || num > 255) return false;
		}
		return true;
	}
}
