package it.hakvoort.neuroclient.reply;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public interface Reply {
	
	// Response codes
	public enum ResponseCode {OK, ERROR};
	
	public ResponseCode getResponseCode();
	public void setResponseCode(ResponseCode response);
}
