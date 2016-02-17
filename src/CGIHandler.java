import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 *
 * <p>Description: Used to handle cgi script requests. It will create a runnable
 * thread which will execute the script on the server and read the output from
 * that script, which will then be sent back to the client.</p>
 *
 *
 * @author Poushali Banerjee
 * @version 3.4
 */
public class CGIHandler {

  
    Request cgiReq;
    String filename;
    Environment cgiEv;
  /**
   * Constructor for CGIHandler class. Used to save the variables necessary for
   * the cgi script to run properly and for the response to be sent back to the
   * client.
   *
   * @param rq Request variable which correlates to the request that was sent
   *   from the client to the server.
   * @param ev Environment variable used to get the state of the server
   *   environment. This information should have been saved when the server was
   *   configured.
   */
  public CGIHandler(Request rq, Environment ev) {
    filename = rq.getScriptName();
    cgiReq = rq;
    cgiEv=ev;
  }
  
  /**
   * Function used to execute the cgi script that was being requested by the
   * client. 
   * 
   * HINT: Try using the Runtime.getRuntime().exec() function to execute
   * the script. Also look into using both Data and Buffered Streams.
   *
   * @return BufferedInputStream which is the output from the script execution.
   *   This is sent back to the server and then back to the client.
   */
  public BufferedInputStream runScript() throws IOException {
    
        Process process = null;        
        if (filename.isEmpty()) {
         System.err.println("Need command to run");
         System.exit(-1);
       }
       

    //create a subprocess, execute, get the outputStream, pass it to the response object
    //reponse.writeOutput, reponse.processCGI
      
       if(cgiReq.getScriptName().contains(".pl")){
           process = Runtime.getRuntime().exec("perl " + filename);
       }
       else if(cgiReq.getScriptName().contains(".py")){
           process = Runtime.getRuntime().exec("python " + filename);           
       }
       else{
            System.out.println("Perl or python scripts only");
            //process = Runtime.getRuntime().exec(filename);
       }
      //process.getInputstream gets the piped outputstream of this process object
       
       BufferedInputStream iscgi = new BufferedInputStream(process.getInputStream());

       return iscgi;
       
       
      
  }
}
