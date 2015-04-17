import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


public class Store implements NbServerCallback, ProtocolDefs{
  
  
  // Non-blocking Server To Handle The Requests
  NbServer  myNbServer;
  
  // Stores Server Address
  InetSocketAddress myAddr;
  InetSocketAddress nsAddr;
  InetSocketAddress bankAddr;
  InetSocketAddress contentAddr;
  
  // Bank And Content Connection
  TCPClient bankClient;
  TCPClient contentClient;
  
  HashMap<Long, Double>stockMap;
  private final static String MSG_ARGUMENT_ERR      =   "Invalid command line arguments for Store\n";
  private final static String MSG_REGISTER_ERR      =   "Registration with NameServer failed\n";
  private final static String MSG_BANK_NOREG        =   "Bank has nor registered\n";
  private final static String MSG_CONTENT_NOREG     =   "Content has not registered\n";
  private final static String MSG_CONN_BANK_ERR     =   "Unable to connect with Bank\n";
  private final static String MSG_CONN_CONTENT_ERR  =   "Unable to connect with Content\n";
  private final static String MSG_BIND_ERR          =   "Store unable to listen on given port\n";
  private final static String MSG_BIND_OK           =   "Store waiting for incoming connections\n";

  
  
  public static void main(String[] args){
    
    Store myStore = null;
    
    try {
      myStore = new Store(args);
      System.err.println(MSG_BIND_OK);
      myStore.myNbServer.start();
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    
    
  }
  
  
  
  
  /**
   * Store Constructor
   * @param args        Port And Stock File Path
   * @throws Exception  On Argument Error And File Loading Error
   */
  Store(String[] args) throws Exception{


    // Create Non-blocking Server, And Assign Callback.
    this.myNbServer     = new NbServer(this);
    
    
    // Initialize Stock Map
    this.stockMap = new HashMap<Long, Double>();
    
    
    // Check Argument Count
    int argCount = 0;
    argCount = args.length;
    if (argCount != 3){
      throw new Exception(MSG_ARGUMENT_ERR);
    }
    
    
    // Get port number
    int port;
    try{
      port = Integer.parseInt(args[0]);
    }catch(NumberFormatException e){
      throw new Exception(MSG_ARGUMENT_ERR);
    }
    this.myAddr = new InetSocketAddress("127.0.0.1", port);
    
    
    // Get Stock File Path And Load To Map
    String path = args[1];
    try{
      loadStockFile(path);
    }catch(Exception e){
      throw new Exception(MSG_ARGUMENT_ERR);
    }
    
    
    // Get Name Server Port
    int nsPort;
    try{
      nsPort = Integer.parseInt(args[2]);
    }catch(NumberFormatException e){
      throw new Exception(MSG_ARGUMENT_ERR);
    }
    this.nsAddr = new InetSocketAddress("127.0.0.1", nsPort);
    
    
    // Register MySelf To Name Server
    try{
      NetworkUtils.rpcRegister("Store", this.myAddr, this.nsAddr);
    }catch(Exception e){
      throw new Exception(MSG_REGISTER_ERR);
    }
    
    
    // Lookup Bank Address
    try{
      this.bankAddr = NetworkUtils.rpcLookup("Bank", this.nsAddr);
    }catch(Exception e){
   // TODO Better Distinguish Fail And Not Exist
      throw new Exception(MSG_BANK_NOREG);
    }
    
    
    // Lookup Content Address
    try{
      this.contentAddr = NetworkUtils.rpcLookup("Content", this.nsAddr);
    }catch(Exception e){
      // TODO Better Distinguish Fail And Not Exist
      throw new Exception(MSG_CONTENT_NOREG);
    }
    
    
    // Connect Bank
    try{
      this.bankClient = new TCPClient( this.bankAddr );
    } catch(Exception e) {
      throw new Exception(MSG_CONN_BANK_ERR);
    }
    
    
    // Connect Content
    try{
      this.contentClient = new TCPClient( this.contentAddr );
    } catch(Exception e) {
      throw new Exception(MSG_CONN_CONTENT_ERR);
    }
    
    
    // Bind Port
    try{
      this.myNbServer.bind(port);
    }catch(Exception e){
      throw new Exception(MSG_BIND_ERR);
    }
    
    
    
  }
  
  
  
  
  
  /**
   * Load Stock File To Map
   * @param path            The File Path
   * @throws Exception 
   */
  private void loadStockFile(String path) throws Exception{
    
    
    BufferedReader br = new BufferedReader(new FileReader(path));
    try {
      String line = br.readLine();
      while (line != null) {
        
        //Ignore Empty Line
        if( line.equals("") ){
          continue;
        }
        
        String[] split = line.split(" ");
        
        //Id Must Be 10 Digits
        if( split[0].length() != 10 ){
          throw new Exception();
        }
        
        Long    itemId      =   Long.   parseLong   ( split[0] );
        Double  itemPrice   =   Double. parseDouble ( split[1] );
        this.stockMap.put(itemId, itemPrice);
        
        line = br.readLine();
        
      }
    } finally {
      
      br.close();
      
    }
  }

  
  
  
  
  
  /**
   * Reply Query Request
   * @param requestData     Request Data
   * @return                All Items Id With Price
   */
  private ByteBuffer query(ByteBuffer requestData){
    
    
    // Construct Response Data
    ByteBuffer responseData = ByteBuffer.allocate(1024);
    responseData.putInt(RT_STORE_RSP_QUERY);
    
    
    // Put Total Items Count
    int itemSize = this.stockMap.size();
    responseData.putInt( itemSize );
    
    
    // For Each Item, Put ID And Price
    Iterator<Entry<Long, Double>> itr = 
        this.stockMap.entrySet().iterator();
    
    while ( itr.hasNext() ){
      Entry<Long, Double> item = itr.next();
      
      String id     =   item.getKey().toString();
      Double price  =   item.getValue();
      
      XDRParser.putFixString(responseData, id);
      responseData.putDouble(price);
    }
    
    
    // Flip The Data
    responseData.flip();
    
    
    return responseData;
    
  }
  
  
  
  
  
  
  private ByteBuffer buy(ByteBuffer requestData){
    return null;
    
  }
  
  
  
  
  
  private boolean rpcTransaction( long id, Double price, String ccn ) throws Exception {
    
    
    // Construct Register Request Data
    ByteBuffer requestData = ByteBuffer.allocate(1024);
    requestData.putInt      (RT_BANK_REQ_TRANS);
    
    
    // Put Arguments In
    XDRParser.putFixString(requestData, Long.toString(id));
    requestData.putDouble(price);
    XDRParser.putFixString(requestData, ccn);
    
    
    // Send Request To Bank
    requestData.flip();
    ByteBuffer responseData = this.bankClient.request(requestData);
    
    
    
    return false;
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
    
    if( requestType == RT_STORE_REQ_QUERY ){
      return query(requestData);
      
      
    }else if( requestType == RT_STORE_REQ_BUY ){
      return buy(requestData);
      
    }else{
      
      // Throw Error When Illegal Packet, Connection Will Be Closed.
      throw new Exception("no such method for this server");
    }
    

  }
}
