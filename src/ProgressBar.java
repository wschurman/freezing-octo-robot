import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;


public class ProgressBar extends JPanel{
	
	private final JProgressBar prog = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
	private final JLabel docLabel = new JLabel();
	private final JLabel timeLabel = new JLabel();
	private int numDocs;
	private long start;
	private boolean done = false;
	
	public ProgressBar(int docs){
		numDocs = docs;
		start = System.currentTimeMillis();
		style();
	}
	
	private void style(){
		
		this.setPreferredSize(new Dimension(350, 150));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		prog.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JPanel time = new JPanel();
		time.setLayout(new FlowLayout(FlowLayout.CENTER));
		timeLabel.setFont(new Font("SansSerif", Font.BOLD, 35));
		timeLabel.setForeground(new Color(119, 147, 60));
		time.add(timeLabel);
		time.setBackground(new Color(16, 37, 63));
		
		JPanel forDoc = new JPanel();
		forDoc.setLayout(new FlowLayout(FlowLayout.CENTER));
		docLabel.setFont(new Font("SansSerif", Font.ITALIC, 20));
		docLabel.setForeground(new Color(119, 147, 60));
		forDoc.add(docLabel);
		forDoc.setBackground(new Color(16, 37, 63));
		
		this.add(time);
		this.add(forDoc);
		this.add(prog);
	}
	
	public boolean isDone(){
		return done;
	}
	
	public void updateTime(){
		if(prog.getValue() == 100){
			done = true;
			return;
		}
		long current = System.currentTimeMillis();
		long diff = current - start;
		String secs = (diff / 1000.0) + "";
		int ind = secs.indexOf(".");
		String t = secs.substring(0, Math.min(secs.length(), ind+3));
		timeLabel.setText(t + " secs");
		timeLabel.repaint();
		timeLabel.revalidate();
	}
	
	public void updateDocCount(int count){
		int perc = (int)(count * 100.0 / numDocs);
		docLabel.setText("Completed: " + count + " / " + numDocs);
		prog.setValue(perc);
		prog.revalidate();
		docLabel.revalidate();
	}
	
	
}
