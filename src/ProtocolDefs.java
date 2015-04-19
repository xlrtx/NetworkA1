/**
 * Some Protocol Definition
 * @author s4337746
 *
 */

public interface ProtocolDefs {
  
  
  // Request And Response Type Definition For Name Server
  public final static int RT_NS_REQ_LOOKUP   = 0;
  public final static int RT_NS_RSP_LOOKUP   = 1;
  public final static int RT_NS_REQ_REGISTER = 2;
  public final static int RT_NS_RSP_REGISTER = 3;
  
  
  // Request And Response Type Definition For Store
  public final static int RT_STORE_REQ_QUERY = 4;
  public final static int RT_STORE_RSP_QUERY = 5;
  public final static int RT_STORE_REQ_BUY   = 6;
  public final static int RT_STORE_RSP_BUY   = 7;
  
  
  // Request And Response Type Definition For Bank
  public final static int RT_BANK_REQ_TRANS  = 8;
  public final static int RT_BANK_RSP_TRANS  = 9;
  
  
  // Response Message Definition For NS
  public final static String RSP_LKUP_OK       =   "OK\n";
  public final static String RSP_LKUP_NOTOK    =   "Error: Process has not registered with the Name Server\n";
  public final static String RSP_REG_OK        =   "OK\n";
  public final static String RSP_REG_NOTOK     =   "NOT OK\n";
  
  //Response Message Definition For Bank
  public final static String RSP_TRANS_OK      =   "OK\n";
  public final static String RSP_TRANS_NOTOK   =   "NOT OK\n";
  
  
  //Response Message Definition For Store
  public final static String RSP_BUY_OK      =   "OK\n";
  public final static String RSP_BUY_NOTOK   =   "NOT OK\n";
}
