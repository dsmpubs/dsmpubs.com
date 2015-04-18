package dsmpubs;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

// use the 'fluent' facade API
// fluent = connection and resource management, simple use-cases; 
// fluent auto-buffers to memory but we can end-run memory buffering with RequestHandler...

public class fluenthttp {
		
		public String url = "";
		
		public fluenthttp(String u){
			this.url = u;
			System.out.println("fhttp constructor url="+this.url);
			}
		
		public String get(){
			String xml = "";
			try{
				xml = Request.Get(url)
				        .connectTimeout(10000)
				        .socketTimeout(10000)
				        .execute().returnContent().asString();
			}
			catch(ClientProtocolException cpex){ System.out.println("ClientProtocolException: " + cpex.getMessage()); }
			catch(IOException ioex){System.out.println("IOException: " +ioex.getMessage()) ;}
			return xml;
		}


}

	

