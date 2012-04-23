import java.util.ArrayList;


public abstract class AbstractFilter {
	
	// extend this class to make a filter.
	// A filter takes a document and returns all answers that are the same type as the expected answer.
	
	public abstract ArrayList<Answer> filter(Question q, DocumentSet ds);
	
}
