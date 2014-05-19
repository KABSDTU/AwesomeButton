import java.net.InetAddress;


public class Blocker extends Thread {
	private AwesomeButton main;
	private InetAddress ip;
	private int delay;
	
	public Blocker(AwesomeButton main, InetAddress ip, int delay) {
		this.main = main;
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
		main.unblock(ip);
	}
}
