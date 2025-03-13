package main.java.zenit.zencodearea;

import javafx.concurrent.Task;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import main.java.zenit.completions.CompletionModule;
import main.java.zenit.ui.tree.FileTreeItem;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ZenCodeArea extends CodeArea {
	private ExecutorService executor;
	private CompletionModule completionModule;
//	private int fontSize;
//	private String font;
	private FileTreeItem<String> fileTreeItem;

	private static final String[] KEYWORDS = new String[] {
		"abstract", "assert", "boolean", "break", "byte",
		"case", "catch", "char", "class", "const",
		"continue", "default", "do", "double", "else",
		"enum", "extends", "false", "final", "finally", "float",
		"for", "goto", "if", "implements", "import",
		"instanceof", "int", "interface", "long", "native",
		"new", "package", "private", "protected", "public",
		"return", "short", "static", "strictfp", "super",
		"switch", "synchronized", "this", "throw", "throws",
		"transient", "true", "try", "void", "volatile", "while"
	};

	private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
	private static final String PAREN_PATTERN = "\\(|\\)";
	private static final String BRACE_PATTERN = "\\{|\\}";
	private static final String BRACKET_PATTERN = "\\[|\\]";
	private static final String SEMICOLON_PATTERN = "\\;";
	private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
	private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

	private static final Pattern PATTERN = Pattern.compile(
		"(?<KEYWORD>" + KEYWORD_PATTERN + ")"
		+ "|(?<PAREN>" + PAREN_PATTERN + ")"
		+ "|(?<BRACE>" + BRACE_PATTERN + ")"
		+ "|(?<BRACKET>" + BRACKET_PATTERN + ")"
		+ "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
		+ "|(?<STRING>" + STRING_PATTERN + ")"
		+ "|(?<COMMENT>" + COMMENT_PATTERN + ")"
	);

	public ZenCodeArea() {
		this(14, "Times new Roman");
	}

	public ZenCodeArea(int textSize, String font) {
		setParagraphGraphicFactory(LineNumberFactory.get(this));

		multiPlainChanges().successionEnds(
			Duration.ofMillis(100)).subscribe(
				ignore -> setStyleSpans(0, computeHighlighting(getText(
			))));

		executor = Executors.newSingleThreadExecutor();
		setParagraphGraphicFactory(LineNumberFactory.get(this));
		multiPlainChanges().successionEnds(Duration.ofMillis(500)).supplyTask(
			this::computeHighlightingAsync).awaitLatest(multiPlainChanges()).filterMap(t -> {
				if(t.isSuccess()) {
					return Optional.of(t.get());
				} else {
					t.getFailure().printStackTrace();
					return Optional.empty();
				}
		}).subscribe(this::applyHighlighting);
		computeHighlightingAsync();

//		fontSize = textSize;
//		this.font = font;
		setStyle("-fx-font-size: " + textSize +";-fx-font-family: " + font);

		this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.TAB) {
				int currentParagraph = this.getCurrentParagraph();
				String lineText = this.getParagraph(currentParagraph).getText();
				checkForSuggestions(lineText);
			}
		});

		this.requestFocus();

		completionModule = new CompletionModule(this);

		this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.isControlDown() && event.getCode() == KeyCode.SPACE) {
				completionModule.showCompletions();
				event.consume();
			}
		});
	}

	public void setCompletionFileName(String fileName) {
		if (completionModule != null) {
			completionModule.setCurrentFileName(fileName);
		}
	}

	public ArrayList<String> codeSuggestionArray(){
		ArrayList<String> suggestions = new ArrayList<>();
		suggestions.add("sout");
		suggestions.add("psvm");
		suggestions.add("fori");
		suggestions.add("switch");
		suggestions.add("while");
		suggestions.add("scanner");
		suggestions.add("classdef");
		suggestions.add("try");
		suggestions.add("thread");

		return suggestions;
	}

	//provide code suggestions based on the users input.
	public void checkForSuggestions(String input) {
		ArrayList<String> suggestions = codeSuggestionArray();

		for (String suggestion : suggestions) {
			if (input.contains(suggestion)) {
				switch (suggestion) {
					case "sout":

						/*
						replaceText  replaces a portion of the text in the CodeArea. It takes three parameters: the start position,
						the end position, and the replacement text.

						The start position is calculated by subtracting the length of the suggestion from the current caret position.
						This means that it replaces the text from were the cursor is to the start of the suggestion.

						The end position is the current caret position, which means that the whole word is replaced.

						"moveTo" moves the caret to the position specified. In this case, it moves the caret to 2 positions back, inside the parenthesis.
						 */
						this.replaceText(this.getCaretPosition() - suggestion.length(), this.getCaretPosition(), "System.out.println();");
						this.moveTo(this.getCaretPosition() - 2);
						break;
					case "psvm":
						this.replaceText(this.getCaretPosition() - suggestion.length(), this.getCaretPosition(), "public static void main(String[] args) { }");
						this.moveTo(this.getCaretPosition() - 2);
						break;
					case "fori":
						this.replaceText(this.getCaretPosition() - suggestion.length(), this.getCaretPosition(), "for (int i = 0; i < ; i++) { }");
						this.moveTo(this.getCaretPosition() - 9);
						break;

					case "switch":
						this.replaceText(this.getCaretPosition() - suggestion.length(), this.getCaretPosition(), "switch () { case : break; default: break; }");
						this.moveTo(this.getCaretPosition());
						break;

					case "while":
						this.replaceText(this.getCaretPosition() - suggestion.length(), this.getCaretPosition(), "while () { }");
						this.moveTo(this.getCaretPosition() - 1);
						break;

					case "scanner":
						this.replaceText(this.getCaretPosition() - suggestion.length(), this.getCaretPosition(), "Scanner input = new Scanner(System.in);");
						this.moveTo(this.getCaretPosition() - 1);
						break;

					case "classdef":
						this.replaceText(this.getCaretPosition() - suggestion.length(), this.getCaretPosition(), "public class ClassName { }");
						this.moveTo(this.getCaretPosition() - 1);
						break;

					case "try":
						this.replaceText(this.getCaretPosition() - suggestion.length(), this.getCaretPosition(), "try { } catch (Exception e) { }");
						this.moveTo(this.getCaretPosition() - 1);
						break;

					case "thread":
						this.replaceText(this.getCaretPosition() - suggestion.length(), this.getCaretPosition(), "Thread thread = new Thread();");
						this.moveTo(this.getCaretPosition() - 1);
						break;
				}
			}
		}
	}

	public void codeSuggestionDropDownMenu(){

	}

	public void update() {
		var highlighting = computeHighlighting(getText());
		applyHighlighting(highlighting);
	}
	
