package anakthsh2022;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Search {
    private DirectoryReader ireader;
    private IndexSearcher isearcher;
    private Directory directory;
    private Analyzer analyzer;
    private ScoreDoc[] hits;


	public Search(String field, String indexPath, String keyword, String analyzer, String sortSelected)
			throws IOException {

		try {
			Path indexP = Paths.get(indexPath);
			this.directory = FSDirectory.open(indexP);
			this.ireader = DirectoryReader.open(directory);
			this.isearcher = new IndexSearcher(ireader);
			getAnalyzer(analyzer);
			quarry(field, keyword, sortSelected);
			directory.close();
		} catch (FileAlreadyExistsException exising) {
			System.out.println(exising.fillInStackTrace());

		}
	}



	private void quarry(String field, String keyword, String sortSelected) throws IOException {

		QueryParser parser = null;
		if (field.equals("wholeDocument")) {

			String[] fileds = { "type", "title", "director", "country", "rating", "description" };
			parser = new MultiFieldQueryParser(fileds, analyzer);
		} else {
			parser = new QueryParser(field, analyzer);
		}

		Query query = null;
		try {
			query = parser.parse(keyword);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		hits = sortBy(sortSelected, query);
		for (int i = 0; i < hits.length; i++) {
			Document hitDoc = isearcher.doc(hits[i].doc);
		}

		ireader.close();
		directory.close();

	}

    public ScoreDoc[] sortBy(String sortByValue, Query query) throws IOException {
        if (sortByValue.equals("Relevance")){
            hits = isearcher.search(query, 1000,Sort.RELEVANCE).scoreDocs;
        } else if (sortByValue.equals("Indexorder")){
            hits = isearcher.search(query, 1000,Sort.INDEXORDER).scoreDocs;
        }
        return hits;
    }

    private void getAnalyzer(String type) {
        if (type.equals("Standard")) {
            analyzer = new StandardAnalyzer();
        } else if (type.equals("WhitespaceAnalyzer")) {
            analyzer = new WhitespaceAnalyzer();
        } else if (type.equals("SimpleAnalyzer")) {
            analyzer = new SimpleAnalyzer();
        } else if (type.equals("StopAnalyzer")) {
            Path stopWordsPath = Paths.get("stopWords.txt");
            try {
                analyzer = new StopAnalyzer(stopWordsPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("An incorrect option of the analyzer is given please try again.");
        }
    }

    public ScoreDoc[] getHits() {
        return hits;
    }

    
    
}
