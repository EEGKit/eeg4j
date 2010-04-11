package it.hakvoort.nia.example;

import it.hakvoort.nia.NiaDevice;
import it.hakvoort.nia.NiaListener;
import it.hakvoort.nia.NiaSample;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class ExampleNiaReader implements NiaListener {
	
	static int counter = 0;
	
	public static void main(String[] args) {
		// create a NiaDevice
		NiaDevice nia = new NiaDevice();
		
		// register this class as listener
		nia.addListener(new ExampleNiaReader());
		
		// set signed to false
		nia.setSigned(false);

		// start the NiaDevice 
		nia.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// stop the NiaDevice
		nia.stop();
	}

	@Override
	public void receivedSample(NiaSample sample) {
		System.out.println(sample.toString());
	}
}