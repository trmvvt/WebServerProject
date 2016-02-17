import java.io.*;
import java.net.Socket;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Date;
import java.util.Calendar;
import java.text.*;

/**
 *
 * Description: Used to process incoming requests to the server and generate
 * the appropriate response to that request. This is where most of the server
 * processing is done, thus making it a very important class to the
 * implementation of your server.
 * @author Poushali Banerjee
 * @version 2.3
 */
public class Response {
	
	private static Hashtable<Integer, String> statusCodes = initStatusCodes();
	private static int statusCode;		// status code of client's request
	private int contentLength;	// length of the message body sent to client
        private static Boolean permitted = true;
	
	public static final String DATE_FORMAT_NOW = "E, dd MMM yyyy HH:mm:ss z";
	public static final String PAGE_NOT_FOUND_FILE = "error/pageNotFound.html";
	public static final String BAD_REQUEST_FILE = "error/badRequest.html";
        public static final String NO_CONTENT_FILE = "error/noContent.html";
        public static final String NOT_IMPL_FILE = "error/notImpl.html";
        public static final String SERVER_ERROR = "error/internalError.html";
        public static final String FORBIDDEN = "error/forbidden.html";
        public static final String UNAUTHORIZED = "error/unauthorized.html";
        Authentication authObj = new Authentication();

	
  /**
   * Default constructor for the response object. Variables are reset and/or
   * intialized here. These variables will be used throughout request processing
   * and response generation.
   */
  public Response() {
	  statusCode = 0;
          permitted = true;
  }

