package com.mhae.nio;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * Simple test server using the BufferedDataInputStream
 * @author michaelhaeuptle
 *
 */
public class NIOStreamingServer
{
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try {
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			InetSocketAddress isa = new InetSocketAddress("127.0.0.1", 4567);
	    serverChannel.socket().bind(isa);
	    
	    SocketChannel clientChannel = serverChannel.accept();
	    
	    BufferedDataInputStream is = new BufferedDataInputStream(clientChannel, 6234);
	    
	    // Fairly large example string
	    String ss = "";
			for (int i=0; i<333; i++) ss += "A";
			
	    long check = 0; // Used to check the ever increasing longs that the client is sending
	    
	    while (true) {
	    	int anInt = is.readInt();
	    	if (anInt != 140267) {
	    		System.out.println("check="+140267+" != "+anInt);
    			System.exit(1);
	    	}
	    	
	    	for (int i=0; i<20; i++) {
	    		long l = is.readLong();
	    		if (check != l) {
	    			System.out.println("check="+check+" != "+l);
	    			System.exit(1);
	    		}
	    		check++;
	    	}
	    	// System.out.println();
	    	byte b = is.readByte();
    		if (b != 1) {
    			System.out.println("check="+1+" != "+b);
    			System.exit(1);
    		}
    		
    		String s = is.readUTF8();
    		if (! s.equals(ss)) {
    			System.out.println("check="+ss+" != "+s);
    			System.exit(1);
    		}
    		
    		double d = is.readDouble();
    		if (d != 1.5) {
    			System.out.println("check="+"1.5"+" != "+d);
    			System.exit(1);
    		}
    		
    		char c = is.readChar();
    		if (c != 'c') {
    			System.out.println("check="+"c"+" != "+c);
    			System.exit(1);
    		}
    		
	    }
	    
	    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
}
