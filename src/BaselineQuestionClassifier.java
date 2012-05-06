import java.util.HashSet;

import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;



public class BaselineQuestionClassifier extends AbstractQuestionClassifier {
	
	@Override
	public void classifyQuestion(Question q) {
		
		HashSet<String> occ = new HashSet<String>();
		
		for (CoreLabel token: q.getTaggedQuestion().get(TokensAnnotation.class)) {
			String word = token.get(TextAnnotation.class);
			occ.add(word.toLowerCase());
		}
		
		// TIME, LOCATION, ORGANIZATION, PERSON, MONEY, PERCENT, DATE
		
		if (occ.contains("who")) {
			
			// do something with plural vs singular to determine group vs individual
			q.atype = Question.answer_type.PERSON;
			
		} else if (occ.contains("percent")) {
			
			q.atype = Question.answer_type.PERCENT;
			
		} else if (occ.contains("time") || occ.contains("hour") || occ.contains("minute") || occ.contains("second")) {
			
			q.atype = Question.answer_type.TIME;
			
		} else if (occ.contains("what")) {
			
			if (occ.contains("year")) {
				q.atype = Question.answer_type.TIME;
			} else if (occ.contains("population")) {
				
				q.atype = Question.answer_type.NUMBER;
				
			} else {
				q.atype = Question.answer_type.STANDARD;
			}
			
		} else if (occ.contains("which")) {
			
			q.atype = Question.answer_type.ORGANIZATION;
			
		} else if (occ.contains("where")) {
			
			q.atype = Question.answer_type.LOCATION;
			
		} else if (occ.contains("when")) {
			
			q.atype = Question.answer_type.TIME;
			
		} else if (occ.contains("why")) {
			
			q.atype = Question.answer_type.REASON;
			
		} else if (occ.contains("money") || occ.contains("cost")){
			
			q.atype = Question.answer_type.MONEY;
			
		} else if (occ.contains("how") && occ.contains("many")){
			
			q.atype = Question.answer_type.NUMBER;	
		} else {
			q.atype = Question.answer_type.STANDARD;
		}
	}

}
