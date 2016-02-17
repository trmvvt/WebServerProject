import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.lang.*;

/**
 *
 * Description: This class will configure your server to the specifications
 * found within the httpd.conf file.
 * @author Poushali Banerjee
 * @version 1.0
 */
public class HttpdConf {

    private static Hashtable<String, String> conf_var = new Hashtable<String, String>();
    private static Hashtable<String, String> alias = new Hashtable<String, String>(); // includes ScriptAlias
    private static Vector<String> directoryIndex = new Vector<String>(); 
    // the default files to serve when a file is not specified in the Request-URI
    
    private static Hashtable<String,String> MIMETable = new Hashtable<String, String>();
    // key: extension	value: MIME type
    
    
    /** Returns the MIME type of the given extension or null if the 
     *  extension does not have a MIME type. 
     * 
     * @param extension is the extension of the file without a dot.
     */
    public static String getMIMEType(String extension){
    	return MIMETable.get(extension);
    }	//	end getMIMEType
    
    
    /** DEBUGGER METHODS **/
    public static void print(){
    	
    	System.out.println("\nConfiguration variables in httpd.conf:");
    	for(String key : conf_var.keySet()){
    		System.out.println(key + "\t" + conf_var.get(key));
    	}
    	
    	System.out.println("\nDirectoryIndex:");
    	for(String file : directoryIndex){
    		System.out.println(file);
    	}
    	
    	System.out.println("\nItems in hashtable alias: ");
    	for(String key : alias.keySet()){
    		System.out.println(key + "\t" + alias.get(key));
    	}
    	
    	System.out.println("Extensions and MIME Types:");
    	for(String str : MIMETable.keySet()){
    		System.out.println(str + "\t\t" + MIMETable.get(str));
    	}   	
    }	//	end print

  /**
   * Reads in a httpd.conf file, parses it and saves the data stored within that
   * file. This allows for proper configuration of your server since the
   * information stored in your configuration file should allow for your server
   * to function.
   *
   * @param path path to your httpd.conf file
   */
  public static void readHttpd(String path) throws FileNotFoundException, IOException{
      
	  StringTokenizer st2, st3;
	  String variableName;	// the variable name in httpd.conf
	  String token;			// used to process strings
	  Vector<String> variableValues;
      BufferedReader httpd_conf_reader;
      String line;
      String Filename = path + "httpd.conf";
      httpd_conf_reader = new BufferedReader(new FileReader(Filename));

      line = httpd_conf_reader.readLine().trim();

      while (line != null) {
    	  try{
    	         while(line.startsWith("#") || line.isEmpty()) {
    	              line = httpd_conf_reader.readLine().trim();
    	          }
    	         
    	          st2 = new StringTokenizer(line);
    	          variableName = st2.nextToken();                  
    	          if(line.startsWith("Alias") || line.startsWith("ScriptAlias")){
    	        	  if(0 <= line.indexOf("\"")){
    	            	  // Remove "" in the variable value(s)
    	        		  variableValues = getVariableValues(line);
    	        		  alias.put(variableValues.elementAt(0), variableValues.elementAt(1));
    	        	  }
    	        	  else{
    	        		  alias.put(st2.nextToken(),st2.nextToken());
    	        	  }	//	end if        	     
    	          }
    	          else if(line.startsWith("DirectoryIndex")){
    	        	  if(0 <= line.indexOf("\"")){
    	            	  // Remove "" in the variable value(s)
    	        		  directoryIndex = getVariableValues(line);
    	        	  }
    	        	  else{
    	        		  while(st2.hasMoreTokens()){
    	        			  directoryIndex.add(st2.nextToken());
    	        		  }	//	end while
    	        	  }	//	end if 
    	          }
                  
    	          else {  
    	        	  if(0 <= line.indexOf("\"")){
                              // Remove "" in the variable value
                              st3 = new StringTokenizer(line, "\"");
                              token = st3.nextToken();	// this token is the variable name
                              token = st3.nextToken();	// variable value

                              if(-1 < token.indexOf("$")){
                                  // Replace the environment variables with their corresponding values
                                  token = WebServer.replaceEnvVars(token);
                              } //  end if 

                                conf_var.put(variableName, token);
                              }
                              else{
                                String token2 = st2.nextToken();
                                  if(-1 < token2.indexOf("$")){
                                      // Replace the environment variables with their corresponding values
                                      token2 = WebServer.replaceEnvVars(token2);
                                  } //  end if 
                                  conf_var.put(variableName, token2);
    	        	  }	//	end if
    	          }
    	          line = httpd_conf_reader.readLine().trim();
    	  }
    	  catch(NullPointerException e){
    		  // End of file
    		  break;
    	  }
      }
  }
  
