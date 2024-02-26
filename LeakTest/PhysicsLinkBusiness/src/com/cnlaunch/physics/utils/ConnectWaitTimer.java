package com.cnlaunch.physics.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 用于监控蓝牙连接长时间超时
 */
public final class ConnectWaitTimer {

	//监控蓝牙连接超时时间为45秒
	private static final int INACTIVITY_DELAY_SECONDS = 45;

	private final ScheduledExecutorService inactivityTimer = Executors
			.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
	private ScheduledFuture<?> inactivityFuture = null;

	public ConnectWaitTimer() {

	}

	public void onStart(Runnable runnable) {
		cancel();
		inactivityFuture = inactivityTimer.schedule(runnable,
				INACTIVITY_DELAY_SECONDS, TimeUnit.SECONDS);
	}

	public void onStop() {
		cancel();
	}

	private void cancel() {
		if (inactivityFuture != null) {
			inactivityFuture.cancel(true);
			inactivityFuture = null;
		}
	}

	public void shutdown() {
		cancel();
		inactivityTimer.shutdown();
	}

	private static final class DaemonThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			return thread;
		}
	}
}
