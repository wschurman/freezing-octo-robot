import java.util.ArrayList;
import java.util.HashMap;


public class BaselineAnswerExtractor extends AbstractAnswerExtractor {

	@Override
	public ArrayList<Answer> extractAnswers(Question q, ArrayList<Answer> as) {
		
		// cosine similarity need
		
		HashMap<String, Integer> ans = new HashMap<String, Integer>();
		
		for (Answer a : as) {
			if (!ans.containsKey(a.answer)) {
				ans.put(a.answer, 0);
			}
			ans.put(a.answer, ans.get(a.answer) + 1);
		}
		
		int max = 0;
		String maxs = "";
		
		for (String s : ans.keySet()) {
			if (ans.get(s) > max) {
				max = ans.get(s);
				maxs = s;
			}
		}
		
		ArrayList<Answer> ret = new ArrayList<Answer>();
		ret.add(new Answer(maxs));
		
		return ret;
	}

}