  /** Returns the description of the given status code or null if the 
   *  status code is invalid.
   *
   * @param statusCode
   * @return
   */
  public String getStatusCodeDescription(int statusCode){
	  return(statusCodes.get(statusCode));
  }	//	end getStatusCodeDescription
  

  
  /**
   * Used to process the request that came from the client to the server. There
   * are many things that need to be checked and verified during the processing
   * of a request. You will need to check for authentication, errors, cgi
   * scripts, type of request, etc, and handle each condition appropriately.
   *
   * HINT: it helps to use boolean flags throughout your code to check for the
   * various conditions that may or may not occur.
   *
   * @param myRequest Request object that was generated and stores all the
   *   request information that came in from the client
   * @param env Environment object that contains the environment variables
   *   necessary for cgi script execution.
   * @return the status code of the response
   */
  public void processRequest(Request myRequest, Environment env, OutputStream out, Socket clientSocket) throws IOException, FileNotFoundException, Base64FormatException{

	  String requestMethod = myRequest.getMethod();
          checkAuthentication(myRequest, out);
	  if(!checkAuthentication(myRequest, out)){
              statusCode = 403;
              writeOutput(out, myRequest, new File(getDocumentRoot() + System.getProperty("file.separator") + FORBIDDEN));
          }
          
          if(permitted == true){
	  if(requestMethod.equalsIgnoreCase("GET")){
		  processGETMethod(myRequest, out);
	  }
	  else if(requestMethod.equalsIgnoreCase("HEAD")){
		  // Identical to GET, except the server must not return a body
		  processGETMethod(myRequest, out); 
	  }
	  else if(requestMethod.equalsIgnoreCase("POST")){
		  processPOST(myRequest,out);
	  }
	  else if(requestMethod.equalsIgnoreCase("PUT")){
		  processPUT(myRequest, out);
	  }
	  else{
		  statusCode = 501; //method not implemented
                  writeOutput(out, myRequest, new File(getDocumentRoot() + System.getProperty("file.separator") + NOT_IMPL_FILE));
	  }	//	end if
  }
          
          
          if(statusCode != 0){
            writeToLog(myRequest, clientSocket);
          }
            
  }	//	end processRequest
  private String getDateHeader(){
      return("Date: " + getDate());
  }	//	end getDateHeader
  
  
  private String getServerHeader(){
	  return("Server: Poushali Banerjee, Lee Gemma Fu-Sun");
  }	//	end getServerHeader
  
  
  private String getDate(){
	  Calendar cal = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
      return (sdf.format(cal.getTime()));
  }	//	end getDate
  
  
  /** Generates the HTTP response that corresponds to the GET method, which
   *  includes a representation of the resource specified in the request-URI.
   *  Updates the status code of the response.
   * 
   * @param myRequest contains the HTTP request from the client.
   * @return the status code of the response
   */
  private void processGETMethod(Request myRequest, OutputStream out) throws IOException{
	  String fileSeparator = System.getProperty("file.separator");
	  String documentRoot = getDocumentRoot();
	  
	  if(myRequest.getRequest_URI().equals("/")){
		  // The request-URI is a default file
		  Vector<String> defaultFiles = HttpdConf.getDefaultFiles();//get all default files
		  if(!defaultFiles.isEmpty()){//if the vector contains some files
			  File defaultFile;			  
			  for(String defaultFileName : defaultFiles){
                              //create a file for every defaultFileName
				  defaultFile = new File(documentRoot + fileSeparator + defaultFileName);
				  if(defaultFile.exists()){//if it already exists
                                      if(isFileModified(myRequest, defaultFile)){
                                          statusCode = 200;
                                      }
                                      else{
                                          statusCode = 304;
                                      } //  end if
                                      
					  writeOutput(out, myRequest, defaultFile);
					  return ;
				  }	//	end if
			  }	//	end for
			  
			  // Default files do not exist
			  statusCode = 404;
			  writeOutput(out, myRequest, new File(documentRoot + fileSeparator + PAGE_NOT_FOUND_FILE));
		  }
		  else{
			  statusCode = 404;
			  writeOutput(out, myRequest, new File(documentRoot + fileSeparator + PAGE_NOT_FOUND_FILE));
		  }	//	end if
	  }
          //request for a specific file
	  else{
              
		String requestedFileName = null;  //String requestedFileName = myRequest.getRequest_URI().substring(1);
               
                File requestedFile;
                if(WebServer.setCGI == true){
                  requestedFile = new File(documentRoot + "/out.html");
                   
              }
              else{
                   requestedFileName= myRequest.getScriptName().replaceAll("%20", " ");
                   requestedFile = new File(documentRoot + fileSeparator + requestedFileName);//create the file
              }
		  
		  
//                  Date currentTime = new Date();
//                  
//                  if(requestedFile.lastModified() == currentTime.getTime()){
//                      statusCode = 304;
//                      writeOutput(out,myRequest,requestedFile);
//                  }
		  if(requestedFile.exists()){
                      if(isFileModified(myRequest, requestedFile)){
                          statusCode = 200;
                      }
                      else{
                          statusCode = 304;
                      } //  end if
			  
                        writeOutput(out, myRequest, requestedFile);	
		  }
		  else{
                      
                            
			  statusCode = 404;
			  writeOutput(out, myRequest, new File(documentRoot + fileSeparator + PAGE_NOT_FOUND_FILE));
		  }	//	end if	
	  }	//	end if
  }	//	end processGETMethod
  
  
   /** Returns true if the requested file has been modified since the date 
   *  specified in the HTTP request header If-Modified-Since; otherwise,
   *  it returns false. It also returns true if the HTTP request did not
   *  include If-Modified-Since.
   * @pre The file exists. 
   * @param myRequest represents the HTTP request.
   * @param requestedFile is the file requested in the HTTP request.
   */
  private boolean isFileModified(Request myRequest, File requestedFile){
      SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	  String ifModifiedSince = myRequest.getHeaderValue("If-Modified-Since");
	  Date ifModifiedSinceDate = null;
	  Date lastModifiedDate = null;
	  
	  if(ifModifiedSince != null){
		  try{
			  ifModifiedSinceDate = sdf.parse(ifModifiedSince);
			  lastModifiedDate = new Date(requestedFile.lastModified());
			  
			  if(ifModifiedSinceDate.compareTo(lastModifiedDate) <= 0){
//				  System.out.println("File was modified 1");
//				  System.out.println("If-Modified-Since: " + ifModifiedSinceDate.toString());
//				  System.out.println("Last_Modified: " + lastModifiedDate.toString());
				  
				  return true; // File was modified
			  }
			  else{
				  
				  return false;
			  }	//	end if
		  }
		  catch(ParseException e){
			  		  
			  return true;	// Assume file was modified
		  }	//	end try
	  }
	  else{
		  
		  return true;
	  }	//	end if
  }	//	end isFileModified
  
 /**
  * process the POST method and dliver appropriate status codes
  * writes the message bosy to the output
  */
 private void processPOST(Request myRequest, OutputStream out) throws IOException{
     
     
     String fileSeparator = System.getProperty("file.separator");
     String documentRoot = getDocumentRoot();
     FileWriter fstream = new FileWriter(documentRoot + fileSeparator + "post.html");// fileWriter will write to this  
     BufferedWriter postToFile = new BufferedWriter(fstream);
    
         if(myRequest.messageBody.isEmpty()){
             setStatus(204);
             writeOutput(out,myRequest,new File(documentRoot + fileSeparator + NO_CONTENT_FILE));
         }
         else{
            for(int i = 0;i<=myRequest.messageBody.size();i++){

                String temp = myRequest.messageBody.get(i);
                postToFile.write(temp);
            }            
            setStatus(201);
            writeOutput(out,myRequest,new File(documentRoot + fileSeparator + "post.html"));         
         }
     
      postToFile.close();
 
 }
 
