package main.java.zenit.zencodearea;

import javafx.concurrent.Task;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
//	private int fontSize;
//	private String font;

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
	}

	//provide code suggestions based on the users input.
	public void checkForSuggestions(String input) {
		ArrayList<String> suggestions = new ArrayList<>();
		suggestions.add("sout");
		suggestions.add("psvm");
		suggestions.add("fori");

		for (String suggestion : suggestions) {
			if (input.contains(suggestion)) {
				switch (suggestion) {
					case "sout":

						//the first getCaretPosition calculates the starting position for replacing the text.
						//the second getCaretPosition returns the current position of the caret (cursor) in the text area.
						// Subtracting suggestion.length() moves the position back by the length of the suggestion string,
						// effectively marking the start of the text to be replaced.


						/*
						replaceText  replaces a portion of the text in the CodeArea. It takes three parameters: the start position,
						the end position, and the replacement text.
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
				}
			}
		}
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
}
