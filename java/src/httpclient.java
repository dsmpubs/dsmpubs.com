

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

// use the 'fluent' facade API
// fluent = connection and resource management, simple use-cases; 
// fluent auto-buffers to memory but we can end-run memory buffering with RequestHandler...

public class httpclient {
	
	public URI uri = null;
	public CloseableHttpClient client = null;
	public HttpGet httpget;
	public CloseableHttpResponse resp = null;
	
	httpclient(URI uri){
		this.uri = uri;
		this.client = HttpClients.createDefault();
		//this.httpget = new HttpGet(u);
		}
	
	
	public String get() throws IOException{ 
		
		String xml ="";
		try{
			this.httpget = new HttpGet(this.uri);
			this.resp = client.execute(this.httpget);
			HttpEntity body = this.resp.getEntity();
			xml = EntityUtils.toString(body);
			EntityUtils.consume(body);
		}
		catch (Exception ex){System.out.println("Exception: "+ ex.getMessage()); ex.printStackTrace(); pubmed.log.error(ex.getMessage());}
		finally{this.resp.close();}
		return xml;
	}
	
}
	
	
	
	
	


