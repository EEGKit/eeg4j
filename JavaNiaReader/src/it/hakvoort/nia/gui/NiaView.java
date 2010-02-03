package it.hakvoort.nia.gui;

import it.hakvoort.nia.NiaListener;
import it.hakvoort.nia.NiaSample;

import javax.swing.JFrame;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class NiaView extends JFrame implements NiaListener {
	
	private NiaSignalPanel signalPanel;
	
	private NiaSample lastSample = null;
	
	private boolean draw = true;
	
	private DrawThread drawThread;

	public NiaView() {
		super("NiaView");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 400);
		
		signalPanel = new NiaSignalPanel(0x7A1200, 0.000025);
		add(signalPanel);
		
		drawThread = new DrawThread(100);
		drawThread.start();
	}
	
	@Override
	public void receivedSample(NiaSample sample) {
		lastSample = sample;
	}
	
	public class DrawThread extends Thread {
		
		// the sleep interval in ms
		private int interval = 1000;
		
		public DrawThread(int frequency) {
			this.interval = 1000/frequency;
		}
		
		@Override
		public void run() {
			while(draw) {
				try {
					sleep(interval);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				
				if(lastSample != null) {
					signalPanel.setCurrentValue(lastSample.value);
				}
			}
		}
	}
	
}