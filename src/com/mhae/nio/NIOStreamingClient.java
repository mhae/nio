package com.mhae.nio;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NIOStreamingClient {

	public static int  writeUTF(ByteBuffer bb, String str) throws UnsupportedEncodingException 
	{
    byte[] bytes = str.getBytes("UTF-8");
    bb.putShort((short) bytes.length);
    bb.put(bytes);
    return 2+bytes.length;
  }
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		SocketChannel socketChannel;
		try {
			socketChannel = SocketChannel.open();
			socketChannel.connect(new InetSocketAddress("127.0.0.1", 4567));
			// socketChannel.socket().setTcpNoDelay(true);

			BufferedDataOutputStream dos = new BufferedDataOutputStream(socketChannel, 1024*5);

			String s = "";
			for (int i=0; i<333; i++) s += "A";
			
			long check = 0;
			for (int r = 0; r < 100; r++) {
				long ts = System.currentTimeMillis();
				long bytes = 0;

				for (int i = 0; i < 50000; i++) {

					dos.writeInt(140267); bytes += 4; 
					
					for (long l = 0; l < 20; l++) {
						dos.writeLong(check); check++;
						bytes += 8;
					}
					
					dos.write((byte)1); bytes++;
					
					dos.writeUTF8(s); bytes += s.length()+2;
					
					dos.writeDouble(1.5); bytes += 8;

					dos.writeChar('c'); bytes += 1;

					dos.flush();
				}
				dos.flush();

				long te = System.currentTimeMillis();
				double tput = (double) (bytes / 1024) / ((double) (te - ts) / 1000);
				System.out.println(te - ts + "ms, tput=" + tput + "KB/s, transferred="
						+ bytes / 1024 + "KB");

			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			socketChannel.socket().close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
