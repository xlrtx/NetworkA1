/**
 * An Template For Non-Blocking Server
 * @author s4337746
 *
 */
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class NbServer {

  private final static boolean DEBUG = true;
  
  NbServerCallback callee;

  
  private int port;
  private Selector selector = null;
  private ServerSocketChannel serverSocketChannel = null;
  private ServerSocket serverSocket = null;

  
  

  public NbServer(NbServerCallback callee){
    
    this.callee = callee;
    
  }
  
  public void bind(int port) throws Exception{
    
    this.port = port;
    
    // open selector
    selector = Selector.open();
    
    // open socket channel
    serverSocketChannel = ServerSocketChannel.open();
    
    // set the socket associated with this channel
    serverSocket = serverSocketChannel.socket();
    
    // set Blocking mode to non-blocking
    serverSocketChannel.configureBlocking(false);
    serverSocket.bind(new InetSocketAddress(this.port));

    // registers this channel with the given selector, returning a selection key
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    
  }


  public void start(){
    try {
      while (selector.select() > 0) {
          for (SelectionKey key : selector.selectedKeys()) {
              // test whether this key's channel is ready to accept a new socket connection
              if (key.isAcceptable()) {
                  // accept the connection
                  ServerSocketChannel server = (ServerSocketChannel) key.channel();
                  SocketChannel sc = server.accept();
                  if (sc == null)
                      continue;
                  
                  if(DEBUG){
                    System.out.println("Connection accepted from: " + sc.getRemoteAddress());
                  }
                  
                  // set blocking mode of the channel
                  sc.configureBlocking(false);
                  // allocate buffer
                  ByteBuffer buffer = ByteBuffer.allocate(1024);
                  // set register status to READ
                  sc.register(selector, SelectionKey.OP_READ, buffer);
              }
              // test whether this key's channel is ready for reading from Client
              else if (key.isReadable()) {
                ByteBuffer replyData = null;
                
                  // get allocated buffer with size 1024
                  ByteBuffer buffer = (ByteBuffer) key.attachment();
                  SocketChannel sc = (SocketChannel) key.channel();
                  boolean illegalRequest = false;
                  int readBytes = 0;
                  String message = null;
                  // try to read bytes from the channel into the buffer
                  try {
                      int ret;
                      try {
                          while ((ret = sc.read(buffer)) > 0)
                              readBytes += ret;
                      } catch (Exception e) {
                          readBytes = 0;
                      } finally {
                          buffer.flip();
                      }
                      // finished reading, form message
                      if (readBytes > 0) {
                        
                        try {
                          //replyData = consumeRequest(buffer);
                          replyData = callee.consumeRequest(buffer);
                        } catch (Exception e) {
                          /* Illegal request */
                          illegalRequest = true;
                        }
                          message = Charset.forName("UTF-8").decode(buffer).toString();
                          buffer = null;
                          
                      }else{
                        
                        //buffer is empty
                        
                      }
                  } finally {
                      if (buffer != null)
                          buffer.clear();
                  }
                  // react by Client's message
                  if (readBytes > 0) {
                    
                      if(DEBUG){
                        System.out.println("Message from Client" + sc.getRemoteAddress() + ": " + message);
                      }
                      
                      // if exit, close socket channel
                      if (illegalRequest || "exit".equalsIgnoreCase(message.trim())) {
                        
                          if(DEBUG){
                            System.out.println("Client send illegal request " + sc.getRemoteAddress() +" finish up");
                          }
                          
                          sc.close();
                      } else {
                          // set register status to WRITE
                        byte[] replyByteData = new byte[replyData.remaining()];
                        replyData.get(replyByteData);
                        sc.register(key.selector(), SelectionKey.OP_WRITE, replyByteData);
                      }
                  }
              }
              // test whether this key's channel is ready for sending to Client
              else if (key.isWritable()) {
                  SocketChannel sc = (SocketChannel) key.channel();
                  ByteBuffer buffer = ByteBuffer.allocate(1024);
                  buffer.put((byte[])(key.attachment()));
                  buffer.flip();
                  sc.write(buffer);
                  // set register status to READ
                  sc.register(key.selector(), SelectionKey.OP_READ, buffer);
              }
          }
          if (selector.isOpen()) {
              selector.selectedKeys().clear();
          } else {
              break;
          }
      }
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        if (serverSocketChannel != null) {
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
  }
  
  //protected abstract ByteBuffer consumeRequest(ByteBuffer requestData) throws Exception;
}