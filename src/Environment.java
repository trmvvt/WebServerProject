import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.Hashtable;

/**
 *
 * <p>Description: Helper class to save all the environment variables that are
 * needed to ensure proper configuration and execution of CGI scripts
 *
 * @author Poushali Banerjee
 * @version 2.0
 */
public class Environment {

    
    private String[] cgi_container = new String[30];
    private String GATEWAY_INTERFACE, SERVER_ADMIN, SERVER_NAME, SERVER_SOFTWARE, SERVER_ADDR,
            DOCUMENT_ROOT, SERVER_ROOT;
    
    private String REMOTE_ADDR;
    private String AUTH_TYPE;
    private String REMOTE_USER;
    private String HTTPstr= "HTTP_";
    private Hashtable<String, String> envHeaders = new Hashtable<String, String>();
    private Hashtable<String, String> envHTTPHolder = new Hashtable<String, String>();
    Vector<String> tempVec = new Vector();
    Request envReqObj ;
  
    /**
   * Constructor for Environment object. Saves the information from the server
   * configuration file as well as from the request into a data structure so if
   * the request is for a cgi script, it can be executed properly.
   *
   * @param c HttpdConf file which contains some variables which need to be
   *   saved in order for cgi scripts to execute properly.
   * @param r Request object which contains request specific variables needed
   *   to execute cgi scripts
   * @param cl Socket used by the client to connect to the server
   */
  public Environment(HttpdConf c, Request r, Socket cl) {
      
      tempVec = r.headerVec;
      envReqObj = r;
     
        //non request specific variables
        GATEWAY_INTERFACE = "CGI/1.1";        
        SERVER_ADMIN = c.getConfigurationVariableValue("ServerAdmin");
        SERVER_NAME = c.getConfigurationVariableValue("ServerName");
        SERVER_SOFTWARE = SERVER_NAME + "v2.0";
        SERVER_ADDR = "";
        DOCUMENT_ROOT = c.getConfigurationVariableValue("DocumentRoot");
        SERVER_ROOT = c.getConfigurationVariableValue("ServerRoot");
        
        //request-specific variables
        try{
        envHTTPHolder.put("SERVER_PROTOCOL",r.getHTTPVersion() );
        envHTTPHolder.put("SERVER_PORT", c.getConfigurationVariableValue("Listen"));//should come from request r
        envHTTPHolder.put("REQUEST_METHOD",r.getMethod());
        envHTTPHolder.put("QUERY_STRING", r.getQueryString());
        envHTTPHolder.put("CONTENT_TYPE", r.getHeaderValue("CONTENT-TYPE"));
        envHTTPHolder.put("CONTENT_LENGTH", r.getHeaderValue("CONTENT-LENGTH"));
        envHTTPHolder.put("PATH_INFO", r.getPath());
        envHTTPHolder.put("PATH_TRANSLATED" ,SERVER_ROOT + r.getPath());
        envHTTPHolder.put("SCRIPT_NAME", r.getScriptName());     
        REMOTE_ADDR = " ";
        AUTH_TYPE = " ";
        REMOTE_USER = " ";
        }
        catch(NullPointerException e){
           
        }
        
  }

  /**
   * Function to save static variables into your data structure which can be
   * then used during script execution. Variables that can also be accessed
   * directly from the request object or the socket (objects passed in during
   * instantiation) can be saved here as well.
   * HINT: Most request variables that need to be saved need to have an HTTP_
   * variable name so take this into consideration when planning how to design
   * and save environment variables.
   */
  public void setEnv() {

    try{ 
      //need to populate data into cgi_container;
   for(int i = 0;i<tempVec.size();i++){
       
       cgi_container[i] = HTTPstr.concat(tempVec.elementAt(i).toUpperCase().replace('-', '_'));
       //cgi_container[i] = (String)tempVec.elementAt(i);
       
       if(envHTTPHolder.contains(tempVec.elementAt(i))){
         envHeaders.put(cgi_container[i],envHTTPHolder.get(tempVec.elementAt(i)) );
       }   
       else
   {
       envHeaders.put(cgi_container[i],envReqObj.getHeaderValue(tempVec.elementAt(i)));       
   } 
  }
    }
    catch(NullPointerException e){
        
    }
      
  }

  /**
   * Function to return a data structure that contains all the environment
   * variables that were saved.
   *
   * @return String[]. Chose as data type upon return due to parsing of variable
   *   content and name which both need to be stored.
   */
  public String getDocRoot(){
      return DOCUMENT_ROOT;
  }
  
  public String getServerRoot(){
      return SERVER_ROOT;
  }
  public String[] getEnv() {
      return cgi_container;
  }

  /**
   * Used to add another variable to the data structure. Usually used when the
   * data has not be stored yet through the setEnv function.
   *
   * @param var String that contains both the variable name and data. Used this
   *   way for parsing purposes later on.
   */
  public void addEnvVar(String var) {  

      //add the variable if the container does not already have it
           
  }
  public String getPath(){
      return envHTTPHolder.get("PATH_TRANSLATED");
  }
}