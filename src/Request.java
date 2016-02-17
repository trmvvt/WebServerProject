import java.io.*;
import java.lang.String;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Hashtable;

/**
 *
 * Description: Used to store and process requests that come from the client
 * to the server.
 *
 * @author Poushali Banerjee
 * @version 2.6
 */
public class Request {

    private String method;
    private static String firstRequestLine;
    private String request_URI, queryString;
    private String HTTPversion;
    
    private static Hashtable<String, String> headers = new Hashtable<String, String>();
    private Hashtable<String, String> variables = new Hashtable<String, String>();
    Vector<String> queryVar = new Vector();
    Vector<String> messageBody = new Vector();
     Vector headerVec = new Vector();
    
  /**
   * Default constructor used to reset your variables and data structures for
   * each new incoming request.
   */
  public Request() {

  }

  /**
   * 
   * @return the name of the method
   */
  public String getMethod(){
	  return method;
  }	//	end getMethod
  
  /** 
   * Returns the first line in the HTTP request.
   */
  public String getFirstLine(){
	  return firstRequestLine;
  }	//	end getFirstLine
  
  /**
   * 
   * @return the whole request URI
   */
  public String getRequest_URI(){
	  return request_URI;
  }	//	end getRequest_uri
  
  /**
   * 
   * @return extra path information provided after the script name
   */
  public String getPath(){
      int index = request_URI.indexOf(".");
      int index1 = request_URI.indexOf("/", index);
      int index2 = request_URI.indexOf("?");
      //end before query string starts
      return request_URI.substring(index1+1, index2 -1);
      
  }
  
  /**
   * 
   * @return the name of the scipt only/
   * gives entire path
   * file requested
   */
  public String getScriptName(){
      int index = request_URI.indexOf("/");
      int index2 = request_URI.indexOf(".");
 
      if(index2 == -1){
          return "index.html";
      }
      int index3 = request_URI.indexOf("/", index2);
      if(index3 == -1){
          
          return request_URI.substring(index+1);
      }
      return request_URI.substring(index+1, index3);
  }
  
  /**
   * 
   * @return the query string
   */
  public String getQueryString(){
      return queryString;
  }
  public String getHTTPVersion(){
	  return HTTPversion;
  }	//	end getVersion
  
  /** Get the field value of the specified header. 
   *  Returns null if the header was not included in the HTTP request. 
   */
  public String getHeaderValue(String target){
      //if(headers.contains(target)){
        return headers.get(target);  
      //}
        
  }	//	end getHeaderValue
  
  
  /**
   * Get the value of the variable requested in the query String
   */
  public String getVarValue(String var){
      return variables.get(var);
  }
  /**
   * Parse the incoming request depending on the type of request you are
   * receiving. This information is found from the first line of the incoming
   * request. You will also want to check and make sure the request you are
   * receiving is a valid request. If the request is not valid, throw an error
   * using the http error codes.
   *
   * @param inMsg BufferedReader which grabs the incoming message from the
   *   client socket
   */
  public void parse(BufferedReader inMsg) throws IOException {
    
      firstRequestLine = inMsg.readLine();     
      parseFirstLine(firstRequestLine);
      createRequest(inMsg);
  }

  /**
   * Used to first check whether a requested file path has an alias set within
   * the configuration file and if so, replaces the alias of the file or
   * directory with the real path. This way, the server can find the right file
   * in the tree.
   *
   * HINT: Remember that any one path can have multiple aliases found within the
   * httpd.conf file. For example, the URI
   * http://www.blah.net/blah/help/hello.html could have an alias where blah is
   * equivalent to http://www.blah.net/blah_blah_blah and help could be an alias
   * for http://www.blah.net/blah_blah_blah/bleh/help. Another thing to note is
   * that your URI could also include script aliases which means you may be
   * required to run a cgi script.
   *
   * @param config HttpdConf Object which contains all the information on how
   *   the server is configured.
   */
  public void setHttpdConf(HttpdConf config) {

      if(config.isScript(firstRequestLine)){
       firstRequestLine = config.solveAlias(firstRequestLine);   
      }
      
  }
	//function to print out all the information from the request. Used for debugging
  /**
   * Print function used for debugging purposes. Helpful to make sure you are
   * parsing the incoming request properly.
   */
  public void print() {
    System.out.println("The method was " + method);
    System.out.println("The Request-URI was " + request_URI);
    //System.out.println("The query string was " + query);
    System.out.println("The HTTP version is " + HTTPversion);

    System.out.println("\nThe following headers were included:");
    for(String header : headers.keySet()){
    	System.out.println(header + ": " + headers.get(header));
    }	//	end for

    //System.out.println("The message body was: \n" + body);
  }

