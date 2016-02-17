import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.lang.Object;

       





/**
 * <p>Title: Authentication.java</p>
 *
 * <p>Description: Used when authentication of the user is needed before access
 * is given to certain files. This class will take the information submitted by
 * the user and check the .htaccess file to see if that user has access to the
 * file he/she is trying to view. Two main functions exist in this class. One
 * function to check if authentication is needed, and another to decode and
 * validate the authentication data once it has been received by the server.</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * @author Poushali Banerjee
 * @version 3.0
 */
public class Authentication {
	
    private static Hashtable<String, String> authMap = new Hashtable<String, String>();
    private Hashtable<String, String> userInfo = new Hashtable<String, String>();
  public Authentication() {
    /* dummy constructor */
  }
  
  /**
   * get value of the requested authentication variable
   */

  public static String getAuthValue(String value){
      return authMap.get(value);
  }
  /**
   * This function should check to see if the .htaccess file exists anywhere in
   * your directory structure. If it exists, grab the information out of the
   * file and save it somewhere for use.
   *
   * @param path The path to the working directory. It should not contain a
   *   file name at the end since we only want the directory the .htaccess file
   *   would be located in.
   * @return true if the .htaccess file is found and the data from it has been
   *   parsed out properly. Return false otherwise
   */
  public boolean authIsNeeded(String path) throws FileNotFoundException, IOException, Base64FormatException{ 
      
      File tempFile = new File(path);
      if(tempFile.isDirectory()){
      
              String[] fileContainer = tempFile.list();

              for(int i =0;i<fileContainer.length;i++){
                  if(fileContainer[i].endsWith(HttpdConf.getAccessFileName())){//check if file exists
        
                      File authFile = new File(path + "/" +HttpdConf.getAccessFileName());
                      
                      BufferedReader br = new BufferedReader(new FileReader(authFile));
          String temp = br.readLine();
          
          while(temp != null){
             int index = temp.indexOf(" ");
            
          authMap.put(temp.substring(0, index),temp.substring(index+1));
          temp = br.readLine();
        
          }

          //reading and decrypting from htpasswd
          
          File pswdFile = new File(authMap.get("AuthUserFile"));
          br = new BufferedReader(new FileReader(pswdFile));
          temp = br.readLine();
          while(temp != null){
             int index = temp.indexOf(":");
            //Base64Decoder myDcd = new Base64Decoder(temp.substring(index+1));
            //String decoded = myDcd.processString();
            //System.out.println(decoded);
          userInfo.put(temp.substring(0, index),temp.substring(index+1));
          temp = br.readLine();
        
          }
           return true;        
      }
      
}
              ////////////////////////////////////////////////
        
      }
      return false;
      
}

  /**
   * Checks the incoming information from the client against the user file to
   * see if the authentication information is correct. If it is correct, the
   * user can proceed, otherwise he or she is blocked and not allowed to access
   * files. This class uses the Base64Decoder class to check information.
   *
   * @param input String passed in through the header which is encoded. Use the
   *   Base64Decoder to decode this information so it can be used to check
   *   against in the user file.
   * @return true if data passed in matches what is in the user file, false
   *   otherwise
   */
  public boolean checkAuth(String input) throws IOException, Base64FormatException {
         
      Base64Decoder myDcd = new Base64Decoder(input);
      String decoded = myDcd.processString();              
      int index = decoded.indexOf(":");
      String part1 = decoded.substring(0, index);
      String part2 = decoded.substring(index+1); 
      
      if(part2.equals(userInfo.get(part1))){          
         return true;
      }      
          return false;

  }   
      
}
