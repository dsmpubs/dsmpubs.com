

import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.DailyRollingFileAppender;
 
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;



//main class	
public class pubmed {
	
	
	public static Logger log;  // this is the DSMPubs logger, used across the dmspubs app.
	public static String urisearch = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=";
	public static String urisummary = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id=";
	public static String urifetch = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=";
	
	public static SimpleDateFormat mysqldate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static SimpleDateFormat entrezdate = new SimpleDateFormat("yyyy/MM/dd");				// entrez is the query language API for the NIH Pubmed db

	//record for dsmpubs.searchquery table 
	public class queryrec {
		public int uid; public String email; public String query; public Timestamp createdate;
		public String datenow = new Date().toString() ;
		public queryrec(){};
		public queryrec(int u, String e, String q, Timestamp d){this.uid=u; this.email=e; this.query=q; this.createdate = d;}
	}
	
	public static List<queryrec> querylist = new ArrayList<queryrec>();
	public static List<article> articlelist = new ArrayList<article>();
	
	public static void main(String[] args) throws IOException, XMLStreamException, FactoryConfigurationError {
        System.out.println("dsmpubs.pubmed.main() \n");
		
        pubmed pm = new pubmed();

		log = pm.getLogger();	
        log.info("main entry point.");
       
	   // query the database for pubmed query searches
        // orders the searches by user id ascending
        //TODO: pull the user emails for later use.
        pm.populatequerylist();
		
		// create a Pubmed 'entrez' API data-range for searching last 24 hours 
		// create a mysql data-range for searching last 5 days
		long ms1 = new Date().getTime();
		long ms2 = ms1-86400000;   // -1 day by millisecs
		long ms3 = ms1-432000000;  // -5 days by millisecs
		
		String dStr = entrezdate.format(new Date(ms2))+":"+entrezdate.format(new Date(ms1))+"[Publication Date]";  // last 24 hours date range
        
        //iterate by user
        for (queryrec qr: querylist){
				log.info("uid:"+qr.uid);
        		//TODO: clean up extra spaces etc on the client side or in the PHP write-query-to-db api.
	        	String q = qr.query.replaceAll("  ", " ").trim();
	        	q = q.replace("( ", "(").replace(" )" , ")");
	        	q = q.replace(" ", "+").replace("," , "");
				q+= "AND+"+dStr;
				System.out.println(q+"\n");
				
	        	URL url = null;
	        	URI uri = null;
	        	
	        	// the 'search' function returns a list of article ids given a search query
	        	try {
	        	      url = new URL(urisearch + q);
	        	      String nullFragment = null;
	        	      uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);
	        	      System.out.println("URI " + uri.toString() + " is OK\n");
					  log.info("searchIds URI:"+ uri.toString() );
	        	    } catch (MalformedURLException e) {
	        	      System.out.println("URL " + urisearch + q + " is a malformed URL\n");
					  log.info("malformed URL:"+ urisearch + q);
	        	    } catch (URISyntaxException e) {
	        	      System.out.println("URI " + urisearch + q + " is a malformed URL\n");
					  log.info("malformed URI:"+ urisearch + q);
	        	    }
	        	
	            httpclient http = new httpclient(uri);
				String xml = http.get(); // here's the IOException
				// parse an Article(s) ID List from the XML result
	            xmlparser parser = new xmlparser(xml);
	            String idlist = parser.fetchidstring();
	            System.out.print("idlist:"+idlist + "\n");
				log.info("idlist:"+idlist);
	            parser.close();
	            parser = null;
				
				
				//If we found some articles then process, else fall thru to next for (next user / next search)...
				if (idlist.length()>0){
					// prune id string by comparing to previous 5 day's Ids for this user
					idlist = pm.auditarticleids(qr.uid, idlist);
					System.out.print(idlist + "\n");
					log.info("idlist audited:"+idlist);
					
					
					// the 'fetch' function returns a synopsis of article(s) given article id(s)
					try {
						  url = new URL(urifetch + idlist + "&retmode=xml");
						  String nullFragment = null;
						  uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);
						  System.out.println("URI " + uri.toString() + " is OK\n");
						} catch (MalformedURLException e) {
						  System.out.println("URL " + urisearch + q + " is a malformed URL\n");
						} catch (URISyntaxException e) {
						  System.out.println("URI " + urisearch + q + " is a malformed URL\n");
						}
					http.uri = uri; 
					log.info("Fetch:"+uri);
					xml = http.get(); // here's the IOException

					// parse Article summary(s) form the result XML
					parser = new xmlparser(xml);
					articlelist = parser.listarticles();
			  
					// cache the articles in mysql
				   int rows =  pm.populatearticlestable(qr.uid, qr.email, articlelist);
				   
				   // using the articlelist create and send emails
				   //TODO: create secondary emailing function using data from mysql in case we have to resend
				   // we are iterating the querylist by userid, we have an email for each query, email this users articles Alerts
				   //TODO: thread this out...
				   pm.alertuser(qr.email, articlelist);
				   }
        	}
        
	}
	
	
	public String auditarticleids(int uid, String idlist){
       Connection cnx = null;
        Statement query = null;
        ResultSet dataset = null;
		long ms3 = new Date().getTime() - 432000000;  // -5 days by millisecs;
		String datepast = mysqldate.format(new Date(ms3));
		String sqlinlist = idlist;  //copy idlist
		sqlinlist = "('" + sqlinlist.replace(",","','") + "')";
		String sql = "select pubmedid from dsmpubsearch.articles "
					 + " where uid = " +uid 
					 + " AND createdate > '" + datepast
					 + "' AND pubmedid IN " + sqlinlist
					 + " order by pubmedid";

		try {Class.forName("com.mysql.jdbc.Driver").newInstance(); } 
        catch (Exception ex) { System.out.println("hmmm, class not found exception...?\n");   }
        try{
	    	cnx = DriverManager.getConnection("jdbc:mysql://localhost/dsmpubsearch?user=root&password=masomurray");
			//cnx = DriverManager.getConnection("jdbc:mysql://54.243.178.189/dsmpubsearch?user=root&password=masomurray");
	    	query = cnx.createStatement();
	    	dataset = query.executeQuery(sql);
	    	while (dataset.next()){
	    		//System.out.println(dataset.getString("query"));
				// remove dupe ids, remove any remaining double commas, from idlist
				idlist = idlist.replace(dataset.getString("pubmedid") , "").replace(",," , ",");  
	    	}
	    }	
        catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()+"\n");
			System.out.println("SQLState: " + ex.getSQLState()+"\n");
			System.out.println("VendorError: " + ex.getErrorCode()+"\n");
    	}
        finally {  
    	 		if (dataset  != null) {
    	 			try {dataset.close();} 
    	 			catch (SQLException sqlEx) { } // ignore
    	 			dataset  = null;
    	 		}

    	 		if (query != null) {
	    	        try {query.close();} 
	    	        catch (SQLException sqlEx) { } // ignore
	    	        query = null;
    	 		}
    	}
		//trim any beginning and trailing commas (if last pubmedid in list was removed)
		if (idlist.substring(idlist.length()-1).equals(",")){idlist = idlist.substring(0,idlist.length()-1);}
		if (idlist.substring(0,1).equals(",")){idlist = idlist.substring(1,idlist.length());}
		return idlist;
	}
	
	//TODO: generate email templates for HTML, plain text and SMS...
	public void alertuser(String emailto, List<article> alist){
		
		DateFormat fulldate = DateFormat.getDateInstance(DateFormat.FULL, Locale.US);
		StringBuilder sbbody = new StringBuilder("<table style='width:100%;'><tr><td style='background:#CDC; text-align:center;' ><br /><h3>DSM Article Alerts for " + fulldate.format(new Date()) +"</h3><br /></td></tr></table>" );
		
		int i=1;
		
		 for (article art : alist){
			 	String bg = i++ % 2 == 0 ? "<table style='background:#D8E8D8;padding:8px;'>" : "<table style='background:#DED;padding:8px;'>";
	        	// we already converted null values to empty fields "" in  populatearticlestable().
			    sbbody.append(bg + "<tr><td><h4><strong>"+art.atitle+"</strong></h4></td></tr>");
	        	sbbody.append("<tr><td style='font-family:Tahoma,Georgia,sans-serif;'><h4>"+art.journal+"&nbsp; : &nbsp; " +art.pubMM+"-"+art.pubdd+"-"+art.pubyy+"</h4></td></tr>");
	        	sbbody.append("<tr><td style='font-family:Tahoma,Georgia,sans-serif;'><p><font size='2'>"+art.aabstract.replace("\r\n", "<br />")+"</font></p></td></tr>");
	        	sbbody.append(art.acopyrite.length()> 0 ? "<tr><td style='font-family:Tahoma,Georgia,sans-serif;'><h5><i><u>"+art.acopyrite+"</u></i></h5></td></tr></table><br />" : "</table><br />");
		 	}
		 
		 smtp email = new smtp();
		 email.sendmail(emailto, sbbody.toString());
		 log.info("Emailed:"+emailto);	
		}


	
	// populate the pubmed.querylist list of user querys from the mysql searchqueries table.
	// the searchqueries table is populated from the webserver in an ajax call from dsmpubs.js to index.php
    public void populatequerylist(){
    	
        Connection cnx = null;
        Statement query = null;
        ResultSet dataset = null;
        //SimpleDateFormat mysqldate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = " select u.email, s.uid, s.query, s.createdate " +
        			 " from dsmpubsearch.searchqueries s join dsmpubsearch.users u on s.uid = u.idusers " +
        			 " where s.active = 1 ORDER by uid asc;";
        
        try {Class.forName("com.mysql.jdbc.Driver").newInstance(); } 
        catch (Exception ex) { System.out.println("hmmm, class not found exception...?");   }
        try{
	    	cnx = DriverManager.getConnection("jdbc:mysql://localhost/dsmpubsearch?user=root&password=masomurray");
			//cnx = DriverManager.getConnection("jdbc:mysql://54.243.178.189/dsmpubsearch?user=root&password=masomurray");
	    	query = cnx.createStatement();
	    	dataset = query.executeQuery(sql);
	    	while (dataset.next()){
	    		//System.out.println(dataset.getString("query"));
	    		querylist.add(new queryrec(dataset.getInt("uid"), dataset.getString("email"), dataset.getString("query"), dataset.getTimestamp("createdate")));
	    	}
	    }	
        catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
    	}
        finally {  
    	 		if (dataset  != null) {
    	 			try {dataset.close();} 
    	 			catch (SQLException sqlEx) { } // ignore
    	 			dataset  = null;
    	 		}

    	 		if (query != null) {
	    	        try {query.close();} 
	    	        catch (SQLException sqlEx) { } // ignore
	    	        query = null;
    	 		}
    	}
    
    }
        

    
 // populate the pubmed.articles table with search results
    public int populatearticlestable(int uid, String email, List<article> alist){
    	
        Connection cnx = null;
        Statement query = null;
        ResultSet dataset = null;
        //SimpleDateFormat mysqldate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int nrows = 0;
        
        String vals = "";
        String tmp, journal, title, copyrite, aabstract;
        tmp = journal = title = copyrite = aabstract = "";
        String sql = "INSERT INTO dsmpubsearch.articles (uid, emailto, pubmedid, issn, journal, articletitle, articlecopyrite, abstract, pubyy, pubMM, pubdd, createdate, active, publish) VALUES ";
        for (article art : alist){
        	//TODO: better validate everything (not just text fields).
        	// initialize all fields (ensure all fields have atleast an empty value)
        	art.aabstract = art.aabstract == null ? "" : art.aabstract;
        	art.acopyrite = art.acopyrite == null ? "" : art.acopyrite;
        	art.atitle = art.atitle == null ? "" : art.atitle ;
        	art.journal = art.journal == null ? "" : art.journal ;
        	
        	journal = art.journal.length()> 0 ? art.journal.replace("'", "''") : "";
        	title = art.atitle.length()> 0 ? art.atitle.replace("'", "''") : "";
        	copyrite = art.acopyrite.length()> 0 ? art.acopyrite.replace("'", "''") : "";
        	aabstract = art.aabstract.length()> 0 ? art.aabstract.replace("'", "''") : "";
        	
       		vals+= "(" + uid + ", '" +
       					email + "', '" +
    					art.pubmedid +"', '" +
    					art.issn + "', '" +
    					journal + "', '" +
    					title + "', '" +
    					copyrite +"', '" +
    					aabstract +"', '" +
    					art.pubyy + "', '" +
    					art.pubMM + "', '" +
    					art.pubdd +"', '" +
    					mysqldate.format(new Date()) +"', " +		// createdate
    					1+", " +									// active flag
    					1+"),";										// publish flag
        	}
        vals = vals.substring(0, vals.length()-1);					// strip last comma
        sql += vals + ";";
        try {Class.forName("com.mysql.jdbc.Driver").newInstance(); } 
        catch (Exception ex) { System.out.println("hmmm, class not found exception...?");   }
        try{
	    	cnx = DriverManager.getConnection("jdbc:mysql://localhost/dsmpubsearch?user=root&password=masomurray");
			//cnx = DriverManager.getConnection("jdbc:mysql://54.243.178.189/dsmpubsearch?user=root&password=masomurray");
	    	query = cnx.createStatement();
	    	nrows += query.executeUpdate(sql);
        	}	
        catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
    	}
        finally {  
    	 		if (query != null) {
    	 			try {query.close();} 
    	 			catch (SQLException sqlEx) { } // ignore
    	 			query  = null;
    	 		}

    	 		if (cnx != null) {
	    	        try {cnx.close();} 
	    	        catch (SQLException sqlEx) { } // ignore
	    	        cnx = null;
    	 		}
    	}
        return nrows;
    } // populatearticlestable
        

	public Logger getLogger	(){
		PatternLayout layout = new PatternLayout();
		//            priority-date-loggername-Method-msg-linefeed   
        String conversionPattern = "[%p] %d %c %M - %m%n";
        layout.setConversionPattern(conversionPattern);
 
        // creates daily rolling file appender
        DailyRollingFileAppender rollingAppender = new DailyRollingFileAppender();
        rollingAppender.setFile("../logs/dsmpubs.log");
        rollingAppender.setDatePattern("'.'yyyy-MM-dd");
        rollingAppender.setLayout(layout);
        rollingAppender.activateOptions();
 
        // configures the root logger
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(rollingAppender);
 
        // creates a custom logger and log messages
        return Logger.getLogger("DSMPubs");
	}
    
    
    

}//pubmed
