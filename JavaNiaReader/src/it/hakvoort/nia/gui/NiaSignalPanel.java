package it.hakvoort.nia.gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class NiaSignalPanel extends JPanel {
	
	private int baseline;

	private int offsetY;
	
	private int currentX;
	private int currentY;
	
	private double scaleY = 1.0;
	
	private boolean reset = true;
	
	private Color lineColor = Color.RED;
	private Color baselineColor = Color.BLACK;
	private Color background = getBackground();
	
	public NiaSignalPanel(int offset, double scale) {
		this.offsetY = offset;
		this.scaleY = scale;
		
		this.currentX = 0;
		this.currentY = getHeight()/2;
	}

	public void setCurrentValue(int value) {
		if((currentX + 1) >= getWidth()) {
			reset = true;
		}
		
		final int oldX = reset ? 0 : currentX;
		final int oldY = currentY;

		final int newY = (int) ((value-offsetY) * scaleY);
		final int newX = reset ? 0 : (currentX + 1);

		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
					drawGraph(oldX, oldY, newX, newY);				
			}
		});
		
		currentX = newX;
		currentY = newY;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		baseline = getHeight()/2;
		currentY = baseline;
		currentX = 0;
	}

	private void drawGraph(int oldX, int oldY, int newX, int newY) {
		Graphics graphics = getGraphics();

		if(reset) {
			graphics.setColor(background);
			graphics.fillRect(0, 0, getWidth(), getHeight());
			reset = false;
		}
		
		drawBaseline(graphics);
		drawLine(graphics, oldX, oldY, newX, newY);
	}

	private void drawBaseline(Graphics g) {
		g.setColor(baselineColor);
		g.drawLine(0, baseline, getWidth(), baseline);
	}

	private void drawLine(Graphics g, int oldX, int oldY, int xNew, int yNew) {		
		g.setColor(lineColor);
		g.drawLine(oldX, baseline-oldY, xNew, baseline-yNew);
	}
}
