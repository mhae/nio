package com.mhae.nio;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * A buffered DataInputStream using a direct ByteBuffer to read in as many bytes as possible from the underlying channel.
 * The direct ByteBuffer allows for efficient decoding of primitives.
 * @author michaelhaeuptle
 *
 */
public class BufferedDataInputStream
{
	final ByteBuffer buffer;
	final ReadableByteChannel channel;
	
	
	public BufferedDataInputStream(ReadableByteChannel channel, int bufferSize)
	{
		if (bufferSize < 256) 
			throw new IllegalArgumentException("Buffer size must be greater or equal than 256"); 
		buffer = ByteBuffer.allocateDirect(bufferSize); 
		buffer.flip();
		
		this.channel = channel;
	}
	
	
	/**
	 * Reads at least the specified number of bytes from the underlying channel. May block.
	 * @param byteCount
	 * @throws IOException
	 */
	private void ensureAvailableBytes(int byteCount) throws IOException
	{
		// Is there enough data available in the buffer?
		if (buffer.remaining() >= byteCount) return;
			
		// 2 main conditions:
		// a) If there is still some space left in the buffer, read more data from the channel (between limit and capacity).
		//    Note, it is not guaranteed that all space will be used between limit and capacity. 
		// b) If the buffer is completely fill (limit == capacity-8) then we compact the buffer to make some more room
		if (buffer.limit() != buffer.capacity() && 
				(buffer.capacity() - buffer.limit()) >8) { // if still space (need at least 8 bytes) then avoid compact (somewhat expensive operation)
			
			// Try to read some more after end of the buffer (indicated by limit). 
			// We need to save current pos and limit so that we can later restore the old position and new limit in the buffer
			int pos = buffer.position();
			int limit = buffer.limit();
			buffer.position(buffer.limit());
			buffer.limit(buffer.capacity());
			
			int bytesRead = channel.read(buffer);
			if (bytesRead == -1) throw new EOFException();
			
			// restore where we left off
			buffer.position(pos);
			buffer.limit(limit+bytesRead);
		}
		else { // reached the end ... compact the buffer and read some more
			buffer.compact();
			int bytesRead = channel.read(buffer);
			if (bytesRead == -1) throw new EOFException();
			buffer.flip();
		}
	}
	
	
	public int readInt() throws IOException
	{
		ensureAvailableBytes(4);
		return buffer.getInt();
	}
	
	public long readLong() throws IOException
	{
		ensureAvailableBytes(8);
		return buffer.getLong();
	}
	
	public byte readByte() throws IOException {
		ensureAvailableBytes(1);
    return buffer.get();
  }
	
	public short readShort() throws IOException {
		ensureAvailableBytes(2);
    return buffer.getShort();
  }
	
	 public int readUnsignedShort() throws IOException {
		 ensureAvailableBytes(2);
	    return (int)(buffer.getShort() & 0xffff);
	  }
	
	public String readUTF8() throws IOException {
    int len = readUnsignedShort();
    byte[] data = new byte[ len ];
    int offset = 0;

    int bytesRemaining = len;

    do {
    	ensureAvailableBytes(bytesRemaining);
      int bytesToRead = Math.min(buffer.remaining(), bytesRemaining);

      buffer.get(data, offset, bytesToRead);
      offset += bytesToRead;
      bytesRemaining -= bytesToRead;
    }
    while (bytesRemaining > 0);
  
    String ret = new String(data, "UTF-8");

    return ret;
  }

	public float readFloat() throws IOException {
		ensureAvailableBytes(4);
    return buffer.getFloat();
  }



  public double readDouble() throws IOException {
  	ensureAvailableBytes(8);
    return buffer.getDouble();
  }
  
  public char readChar() throws IOException {
  	ensureAvailableBytes(2);
    return buffer.getChar();
  }
	
}