  /** Returns all the values given for the configuration variable. 
   *  The returned values have no "". 
   * @param line is a line of text read from httpd.conf
   */
  private static Vector<String> getVariableValues(String line){
	  String confLine = line;
	  
	  Vector<String> variableValues = new Vector<String>();
	  StringTokenizer tokenizer = new StringTokenizer(confLine);
	  String token;
	  int index;
	  
	  tokenizer.nextToken();	// this token is the variable name
	  
	  if(0 <= confLine.indexOf("\"")){  
		  while(tokenizer.hasMoreTokens()){
			  token = tokenizer.nextToken();
			  
			  if(0 <= token.indexOf("\"")){
				  confLine = confLine.substring(confLine.indexOf("\"") + 1).trim();
				  index = confLine.indexOf("\"");
				  
				  if(0 < index){
					  variableValues.add(confLine.substring(0, index));
				  }
				  else{
					  confLine = confLine.substring(confLine.indexOf("\"") + 1).trim();
					  index = confLine.indexOf("\"");
					  variableValues.add(confLine.substring(0, index));
				  }	//	end if
				  
				  if(index + 1 < confLine.length()){
					  confLine = confLine.substring(index + 1).trim();
					  tokenizer = new StringTokenizer(confLine);
				  }
				  else{
					  break;
				  }	//	end if
			  }
			  else{
				  variableValues.add(token);
			  }	//	end if
		  }	//	end while
	  }
	  else{
		  while(tokenizer.hasMoreTokens()){
			  variableValues.add(tokenizer.nextToken());
		  }	// end while
	  }	// end if
	  
	  return variableValues;
  }	//	end getVariableValues
  
  /**
   * Function to convert aliases set within your httpd.conf file to their
   * absolute path. This allows for aliases to be found on your server and
   * returned back to the client.
   * HINT: You may find it helpful to create a private class to store your
   * aliases.
   *
   * @param fakeName String which contains the alias of the file or directory
   * @return String value which contains the absolute path to the file or
   *   directory as determined within the httpd.conf file
   */
  public static String solveAlias(String fakeName){
    return alias.get(fakeName);
  }

  /**
   * Used to read the mime.types file and save all the information from that file
   * into a data structure that can be used to validate file types when
   * generating response messages.
   *
   * @param path String value of path to mime.types file
   */
  public static void readMIME (String path) throws IOException {
      StringTokenizer st3;
	  BufferedReader mimeReader;
      String line, mimeType;
      mimeReader = new BufferedReader(new FileReader(path + "MIME.types"));
      
      try{
    	  line = mimeReader.readLine().trim();
    	  while(line != null){
              if(!line.isEmpty() && !line.startsWith("#")){
            	  st3 = new StringTokenizer(line);
                  mimeType = st3.nextToken();
                  
                  while(st3.hasMoreTokens()){
                      MIMETable.put(st3.nextToken(), mimeType);
                  }	//	end while
              }	//	end if
              
              line = mimeReader.readLine().trim();
          }	//	end while
      }    
      catch(NullPointerException e){
    	  // End of stream
      }	// end try
  }

  /**
   * Helper function to determine whether the name of a file or directory is an
   * alias for another file or directory as noted in the httpd.conf file.
   *
   * @param name String value of the alias we want to check to determine
   *   whether it is or is not an alias for another file or directory
   * @return true if it is an alias, false otherwise
   */
  public static boolean isScript(String name)	{
            return alias.contains(name);
  }

  /* Gets the value of the given configuration variable. 
   * Returns null if configurationVariable is not included in httpd.conf.
   * 
   * Use the method getDirectoryIndex() to get the list of default files to
   * serve when a file is not specified in the request-URI.
   */
  public static String getConfigurationVariableValue(String configurationVariable){
	  return(conf_var.get(configurationVariable));
  }	//	end getConfigurationVariableValue
  /**
   * get name of accessFile as set in http.conf file
   */
  
  public static String getAccessFileName(){
      return conf_var.get("AccessFileName");
  }
  /** Returns the default files to serve when a file is not specified in 
   *  the Request-URI.
   */
  public static Vector<String> getDefaultFiles(){
	  return (directoryIndex);
  }	//	end getDefaultFiles
  
  /**
   * Helper function to see if you've parsed your httpd.conf file properly. Used
   * for debugging purposes.
   */
  /*public void testPrint(){
    System.out.println("ServerRoot: "+ serverRoot);
    System.out.println("DocumentRoot: "+documentRoot);
    System.out.println("ListenPort: "+listen);
    System.out.println("LogFile: "+logFile);
    //System.out.println("AccessFileName: " + accessFileName);
    System.out.println("ScriptAlias: "+scriptAliasPath+" "+solveAlias(scriptAliasPath));
  }*/
  

}