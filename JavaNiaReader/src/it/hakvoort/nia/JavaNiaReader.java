package it.hakvoort.nia;

import it.hakvoort.nia.gui.NiaView;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class JavaNiaReader {

	private NiaDevice nia;
	private NiaView view;
	
	public JavaNiaReader() {
		nia = new NiaDevice();
		view = new NiaView();
		
		nia.setSigned(false);
		nia.start();
		
		view.setVisible(true);
		nia.addListener(view);
	}
	
	public static void main(String[] args) {		
		JavaNiaReader reader = new JavaNiaReader();
	}
}