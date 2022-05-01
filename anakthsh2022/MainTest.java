package anakthsh2022;

import java.io.File;
import java.io.IOException;

public class MainTest {

	 public static void main(String Args[]) throws IOException {
	    	
	    //	Corpus cor =new Corpus("C:\\Users\\vlaho\\Desktop\\test");
	    //	IndexCreator in=new IndexCreator("type","C:\\Users\\vlaho\\Desktop\\test",cor.getDocuments(),"Standard");
	    	
	    	//Tester te =new Tester();
	    	String path="C:\\Users\\vlaho\\Desktop\\test";
	    	
	    	String path2 ="C:\\Users\\vlaho\\eclipse-workspace\\anakthsh2022\\inputFiles";
	    	//File file = new File(path);
	    	//te.getTypeDoc(file);
	    	Corpus cor =new Corpus(path);
	    	IndexCreator in =new IndexCreator("type",path,cor.getDocuments(),"Standard");
	    	Search s =new Search("title",path,"Dick","Standard","Relevance");
	    	
	    }
}