  /**
   * private function used by request object to parse the information passed
   * through from the client to the server and save it for future use. The type
   * of request can be found on this first line of the request.
   *
   * @param first String representation of the first line of the request from
   *   the client. Passed in as one long string which can easily be parsed.
   */
  private void parseFirstLine(String first) {

     StringTokenizer st = new StringTokenizer(first);
     while(st.hasMoreElements()){
         method = st.nextToken();
         request_URI = st.nextToken();
         if(request_URI.contains("?")){
             int index = request_URI.indexOf("?");
             queryString = request_URI.substring(index+1);
             if(queryString.contains("+")){
                 queryString.replace('+', ' ');
             }
             if(queryString.contains("&")){
              StringTokenizer st2 = new StringTokenizer(queryString);
              while(st2.hasMoreElements()){
                  String token = st2.nextToken("&");
                  int index1 = token.indexOf("=");
                  setVarFirstLine(token.substring(0, index1), token.substring(index1+1));
                  
              }
         }
             else {
                 int index2 = queryString.indexOf("=");
                 setVarFirstLine(queryString.substring(0, index2), queryString.substring(index2+1));
             }
         
         }
         
         if(request_URI.contains(".cgi") || request_URI.contains(".pl") || request_URI.contains(".py")
                 || request_URI.contains(".php")){
            WebServer.setCGI = true;
         }
         HTTPversion = st.nextToken();
     }
 
  }

  /**
   * private function used by the request object to determine whether an incoming
   * request is a valid request or not. Useful when throwing error messages.
   *
   * @return true if request is valid, false otherwise
   */
  //private boolean checkRequest() {

  //}

  /**
   * private function used by the request object to grab variables that may have
   * been passed to the server when the request was made. Remember that GET and
   * HEAD requests include their variables on the first line of the request while
   * POST and PUT requests include their variables within the body of the
   * message.
   */
  
  //To get variables that were included in first line
  //for GET, HEAD
  private void setVarFirstLine(String arg1, String arg2) {
      variables.put(arg1, arg2);
      queryVar.add(arg1);
  }

  /**
   * private function used by the request object to grab variables that may have
   * been passed to the server when the request was made. Remember that POST and
   * PUT requests include their variables within the body of the message and not
   * in the first line, so another method is needed to retrieve these variables.
   */
  //To get variables from body
  //in case of POST, PUT
  private void setVarNotFirstLine(String arg1, String arg2) {

      variables.put(arg1, arg2);
      queryVar.add(arg1);
  }

  /**
   * private function used by the request object to parse the rest of the request
   * message (e.g. other headers and the body of the message) from the client so
   * it can be used later when actual processing of the request happens.
   *
   * @param inFile BufferedReader object that comes through the socket. Needs to
   *   be processed properly before the data stored within it can be used.
   */
  private void createRequest(BufferedReader inFile) throws IOException {

	  int index;
	  String header, value;          
	  
	  try{
		  String requestLine = inFile.readLine().trim();
		  
		  //	Parse the header(s)
		  //while(!requestLine.isEmpty()){
                  while(requestLine.length() != 0){
                 
			 if(requestLine.contains(":")){
                             index = requestLine.indexOf(":");
                             header = requestLine.substring(0, index).trim();
			  value = requestLine.substring(index + 1).trim();
			  headers.put(header, value);
                          headerVec.add(header);  
                          
                         }
                           requestLine = inFile.readLine().trim();
                  }
                  
                        //if the message body contains a queryString
                         //parse and store it
                         //else store the body in a vector
			  if(method.equals("POST") ||
                          method.equals("PUT")){
                              requestLine = inFile.readLine().trim();
                          
                          while(!requestLine.isEmpty()){
                              
                              if (requestLine.contains("=")) {
                                  if (requestLine.contains("&")) {
                                      StringTokenizer st2 = new StringTokenizer(requestLine);
                                      while (st2.hasMoreElements()) {
                                          String token = st2.nextToken("&");
                                          int indexEq = token.indexOf("=");
                                          setVarNotFirstLine(token.substring(0, indexEq), token.substring(indexEq + 1));

                                      }
                                  } else {
                                      int indexEq = requestLine.indexOf("=");
                                      setVarNotFirstLine(requestLine.substring(0, indexEq), requestLine.substring(indexEq + 1));
                                  }
                                  
                              }
                              else{
                                messageBody.add(requestLine);  
                              }
                              requestLine = inFile.readLine().trim();
                          }
                          }
		  	//	end while
		                    
		  // Process the request body, if any (PENDING)!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		  
    
	  }
	  catch(NullPointerException e){
		  // End of the stream has been reached 
	  }	//	end try
     
  
}
}
