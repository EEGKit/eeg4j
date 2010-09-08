package it.hakvoort.eeg.util;

import java.util.Arrays;

public abstract class DataBuffer {
	
	protected int index = 0;
	protected int size = 0;
	
	protected int capacity;
	
	// the gain for incoming data
	protected int gain = 100;
	
	// the minimum value of the incoming data
	protected int minimum = 0;

	// the maximum value of the incoming data
	protected int maximum = 0;
	
	public void setGain(int gain) {
		this.gain = gain;
	}
	
	public int getGain() {
		return this.gain;
	}
	
	public DataBuffer(int capacity) {
		this.capacity = capacity;
	}
	
	public static class Float extends DataBuffer {

		public float[] data;
		
		public Float(int capacity) {
			super(capacity);
			
			data = new float[capacity];
			Arrays.fill(data, 0);
		}
				
		public synchronized void add(float value) {
//			data[index] = gain > 0 ? value * ((maximum - minimum) / gain) : 0;
			data[index] = value;

			index = (index+1) % capacity;
			size = Math.min(size+1, capacity);
		}
		
		public void getData(float[] target) {
			for(int i=0; i<capacity; i++) {
				target[i] = data[((index%size) + i) % capacity];
			}
		}		
	}
	
	public static class Double extends DataBuffer {
		
		public double[] data;
		
		public Double(int capacity) {
			super(capacity);
			
			data = new double[capacity];
			Arrays.fill(data, 0);
		}
		
		public void add(double value) {
//			data[index] = gain > 0 ? value * ((maximum - minimum) / gain) : 0;
			data[index] = value;
			
			index = (index+1) % capacity;
			size = Math.min(size+1, capacity);
		}
		
		public void getData(double[] target) {
			if(size <= 0) {
				return;
			}
			
			for(int i=0; i<capacity; i++) {
				target[i] = data[((index%size) + i) % capacity];
			}
		}
	}
}
