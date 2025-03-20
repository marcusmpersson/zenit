package main.java.zenit.searchinfile;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 * Controller for the search bar in the main window. Handles search and replace functionality.
 * @author Louis Brown
 */
public class SearchInFileController extends AnchorPane {
	
	@FXML 
	private Button btnEsc;

	@FXML TextField searchField;

	@FXML
	private AnchorPane searchBar;
	
	@FXML
	private Label lblOccurrences;

	@FXML
	private Button btnUp;

	@FXML
	private Button btnDown;

	private Search search;
	
	private int occurrences = 0;

	@FXML
	private AnchorPane replaceBar;

	@FXML
	private TextField replaceField;

	@FXML
	private Button btnReplaceOne;

	@FXML
	private Button btnReplaceAll;

	@FXML
	private Button btnAddReplace;

	/**
	 * Constructor for the SearchInFileController
	 * @param search The search object to use
	 * @param searchField The search field
	 * @param btnEsc The escape button
	 * @param lblOccurrences The label for the occurrences
	 * @param searchBar The search bar
	 * @param btnUp The up button
	 * @param btnDown The down button
	 * @param replaceOne The replace one button
	 * @param replaceAll The replace all button
	 * @param addReplace The add replace button
	 * @param replaceBar The replace bar
	 * @param replaceField The replace field
	 */
	public SearchInFileController(Search search, TextField searchField, Button btnEsc, Label lblOccurrences, AnchorPane searchBar, Button btnUp
			, Button btnDown, Button replaceOne, Button replaceAll, Button addReplace, AnchorPane replaceBar, TextField replaceField) {
		this.search = search;
		this.searchField = searchField;
		this.btnEsc = btnEsc;
		this.lblOccurrences = lblOccurrences;
		this.searchBar = searchBar;
		this.btnUp = btnUp;
		this.btnDown = btnDown;
		this.replaceBar = replaceBar;
		this.replaceField = replaceField;
		this.btnReplaceOne = replaceOne;
		this.btnReplaceAll = replaceAll;
		this.btnAddReplace = addReplace;

		initialize();
	}
	
	private void makeNewSearch(String searchWord) {
		
		occurrences = search.searchInFile(searchWord);

		if(occurrences < 1) {
			if(searchField.getText().length() > 0) {
				lblOccurrences.setText("0/" + occurrences);
			}else {
				lblOccurrences.setText("");
			}

		}else {
			lblOccurrences.setText("1/" + occurrences);
		}	
	}

	/**
	 * Initializes the search bar and its functionality.
	 */
	private void initialize() {
		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			search.clearZen();
			makeNewSearch(searchField.getText());
		});

		btnEsc.setOnAction(event -> {
			search.cleanZen();
			searchBar.setVisible(false);
			searchBar.setManaged(false);
			searchBar.setPrefHeight(0);
			searchBar.setMinHeight(0);
			searchBar.setMaxHeight(0);

			if (searchBar.getParent() != null) {
				searchBar.getParent().requestLayout();
			}

			btnAddReplace.setText("▶");
			replaceBar.setVisible(false);
			replaceBar.setManaged(false);
			replaceBar.setPrefHeight(0);
			replaceBar.setMinHeight(0);
			replaceBar.setMaxHeight(0);

		});

		btnUp.setOnAction(event ->{
			int i = search.jumpUp();
			i++;
			lblOccurrences.setText(i + "/" + occurrences);
		});

		btnDown.setOnAction(event -> {
			int i = search.jumpDown();
			i++;
			lblOccurrences.setText(i + "/" + occurrences);

		});

		btnAddReplace.setOnAction(event -> {
			if (btnAddReplace.getText().equals("▶")) {
				btnAddReplace.setText("▲");
				replaceBar.setVisible(true);
				replaceBar.setManaged(true);
				replaceBar.setPrefHeight(30);
				replaceBar.setMinHeight(30);
				replaceBar.setMaxHeight(30);

				if (replaceBar.getParent() != null) {
					replaceBar.getParent().requestLayout();
				}
			} else {
				btnAddReplace.setText("▶");
				replaceBar.setVisible(false);
				replaceBar.setManaged(false);
				replaceBar.setPrefHeight(0);
				replaceBar.setMinHeight(0);
				replaceBar.setMaxHeight(0);
			}
		});

		btnReplaceAll.setOnAction(event -> {
			search.replaceAll(replaceField.getText());
		});

		btnReplaceOne.setOnAction(event -> {
			search.replaceOne(replaceField.getText());
		});
	}
}
