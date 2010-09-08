package it.hakvoort.eeg.gui;

import it.hakvoort.eeg.util.DataBuffer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	
	// the min value of the incoming data
	private int min;
	
	// the max value of the incoming data
	private int max;
	
	// target array for plotting data
	private float[] target;
	
	// the graph
	private JTailedLineGraph graph;
	
	// draw new incomming samples at the end of the graph
	private boolean tailing = false;
	
	// show samples per second
	private boolean showSps = false;
	
	// total number of processed samples
	private long samples = 0;
	
	// startTime for calculating samples per second
	private long startTime = 0;
		
	public RawSignalPlot(int size) {
		this(size, -1, 1);
	}

	public RawSignalPlot(int size, int min, int max) {
		super("Raw Signal");
		
		this.size = size;
		this.min = min;
		this.max = max;
		
		startTime = System.currentTimeMillis();
		
		buffer = new DataBuffer.Float(size);
		target = new float[size];

		signalDataSeries = new RawSignalDataSeries();
		
		model.addSeries(signalDataSeries);
		
		init();
	}	
	
	private void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenu file = new JMenu("File");
		
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		file.add(exit);
		
		JMenu view = new JMenu("View");
		
		JMenuItem sampleRate = new JCheckBoxMenuItem("Sample rate");
		sampleRate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				showSps(((JCheckBoxMenuItem) event.getSource()).getState());
			}
		});
		
		JMenuItem tailing = new JCheckBoxMenuItem("Tailing");
		tailing.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				setTailing(((JCheckBoxMenuItem) event.getSource()).getState());
			}
		});
		
		JMenuItem grid = new JCheckBoxMenuItem("Grid");
		grid.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				setGridLines(((JCheckBoxMenuItem) event.getSource()).getState());
			}
		});
		
		final GainDialog scaleDialog = new GainDialog("Gain", this);
		
		JMenuItem scale = new JMenuItem("Scale");
		scale.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				scaleDialog.setVisible(true);
			}
		});
		
		final AxisDialog yAxisDialog = new AxisDialog("Y-Axis", min, max, this);
		
		JMenuItem yAxis = new JMenuItem("Y-Axis");
		yAxis.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				yAxisDialog.setVisible(true);
			}
		});
				
		view.add(scale);
		view.add(yAxis);
		view.addSeparator();
		view.add(sampleRate);
		view.add(tailing);
		view.add(grid);
		
		menuBar.add(file);
		menuBar.add(view);
		
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,1));
        
        graph = new JTailedLineGraph(model);
        graph.setColor(0, Color.red);
        graph.setYExtrema(min, max);
        
        panel.add(graph);
        
        add(panel,"Center");
        
        setJMenuBar(menuBar);
        
        setSize(600, 400);
        setVisible(true);
	}
	
	private void setGain(int gain) {
		buffer.setGain(gain);
	}
	
	private int getGain() {
		return buffer.getGain();
	}
	
	private void setYExtrema(int min, int max) {
		graph.setYExtrema(min, max);
	}
	
	private void setGridLines(boolean value) {
		graph.setGridLines(value);
	}
	
	private void showSps(boolean showSps) {
		this.showSps = showSps;
	}
	
	private void setTailing(boolean tailing) {
		this.tailing = tailing;
		repaint();
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
		
	private class JTailedLineGraph extends JLineGraph {

		public JTailedLineGraph(Graph2DModel graph2dModel) {
			super(graph2dModel);
		}
		
		@Override
		protected void offscreenPaint(Graphics graphics) {
			super.offscreenPaint(graphics);
			
			if(!tailing) {
				Point p1 = dataToScreen(samples, getYMaximum());
				Point p2 = dataToScreen(samples, getYMinimum());
			
				graphics.setColor(Color.BLUE);
				graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
			}
			
			if(showSps) {
				int sps = (int) (samples / (double) ((System.currentTimeMillis()-startTime) / 1000));

				int x = getWidth() - 150;
				int y = 20;
				
				int width = 120;
				int height = 20;
				
				// draw info rect
				graphics.setColor(Color.LIGHT_GRAY);
				graphics.fillRect(x, y, width, height);
				
				// draw info boundary
				graphics.setColor(Color.DARK_GRAY);
				graphics.drawRect(x, y, width, height);
				
				FontMetrics metrics = graphics.getFontMetrics();
				
				if(System.currentTimeMillis()-startTime > 1000) {
					graphics.drawString(String.format("%s samples/sec", sps), x+5, y + metrics.getHeight());	
				} else {
					graphics.drawString(String.format("calculating.."), x+5, y + metrics.getHeight());
				}
			}
		}
	}
	
	private class RawSignalDataSeries extends DefaultGraph2DModel.DataSeries {
		
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
	
	private class AxisDialog extends JFrame {
		
		private final int minimum;
		private final int maximum; 
		
		private JFrame parent;
		
		public AxisDialog(String title, int min, int max, JFrame parent) {
			super(title);
			
			this.minimum = min;
			this.maximum = max;
			
			this.parent = parent;
			
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setContentPane(createGUI());
	        setResizable(false);
	        pack();
		}
		
		private JPanel createGUI() {
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(2, 1));
						
			JPanel minPanel = new JPanel();
			minPanel.setBorder(BorderFactory.createTitledBorder("Minimum"));
			
			JPanel maxPanel = new JPanel();
			maxPanel.setBorder(BorderFactory.createTitledBorder("Maximum"));
			
			final JLabel minLabel = new JLabel(Integer.toString(min));
			final JLabel maxLabel = new JLabel(Integer.toString(max));
			
			int minWidth = getFontMetrics(minLabel.getFont()).stringWidth(Integer.toString(min));
			int maxWidth = getFontMetrics(minLabel.getFont()).stringWidth(Integer.toString(max));
			int labelWidth = Math.max(minWidth, maxWidth);
			
			minLabel.setPreferredSize(new Dimension(labelWidth, 10));
			maxLabel.setPreferredSize(new Dimension(labelWidth, 10));
			
			final JSlider minSlider = new JSlider();
			minSlider.setMinimum(minimum);
			minSlider.setMaximum(maximum);
			minSlider.setMajorTickSpacing(maximum);
			minSlider.setMinorTickSpacing(maximum);
			minSlider.setPaintTicks(true);
			minSlider.setPaintLabels(true);
			minSlider.setPreferredSize(new Dimension(400, 50));
			minSlider.setValue(min);
						
			minPanel.add(minLabel, BorderLayout.WEST);
			minPanel.add(minSlider, BorderLayout.CENTER);
						
			final JSlider maxSlider = new JSlider();
			maxSlider.setMinimum(minimum);
			maxSlider.setMaximum(maximum);
			maxSlider.setMajorTickSpacing(maximum);
			maxSlider.setMinorTickSpacing(maximum);
			maxSlider.setPaintTicks(true);
			maxSlider.setPaintLabels(true);
			maxSlider.setPreferredSize(new Dimension(400, 50));
			maxSlider.setValue(max);
						
			maxPanel.add(maxLabel, BorderLayout.WEST);
			maxPanel.add(maxSlider, BorderLayout.CENTER);
			
			minSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent event) {
					int newMin = ((JSlider)event.getSource()).getValue();
					
					if(newMin == min) {
						return;
					}
					
					min = newMin;
					minLabel.setText(Integer.toString(min));
					
					if(min >= max) {
						max++;
						
						maxSlider.setValue(max);
						maxLabel.setText(Integer.toString(max));
					}

					setYExtrema(min, max);
				}
			});
			
			maxSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent event) {
					int newMax = ((JSlider)event.getSource()).getValue();
					
					if(newMax == max) {
						return;
					}
					
					max = newMax;
					maxLabel.setText(Integer.toString(max));
					
					if(max <= min) {
						min--;
						
						minSlider.setValue(min);
						minLabel.setText(Integer.toString(min));
					}
					
					setYExtrema(min, max);
				}
			});
			
			panel.add(minPanel);
			panel.add(maxPanel);
			
			return panel;
		}
		
		@Override
		public void setVisible(boolean b) {
			Point location = parent.getLocationOnScreen();
			
			int x = (location.x + parent.getSize().width / 2) - getWidth() / 2;
			int y = (location.y + parent.getSize().height / 2) - getHeight() / 2;
			
			setLocation(x, y);
			
			super.setVisible(b);
		}

	}
	
	private class GainDialog extends JFrame {
		
		private int min = 0;
		private int max = 200;
		
		private JFrame parent;
		
		public GainDialog(String title, JFrame parent) {
			super(title);
			
			this.parent = parent;
			
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setContentPane(createGUI());
	        setResizable(false);
	        pack();
		}
		
		private JPanel createGUI() {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Gain"));
			
			final JLabel label = new JLabel(Integer.toString(getGain()));
			label.setPreferredSize(new Dimension(30, 10));
			
			JSlider slider = new JSlider();
			slider.setMinimum(min);
			slider.setMaximum(max);
			slider.setMajorTickSpacing(max / 2);
			slider.setMinorTickSpacing(max);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			slider.setValue(getGain());
			
			slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent event) {
					int gain = ((JSlider)event.getSource()).getValue();
					label.setText(Integer.toString(gain));
					setGain(gain);
				}
			});
			
			panel.add(label, BorderLayout.WEST);
			panel.add(slider, BorderLayout.CENTER);
			
			return panel;
		}
		
		@Override
		public void setVisible(boolean b) {
			Point location = parent.getLocationOnScreen();
			
			int x = (location.x + parent.getSize().width / 2) - getWidth() / 2;
			int y = (location.y + parent.getSize().height / 2) - getHeight() / 2;
			
			setLocation(x, y);
			
			super.setVisible(b);
		}
	}
}
