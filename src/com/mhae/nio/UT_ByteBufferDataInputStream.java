package com.mhae.nio;
import static org.junit.Assert.*;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;


/**
 * This unit tests starts a client and server, and runs
 * @author michaelhaeuptle
 *
 */
public class UT_ByteBufferDataInputStream {

	@BeforeClass
	public static void setup() throws IOException
	{
	}
	
	
	private static CountDownLatch threadCoordinationLatch = new CountDownLatch(1);
	
	public static class ServerThread extends Thread
	{
		final ServerSocketChannel serverChannel;
		final int bufferSize;
		
		public ServerThread(int port, int bufferSize) throws IOException
		{
			serverChannel = ServerSocketChannel.open();
			InetSocketAddress isa = new InetSocketAddress("127.0.0.1", port);
	    serverChannel.socket().bind(isa);
	    this.bufferSize = bufferSize;
		}
		
		public void run()
		{
			threadCoordinationLatch.countDown(); // once the server is up allow the client to start
			 try {
				SocketChannel clientChannel = serverChannel.accept();
				
				BufferedDataInputStream is = new BufferedDataInputStream(clientChannel, bufferSize);
		    
		    String ss = "";
				for (int i=0; i<333; i++) ss += "A";
				
		    long check = 0;
		    
		    while (true) {
		    	int anInt = is.readInt();
		    	if (anInt != 140267) {
		    		fail("check="+140267+" != "+anInt);
		    	}
		    	
		    	for (int i=0; i<20; i++) {
		    		long l = is.readLong();
		    		if (check != l) {
		    			fail("check="+check+" != "+l);
		    		}
		    		check++; 
		    	}
		
		    	byte b = is.readByte();
	    		if (b != 1) {
	    			fail("check="+1+" != "+b);
	    		}
	    		
	    		String s = is.readUTF8();
	    		if (! s.equals(ss)) {
	    			fail("check="+ss+" != "+s);
	    		}
	    		
	    		double d = is.readDouble();
	    		if (d != 1.5) {
	    			fail("check="+"1.5"+" != "+d);
	    		}
	    		
	    		char c = is.readChar();
	    		if (c != 'c') {
	    			fail("check="+"c"+" != "+c);
	    		}
	    		
		    }
				
			}
			 catch (EOFException eof) {
				 // this is ok - we're done
			 }
			 catch (IOException e) {
				fail("Unable to accept: "+e);
			}
			finally {
				try {
					serverChannel.socket().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public static class ClientThread extends Thread
	{
		
		final int runs;
		final int blocks;
		final int port;
		
		public ClientThread(int port, int runs, int blocks)
		{
			this.runs = runs;
			this.blocks = blocks;
			this.port = port;
		}
		
		
		public void run()
		{
			
			try {
				threadCoordinationLatch.await();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
  		SocketChannel socketChannel;
  		try {
  			socketChannel = SocketChannel.open();
  			socketChannel.connect(new InetSocketAddress("127.0.0.1", port));
  
  			BufferedDataOutputStream dos = new BufferedDataOutputStream(socketChannel, 1024*8);
  			
  			String s = "";
  			for (int i=0; i<333; i++) s += "A";
  			
  			long check = 0;
  			for (int r = 0; r < runs; r++) {
  				long ts = System.currentTimeMillis();
  				long bytes = 0;

  				for (int i = 0; i < blocks; i++) {

  					dos.writeInt(140267); bytes += 4; 
  					
  					for (long l = 0; l < 20; l++) {
  						dos.writeLong(check); check++;
  						bytes += 8;
  					}
  					
  					dos.write((byte)1); bytes++;
  					
  					dos.writeUTF8(s); 
  					
  					dos.writeDouble(1.5); bytes += 8;

  					dos.writeChar('c'); bytes += 2;
  				}
  				
  				dos.flush();

  				long te = System.currentTimeMillis();
  				double tput = (double) (bytes / 1024) / ((double) (te - ts) / 1000);
  				System.out.println(te - ts + "ms, tput=" + tput + "KB/s, transferred="
  						+ bytes / 1024 + "KB");

  			}

  			try {
  				Thread.sleep(500);
  			} catch (InterruptedException e) {
  			}
  			socketChannel.socket().close();
  			
  		}
  		catch (IOException e)
  		{
  			fail("Unabel to send data: "+e);
  		}
		}
		
	}
	
	@Test
	public void quickTest() {
		try {
			ServerThread st = new ServerThread(4567, 1001*3);
			st.start();
			ClientThread ct = new ClientThread(4567, 1, 50000);
			ct.start();
			
			st.join();
			ct.join();
			
		} catch (IOException e) {
			fail("IO Error: "+e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Test with varying server buffer sizes. This test will take a while.
	 */
	@Test
	public void bufferSizeTest() {
		
		Random rand = new Random();
		
		for (int n=0; n<10; n++) {
			int bufferSize = 1024+rand.nextInt(20000);
			System.out.println("bufferSize="+bufferSize);
	
  		try {
  			ServerThread st = new ServerThread(4567+n, bufferSize);
  			st.start();
  			ClientThread ct = new ClientThread(4567+n, 2, 50000);
  			ct.start();
  			
  			st.join();
  			ct.join();
  			
  		} catch (IOException e) {
  			fail("IO Error: "+e);
  		} catch (InterruptedException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}
		}
		
	}
	

	public static void main(String[] args) {
    Result result = JUnitCore.runClasses(UT_ByteBufferDataInputStream.class);
    for (Failure failure : result.getFailures()) {
      System.out.println(failure.toString());
    }
  }
	
}
