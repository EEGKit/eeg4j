package it.hakvoort.eeg.util;

import java.util.Arrays;

public abstract class WindowedBuffer {
	
	public enum Window {RECTANGLE, HANN, HAMMING, BLACKMAN, BLACKMAN_HARRIS};
	
	public static float BLACKMAN_ALPHA = 0.16f;

	public int index = 0;
	public int size = 0;
	
	public int capacity;
	
	public WindowedBuffer(int capacity) {
		this.capacity = capacity;
	}
	
	protected float applyWindow(float value, int n, Window window) {
		return (float) applyWindow((double) value, n, window);
	}
	
	protected double applyWindow(double value, int n, Window window) {
		switch(window) {
			case HANN:
				return value * (0.5 * (1 - Math.cos((2 * Math.PI * n) / (capacity - 1))));
			case HAMMING:
				return value * (0.54 - 0.46 * Math.cos((2 * Math.PI * n) / (capacity - 1)));
			case BLACKMAN:
				return value * ((1 - BLACKMAN_ALPHA) / 2 - 0.5 * Math.cos((2 * Math.PI * n) / (capacity - 1)) + (BLACKMAN_ALPHA / 2) * Math.cos((4 * Math.PI * n) / (capacity - 1)));
			case BLACKMAN_HARRIS:
				return value * (0.35875f - 0.48829f * Math.cos((2 * Math.PI * n) / (capacity - 1)) + 0.14128f * Math.cos((4 * Math.PI * n) / (capacity - 1)) - 0.01168f * Math.cos((6 * Math.PI * n) / (capacity - 1)));
			default:
				return value;
		}
	}
	
	public static class Float extends WindowedBuffer {

		public float[] data;
		
		public Float(int capacity) {
			super(capacity);
			
			data = new float[capacity];
			Arrays.fill(data, 0);
		}
				
		public synchronized void add(float value) {
			data[index] = value;
			index = (index+1) % capacity;
			size = Math.min(size+1, capacity);
		}
		
		public void getData(float[] target) {
			getData(target, Window.RECTANGLE);
		}
		
		public void getData(float[] target, Window window) {
			if(size <= 0) {
				return;
			}
			
			for(int i=0; i<capacity; i++) {
				target[i] = applyWindow(data[((index%size) + i) % capacity], i, window);
			}
		}
	}
	
	public static class Double extends WindowedBuffer {
		
		public double[] data;

		public Double(int capacity) {
			super(capacity);
			
			data = new double[capacity];
			Arrays.fill(data, 0);
		}
		
		public void add(double value) {
			data[index] = value;
			index = (index+1) % capacity;
			size = Math.min(size+1, capacity);
		}
		
		public void getData(double[] target) {
			getData(target, Window.RECTANGLE);
		}
		
		public void getData(double[] target, Window window) {
			for(int i=0; i<capacity; i++) {
				target[i] = applyWindow(data[((index%size) + i) % capacity], i, window);
			}
		}
	}
}
