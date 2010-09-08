package it.hakvoort.eeg.util;

import it.hakvoort.eeg.util.WindowedDataBuffer.Window;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class FFTDataBuffer {
	
	// use this window
	private Window window = Window.HANN;
		
	// the size of the buffer and fft
	private int size;
	
	// the sample rate of the incomming samples
	private int rate;
	
	// the resolution of the incomming samples
	private double resolution;
	
	// the highpass and lowpass
	protected int highpass;
	protected int lowpass;
	
	// WindowedDataBuffer contains incomming samples and handles windowing
	private WindowedDataBuffer.Float buffer;

	// the FFT
	private FloatFFT_1D fft;
	
	// target array for performing fft
	private float[] target;
	
	// bin array
	private double[] bins;
	
	// average magnitude of frequencies in bins
	private double averageMagnitude = 0d;
	
	public FFTDataBuffer(int size, int rate) {
		this.size = size;
		this.rate = rate;
		
		this.resolution = ((double) rate) / ((double) size);
		
		this.highpass 	= 0;
		this.lowpass 	= rate/2;
		
		buffer = new WindowedDataBuffer.Float(size);
		fft = new FloatFFT_1D(size);

		target = new float[size];
		bins = new double[getBinCount()];
	}
	
	public void setGain(int gain) {
		buffer.setGain(gain);
	}
	
	public int getGain() {
		return buffer.getGain();
	}
	
	public void setWindow(Window window) {
		this.window = window;
	}
	
	public Window getWindow() {
		return this.window;
	}
	
	public void setFrequencyRange(int min, int max) {
		this.setHighpass(min);
		this.setLowpass(max);
	}
	
	public void setHighpass(int highpass) {
		this.highpass = Math.max(highpass, 0);
	}
	
	public void setLowpass(int lowpass) {
		this.lowpass = Math.min(lowpass, this.rate / 2);
	}
	
	public int getHighpass() {
		return this.highpass;
	}
	
	public int getLowpass() {
		return this.lowpass;
	}
	
	public double getFrequencyResolution() {
		return this.resolution;
	}
	
	public int getBinCount() {
		return (int) ((lowpass - highpass) / resolution + 1);
	}
	
	public double[] getBins() {
		return this.bins;
	}
	
	public double getAverageMagnitude() {
		return this.averageMagnitude;
	}
	
	public double getMagnitude(double frequency) {
		if (frequency < highpass || frequency > lowpass) {
			return 0d;
		}

		if(frequency % resolution != 0) {
			double offset = frequency % resolution;

			double value1 = bins[(int) (((frequency - offset) - highpass) / resolution)];
			double value2 = bins[(int) (((frequency - offset + resolution) - highpass) / resolution)];

			double scale1 = 1 - offset / resolution;
			double scale2 = offset / resolution;

			return (scale1 * value1) + (scale2 * value2);
		}

		return bins[(int) ((frequency - highpass) / resolution)];
	}
	
	public void add(float value) {
		buffer.add(value);
	}
	
	public void applyFFT() {
		int binCount = getBinCount();
		double totalMagnitude = 0;
		
		bins = new double[binCount];
		
		// get data from buffer
		buffer.getData(target, window);
		
		// perform fft
		fft.realForward(target);
		
		// get the values between the highpass and lowpass frequencies
		for(double f = highpass, i = 0; f < lowpass; f += resolution, i++) {
			int index = (int) ((f / resolution)) * 2;
			
			// magnitude of frequency
			bins[(int) i] = Math.sqrt(target[index]*target[index] + target[index+1]*target[index+1]) / (size / 2);

			totalMagnitude += bins[(int) i];
		}
		
		// update average magnitude
		averageMagnitude = totalMagnitude / ((double) binCount);
	}
}
