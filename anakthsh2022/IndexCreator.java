package anakthsh2022;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

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
		getAnalyzer(analyzerType);
		createIndex(/*analyzerType*/);

	}

	public IndexCreator(String indexType, String indexPath, File[] documents, String analyzerType,
			ArrayList<String> history) throws IOException {
		this.indexType = indexType;
		this.indexPath = indexPath;
		this.document = documents;
		this.history = history;
		getAnalyzer(analyzerType);
		createIndex(/*analyzerType*/);
	}

	private void createIndex(/*String analyzerType*/) throws IOException {

		try {
			Path indexP = Paths.get(indexPath);
			//getAnalyzer(analyzerType);
			Directory directory = FSDirectory.open(indexP);
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter iwriter = new IndexWriter(directory, config);
			System.out.println("Start creating index.");
			String Fields[] = { "type", "title", "director", "country", "rating", "description" };
			for (String str : Fields) {
				if (indexType.equals(str)) {
					concreteDocuments(document, iwriter);
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
				ArrayList<String> tokens = new ArrayList<String>();
				int flag = 0; // gia ta "
				int flag2 = 0; // gia ta ,
				splitString(data, token, tokens, flag, flag2);
				type = tokens.get(1);
				title = tokens.get(2);
				director = tokens.get(3);
				country = tokens.get(5);
				rating = tokens.get(8);
				description = tokens.get(11);
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

	private void initializeFieldsDocument(String type, String title, String director, String country,
			String rating, String description, Document doc) {
		doc.add(new Field("type", type, TextField.TYPE_STORED));
		doc.add(new Field("title", title, TextField.TYPE_STORED));
		doc.add(new Field("director", director, TextField.TYPE_STORED));
		doc.add(new Field("country", country, TextField.TYPE_STORED));
		doc.add(new Field("rating", rating, TextField.TYPE_STORED));
		doc.add(new Field("description", description, TextField.TYPE_STORED));
	}

	private void splitString(String line, String token, ArrayList<String> listWithTokensInLine, int flag, int flag2) {

		for (int i = 0; i < line.length(); i++) {

			if (line.charAt(i) != '"' && line.charAt(i) != ',') {
				/*
				if (i + 1 == line.length()) {
					token = token + line.charAt(i);
					listWithTokensInLine.add(token);
					flag2 = 0;
					break;
				}
				*/
				if(IsLastChar(i,line,token,listWithTokensInLine,flag2)) {
					break;
				}
				token = token + line.charAt(i);
				flag2 = 0;
			} else if (line.charAt(i) == '"') {

				if (flag == 0) {
					flag++;

				} else {
					flag--;
					/*
					if (i + 1 == line.length()) {
						token = token + line.charAt(i);
						listWithTokensInLine.add(token);
						flag2 = 0;
						break;
					}
					*/
					if(IsLastChar(i,line,token,listWithTokensInLine,flag2)) {
						break;
					}
					
				}
				flag2 = 0;
				token = token + line.charAt(i);
			} else if (line.charAt(i) == ',') {
				if (flag2 == 1) {
					// tokens.add("NULL");
					token = "NULL";

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

	private boolean IsLastChar(int position, String line, String token, ArrayList<String> listWithTokensInLine, int flag2) {
		
		if (position + 1 == line.length()) {
			token = token + line.charAt(position);
			listWithTokensInLine.add(token);
			flag2 = 0;
			return true;
		}
		return false;
		
		
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
			JOptionPane.showMessageDialog(new JFrame(),
					"An incorrect option of the analyzer is given please try again");
		}
	}
}
