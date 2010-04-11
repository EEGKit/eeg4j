package it.hakvoort.usb;

import java.util.ArrayList;
import java.util.List;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;

public class UsbDeviceManager {
	
	private static UsbDeviceManager instance = null;

	private long time = 0;
	
	private List<UsbDevice> devices = new ArrayList<UsbDevice>();
	
	public UsbDeviceManager() throws SecurityException, UsbException {
		loadDevices();
	}
	
	public static UsbDeviceManager getInstance() throws SecurityException, UsbException {
		if(instance == null) {
			instance = new UsbDeviceManager();
		}
		
		return instance;
	}
	
	public UsbDevice getDevice(short idVendor, short idProduct, boolean reload) throws SecurityException, UsbException {
		if(reload) {
			loadDevices();
		}
		
		return getDevice(idVendor, idProduct);
	}

	public UsbDevice getDevice(short idVendor, short idProduct) throws SecurityException, UsbException {
		// if the requested device is available, return it
		for(UsbDevice device : devices) {
			if(device.getUsbDeviceDescriptor().idVendor() == idVendor && device.getUsbDeviceDescriptor().idProduct() == idProduct) {
				return device;
			}
		}
		
		// return null when the requested device was not found
		return null;
	}
	
	public long getTime() {
		return this.time;
	}

	private void loadDevices() throws SecurityException, UsbException {
		// get services from the UsbHostManager
		UsbServices services = UsbHostManager.getUsbServices();
		
		// get the root UsbHub
		UsbHub root = services.getRootUsbHub();
		
		// load UsbDevices
		devices = getDevices(root);
		
		// set the time
		time = System.currentTimeMillis();
	}
	
	private List<UsbDevice> getDevices(UsbDevice device) {
		List<UsbDevice> devices = new ArrayList<UsbDevice>();
		
		// if the current device is an UsbHub, add all underlying devices to the list
		// else add the device to the list
		if(device.isUsbHub()) {
			for(UsbDevice d : (List<UsbDevice>) ((UsbHub) device).getAttachedUsbDevices()) {
				devices.addAll(getDevices(d));
			}
		} else {
			devices.add(device);
		}

		return devices;
	}
}

