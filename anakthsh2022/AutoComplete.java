package anakthsh2022;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class AutoComplete {
	
    private String indexDir;
  
    public AutoComplete(String indexDir){
        this.indexDir = indexDir;
    }


    public ArrayList<String> moreLikeThis(String wordToComplete) throws IOException {
        ArrayList<String> suggestions = new ArrayList<String>();
        Path path = Paths.get(indexDir);
        FSDirectory directory = FSDirectory.open(path);
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        //int numDocs = reader.maxDoc();
        Query query = new FuzzyQuery(new Term("word",wordToComplete));
        MoreLikeThis mlt = new MoreLikeThis(reader);
        mlt.setFieldNames(new String[] {"word"});
        mlt.setMinTermFreq(1);
        mlt.setMinDocFreq(1);

        Document doc = null;
        TopDocs similarDocs = searcher.search(query, 10);
        for (int i = 0 ; i <=10; i++) {
            try {
                doc = reader.document(similarDocs.scoreDocs[i].doc);
                suggestions.add(doc.getField("word").stringValue());
            }
          catch(ArrayIndexOutOfBoundsException exception) {
        	   //System.out.println("to try de douleuei");
            }

        }

        reader.close();
        directory.close();
       
        return suggestions;
    }



}
