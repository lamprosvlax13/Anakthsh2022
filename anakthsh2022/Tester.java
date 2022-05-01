package anakthsh2022;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;



public class Tester {
	

public void getTypeDoc(File file) {
	
		String path = file.getAbsolutePath();
        String type="";
        String title = "";

        
        
        
        
        
        
        
        
        
        int k=0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            while (line != null) {
              //System.out.println(line);
            	
            	write(k,line);
            	k++;
              line = br.readLine();
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        
        
        
        
        
        
        
        
        
        
        
        
        
        /*
        
        
        
        try {
            Scanner reader = new Scanner(file);
            //title = reader.nextLine();
           int k=0;
            while (reader.nextLine()!=null) {
            	String data = reader.nextLine();
            	//write(k,data);
            	//k++;
            	System.out.println(data);
            	}
            	//System.out.println(data);
                //article = article + data + "\n";
               // link = data;
           
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        */
}     
        
        private void write(int i, String line) {
        	 try {
        		 
                 // Create a FileWriter object
                 // to write in the file
                 FileWriter fWriter = new FileWriter(
                     "C:\\Users\\vlaho\\eclipse-workspace\\anakthsh2022\\testfiles\\"+i+"file.csv");
      
                 // Writing into file
                 // Note: The content taken above inside the
                 // string
                 fWriter.write(line);
      
                 // Printing the contents of a file
                // System.out.println(text);
      
                 // Closing the file writing connection
                 fWriter.close();
      
                 // Display message for successful execution of
                 // program on the console
              //   System.out.println(
             //        "File is created successfully with the content.");
             }
      
             // Catch block to handle if exception occurs
             catch (IOException e) {
      
                 // Print the exception
                 System.out.print(e.getMessage());
             }
         }
     
        	
        	
        	
	// TODO Auto-generated method stub
	


		private void splitString(String data, String token, ArrayList<String> tokens, int flag) {
    		
    		for(int i=0; i<data.length(); i++) {
    			
    			if(data.charAt(i)!='"' && data.charAt(i)!=',' ) {
    				if(i+1==data.length()) {
    					token=token+data.charAt(i);
    					tokens.add(token);
    					break;
    				}
    				token=token+data.charAt(i);
    			}
    			else if(data.charAt(i)=='"') {
    				
    				if(flag==0) {
    					flag++;
    					
    				}else {
    					flag--;
    					if(i+1==data.length()) {
    						token=token+data.charAt(i);
    						tokens.add(token);
    						break;
    					}
    				}
    				token=token+data.charAt(i);
    			}else if(data.charAt(i)==',') {
    				if (flag%2==0) {
    					
    					tokens.add(token);
    					token="";
    				}else {
    					token=token+data.charAt(i);
    				}
    			}
    		}
    		
    		
    		//for(String str:tokens) {
    		//	System.out.println(str);
    		//}
    	}
      
        
        
        
}
