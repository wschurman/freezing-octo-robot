

public class BaselineQuestionClassifier extends AbstractQuestionClassifier {

	@Override
	public void classifyQuestion(Question q) {
		
		q.qtype = Question.question_type.STANDARD;
		q.atype = Question.answer_type.STANDARD;
		
	}

}
