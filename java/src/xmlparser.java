

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.stream.*;
import javax.xml.stream.events.*;
import javax.xml.stream.util.StreamReaderDelegate;

// using the StAX forward cursor
public class xmlparser {
	
	
	public XMLStreamReader rdr = null;  // XMLEventReader is the forward cursor StAX reader (?)
										// StAX reader delegate over-ride: tracks Node and nodelevel
	public String xml = "";
	public Stack<String> nodestack = new Stack<String>();
	public int nodelevel = 0;
	
	// USE: pass in the XML to be parsed:
	// The constrcutor builds the parser:
	//The parser is forward-only cursor XMLStreamReader object with the next() function overidden to manage a nodestack and nodelevel int.
	public xmlparser(String strxml) throws XMLStreamException, FactoryConfigurationError {
		this.xml = strxml;
		XMLInputFactory factory = XMLInputFactory.newInstance();
		InputStream instr = new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8)); 
		this.rdr = factory.createXMLStreamReader(instr);
		//our StAX XMLStreamReader uses a delegates a lambda herebelow to track node and nodelevel
		this.rdr = new StreamReaderDelegate(this.rdr) { // push current node tag to stack; increment nodelevel, pop and decrement
			public int next() throws XMLStreamException {
					int event = super.next();
					String trace = "";
					switch (event){
						case XMLStreamConstants.START_ELEMENT:
							nodestack.push(super.getLocalName());
							nodelevel++;
							//System.out.println(nodelevel+" - "+nodestack.peek());
							break;
						case XMLStreamConstants.END_ELEMENT:
							if ((super.getLocalName())== nodestack.peek()){
								trace = nodestack.peek(); // debug, get peek() before popping...
								nodestack.pop();
								--nodelevel;
								//System.out.println(nodelevel+" - "+trace);
							}
							break;
						} //switch
					return event;
				} // next() override
			public int nextTag() throws XMLStreamException {
				int event = super.nextTag();
				switch (event){
					case XMLStreamConstants.START_ELEMENT:
						nodestack.push(super.getLocalName());
						nodelevel++;
						System.out.println(nodelevel+" - "+nodestack.peek());
						break;
					case XMLStreamConstants.END_ELEMENT:
						if ((super.getLocalName())== nodestack.peek()){
							nodestack.pop();
							nodelevel--;
						}
						if(nodelevel>0) System.out.println(nodelevel+" - "+nodestack.peek());
						break;
					} //switch
				return event;
			} // next() override
			
			}; // lambda
	}// constructor
	
	
	public void close(){
		try {this.rdr.close(); rdr=null;} 
		catch (XMLStreamException e) {e.printStackTrace();}
		}
	
	
	//TODO: rewrite using delegated rdr's nodestack and nodelevel tracking...
	// Use the constructor to pass in XML from medline search; parse for ArticleIds.
	// return a list of ArticleIds
	public String fetchidstring(){
		int eventType = -1;
		String tag = "";
		String val = "";
		boolean endflag = false;
		// TODO: cleanup, make shallow, just look for idlist and ID tags... break as soon as domne w ID tags.
		try {
			while (this.rdr.hasNext() & !endflag) {
				eventType = this.rdr.next();
				if (eventType == XMLStreamConstants.START_ELEMENT) {
					System.out.println(eventType);
					System.out.println(this.rdr.getLocalName());
					while (this.rdr.hasNext() & !endflag){
						if (this.rdr.next() == XMLEvent.START_ELEMENT){ 
							if (this.rdr.getLocalName()== "IdList"){
								while (this.rdr.hasNext() & !endflag){
									if (this.rdr.next() == XMLEvent.START_ELEMENT) {
										tag = this.rdr.getLocalName();
										switch (tag){
										case "Id" :
											val += this.rdr.getElementText() + ",";
											break;
										case "TranslationSet":
											endflag = true;
											break;
										default: // TODO: strip last comma, this is the end of the contiguous Ids list
											break;
										} // switch
									} //if
								} //while
							} //if	
						}//if
					}//while
				 }//if
			}//while
		} //try
		catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			pubmed.log.info("rdr.hasnext() error in fetchidstring():"+e.getMessage());
		}	
		// if no ID's found return empty string
		if (val.length()>0) {return val.substring(0, val.length() - 1);} //strip last comma		
		return "";
	}

	
	// Use constructor to pass in XZML from a pubmed fetch operation:
	// Return a list of populated article-type objects
	public List<article> listarticles(){
			//List<article> articles = new ArrayList<article>();
			int eventType = -1;
			String tag = "";
			String val = "";
			boolean endflag = false;
			List<article> articles = new ArrayList<article>();
			article art = null;
			try {
				eventType = this.rdr.getEventType();
				while (this.rdr.hasNext()) {
					//find the root element
					//eventType = rdr.next();
					switch (eventType){
						case XMLStreamConstants.START_DOCUMENT:
							//System.out.println("start of xml");
							break;
						case XMLStreamConstants.START_ELEMENT:
							tag = this.rdr.getLocalName();
							switch(tag){
								case "PubmedArticle":  // enter article loop
									endflag = false;
									art = new article(); // setup new article for population while in PubmedArticle nodemap
									break;
								case "MedlineCitation":
									//MedlineCitation seems to have the info we need: 
									int l1 = nodelevel;
									while((eventType = rdr.next()) != 1){}; //advance cursor to the next opening tag
									// 1. move the cursor forward; 2. nodelevel will increment on new opening tags; 
									// 2. delegated override to next() pushes and pops nodestack and increments and decrements nodelevel).
									// 3. use getText(), getElementText() positions cursor at closing tag, next read pushes cursor to opening tag short-circuiting reader delegate.
									while ( nodelevel>= l1 ){ // while in MedlineCitation node
										switch (eventType){
											case XMLStreamConstants.START_ELEMENT :
												switch (rdr.getLocalName()){
													case "PMID":
														rdr.next(); // move into textnode
														art.pubmedid = rdr.getText(); 
														break;
													case "Article":   // under Article get Journal; ArticlerTitle; Abstract
														this.processArticle(rdr, art);
														break; // case article;
													} // switch medlinecitation childnode tag names
												break; // medlinecitation case
											case XMLStreamConstants.END_ELEMENT :
												break;
												}// end switch Medlinecitation childnodes=eventtype-startelemnet
											eventType = rdr.next();
										}//while medlinecitation
									} // switch tag - (case Start_Element)
									break;
						case XMLStreamConstants.END_ELEMENT:
							switch (tag = this.rdr.getLocalName()){
								case "PubmedArticle":  // end article node
								articles.add(art);	
								endflag = true;
								break;
							}
							break;
						}
					eventType = rdr.next();  // get next article
					}//while
				}//try
			catch (XMLStreamException ex) {ex.printStackTrace();}	
			return articles;
		}// listarticles()
		
	
	// get Article Contents: Date, Title, Abstracts
	// Called from listrrticles()
		public void processArticle(XMLStreamReader csr, article art) throws XMLStreamException{
			// cursor PubmedArticleSet<PubmedArticle><MedlineCitation><Article>
			//Target1: <Journal> has ISSN ;JournalIssue->PubDate; Title elements
			int event;
			String tag = "";
			int l1 = this.nodelevel;
			while((event = rdr.next()) != 1){}; //advance cursor to the opening tag, first node of <Article> nodemap; 
			while (nodelevel >= l1 ){ // if nodelevel incremented parse the child nodemap (ie Article node has values...);
				switch (event){
					case XMLStreamConstants.START_ELEMENT :
						tag = csr.getLocalName();
						switch (tag){
							case "Journal" :
								int l2 = nodelevel;
								while((event = rdr.next()) != 1){};  // alias for nextTag() w/o the exceptions 
								while(nodelevel>=l2){  //Does <Journal> have child nodes? While in Journal
									switch (event){
										case XMLStreamConstants.START_ELEMENT :
											tag = csr.getLocalName();
											switch (tag){
												case "ISSN":	//Journal ISSN
													csr.next(); // move into textnode
													art.issn = csr.getText(); // use getText()?, getEelemntText() positions cursor at closing tag? next read will push cursor to opening tag short-circuiting reader delegete.
									                break; //fall thru to csr.next(), sg hould get closing tag eventaully
												case "Title":  //Journal Title
													csr.next();
													art.journal = csr.getText(); 
													break;
												case "JournalIssue" : //Journal PubDate
													//special case done inline - traverse <JournalIssue> to get <PubDate>members
													int l3 = this.nodelevel;
													boolean bPubdate = false;
													while((event = rdr.next()) != 1){};  // find next open-tag
													while(nodelevel>=l3){  //Does <Journal> have child nodes? While in Journal
														switch (event){
															case XMLStreamConstants.START_ELEMENT :
																bPubdate = (csr.getLocalName() == "PubDate")? true : false;
																while(bPubdate){
																	switch (event){
																		case XMLStreamConstants.START_ELEMENT :
																			tag = csr.getLocalName();
																			switch (tag){
																				case "Year" : csr.next(); art.pubyy = csr.getText(); break;
																				case "Month" :csr.next(); art.pubMM = csr.getText(); break;
																				case "Day" : csr.next();  art.pubdd = csr.getText(); break;
																				}
																			break;
																		case XMLStreamConstants.END_ELEMENT :
																			bPubdate = (csr.getLocalName() == "PubDate")? false : true;
																			break;	
																		}
																	event = csr.next();
																	}
																break;
															}
														event = csr.next();
														}
													break;
											}//switch journal tags
											break;
										}
									event = csr.next();
									}
									break; // case Journal
							case "ArticleTitle":
								csr.next(); 
								art.atitle = csr.getText();
								break;
							case "Abstract" :
								this.processAbstract(rdr, art);
								
						} // switch 2
						break; //start_element
				} // swicth 1
				event = csr.next();
				} //while 1
			}// processArticle
	

		// Get Article Abstracts, Article copyrite
		// called from processarticle()
		public void processAbstract( XMLStreamReader csr, article art) throws XMLStreamException{
			// cursor position: <PubmedArticleSet><PubmedArticle><MedlineCitation><Article><Abstract>
			// Target: <Abstract> may have 1 or more <AbstractText> tags; <AbtsractText> tags may have a Label" attribute.> has ISSN ;JournalIssue->PubDate; Title elements
			int event;
			boolean bAbstract = true;  // this function was called because we found an <Abstract> node
			String tag = "";
			int l1 = this.nodelevel;
			while((event = csr.next()) != 1){}; //advance cursor to the opening tag, first node of <Abstract> nodemap; 
			while (nodelevel >= l1 && bAbstract ){ // if nodelevel incremented parse the child nodemap (ie Article node has values...);
				switch (event){
					case XMLStreamConstants.START_ELEMENT :
						switch (csr.getLocalName()){
							case "CopyrightInformation":
								csr.next();
								art.acopyrite = csr.getText();
								break;
							case "AbstractText":
						 		//first get any Abstract label/headings from Label attribute of <AbstractText> tag
						 		int c = rdr.getAttributeCount();
						 		for (int i=0; i<c;i++){
						 			switch (csr.getAttributeLocalName(i)){
						 				case "Label":
						 					art.aabstract += "\r\n" + csr.getAttributeValue(i) + ":\r\n";
						 					i=c; // break loop
						 					break;
						 				}
						 			}
						 		// next get text chunk of abstract text node of <AbstractText>
						 		csr.next();
						 		art.aabstract += csr.getText(); 
						 		break;
						 	}
						break;
					case XMLStreamConstants.END_ELEMENT :
						switch (csr.getLocalName()){
							case "Abstract":
								bAbstract = false;
								break;
						 	}
						break;
						}
				event = csr.next();
				}
		 } //processAbstract()
		
		

}// xmlparser class
