
import java.beans.XMLEncoder;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Description: This is the main class of the server. This is where
 * everything is instantiated and configured. Here is also where multithreading
 * of the server will occur.
 * @author Poushali Banerjee
 * @version 2.6
 */
public class WebServer {
   
    
   
    static Boolean setCGI = false;

	private ServerSocket listenSocket;	
	protected HttpdConf httpObj = new HttpdConf();
        
    protected WebServer() throws FileNotFoundException, IOException{
    	
  
        HttpdConf.readHttpd("");
        HttpdConf.readMIME("");
        System.out.println(HttpdConf.getConfigurationVariableValue("Listen"));
   	listenSocket = new ServerSocket(Integer.parseInt(HttpdConf.getConfigurationVariableValue("Listen")));
        
    }	//	end constructor
 
    /** Returns the OutputStream that sends the response to the client or null if 
     *  there is no connection with a client.
     */
    
    
  
    /** Processes requests from clients and generates responses */
     protected void listen(){
    	Socket clientSocket;
    	
    	while(true){
    		try{
    			clientSocket = listenSocket.accept();
    			(new ClientThread(clientSocket)).start();
    		}
    		catch(Exception e){
    			System.out.println(e.getMessage());
    		}    		
    	}	//	end while
    }	//	end listen
    
    public static String replaceEnvVars(String path){
        String fileSeparator = System.getProperty("file.separator");
        String envVar;          //      Environment variable
        String envVarValue;     //      Value of environment variable
        int fileSeparatorIndex = 0;
        int dollarIndex = path.indexOf("$");

        while(-1 < dollarIndex){
                fileSeparatorIndex = path.indexOf(fileSeparator, dollarIndex);

                if(-1 < fileSeparatorIndex){
                        envVar = path.substring(dollarIndex+1, fileSeparatorIndex);
                }
                else{
                        //      No filename appended to the environment variable
                        envVar = path.substring(dollarIndex + 1);
                }       //      end if

                envVarValue = System.getenv(envVar);
                path = path.replaceAll("\\p{Punct}" + envVar, envVarValue);
//              System.out.println("$"+envVar);
                dollarIndex = path.indexOf("$");
        }       //      end while

        return path;
    }   // end replaceEnvVars
    
    /**
     * Here you will create your server, configure all the settings and listen to
     * the port designated within your configuration. Whenever a request comes in,
     * you will need to process that request appropriately and respond as needed.
     *
     * @param args String[]
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, Base64FormatException  {      
            WebServer server = new WebServer();
            server.listen();
        
    }	//	end main
  /**
   * Private class used for multithreading
   */
    /**
   * Private class used for multithreading
   */
  class ClientThread extends Thread {
	 
	private Socket socket;	//	client socket  
	
    /**
     * Constructor used to start a thread.
     */
    public ClientThread(Socket clientSocket) {
    	socket = clientSocket;
    }
    
    /**
     * Used to run your server thread. Here is where you will be processing all
     * requests and returning your responses.
     */
    public void run() {
    	try{
    		Request request;
    		BufferedReader requestReader;
        Response response;
        OutputStream out;	//	Sends response to the client  
        		
        	requestReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	out = socket.getOutputStream();
            request = new Request();
            request.parse(requestReader);
            
            response = new Response();
            if(setCGI == true){
                   Environment cgiEnv = new Environment(httpObj, request, socket);
                   cgiEnv.setEnv();
                   CGIHandler handleCGI = new CGIHandler(request, cgiEnv);
                   BufferedInputStream isserver = handleCGI.runScript();
                   BufferedOutputStream dest = new BufferedOutputStream(null);                   
                   BufferedOutputStream osserver = CopyStream(isserver, dest);
                                
                }
                try {
                    response.processRequest(request, null,out, socket);
                } catch (IOException ex) {
                    Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Base64FormatException ex) {
                    Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            out.close();
            out = null; 
            setCGI = false;
            
    	}
    	catch(IOException e){
    		System.out.println(e.getMessage());
    	}	//	end try
    }	//	end run
  }	//	end ClientThread
  
  
  private static BufferedOutputStream CopyStream(BufferedInputStream bis, BufferedOutputStream bos) throws IOException {
 
      int byte_;
      Response res = new Response();
      String documentRoot = res.getDocumentRoot();
      FileWriter fstream = new FileWriter(documentRoot + "out.html");// fileWriter will write to this  
      BufferedWriter outToFile = new BufferedWriter(fstream);
      while ((byte_ = bis.read()) != -1) {
  
          try {             
              outToFile.write((char)byte_);                       
    }catch (Exception e){//Catch exception if any
      System.err.println("Error: " + e.getMessage());
          }
          bos.write(byte_);
      }
      outToFile.close();//Close the output stream
      return bos;
}
  //	end ClientThread
}
//	end WebServer


