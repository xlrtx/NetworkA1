import java.nio.ByteBuffer;

/**
 * Callback Interface For Non-blocking Servers To
 * Consume The Request Data.
 * @author s4337746
 *
 */
public interface NbServerCallback {
  
  
  ByteBuffer consumeRequest(ByteBuffer requestData) throws Exception;
  
  
}
