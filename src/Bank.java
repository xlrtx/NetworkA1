import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


public class Bank implements NbServerCallback, ProtocolDefs{

  //Non-blocking Server To Handle The Requests
  NbServer myNbServer;
  
  
  // Stores Server Address
  InetSocketAddress myAddr;
  InetSocketAddress nsAddr;
  
  
  private final static String MSG_ARGUMENT_ERR  =   "Invalid command line arguments for Bank\n";
  private final static String MSG_REGISTER_ERR  =   "Bank registration to NameServer failed\n";
  private final static String MSG_BIND_ERR      =   "Bank unable to listen on given port\n";
  private final static String MSG_BIND_OK       =   "Bank waiting for incoming connections\n";
  
  
  public static void main(String[] args) {
    
    NameServer myBank = null;
    
    try {
      myBank = new NameServer(args);
      System.err.println(MSG_BIND_OK);
      myBank.myNbServer.start();
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    
    
    
  }
  

  
  /**
   * Bank Constructor
   * @param args        The port for this name server.
   * @throws Exception  On argument error.
   */
  Bank(String[] args) throws Exception{
    
    
    //Create Non-blocking Server, And Assign Callback.
    this.myNbServer = new NbServer(this);
    
    
    // Check Argument Count
    int argCount = 0;
    argCount = args.length;
    if (argCount != 2){
      throw new Exception(MSG_ARGUMENT_ERR);
    }
    
    
    // Get Port
    int port;
    try{
      port = Integer.parseInt(args[0]);
    }catch(NumberFormatException e){
      throw new Exception(MSG_ARGUMENT_ERR);
    }
    
    
    // Get Name Server Port
    int nsPort;
    try{
      nsPort = Integer.parseInt(args[1]);
    }catch(NumberFormatException e){
      throw new Exception(MSG_ARGUMENT_ERR);
    }
    this.nsAddr = new InetSocketAddress("127.0.0.1", nsPort);
    
    
    // Register MySelf To Name Server
    try{
      NetworkUtils.rpcRegister("Bank", this.myAddr, this.nsAddr);
    }catch(Exception e){
      throw new Exception(MSG_REGISTER_ERR);
    }
    
    
    // Bind Port
    try{
      this.myNbServer.bind(port);
    }catch(Exception e){
      throw new Exception(MSG_BIND_ERR);
    }
    

  }


  private ByteBuffer transaction(ByteBuffer requestData) {

    

    //Parse Data
    Long    id      =   Long.parseLong( XDRParser.getFixString(requestData, 10) );
    Double  price   =   requestData.getDouble();
    String  ccn     =   XDRParser.getFixString(requestData, 16);
    InetSocketAddress address = this.serverMap.get(name);
  }
  
  /**
   * Consume Request And Generate Response Data, NbServer Callback.
   * @param requestData
   * @return
   * @throws Exception
   */
  @Override
  public ByteBuffer consumeRequest(ByteBuffer requestData) throws Exception{

    int requestType = requestData.getInt();
    
    if( requestType == RT_BANK_REQ_TRANS ){
      return transaction(requestData);
    }else{
      // Throw Error When Illegal Packet, Connection Will Be Closed.
     throw new Exception("no such method for this server");
    }
    
  }


  

}
