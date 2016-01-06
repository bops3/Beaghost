package com.kritosoft.beaghost;

public class Clock implements Runnable {
	private int desiredTicksPerSecond;
	private Tickable toTick;
	private double lastTickDelayMillis = 0, lastTickMethodDelayMillis, neededDelay;
	private Thread thread;

	public Clock(int desiredTicksPerSecond, Tickable toTick) {
		this.desiredTicksPerSecond = desiredTicksPerSecond;
		this.toTick = toTick;

		neededDelay = 1000d / desiredTicksPerSecond;
		lastTickDelayMillis = neededDelay; // first time only
	}

	public synchronized int getDesiredTicksPerSecond() {
		return desiredTicksPerSecond;
	}

	public synchronized double getRealTicksPerSecond() {
		return 1000d / lastTickDelayMillis;
	}
	
	public synchronized void setDesiredTicksPerSecond(int desiredTicksPerSecond) {
		this.desiredTicksPerSecond = desiredTicksPerSecond;
		neededDelay = 1000d / desiredTicksPerSecond;
	}

	private void mainTick() throws Exception {
		long timeBegin = System.currentTimeMillis();
		synchronized (this) {
			try {
				toTick.tick((int) (lastTickDelayMillis));
			} catch (Exception e) {
				throw new Exception("Tickable ticked by TickMaker threw this Exception", e);
			}
			lastTickMethodDelayMillis = (int) (System.currentTimeMillis() - timeBegin);
			if (neededDelay > lastTickMethodDelayMillis) {
				Thread.sleep((long) (neededDelay - lastTickMethodDelayMillis));
			}
			lastTickDelayMillis = (int) (System.currentTimeMillis() - timeBegin);
		}
	}

	@Override
	public void run() {
		try {
			while (!thread.isInterrupted()) {
				mainTick();
			}
		} catch (Exception e) {
			if (!(e instanceof InterruptedException))
				e.printStackTrace();
		}
	}

	public void startTicking() {
		if (thread == null || !thread.isAlive()) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public void stopTicking() {
		if (thread != null)
			thread.interrupt();
	}

}

interface Tickable {
	public void tick(int millisDelta);
}
