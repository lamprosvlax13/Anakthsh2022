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
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
			concreteAnalyzer(analyzer);
			quarry(field, keyword, sortSelected);
			directory.close();
		} catch (FileAlreadyExistsException exising) {
			System.out.println(exising.fillInStackTrace());

		}
	}

	private void quarry(String field, String keyword, String sortSelected) throws IOException {

		QueryParser parser = null;
		parser = chooseParserType(field, parser);
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

	private QueryParser chooseParserType(String field, QueryParser parser) {

		if (field.equals("wholeDocument")) {

			String[] fields = { "type", "title", "director", "country", "rating", "description" };
			parser = new MultiFieldQueryParser(fields, analyzer);

		} else {
			parser = new QueryParser(field, analyzer);
		}
		return parser;
	}

	public ScoreDoc[] sortBy(String sortByValue, Query query) throws IOException {

		HashMap<String, Sort> map = new HashMap<String, Sort>();
		map.put("Relevance", Sort.RELEVANCE);
		map.put("IndexOrder", Sort.INDEXORDER);
		Set<Map.Entry<String, Sort>> set = map.entrySet();
		for (Entry<String, Sort> me : set) {
			if (sortByValue.equals(me.getKey())) {
				hits = isearcher.search(query, 100000, me.getValue()).scoreDocs;
				break;
			}
		}
		String fieldSort[] = { "type", "title" };
		for (String field : fieldSort) {
			if (sortByValue.equals(field)) {
				Sort sort = new Sort(SortField.FIELD_SCORE, new SortField(field, Type.STRING));
				hits = isearcher.search(query, 100000, sort, true).scoreDocs;
				break;
			}
		}

		return hits;
	}

	private void concreteAnalyzer(String type) throws IOException {
		Set<Map.Entry<String, Analyzer>> set = initializMapAnalyzers();
		for (Entry<String, Analyzer> me : set) {
			if (type.equals(me.getKey())) {
				analyzer = me.getValue();
				break;
			}

		}
		if (analyzer == null) {
			JOptionPane.showMessageDialog(new JFrame(),
					"An incorrect option of the analyzer is given please try again");
		}

	}

	private Set<Map.Entry<String, Analyzer>> initializMapAnalyzers() throws IOException {
		Path stopWordsPath = Paths.get("utils\\stopWords.txt");
		HashMap<String, Analyzer> list = new HashMap<String, Analyzer>();
		list.put("StandardAnalyzer", new StandardAnalyzer());
		list.put("WhitespaceAnalyzer", new WhitespaceAnalyzer());
		list.put("SimpleAnalyzer", new SimpleAnalyzer());
		list.put("StopAnalyzer", new StopAnalyzer(stopWordsPath));
		Set<Map.Entry<String, Analyzer>> set = list.entrySet();
		return set;
	}

	public ScoreDoc[] getHits() {
		return hits;
	}

}