 /**
  * implementation of PUT
  * put the message body in the requested file
  * file is modified if it already exists, new file created otherwise
  * 
  * @param myRequest
  */
 private void processPUT(Request myRequest, OutputStream out) throws IOException{
  
     
     String fileSeparator = System.getProperty("file.separator");
     String documentRoot = getDocumentRoot();
     File PUTFile = new File(myRequest.getScriptName());
     if(PUTFile.exists()){
     FileWriter fstream = new FileWriter(PUTFile);// fileWriter will write to this  
     BufferedWriter putToFile = new BufferedWriter(fstream);

         if(myRequest.messageBody.isEmpty()){
             setStatus(204);
             writeOutput(out,myRequest,new File(documentRoot + fileSeparator + NO_CONTENT_FILE));
         }
         else{
          for(int i = 0;i<=myRequest.messageBody.size();i++){
                                  String temp = myRequest.messageBody.get(i);
                                  putToFile.write(temp);
                                  setStatus(201);
                                  writeOutput(out,myRequest,PUTFile);
     }
         }
        
     putToFile.close();
     }
     else{
        statusCode = 404;
	writeOutput(out, myRequest, new File(documentRoot + fileSeparator + PAGE_NOT_FOUND_FILE));
     }
 }
 
 /**
  * 
  * @param requestedFileName
  * @return the content type
  */
  private String getContentTypeHeader(String requestedFileName){
	  String fileExtension = requestedFileName.substring(requestedFileName.lastIndexOf(".") + 1);
	  return("Content-Type: " + HttpdConf.getMIMEType(fileExtension));
  }	//	end getContentTypeHeader
  
  /**
   * 
   * @param requestedFile
   * @return length of the content
   */
  private String getContentLengthHeader(File requestedFile){
	  contentLength = (int)requestedFile.length();
	  return ("Content-Length: " + contentLength);
  }	//	end getContentLengthHeader
  
    private String getLastModifiedHeader(File requestedFile){
      SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
      return ("Last-Modified: " + sdf.format(new Date(requestedFile.lastModified())));
  }	//	end getLastModifiedHeader
  
  /** Returns the absolute path of the document root.
   *  The path uses the system's file separator (either / or \).
   */
  public String getDocumentRoot(){
	  
	  String documentRoot = HttpdConf.getConfigurationVariableValue("DocumentRoot");
	  	  
	  return documentRoot;
  }	//	end getDocumentRoot
  
  
  /**
   * Used to output a correctly formatted response to the client. This function
   * will need to process any output from a cgi script as well as generate the
   * appropriate headers and body required by an HTTP response.
   *
   * @pre requestedFile exists
   * @param out is used to( send the response back to the socket.
   */
    public void writeOutput(OutputStream out, Request myRequest, File requestedFile) 
  	throws IOException{
	  
	  PrintWriter writer = new PrintWriter(out, true);
	  writer.println(myRequest.getHTTPVersion() + " " + statusCode + " " + statusCodes.get(statusCode));
	  writer.println(getDateHeader());
	  writer.println(getServerHeader());
	  writer.println(getContentTypeHeader(requestedFile.getName()));
	  writer.println(getContentLengthHeader(requestedFile));
	  writer.println(getLastModifiedHeader(requestedFile) + "\n");
	  //writer.println(getCacheControlHeader() + "\n");
          

          
	  if(shouldSendBody(myRequest)){
		  // Send HTTP response body
		  FileInputStream file = new FileInputStream(requestedFile);
		  int inputByte;
		  
		  try{
			  inputByte = file.read();
			  
			  while(inputByte != -1){
				  out.write(inputByte);
				  inputByte = file.read();
			  }	//	end while
                         
                          /*if(!myRequest.queryVar.isEmpty()){

                              for(int i =0;i<myRequest.queryVar.size();i++){
                              
                               String temp = myRequest.queryVar.get(i); 
                               System.out.println(temp);     
                               out.write(temp.getBytes());
                               out.write(" : ".getBytes());
                               out.write(myRequest.getVarValue(temp).getBytes()); 
                               
                              }
                          }*/
		  }
		  catch(IOException e){
			  System.out.println(e.getMessage());
			  statusCode = 0;
		  }	//	end try
		  file.close();
                  writer.close();
	  }	//	end if
  }	//	end writeOutput
/**
 * determine if response should have a message body or not
 */
  
