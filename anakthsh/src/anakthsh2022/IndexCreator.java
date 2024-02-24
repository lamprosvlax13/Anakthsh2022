package anakthsh2022;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import javax.swing.*;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class IndexCreator {

	private String indexPath;
	private String indexType;
	private File[] document;
	private Analyzer analyzer;
	private ArrayList<String> history;

	public IndexCreator(String indexType, String indexPath, File[] files, String analyzerType) throws IOException {
		this.indexType = indexType;
		this.indexPath = indexPath;
		this.document = files;
		concreteAnalyzer(analyzerType);
		createIndex();

	}

	public IndexCreator(String indexType, String indexPath, File[] documents, String analyzerType,
			ArrayList<String> history) throws IOException {
		this.indexType = indexType;
		this.indexPath = indexPath;
		this.document = documents;
		this.history = history;
		concreteAnalyzer(analyzerType);
		createIndex();
	}

	private void createIndex() throws IOException {

		try {
			Path indexP = Paths.get(indexPath);
			Directory directory = FSDirectory.open(indexP);
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter iwriter = new IndexWriter(directory, config);
			System.out.println("Start creating index.");
			String Fields[] = { "type", "title", "director", "country", "rating", "description" };
			for (String str : Fields) {
				if (indexType.equals(str)) {
					concreteDocuments(document, iwriter);
					break;
				}
			}

			if (indexType.equals("History")) {
				for (String searchedWords : history) {
					Document doc = getHistoryDoc(searchedWords);
					iwriter.addDocument(doc);
				}
			}
			iwriter.close();
			System.out.println("Index created");
			directory.close();
		} catch (FileAlreadyExistsException exising) {
			// exising.fillInStackTrace();
			System.out.println(exising.fillInStackTrace());
		}

	}

	private Document getHistoryDoc(String searchedWord) {
		Document doc = new Document();
		doc.add(new Field("word", searchedWord, TextField.TYPE_STORED));
		return doc;
	}

	private void concreteDocuments(File[] file, IndexWriter iwriter) throws IOException {
		String path = file[0].getAbsolutePath();
		String type = "";
		String title = "";
		String director = "";
		String country = "";
		String rating = "";
		String description = "";
		try {
			FileReader fileReader = new FileReader(path);
			BufferedReader br = new BufferedReader(fileReader);
			String line = br.readLine();
			while (line != null) {
				Document doc = new Document();
				String data = line;
				String token = "";
				ArrayList<String> tokensInLine = new ArrayList<String>();

				int flag = 0; // gia ta "
				int flag2 = 0; // gia ta ,
				splitString(data, token, tokensInLine, flag, flag2);
				type = tokensInLine.get(1);
				title = tokensInLine.get(2);
				director = tokensInLine.get(3);
				country = tokensInLine.get(5);
				rating = tokensInLine.get(8);
				description = tokensInLine.get(11);
				initializeFieldsDocument(type, title, director, country, rating, description, doc);
				iwriter.addDocument(doc);
				line = br.readLine();
			}
			fileReader.close();

		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}

	private void initializeFieldsDocument(String type, String title, String director, String country, String rating,
			String description, Document doc) {
		doc.add((IndexableField) new SortedDocValuesField("type", new BytesRef(type)));
		doc.add(new Field("type", type, TextField.TYPE_STORED));
		doc.add((IndexableField) new SortedDocValuesField("title", new BytesRef(title)));
		doc.add(new Field("title", title, TextField.TYPE_STORED));
		doc.add(new Field("director", director, TextField.TYPE_STORED));
		doc.add(new Field("country", country, TextField.TYPE_STORED));
		doc.add(new Field("rating", rating, TextField.TYPE_STORED));
		doc.add(new Field("description", description, TextField.TYPE_STORED));

	}

	private void splitString(String line, String token, ArrayList<String> listWithTokensInLine, int flag, int flag2) {

		for (int i = 0; i < line.length(); i++) {

			if (line.charAt(i) != '"' && line.charAt(i) != ',') {
				if (IsLastChar(i, line, token, listWithTokensInLine, flag2)) {
					break;
				}
				token = token + line.charAt(i);
				flag2 = 0;
			} else if (line.charAt(i) == '"') {

				if (flag == 0) {
					flag++;

				} else {
					flag--;
					if (IsLastChar(i, line, token, listWithTokensInLine, flag2)) {
						break;
					}

				}
				flag2 = 0;
				token = token + line.charAt(i);
			} else if (line.charAt(i) == ',') {
				if (flag2 == 1) {
					token = "Δεν υπάρχει καταχώρηση";
				}

				if (flag % 2 == 0) {

					flag2 = 1;
					listWithTokensInLine.add(token);
					token = "";
				} else {
					token = token + line.charAt(i);
				}

			}

		}

	}

	private boolean IsLastChar(int position, String line, String token, ArrayList<String> listWithTokensInLine,
			int flag2) {

		if (position + 1 == line.length()) {
			token = token + line.charAt(position);
			listWithTokensInLine.add(token);
			flag2 = 0;
			return true;
		}
		return false;

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
}
