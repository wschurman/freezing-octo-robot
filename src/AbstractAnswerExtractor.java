import java.util.ArrayList;


public abstract class AbstractAnswerExtractor {
	
	// extend this class to create an Answer extractor.
	// takes a set of answers that matched the correct answer type and selects the top 5
	
	public abstract ArrayList<Answer> extractAnswers(Question q, ArrayList<Answer> as);
	
}
