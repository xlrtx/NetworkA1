/**
 * 
 * @author Mingyang Zhong
 * Feb. 2014
 * the University of Queensland
 * Code example for course: COMS3200
 * 
 This is simple network program based on Java-IO, TCP blocking mode and single thread. 
 The TCPClient reads inputs from the keyboard then sends it to TCPServer.
 The TCPServer reads packets from the socket channel and convert it to upper case, and then sends back to TCPClient. 
 The program assumes that the data in any received packets will be in string form.
 Typing 'exit' will close the program.
 * 
 */

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
public class TCPClient {
  
  private Socket clientSocket;
  
  
  TCPClient(InetSocketAddress address) throws Exception{
    
    this.clientSocket = new Socket(address.getAddress(), address.getPort());
    
  }
  
  
  
  
  public void close() throws Exception{
    
    this.clientSocket.close();
    
  }
  
  
  
  
  public ByteBuffer request(ByteBuffer requestData, boolean autoClose) throws IOException{
    
    
    BufferedInputStream in;
    BufferedOutputStream out;

    
    out =  new BufferedOutputStream (this.clientSocket.getOutputStream());
    in  =  new BufferedInputStream  (this.clientSocket.getInputStream());

    
    // Send Request
    byte[] outBytes = new byte[requestData.remaining()];
    requestData.get ( outBytes );
    out.write       ( outBytes );
    out.flush();
    
    // Get Response
    byte[] inBytes = new byte[1024];
    int recv = in.read(inBytes);
    ByteBuffer responseData = ByteBuffer.allocate(1024);
    responseData.put(inBytes, 0, recv);
    responseData.flip();
 
    // Close everything

    
    
    if (autoClose){
      out.close();
      in.close();
      clientSocket.close();
    }
    
    
    return responseData;
    
  }

    
}
