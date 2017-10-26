package org.corebounce.io;

import javax.sound.midi.ShortMessage;

import org.corebounce.soundium.Soundium;
import org.corebounce.soundium.Subsystem;

import ch.fhnw.ether.midi.AbletonPush;
import ch.fhnw.ether.midi.AbletonPush.TouchStrip;
import ch.fhnw.ether.midi.MidiIO;
import ch.fhnw.util.IProgressListener;
import ch.fhnw.util.Log;
import ch.fhnw.util.TextUtilities;
import ch.fhnw.util.net.osc.IOSCHandler;

public class MIDI extends Subsystem implements IProgressListener, IOSCHandler {
	private static final Log log = Log.create();

	private AbletonPush push;
	private float       progress;
	private int[]       ccvalues = new int[128];

	@SuppressWarnings("unused")
	public MIDI(String[] args) {
		super(CFG_PREFIX, args);

		try {
			push = new AbletonPush(0);

			push.setTouchStrip(TouchStrip.Host_Bar_Bottom);
			setProgress(0);

			if(true) {
				push.setLine(0, Soundium.VERSION);
				push.setLine(1, "(c) 2000-2016    corebounce.org   scheinwerfer.li");
				push.clearLine(2);
				push.setLine(3, "Pascal Mueller   Stefan Arisona   Simon Schubiger  Matthias Specht");
			} else {
				StringBuilder b = new StringBuilder(TextUtilities.repeat(' ', 64));
				for(int i = 0; i < 64; i++) b.setCharAt(i, (char)i);
				push.setLine(0, b.toString());
				for(int i = 0; i < 64; i++) b.setCharAt(i, (char)(i+64));
				push.setLine(1, b.toString());
			}

		} catch(Throwable t) {
			log.warning(t.getMessage());
		}
	}

	public static String   CFG_PREFIX  = "midi";
	public static String[] CFG_OPTIONS = {};

	public AbletonPush getPush() {
		return push;
	}

	@Override
	public void setProgress(float progress) {
		this.progress = progress;
		try {
			push.setTouchStrip(progress);
		} catch (Throwable t) {
			log.warning(t.getMessage());
		}
	}

	@Override
	public void done() {}

	@Override
	public float getProgress() {
		return progress;
	}
	
	@Override
	public Object[] handle(String[] address, int addrIdx, StringBuilder typeString, long timestamp, Object... args) {
		try {
			if("pwc".equals(address[2])) {
				int val = (int)(((Float)args[0]).floatValue() * MidiIO.MAX_14BIT);
				push.handle(new ShortMessage(ShortMessage.PITCH_BEND, val & 0x7F, (val >> 7) & 0x7F));
			} else if("cc".equals(address[2])) {
				int val = (int)(((Float)args[0]).floatValue() * 127);
				int cc = Integer.parseInt(address[3], 16);
				int delta = val - ccvalues[cc];
				ccvalues[cc] = val;
				push.handle(new ShortMessage(ShortMessage.CONTROL_CHANGE, cc, delta < 0 ? delta+128 : delta));
			}  else if("noteOn".equals(address[2])) {
				push.handle(new ShortMessage(ShortMessage.NOTE_ON, Integer.parseInt(address[3], 16), (int)((Float)args[0]).floatValue() * 127));
			}  else if("noteOff".equals(address[2])) {
				push.handle(new ShortMessage(ShortMessage.NOTE_OFF, Integer.parseInt(address[3], 16), (int)((Float)args[0]).floatValue() * 127));
			}
		} catch(Throwable t) {
			log.warning(t);
		}
		return null;
	}
}