  private boolean shouldSendBody(Request myRequest){
	if(myRequest.getMethod().equalsIgnoreCase("HEAD")){
		return false;
	}
	if(statusCode == 304){
		return false;
	}	//	end if
	
	return true;
  }
  
  
  /**
   * Used to test for authentication. If the .htaccess file shows that
   * authentication is needed for access to the file or directory then set the
   * appropriate headers and set the appropriate status codes unless the user
   * has included their authentication. If this is the case, check to make sure
   * their authentication is valid.
   *
   * @param req Request object which is needed to check for authentication
   */
  public Boolean checkAuthentication(Request req, OutputStream out) throws FileNotFoundException, IOException, Base64FormatException{

      
      String fileSeparator = System.getProperty("file.separator");
      String documentRoot = getDocumentRoot();
      String file_path = HttpdConf.getConfigurationVariableValue("DocumentRoot") + req.getScriptName();   
      if(file_path.contains("private")){
          return false;
      }
      
      StringTokenizer auSt = new StringTokenizer(file_path);
    
      String temp = auSt.nextToken("/"); 
     
      while(auSt.hasMoreElements()){                               
                      if(authObj.authIsNeeded(temp)){ 
                          
                          //check if user has already included authentication
                          if(req.headerVec.contains("Authorization")){                              
                             String credentials = req.getHeaderValue("Authorization"); 
      
                             int index = credentials.indexOf(" ");
                             if(authObj.checkAuth(credentials.substring(index + 1))){
                                 
                                 permitted = true;//user can proceed
                             }
                             else{
                                 permitted = false;//forbidden
                                 statusCode = 401;
                                 writeOutput(out,req, new File(documentRoot + fileSeparator + UNAUTHORIZED));//block User
                             }
                          }
                          
                          else{
                              permitted=false;
                              statusCode = 401;
                              
                              PrintWriter writer = new PrintWriter(out, true);
	  writer.println(req.getHTTPVersion() + " " + statusCode + " " + statusCodes.get(statusCode));
	  writer.println(getDateHeader());
	  writer.println(getServerHeader());
          writer.println("WWW-Authenticate: Basic" + "\n\n");
          
                          }
                      }
                     temp = temp +"/" + auSt.nextToken("/");  
                  }
     return true;
  }

  /**
   * Used to set the reason for each HTTP status code as designated by the
   * protocol.
   *
   * @param code int value which corresponds to each status code
   */
  public void setStatus(int code) {
      statusCode = code;

  }
  
  /** Returns a table with the status codes (keys) and the corresponding status phrases (values) **/
  private static Hashtable<Integer, String> initStatusCodes(){
	  Hashtable<Integer, String> statusCodes = new Hashtable<Integer, String>();
	  
	  statusCodes.put(200, "OK");
	  statusCodes.put(201, "Created");
	  statusCodes.put(204, "No Content");
	  statusCodes.put(302, "Found");
	  statusCodes.put(304, "Not Modified");
	  statusCodes.put(400, "Bad Request");
	  statusCodes.put(401, "Unauthorized");
	  statusCodes.put(403, "Forbidden");
	  statusCodes.put(404, "Not Found");
	  statusCodes.put(500, "Internal Server Error");
	  statusCodes.put(501, "Not Implemented");
	  
	  return statusCodes;
  }	//	end initStatusCodes
  
