package it.hakvoort.eeg.gui;

import it.hakvoort.eeg.util.WindowedDataBuffer;
import it.hakvoort.eeg.util.WindowedDataBuffer.Float;
import it.hakvoort.eeg.util.WindowedDataBuffer.Window;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

import JSci.awt.DefaultGraph2DModel;
import JSci.swing.JHistogram;

public class FFTPlot extends JFrame {
	
	private DefaultGraph2DModel model = new DefaultGraph2DModel();
	private FFTDataSeries fftDataSeries;
	
	private int intervalCounter = 0;
	
	// use this window
	private Window window = Window.HANN;
	
	// perform fft after number of added samples
	private int interval = 0;
	
	// the size of the buffer and fft
	private int size;
	
	// the sample rate of the incomming samples
	private int rate;
	
	// the resolution of the incomming samples
	private double resolution;
	
	// min and max frequency
	protected int min;
	protected int max;
	
	// WindowedDataBuffer contains incomming samples and handles windowing
	private WindowedDataBuffer.Float buffer;

	// the FFT
	private FloatFFT_1D fft;
	
	// target array for performing fft
	private float[] target;
	
	// bin array
	private double[] bins;
	
	// average magnitude of frequencies in bin
	private double magnitude = 0d;
	
	// the histogram
	private JHistogram hist;
	
	public FFTPlot(int size, int rate) {
		super("FFT spectrum");

		this.size = size;
		this.rate = rate;
		
		this.resolution = ((double) rate) / ((double) size);
		
		this.min = 0;
		this.max = size/2;
		
		buffer = new WindowedDataBuffer.Float(size);
		fft = new FloatFFT_1D(size);

		target = new float[size];
		bins = new double[(int) ((max - min) / resolution + 1)];
		
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
		this.window = window;
	}
	
	public Window getWindow() {
		return this.window;
	}
	
	public void setFrequencyRange(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	public void setMinFrequency(int min) {
		this.min = min;
	}
	
	public void setMaxFrequency(int max) {
		this.max = max;
	}
	
	public int getMinFrequency() {
		return this.min;
	}
	
	public int getMaxFrequency() {
		return this.max;
	}
	
	public double[] getBins() {
		return this.bins;
	}
	
	public double getAverageMagnitude() {
		return this.magnitude;
	}
	
	public double getMagnitude(double frequency) {
		if (frequency < min || frequency > max) {
			return 0d;
		}

		if(frequency % resolution != 0) {
			double offset = frequency % resolution;

			double value1 = bins[(int) (((frequency - offset) - min) / resolution)];
			double value2 = bins[(int) (((frequency - offset + resolution) - min) / resolution)];

			double scale1 = 1 - offset / resolution;
			double scale2 = offset / resolution;

			return (scale1 * value1) + (scale2 * value2);
		}

		return bins[(int) ((frequency - min) / resolution)];
	}
	
	public void add(float value) {
		buffer.add(value);
		
		intervalCounter++;
		
		if(intervalCounter >= interval) {
			applyFFT();
			updateFFTDataSeries();
			intervalCounter = 0;
		}
	}
	
	private void applyFFT() {
		int binCount = (int) ((max - min) / resolution + 1);
		double averageMagnitude = 0;
		
		bins = new double[binCount];
		
		// get data from buffer
		buffer.getData(target, window);
		
		// perform fft
		fft.realForward(target);
		
		// get the values between the min and max frequencies
		for(double f = min, i = 0; f < max; f += resolution, i++) {
			int index = (int) ((f / resolution)) * 2;
			
			// magnitude of frequency
			bins[(int) i] = Math.sqrt(target[index]*target[index] + target[index+1]*target[index+1]) / (size / 2);

			averageMagnitude += bins[(int) i];
		}
		
		// update magnitude
		magnitude = averageMagnitude / ((double) binCount);
	}
	
	private void updateFFTDataSeries() {
		int binCount = (int) ((max - min) / resolution + 1);
		
		// shift the array by 1
		double[] y2 = new double[binCount + 1];
		System.arraycopy(bins, 0, y2, 1, binCount);
		
		// update the FFTDataSeries
		fftDataSeries.setValues(y2);
	}
	
	public class FFTDataSeries extends DefaultGraph2DModel.DataSeries {
		
		public FFTDataSeries() {
			setValues(new float[length()]);
		}
		
		@Override
		public float getXCoord(int x) {
			return (float) (min + ((x) * resolution));
		}
		
		@Override
		public int length() {
			return (int) ((max - min) / resolution + 1);
		}
	}
}
