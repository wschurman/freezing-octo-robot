import java.util.HashMap;


public class Document {
	
	public HashMap<String, String> content;
	
	public Document(HashMap<String, String> con) {
		this.content = con;
	}
	
	public String getText() {
		return content.get("text");
	}
}
