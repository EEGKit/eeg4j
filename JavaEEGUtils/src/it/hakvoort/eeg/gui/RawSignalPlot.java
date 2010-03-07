package it.hakvoort.eeg.gui;

import it.hakvoort.eeg.util.DataBuffer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JPanel;

import JSci.awt.DefaultGraph2DModel;
import JSci.awt.Graph2DModel;
import JSci.swing.JLineGraph;

public class RawSignalPlot extends JFrame {
	
	private DefaultGraph2DModel model = new DefaultGraph2DModel();
	private RawSignalDataSeries signalDataSeries;

	// DataBuffer contains incomming samples	
	private DataBuffer.Float buffer;
	
	// the size of the buffer
	private int size;
	
	// target array for plotting data
	private float[] target;
	
	// the graph
	private JTailedLineGraph graph;
	
	// draw new incomming samples at the end of the graph
	private boolean tailing = false;
	
	// total number of processed samples
	private long samples = 0;
	
	public RawSignalPlot(int size) {
		super("Raw Signal");
		
		this.size = size;
		
		buffer = new DataBuffer.Float(size);
		target = new float[size];

		signalDataSeries = new RawSignalDataSeries();
		
		model.addSeries(signalDataSeries);
		
		init();
	}

	private void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,1));
        
        graph = new JTailedLineGraph(model);
        graph.setColor(0, Color.red);
        
        panel.add(graph);
        
        add(panel,"Center");
        
        setSize(600, 400);
        setVisible(true);
	}
	
	public void setYExtrema(int min, int max) {
		graph.setYExtrema(min, max);
	}
	
	public void setGridLines(boolean value) {
		graph.setGridLines(value);
	}
	
	public void setTailing(boolean tailing) {
		this.tailing = tailing;
	}
	
	public boolean isTailing() {
		return this.tailing;
	}
	
	public void add(float value) {
		buffer.add(value);
		
		samples++;
		
		if(tailing) {
			buffer.getData(target);
			signalDataSeries.setValues(target);
		} else {
			signalDataSeries.setValues(buffer.data);
		}
	}
	
	public class JTailedLineGraph extends JLineGraph {

		public JTailedLineGraph(Graph2DModel graph2dModel) {
			super(graph2dModel);
		}
		
		@Override
		protected void offscreenPaint(Graphics graphics) {
			super.offscreenPaint(graphics);
			
			if(!tailing) {
				Point top = dataToScreen(samples, getYMaximum());
				Point bottom = dataToScreen(samples, getYMinimum());
			
				graphics.setColor(Color.BLUE);
				graphics.drawLine(top.x, top.y, bottom.x, bottom.y);			
			}
		}
	}
	
	public class RawSignalDataSeries extends DefaultGraph2DModel.DataSeries {
		
		public RawSignalDataSeries() {
			setValues(new float[length()]);
		}
		
		@Override
		public float getXCoord(int x) {
			return (float) ((samples / size) * size) + x;
		}
		
		@Override
		public int length() {
			return size;
		}
	}
}
