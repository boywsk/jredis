package org.jredis.ri.adhoc;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jredis.JRedisFuture;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.JRedisChunkedPipeline;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;

@SuppressWarnings("unused")
public class AdHocTestChunkPipeline {
	public static void main(String[] args) throws Throwable {
		try {
			AdHocTestChunkPipeline tester = new AdHocTestChunkPipeline();
			while(true){
				tester.run();
			}
		} catch (Exception e) {
			e.printStackTrace();
//			Log.error("fault", e); // TODO: 
		}
	}
	final ConnectionSpec spec;
	final JRedisFuture jredis;
	public AdHocTestChunkPipeline() {
		spec = DefaultConnectionSpec.newSpec().setCredentials("jredis").setDatabase(11);
		jredis = new JRedisChunkedPipeline(spec);
	}
	
	static final int wcnt = 2;
	static final int reqnums = 150000;
	
	private void run() {
		final Thread[] workers = new Thread[wcnt];
		for(int i=0;i< workers.length; i++){
			String tname = String.format("w-%02d", i);
			workers[i] = worker(jredis, tname);
		}
		final long start = System.currentTimeMillis();
		for(Thread t : workers){
			t.start();
		}
		for(Thread t : workers){
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Log.log("%d x %d @ %d msecs", wcnt, reqnums, System.currentTimeMillis() - start);
	}
	
	private Thread worker (final JRedisFuture jredis, String tname) {
		final Runnable task = task(jredis);
		return new Thread(task, tname);
	}
	
//	@SuppressWarnings("unused")
	private Runnable task (final JRedisFuture jredis) {
		return new Runnable() {
			final JRedisFuture conn = jredis;
			@Override public void run() {
				final String tname = Thread.currentThread().getName();
				final byte[] key = tname.getBytes();
				final byte[] cntr = (tname + "#").getBytes();
				Future<Long> fCntr = null;
				for(int i=0; i<reqnums; i++) {
//					conn.set(key, key);
//					conn.get(key);
//					conn.ping();
					fCntr = conn.incr(cntr);
				}
				conn.flush();

//				try {
//					Long counter = fCntr.get();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				} catch (ExecutionException e) {
//					e.printStackTrace();
//				}
				
			}
		};
	}
}