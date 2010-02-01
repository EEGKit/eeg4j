package it.hakvoort.neuroclient.reply;

import it.hakvoort.neuroclient.EDFHeader;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class HeaderReply extends DefaultReply {
	
	public EDFHeader header;
	
	public HeaderReply() {
		
	}
	
	public HeaderReply(ResponseCode response, EDFHeader header) {
		super(response);
		this.header = header;
	}
	
	@Override
	public String toString() {
		return String.format("response: %s, header: %s", response, header);
	}
}
