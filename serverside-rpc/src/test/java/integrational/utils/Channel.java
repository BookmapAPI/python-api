package integrational.utils;

import com.bookmap.api.rpc.server.data.outcome.ServerOffEvent;
import com.bookmap.api.rpc.server.data.outcome.converters.ServerOffConverter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Channel extends LinkedBlockingQueue<java.lang.String> {

	private static final String SERVER_OFF_EVENT = new ServerOffConverter().convert(new ServerOffEvent());
	private final Object CHANNEL_STATUS_LOCK = new Object();

	private final CountDownLatch openLatch = new CountDownLatch(1);

	@Override
	public void put(String event) throws InterruptedException {
		// TODO: crunchy fast solution for tests specifically, but maybe it would be better to generalize it making a chain of hooks?
		if (SERVER_OFF_EVENT.equals(event)) {
			close();
			return;
		}

		if (isClosed()) {
			throw new IllegalStateException("integrational.utils.Channel is closed");
		}
		super.put(event);
	}

	public void waitForClose(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
		boolean isTimedOut = !openLatch.await(time, unit);
		if (isTimedOut) {
			throw new TimeoutException("integrational.utils.Channel was not closed by the awaiting time");
		}
	}

	public boolean isClosed() {
		return openLatch.getCount() < 1;
	}

	public void close() {
		openLatch.countDown();
	}

}
