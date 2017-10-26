package ch.fhnw.ether.midi;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Transmitter;

import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.platform.Platform.OS;
import ch.fhnw.util.IOUtilities;
import ch.fhnw.util.Log;
import de.humatic.mmj.CoreMidiDevice;
import de.humatic.mmj.MidiInput;
import de.humatic.mmj.MidiOutput;
import de.humatic.mmj.MidiSystem;

public class MidiIO {
	private static Log log = Log.create();

	static class MidiPort {
		final MidiDevice  dev;
		final Receiver    rx;
		final Transmitter tx;
		final MidiOutput  out;
		final MidiInput   in;
		IMidiHandler      handler;

		public MidiPort(MidiDevice dev) throws MidiUnavailableException {
			this.dev = dev;

			if(Platform.getOS() == OS.MACOSX) {
				in  = getMMJIn();
				out = getMMJOut();
				rx  = null;
				tx  = null;
				Platform.get().addShutdownTask(new Runnable() {
					@Override
					public void run() {
						/*
						if(out != null) out.close();
						if(in  != null) in.close();
						 */
					}
				});
				if(in != null)
					in.addMidiListener(msg->{
						if(handler != null) {
							try {
								MidiMessage midiMsg;
								if(msg.length == 3)
									midiMsg = new ShortMessage(msg[0] & 0xFF, msg[1] & 0xFF, msg[2] & 0xFF);
								else
									midiMsg = new SysexMessage(msg, msg.length);
								handler.handle(midiMsg);
							} catch(Throwable t) {
								log.warning(t);
							}
						}
					});
			} else {
				if(!(dev.isOpen())) {
					dev.open();
					Platform.get().addShutdownTask(new Runnable() {
						@Override public void run() {dev.close();}
					});
				}
				in  = null;
				out = null;
				rx  = dev.getMaxReceivers()    == 0 ? null : dev.getReceiver();
				tx  = dev.getMaxTransmitters() == 0 ? null : dev.getTransmitter();
				Platform.get().addShutdownTask(new Runnable() {
					@Override
					public void run() {
						if(rx != null) rx.close();
						if(tx != null) tx.close();
					}
				});
				if(tx != null) {
					tx.setReceiver(new Receiver() {
						@Override
						public void send(MidiMessage midiMsg, long timeStamp) {
							if(handler != null) handler.handle(midiMsg);
						}

						@Override
						public void close() {}
					});
				}
			}
		}

		private int getMMJPortIndex(boolean output) throws MidiUnavailableException {
			MidiDevice.Info info = dev.getDeviceInfo();
			for(CoreMidiDevice cdev : MidiSystem.getDevices()) {
				if(info.getVendor().equals(cdev.getManufacturer()) && info.getDescription().startsWith(cdev.getName()))
					for(int en = 0; en < cdev.getNumberOfEntities(); en++)
						if(cdev.getEntityName(en).equals(info.getName()))
							return output ? cdev.getOutputIndex(en) : cdev.getOutputIndex(en);
			}
			return -1;
		}

		private MidiOutput getMMJOut() throws MidiUnavailableException {
			int idx = getMMJPortIndex(true);
			if(idx >= 0) return MidiSystem.openMidiOutput(idx);
			throw new MidiUnavailableException("Unalbe to map " + dev.getDeviceInfo().getDescription());
		}

		private MidiInput getMMJIn() throws MidiUnavailableException {
			int idx = getMMJPortIndex(false);
			if(idx >= 0) return MidiSystem.openMidiInput(idx);
			throw new MidiUnavailableException("Unalbe to map " + dev.getDeviceInfo().getDescription());
		}

		public void send(MidiMessage msg) {
			if(rx != null)
				rx.send(msg, -1);
			else if(out != null) {
				out.sendMidi(msg.getMessage());
				sleep();
			}
		}

		private static void sleep() {
			//try {Thread.sleep(1);} catch (InterruptedException e) {}
		}

		public void setHandler(IMidiHandler handler) {
			this.handler = handler;
		}
	}

	private static final Map<MidiDevice, MidiPort> dev2port = new IdentityHashMap<>();

	public static synchronized void setHandler(MidiDevice dev, IMidiHandler handler) throws MidiUnavailableException {
		getPort(dev).setHandler(handler);
	}

	public static synchronized void send(MidiDevice dev, MidiMessage msg) throws MidiUnavailableException {
		getPort(dev).send(msg);
	}

	private static MidiPort getPort(MidiDevice dev) throws MidiUnavailableException {
		MidiPort port = dev2port.get(dev);
		if(port == null) {
			port = new MidiPort(dev);
			dev2port.put(dev, port);
		}
		return port;
	}

	private static final AtomicBoolean inited = new AtomicBoolean();
	public static void init() {
		if(!inited.getAndSet(true)) {
			try {
				if(Platform.getOS() == OS.MACOSX) {
					File tmp = File.createTempFile(MidiIO.class.getName(), ".lck");
					MidiSystem.setLibraryPath(tmp.getParent());
					File dst = new File(tmp.getParentFile(), "libmmj.jnilib");
					IOUtilities.copy(MidiIO.class.getResourceAsStream("/native.osx.x64/" + dst.getName()), dst);
					dst.setExecutable(true);
					tmp.delete();
					dst.deleteOnExit();
				}
			} catch(Throwable t) {
				log.severe(t);
			}
		}
	}

	public static final float MAX_14BIT = 0x3FFF;
	public static int toInt14(int nLowerPart, int nHigherPart) {
		return (nLowerPart & 0x7F) | ((nHigherPart & 0x7F) << 7);
	}	
}