//	public int getFontSize() {
//		return fontSize;	
//	}
//	
//	
//	public String getFont() {
//		return font;	
//	}

	private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
		String text = getText();
		Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
			@Override
			protected StyleSpans<Collection<String>> call() throws Exception {
				return computeHighlighting(text);
			}
		};
		executor.execute(task);
		return task;
	}

	
	private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
		setStyleSpans(0, highlighting);
		InputMap<KeyEvent> im = InputMap.consume(
			EventPattern.keyPressed(KeyCode.TAB), 
			e -> this.replaceSelection("    ")
			);
		Nodes.addInputMap(this, im);
	}

	private static StyleSpans<Collection<String>> computeHighlighting(String text) {
		Matcher matcher = PATTERN.matcher(text);
		int lastKwEnd = 0;
		StyleSpansBuilder<Collection<String>> spansBuilder
		= new StyleSpansBuilder<>();
		while(matcher.find()) {
			String styleClass =
					matcher.group("KEYWORD") != null ? "keyword" :
						matcher.group("PAREN") != null ? "paren" :
							matcher.group("BRACE") != null ? "brace" :
								matcher.group("BRACKET") != null ? "bracket" :
									matcher.group("SEMICOLON") != null ? "semicolon" :
										matcher.group("STRING") != null ? "string" :
											matcher.group("COMMENT") != null ? "comment" :
												null; /* never happens */ 
			assert styleClass != null;
			spansBuilder.add(
					Collections.emptyList(), matcher.start() - lastKwEnd
					);
			spansBuilder.add(
					Collections.singleton(styleClass), matcher.end() - matcher.start()
					);
			lastKwEnd = matcher.end();
		}
		spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);

		return spansBuilder.create();
	}


	public void setFontSize(int newFontSize) {
		//fontSize = newFontSize;
		setStyle("-fx-font-size: " + newFontSize);
	}
	
	public void updateAppearance(String fontFamily, int size) {
	//	font = fontFamily;
		setStyle("-fx-font-family: " + fontFamily + ";" + 
				"-fx-font-size: " + size + ";");
	}

    public void unselectAll() {
		selectRange(0, 0);

    }

	private boolean containsMainMethod(String text) {
		return text.contains("public static void main(String[] args)");
	}

	public void setFileTreeItem(FileTreeItem<String> item) {
		this.fileTreeItem = item;

		this.textProperty().addListener((obs, oldText, newText) -> {
			if (containsMainMethod(newText)) {
				if (fileTreeItem != null) {
					fileTreeItem.addPlayIcon();
				}
			} else {
				if (fileTreeItem != null) {
					fileTreeItem.removePlayIcon();
				}
			}
		});
	}
}
