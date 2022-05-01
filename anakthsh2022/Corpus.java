package anakthsh2022;

import java.io.File;

public class Corpus {

    private String corpusPath;
    private File[] document;

    public Corpus(String corpusPath){
        this.corpusPath = corpusPath;
        setDocument();
        
    }
    public void setDocument() {
    	 File folder = new File(corpusPath);
         document = folder.listFiles();
    	
        
        System.out.println("To file που θα φορτωθεί είναι στο :"+document);
    	
    }
    
    public File[] getDocuments() {
        return document;
    }
   
}

