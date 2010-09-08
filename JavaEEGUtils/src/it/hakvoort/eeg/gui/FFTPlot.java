package it.hakvoort.eeg.gui;

import it.hakvoort.eeg.util.FFTDataBuffer;
import it.hakvoort.eeg.util.WindowedDataBuffer.Window;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import JSci.awt.DefaultGraph2DModel;
import JSci.swing.JHistogram;

public class FFTPlot extends JFrame {
	
	private int intervalCounter = 0;
	
	private DefaultGraph2DModel model = new DefaultGraph2DModel();
	private FFTDataSeries fftDataSeries;
	
	// update fft after number of added samples
	private int interval = 0;
	
	/// the FFT
	private FFTDataBuffer buffer;
	
	// the histogram
	private JHistogram hist;
	
	public FFTPlot(int size, int rate) {
		super("FFT spectrum");
		
		buffer = new FFTDataBuffer(size, rate);
		
		fftDataSeries = new FFTDataSeries();
		
		model.addSeries(fftDataSeries);
		
		init();
	}
	
	public void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,1));
        
        hist = new JHistogram(this.model);
        
        panel.add(hist);
                
        add(panel,"Center");
        
        setSize(600, 400);
        setVisible(true);	
	}
	
	public void setGain(int gain) {
		buffer.setGain(gain);
	}
	
	public float getGain() {
		return buffer.getGain();
	}
	
	public void setYExtrema(int min, int max) {
		hist.setYExtrema(min, max);
	}
	
	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	public int getInterval() {
		return this.interval;
	}
	
	public void setWindow(Window window) {
		buffer.setWindow(window);
	}
	
	public Window getWindow() {
		return buffer.getWindow();
	}
	
	public void setFrequencyRange(int min, int max) {
		buffer.setFrequencyRange(min, max);
	}
	
	public void setMinFrequency(int min) {
		buffer.setHighpass(min);
	}
	
	public void setMaxFrequency(int max) {
		buffer.setLowpass(max);
	}
	
	public int getMinFrequency() {
		return buffer.getHighpass();
	}
	
	public int getMaxFrequency() {
		return buffer.getLowpass();
	}
	
	public double getFrequencyResolution() {
		return buffer.getFrequencyResolution();
	}
	
	public int getBinCount() {
		return buffer.getBinCount();
	}
	
	public double[] getBins() {
		return buffer.getBins();
	}
	
	public double getAverageMagnitude() {
		return buffer.getAverageMagnitude();
	}
	
	public double getMagnitude(double frequency) {
		return buffer.getMagnitude(frequency);
	}
	
	public void add(float value) {
		buffer.add(value);
		
		intervalCounter++;
		
		if(intervalCounter >= interval) {
			buffer.applyFFT();
			updateFFTDataSeries();
			
			intervalCounter = 0;
		}
	}
		
	private void updateFFTDataSeries() {
		int binCount = buffer.getBinCount();
		
		// shift the array by 1
		double[] y2 = new double[binCount + 1];
		System.arraycopy(buffer.getBins(), 0, y2, 1, binCount);
		
		// update the FFTDataSeries
		fftDataSeries.setValues(y2);
	}
	
	public class FFTDataSeries extends DefaultGraph2DModel.DataSeries {
		
		public FFTDataSeries() {
			setValues(new float[length()]);
		}
		
		@Override
		public float getXCoord(int x) {
			return (float) (buffer.getHighpass() + ((x) * buffer.getFrequencyResolution()));
		}
		
		@Override
		public int length() {
			return (int) ((buffer.getLowpass() - buffer.getHighpass()) / buffer.getFrequencyResolution() + 1);
		}
	}
}
