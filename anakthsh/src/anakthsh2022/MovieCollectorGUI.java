
package anakthsh2022;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class MovieCollectorGUI extends JFrame {

	public MovieCollectorGUI() {
		initComponents();
	}

	private AutoComplete autoComplete;
	private String currentSearchWord;
	private String defaultAnalyzer = "StandardAnalyzer";
	private ArrayList<String> listFromHistory;
	private ScoreDoc[] hits;
	private String sortBy = "Relevance";
	private String indexPath = "indexDict";
	private String indexHistory = "indexHistory";
	private String pathOfIndex = indexPath;
	private int fromIndex = 0;
	private int toIndex = 10;
	String fieldPrint = "";
	int lengthStr = 0;
	Highlighter.HighlightPainter color = new DefaultHighlighter.DefaultHighlightPainter(Color.green);

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MovieCollectorGUI GUI = new MovieCollectorGUI();
				try {
					GUI.loadSearchHistory();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				GUI.setVisible(true);
			}
		});
	}

	private void loadSearchHistory() throws IOException {

		listFromHistory = new ArrayList<String>();
		try {
			File historyFile = new File("history.txt");
			if (!historyFile.createNewFile()) {
				Scanner myReader = new Scanner(historyFile);
				while (myReader.hasNextLine()) {
					String data = myReader.nextLine();
					listFromHistory.add(data);
				}
				myReader.close();
			}
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		Path path = Paths.get(indexHistory);
		if (Files.exists(path)) {
			System.out.println("History index already created and ready to use.");
		} else if (listFromHistory.size() > 0) {
			updateHistoryIndex();
		}
		autoComplete = new AutoComplete(indexHistory);

	}

	public void saveHistory() throws IOException {
		try {
			FileWriter historyFile = new FileWriter("history.txt");
			for (String word : listFromHistory) {
				historyFile.write(word + "\n");
			}
			historyFile.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		updateHistoryIndex();
	}

	private void deleteIndex(String folderName) {

		File folder = new File(folderName);
		if (folder.exists()) {
			File[] documents = folder.listFiles();
			for (File f : documents) {
				f.delete();
			}
			folder.delete();
			System.out.println("Deleted index: " + folderName);
		}
	}

	private void updateHistoryIndex() throws IOException {
		deleteIndex("indexHistory");
		IndexCreator indeHistory = new IndexCreator("History", indexHistory, null, "WhitespaceAnalyzer",
				listFromHistory);
	}

	private void createIndexActionPerformed(ActionEvent e) throws IOException {
		Corpus corpus = new Corpus("input");
		deleteIndex(indexPath);

		updateHistoryIndex();
		try {
			IndexCreator index = new IndexCreator("type", indexPath, corpus.getDocuments(), defaultAnalyzer);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		textPane1.setText("Index created");
	}

	private void showSearchHistoryActionPerformed(ActionEvent e) {
		String toTextField = "";
		for (String searchedTerm : listFromHistory) {
			toTextField += searchedTerm + " ";
		}
		textPane1.setText(toTextField);

	}

	private void clearHistoryActionPerformed(ActionEvent e) {
		listFromHistory.clear();
	}

	private void standarAnalyzerActionPerformed(ActionEvent e) {
		defaultAnalyzer = "StandardAnalyzer";
	}

	private void whiteSpaceActionPerformed(ActionEvent e) {
		defaultAnalyzer = "WhitespaceAnalyzer";
	}

	private void simpleAnalyzerActionPerformed(ActionEvent e) {
		defaultAnalyzer = "SimpleAnalyzer";
	}

	private void StopAnalyzerPerformed(ActionEvent e) {
		defaultAnalyzer = "StopAnalyzer";
	}

	private void searchActionPerformed(ActionEvent e) throws IOException {
		currentSearchWord = textField1.getText();
		fromIndex = 0;
		toIndex = 10;
		String[] fields = { "type:", "title:", "director:", "country:", "rating:", "description:", "wholeDocument:" };
		if (!currentSearchWord.equals("")) {
			for (String str : fields) {
				if (currentSearchWord.contains(str)) {

					if (checkCurrentSearchWord(str)) {
						return;
					}
					;

					currentSearchWord = currentSearchWord.substring(str.length());
					fieldPrint = str.substring(0, str.length() - 1); // xwris to :

					Search searchEngine = new Search(fieldPrint, indexPath, currentSearchWord, defaultAnalyzer, sortBy);
					hits = searchEngine.getHits();
					pathOfIndex = indexPath;
					break;
				}

			}
			if (listFromHistory.contains(currentSearchWord) == false) {
				listFromHistory.add(currentSearchWord);
				saveHistory();
				updateHistoryIndex();
			}
			DisplayResults(fieldPrint);
		}

	}

	private boolean checkCurrentSearchWord(String str) {

		String searchWord = currentSearchWord.substring(str.length(), currentSearchWord.length());
		String tokensInSearchWords[] = searchWord.split(" ");
		for (String tok : tokensInSearchWords) {
			if (!tok.equals("")) {
				if (tok.charAt(0) == '*' || tok.charAt(0) == '?') {
					JOptionPane.showMessageDialog(this, "Προσοχή στη χρήση των * ?\n Δοκίμασε ξανά αναζήτηση.");
					return true;
				}
			}

		}
		return false;

	}

	private IndexSearcher getHitDocuments() {
		IndexSearcher isearcher = null;
		Path indexP = Paths.get(pathOfIndex);
		try {
			FSDirectory directory = FSDirectory.open(indexP);
			DirectoryReader ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isearcher;
	}

	private void DisplayResults(String field) throws IOException {
		IndexSearcher isearcher = getHitDocuments();
		String showRes = "Results for :" + currentSearchWord + ":\n";
		if (hits != null) {
			if (hits.length != 0) {
				managementPages();
				textPane1.setText("");
				if (hits.length < 10) {
					toIndex = hits.length;
				}

				for (int i = fromIndex; i < toIndex; i++) {

					String print = ("\n\tFound " + (i + 1) + " out of " + (hits.length) + "\n\n");
					showRes = showRes + print;
					Document hitDoc = isearcher.doc(hits[i].doc);
					if (field.equals("wholeDocument")) {
						String text = hitDoc.get("title");
						showRes = showRes + text;

					} else {
						String text = hitDoc.get(field);
						showRes = showRes + text;

					}

				}

				textPane1.setText(showRes);
			} else {

				JOptionPane.showMessageDialog(this, "Not found !");
				textPane1.setText("");
				return;

			}

			try {
				Highlighter highlighter = textPane1.getHighlighter();
				javax.swing.text.Document doc = textPane1.getDocument();
				String text = doc.getText(0, doc.getLength());// -lengthPrintFound);
				String[] linesWithoutFound = text.split("\n");
				linesWithoutFound[0] = "";
				int position = 0;
				String[] str_search_list;
				str_search_list = currentSearchWord.split(" ");
				for (String inlist : str_search_list) {
					if (inlist.equals("")) {
						continue;
					}
					char firstChar = inlist.charAt(0);
					firstChar = Character.toUpperCase(firstChar);
					String prwtokefalaio = firstChar + inlist.substring(1, inlist.length()).toLowerCase();
					String currentWords[] = { inlist, inlist.toLowerCase(), inlist.toUpperCase(), prwtokefalaio };

					for (String str : currentWords) {
						while ((position = text.indexOf(str, position)) >= 0) {
							highlighter.addHighlight(position, position + str.length(), color);
							position += str.length();

						}
					}

				}

			} catch (BadLocationException e) {
				e.printStackTrace();
			}

		}
	}

	private void managementPages() {

		if (toIndex > hits.length && fromIndex < hits.length) {
			toIndex = hits.length;
		} else if (fromIndex < 0) {
			JOptionPane.showMessageDialog(this, "You are at the first page.");
			fromIndex = 0;
			toIndex = 10;
		} else if (fromIndex >= hits.length) {

			JOptionPane.showMessageDialog(this, "You're in the last page.");
			fromIndex = fromIndex - 10;
			toIndex = hits.length;// toIndex - 10;

		}
	}

	private void nextPageButtonActionPerformed(ActionEvent e) throws IOException {
		fromIndex += 10;
		toIndex += 10;
		DisplayResults(fieldPrint);

	}

	private void previousPageButtonActionPerformed(ActionEvent e) throws IOException {
		fromIndex -= 10;
		toIndex -= 10;
		DisplayResults(fieldPrint);
	}

	private void textField1KeyPressed(KeyEvent e) {
		String toTextField = "";
		try {

			ArrayList<String> suggestions = autoComplete.moreLikeThis(textField1.getText());
			for (String suggest : suggestions) {
				toTextField += suggest + "   ||   ";
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		suggestionsTextField.setText(toTextField);
	}

	private void relevanceMenuItemActionPerformed(ActionEvent e) {
		sortBy = "Relevance";
	}

	private void indexorderMenuItemActionPerformed(ActionEvent e) {
		sortBy = "IndexOrder";
	}

	private void titleMenuItemActionPerformed(ActionEvent e) {
		sortBy = "title";
	}

	private void typeMenuItemActionPerformed(ActionEvent e) {
		sortBy = "type";
	}

	private void descriptionItemeMenuItemActionPerformed(ActionEvent e) throws IOException {
		fieldPrint = "description";
		DisplayResults(fieldPrint);
	}

	private void ratingItemeMenuItemActionPerformed(ActionEvent e) throws IOException {
		fieldPrint = "rating";
		DisplayResults(fieldPrint);

	}

	private void countryItemeMenuItemActionPerformed(ActionEvent e) throws IOException {
		fieldPrint = "country";
		DisplayResults(fieldPrint);
	}

	private void directorItemeMenuItemActionPerformed(ActionEvent e) throws IOException {
		fieldPrint = "director";
		DisplayResults(fieldPrint);
	}

	private void titleItemeMenuItemActionPerformed(ActionEvent e) throws IOException {
		fieldPrint = "title";
		DisplayResults(fieldPrint);
	}

	private void typeItemeMenuItemActionPerformed(ActionEvent e) throws IOException {
		fieldPrint = "type";
		DisplayResults(fieldPrint);
	}

	private void initComponents() {

		menuBar1 = new JMenuBar();
		menu1 = new JMenu();
		menuItem2 = new JMenuItem();
		menuItem6 = new JMenuItem();
		menuItem7 = new JMenuItem();
		analyzerMenu = new JMenu();
		menuItem1 = new JMenuItem();
		menuItem3 = new JMenuItem();
		menuItem4 = new JMenuItem();
		menuItem5 = new JMenuItem();
		sortByMenu = new JMenu();
		fieldForPrint = new JMenu(); //////////
		typeItem = new JMenuItem();
		titleItem = new JMenuItem();
		directorItem = new JMenuItem();
		countryItem = new JMenuItem();
		ratingItem = new JMenuItem();
		descriptionItem = new JMenuItem();
		relevanceMenuItem = new JMenuItem();
		indexorderMenuItem = new JMenuItem();
		titleMenuItem = new JMenuItem();
		typeMenuItem = new JMenuItem();
		previousPageButton = new JButton();
		nextPageButton = new JButton();
		button1 = new JButton();
		textField1 = new JTextField();
		scrollPane1 = new JScrollPane();
		textPane1 = new JTextPane();
		suggestionsTextField = new JTextField();

		// ======== this ========
		Container contentPane = getContentPane();

		// ======== menuBar1 ========
		{

			// ======== menu1 ========
			{
				menu1.setText("Options");

				// ---- menuItem2 ----
				menuItem2.setText("Create Index");
				menuItem2.addActionListener(e -> {
					try {
						createIndexActionPerformed(e);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				});
				menu1.add(menuItem2);

				// ---- menuItem6 ----
				menuItem6.setText("Search History");
				menuItem6.addActionListener(e -> showSearchHistoryActionPerformed(e));
				menu1.add(menuItem6);

				// ---- menuItem7 ----
				menuItem7.setText("Clear Search History");
				menuItem7.addActionListener(e -> clearHistoryActionPerformed(e));
				menu1.add(menuItem7);
			}
			menuBar1.add(menu1);

			// ======== analyzerMenu ========
			{
				analyzerMenu.setText("Analyzer");

				// ---- menuItem1 ----
				menuItem1.setText("Standard Analyzer");
				menuItem1.addActionListener(e -> standarAnalyzerActionPerformed(e));
				analyzerMenu.add(menuItem1);

				// ---- menuItem3 ----
				menuItem3.setText("Whitespace Analyzer");
				menuItem3.addActionListener(e -> whiteSpaceActionPerformed(e));
				analyzerMenu.add(menuItem3);

				// ---- menuItem4 ----
				menuItem4.setText("Simple Analyzer");
				menuItem4.addActionListener(e -> simpleAnalyzerActionPerformed(e));
				analyzerMenu.add(menuItem4);

				// ---- menuItem5 ----
				menuItem5.setText("StopWords Analyzer");
				menuItem5.addActionListener(e -> StopAnalyzerPerformed(e));
				analyzerMenu.add(menuItem5);
			}
			menuBar1.add(analyzerMenu);

			// ======== sortByMenu ========
			{
				sortByMenu.setText("Sort by");

				// ---- relevanceMenuItem ----
				relevanceMenuItem.setText("Relevance");
				relevanceMenuItem.addActionListener(e -> relevanceMenuItemActionPerformed(e));
				sortByMenu.add(relevanceMenuItem);

				// ---- indexorderMenuItem ----
				indexorderMenuItem.setText("Index order");
				indexorderMenuItem.addActionListener(e -> indexorderMenuItemActionPerformed(e));
				sortByMenu.add(indexorderMenuItem);

				// ---- typeMenuItem ----
				typeMenuItem.setText("type");
				typeMenuItem.addActionListener(e -> typeMenuItemActionPerformed(e));
				sortByMenu.add(typeMenuItem);

				// ---- titleMenuItem ----
				titleMenuItem.setText("title");
				titleMenuItem.addActionListener(e -> titleMenuItemActionPerformed(e));
				sortByMenu.add(titleMenuItem);
			}
			menuBar1.add(sortByMenu);

			///////////////////////////////////////////////////
			// ======== fieldForPrint ========
			{
				fieldForPrint.setText("fieldForPrint");
				// ---- type ----
				typeItem.setText("type");
				typeItem.addActionListener(e -> {
					try {
						typeItemeMenuItemActionPerformed(e);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				});
				fieldForPrint.add(typeItem);

				// ---- title ----
				titleItem.setText("title");
				titleItem.addActionListener(e -> {
					try {
						titleItemeMenuItemActionPerformed(e);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				});
				fieldForPrint.add(titleItem);

				// ---- director ----
				directorItem.setText("director");
				directorItem.addActionListener(e -> {
					try {
						directorItemeMenuItemActionPerformed(e);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				});
				fieldForPrint.add(directorItem);

				// ---- country ----
				countryItem.setText("country");
				countryItem.addActionListener(e -> {
					try {
						countryItemeMenuItemActionPerformed(e);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				});
				fieldForPrint.add(countryItem);

				// ---- rating ----
				ratingItem.setText("rating");
				ratingItem.addActionListener(e -> {
					try {
						ratingItemeMenuItemActionPerformed(e);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				});
				fieldForPrint.add(ratingItem);

				// ---- description ----
				descriptionItem.setText("description");
				descriptionItem.addActionListener(e -> {
					try {
						descriptionItemeMenuItemActionPerformed(e);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				});
				fieldForPrint.add(descriptionItem);

			}
			menuBar1.add(fieldForPrint);

			////////////////////////////////////////////////
			// ---- previousPageButton ----
			previousPageButton.setText("Previous page");
			previousPageButton.addActionListener(e -> {
				try {
					previousPageButtonActionPerformed(e);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
			menuBar1.add(previousPageButton);

			// ---- nextPageButton ----
			nextPageButton.setText("Next page");
			nextPageButton.addActionListener(e -> {
				try {
					nextPageButtonActionPerformed(e);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
			menuBar1.add(nextPageButton);

			// ---- button1 ----
			button1.setText("Search");
			button1.addActionListener(e -> {
				try {
					searchActionPerformed(e);
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			});
			menuBar1.add(button1);

			// ---- textField1 ----
			textField1.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					textField1KeyPressed(e);
				}
			});
			menuBar1.add(textField1);

		}
		setJMenuBar(menuBar1);

		// ======== scrollPane1 ========
		{
			scrollPane1.setViewportView(textPane1);
		}

		GroupLayout contentPaneLayout = new GroupLayout(contentPane);
		contentPane.setLayout(contentPaneLayout);
		contentPaneLayout.setHorizontalGroup(contentPaneLayout.createParallelGroup()
				.addGroup(contentPaneLayout.createSequentialGroup().addContainerGap()
						.addGroup(contentPaneLayout.createParallelGroup()
								.addComponent(suggestionsTextField, GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
								.addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE))
						.addContainerGap()));
		contentPaneLayout.setVerticalGroup(contentPaneLayout.createParallelGroup()
				.addGroup(contentPaneLayout.createSequentialGroup()
						.addComponent(suggestionsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE).addContainerGap()));
		pack();
		setLocationRelativeTo(getOwner());

	}

	private JMenuBar menuBar1;
	private JMenu menu1;
	private JMenuItem menuItem2;
	private JMenuItem menuItem6;
	private JMenuItem menuItem7;
	private JMenu analyzerMenu;
	private JMenuItem menuItem1;
	private JMenuItem menuItem3;
	private JMenuItem menuItem4;
	private JMenuItem menuItem5;
	private JMenu sortByMenu;
	private JMenu fieldForPrint;
	private JMenuItem typeItem;
	private JMenuItem titleItem;
	private JMenuItem directorItem;
	private JMenuItem countryItem;
	private JMenuItem ratingItem;
	private JMenuItem descriptionItem;
	private JMenuItem relevanceMenuItem;
	private JMenuItem indexorderMenuItem;
	private JMenuItem typeMenuItem;
	private JMenuItem titleMenuItem;
	private JButton previousPageButton;
	private JButton nextPageButton;
	private JButton button1;
	private JTextField textField1;
	private JScrollPane scrollPane1;
	private JTextPane textPane1;
	private JTextField suggestionsTextField;

}