  /** Opens the server's log file for writing output 
   */
  private PrintWriter openLogFile(){
	  
	  PrintWriter logPrinter;
	  String logFileAbsolutePath = HttpdConf.getConfigurationVariableValue("LogFile");
	  
	  if(logFileAbsolutePath != null){
		  try{
			  if((new File(logFileAbsolutePath)).exists()){
				  logPrinter = new PrintWriter(new FileOutputStream(logFileAbsolutePath, true));
				  return logPrinter;
			  }
			  else{
				  logPrinter = new PrintWriter(new FileOutputStream(logFileAbsolutePath, true));
				  
				  logPrinter.println("# Web Server: Poushali Banerjee, Lee Gemma Fu-Sun");
				  logPrinter.println("# Version: 1.0");
				  logPrinter.println("# Date: " + getDate());
				  logPrinter.println("# Fields: remotehost rfc931 authuser [date] \"request\" status bytes");
				  logPrinter.println("#\tremotehost:\tRemote hostname (or IP number if DNS hostname is not available, or if DNSLookup is Off.");
				  logPrinter.println("#\trfc931:\t\tThe remote logname of the user."); 
				  logPrinter.println("#\tauthuser: 	The username as which the user has authenticated himself."); 
				  logPrinter.println("#\t[date]:\t\tDate and time of the request."); 
				  logPrinter.println("#\t\"request\":\tThe request line exactly as it came from the client."); 
				  logPrinter.println("#\tstatus:\t\tThe HTTP status code returned to the client.");
				  logPrinter.println("#\tbytes:\t\tThe content-length of the document transferred."); 
				  logPrinter.println();
				  
				  return logPrinter;
			  }	//	end if
		  }
		  catch(FileNotFoundException e){
			  System.out.println("Error opening the log file " + logFileAbsolutePath);
			  System.exit(0);
			  return null;	// to keep compiler happy
		  }	//	end try
	  }
	  else{
		  System.out.println("httpd.conf does not include the variable LogFile (the location of your server's log file.");
		  System.out.println("Please add LogFile and its value using the following syntax:");
		  System.out.println("LogFile absolute_path_of_the_server's_log_file");
		  System.out.println("\n* Use quotation marks if the path contains spaces");
		  System.out.println("Exit Program!");
		  System.exit(0);
		  return null;
	  }	//	end if
  }	//	end initLogPrinter

  /**
   * Private function used to return the appropriate mime type for the file that
   * is being requested
   *
   * @param MIMETable Hashtable of mime types from your mime.types file
   * @param extension String value which designates the extension of the file
   *   being requested. This will be used to determine the mime type
   * @return String value that contains the mime type of the file
   */
  private String getMIME(Hashtable MIMETable, String extension) {

      return (String) MIMETable.get(extension);
      
  }

  /**
   * Private function used to determine whether the mime type requested is a
   * valid mime type
   *
   * @param MIMETable Hashtable value of the available mime types as designated
   *   by the mime.types file
   * @param extension String value which consists of the extension type
   *   requested. Used to determine the correct mime type
   * @return true if mime type if valid, false otherwise
   */
  private boolean checkMIME(Hashtable MIMETable, String extension) {

      return MIMETable.contains(extension);
  }

  /**
   * private function used when processing a request from the client. Here, you
   * will check for mime type validity and handle a put request if it is
   * requested. If the request is PUT, you will need to use the body of the
   * request to modify the existing file.
   *
   * @param MIMETable Hashtable that contains the valid mime types as
   *   determined by the mime.types file
   * @param body String value that contains the body of the request.
   */
  //if the requested file is a MIMEType
  private void processWithExistence(Hashtable MIMETable, String body) {

  }

  /**
   * Private function specifically used to handle output from a cgi script. You
   * will need to check the header passed back from the cgi script to determine
   * the status code of the response. From there, add your headers, attach the
   * body and add any other server directives that need to be included.
   *
   * @param dataOut BufferedOutputStream object that will write to the client
   */
  private void processCGI(BufferedOutputStream dataOut) {

      
   }

//  /**
//   * Used to write the appropriate information to the log file.
//   *
//   * @param logPath String value which contains the location of your log file
//   * @param host String value that contains the address of the client who made
//   *   the request
//   */
//  public void writeToLog(String logPath, String host) {
//
//  }	//	end writeToLog
  
  /**
   * Used to write the appropriate information to the log file.
   * Information on each request is written with the following format:
   * remotehost rfc931 authuser [date] "request" status bytes
   *
   * remotehost:Remote hostname (or IP number if DNS hostname is not available, or if DNSLookup is Off. 
   * rfc931: 	The remote logname of the user. 
   * authuser: 	The username as which the user has authenticated himself. 
   * [date]: 	Date and time of the request. 
   * "request": The request line exactly as it came from the client. 
   * status: 	The HTTP status code returned to the client. 
   * bytes: 	The content-length of the document transferred. 
   * 
   * @param myRequest Request object that was generated and stores all the
   *   		request information that came in from the client.
   */
  public void writeToLog(Request myRequest, Socket clientSocket) {
	  PrintWriter logPrinter = openLogFile();

          logPrinter.print(clientSocket.getInetAddress().getHostAddress() + " ");
          logPrinter.print("- ");
	  logPrinter.print("remotehost ");
	  logPrinter.print("rfc931 ");
	  logPrinter.print("authuser ");
	  logPrinter.print("[" + getDate() + "] ");
	  logPrinter.print("\"" + myRequest.getFirstLine() + "\" ");
	  logPrinter.print(statusCode + " ");
	  logPrinter.println(contentLength);
	  
	  logPrinter.close();
  }	//	end writeToLog
    
}	//	end Response
