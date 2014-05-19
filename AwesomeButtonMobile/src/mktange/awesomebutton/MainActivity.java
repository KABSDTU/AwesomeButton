package mktange.awesomebutton;

import java.util.Collection;
import java.util.TreeSet;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	public final static int SERVERPORT = 1990;
	public final static int FAVDIALOG = 0;
	public final static int IPDIALOG = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MobileLib.init(this);
		updateGUI();

		System.out.println("Done initializing");
	}

	public void updateGUI() {

		if (MobileLib.SOUNDS.isEmpty() || MobileLib.FAVORITES.isEmpty()) {
			
			setContentView(R.layout.activity_main);
			TextView info = (TextView) findViewById(R.id.message);
			if (MobileLib.SOUNDS.isEmpty()) {
				info.setVisibility(TextView.VISIBLE);
			} else {
				info.setVisibility(TextView.INVISIBLE);
			}
			
		} else {
			setContentView(R.layout.fav_activity_main);
			
			// Get layouts
			LinearLayout[] favlayouts = {
					(LinearLayout) findViewById(R.id.favorites1),
					(LinearLayout) findViewById(R.id.favorites2)
			};

			addButtons(favlayouts, MobileLib.FAVORITES);
		}

		// Get layouts
		LinearLayout[] layouts = {
				(LinearLayout) findViewById(R.id.regulars1),
				(LinearLayout) findViewById(R.id.regulars2)
		};
		addButtons(layouts, new TreeSet<Sound>(MobileLib.SOUNDS.values()));

	}

	private void addButtons(LinearLayout[] layouts, Collection<Sound> sounds) {
		// Clear them
		for (LinearLayout l : layouts) l.removeAllViews();

		int curr = 0;

		// Add in buttons
		for (final Sound s : sounds) {
			if (!MobileLib.SOUNDS.containsKey(s.id)) continue;
			
			Button b = new Button(this);
			b.setText(s.desc);
			layouts[curr].addView(b);
			curr++;
			if (curr >= layouts.length) curr = 0;

			b.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						System.out.println("Requesting sound \""+s.id+"\"");
						requestSound(s.id);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	private void requestSound(String soundname) throws Exception {
		new RequestSound().execute(MobileLib.SERVERIP, soundname);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.update_list:
			removeDialog(FAVDIALOG);
			new UpdateList(this).execute(MobileLib.SERVERIP);
			break;
		case R.id.clear_list:
			MobileLib.clearData(this);
			removeDialog(FAVDIALOG);
			updateGUI();
			break;
		case R.id.favorites_menu:
			showDialog(FAVDIALOG);
			break;
		case R.id.serverip:
			showDialog(IPDIALOG);
			break;
		}
		return true;
	}
	
	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case FAVDIALOG:
			return DialogCreator.getFavoritesDialog(this);
		case IPDIALOG:
			return DialogCreator.getIpSelectDialog(this);
		default:
			return null;
		}
		
	}
}


