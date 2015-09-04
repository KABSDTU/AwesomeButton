import java.net.InetAddress;
import java.util.HashMap;

public class Blocker {
	private HashMap<InetAddress,Integer> blocked = new HashMap<InetAddress,Integer>();
	private int delay;
	
	public Blocker(int delay) {
		this.delay = delay;
	}
	
	public void setDelay(int delay) {
		this.delay = delay;
	}
	
	/**
	  * Check if an IP is blocked (return false)
	  * Block IP in DELAY time
	**/
	public boolean checkAndBlock(InetAddress ip) {
		boolean alreadyBlocked = isBlocked(ip);
		block(ip);
		return alreadyBlocked;
	}
	
	private boolean isBlocked(InetAddress ip) {
		if (!blocked.containsKey(ip)) return false;
		return blocked.get(ip) > 0;
	}
	
	private void block(InetAddress ip) {
		if (!isBlocked(ip)) {
			blocked.put(ip, 1);
		} else {
			blocked.put(ip, blocked.get(ip) + 1);
		}
		
		// Start thread that will remove "1 block" after delay time
		(new BlockerThread(this, ip, delay)).start();
	}
	
	protected void unblock(InetAddress ip) {
		if (blocked.containsKey(ip)) {
			int timesBlocked = blocked.get(ip);
			if (timesBlocked == 1) {
				blocked.remove(ip);
			} else {
				blocked.put(ip, timesBlocked-1);
			}
		}
	}
}

class BlockerThread extends Thread {
	private Blocker blocker;
	private InetAddress ip;
	private int delay;
	
	public BlockerThread(Blocker blocker, InetAddress ip, int delay) {
		this.blocker = blocker;
		this.ip = ip;
		this.delay = delay;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		blocker.unblock(ip);
	}
}