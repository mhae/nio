Version 0.1

This project provides DataInputStream and DataOutputStream classes for 
highly efficient socket communication based on NIO and direct ByteBuffers.

Using NIO and direct ByteBuffers is significantly faster than the old pre-Java 1.5 socket classes.
Thanks to direct buffers, the reading and writing of primitives is much faster than the non-direct versions.

-- Michael Haeuptle
