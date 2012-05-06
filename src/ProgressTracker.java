import javax.swing.JFrame;
import javax.swing.UIManager;


public class ProgressTracker extends Thread {
	
	private final JFrame mainFrame = new JFrame("Progress");
	private int completed;
	private ProgressBar progress;
	
	public ProgressTracker(int numDocs){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		progress = new ProgressBar(numDocs);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.add(progress);
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
		completed = 0;
	}
	
	public void updateCompletion(int comp){
		completed = comp;
		progress.updateDocCount(comp);
		progress.revalidate();
		mainFrame.repaint();

	}
	
	@Override
	public void run(){
		while(true){
			progress.updateTime();
			progress.revalidate();
			mainFrame.repaint();
			if(progress.isDone())
				break;
			try {
				sleep(345);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception{
		
		ProgressTracker pt = new ProgressTracker(50);
		Thread t = new Thread(pt);
		t.start();
		
		for(int i = 0; i <= 50; i++){
			pt.updateCompletion(i);
			Thread.sleep(400);
		}
	}
	

}
