package org.corebounce.engine;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.corebounce.audio.Audio;
import org.corebounce.audio.MonitorGain;
import org.corebounce.engine.Bouncelet.PatternStyle;
import org.corebounce.io.MIDI;
import org.corebounce.io.OSC;
import org.corebounce.resman.MetaDB;
import org.corebounce.soundium.TabPanel;
import org.corebounce.ui.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.fhnw.ether.media.Parameter;
import ch.fhnw.ether.media.Parametrizable;
import ch.fhnw.ether.midi.AbletonPush;
import ch.fhnw.ether.midi.AbletonPush.Basic;
import ch.fhnw.ether.midi.AbletonPush.PControl;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.ui.ParameterWindow;
import ch.fhnw.util.CollectionUtilities;
import ch.fhnw.util.IDisposable;
import ch.fhnw.util.Log;
import ch.fhnw.util.TextUtilities;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.net.osc.IOSCHandler;

public class Engine extends TabPanel implements IOSCHandler, IDisposable {
	private static final Log log = Log.create();

	public  static final String BOUNCELET     = "bouncelet";
	public  static final String OSC_ENGINE    = "/engine/";
	private static final long   POLL_INTERVAL = 500;

	private static final Parameter MASTER = new Parameter("master", "Master", 0, 1, 1);
	private static final Parameter PAUSED = new Parameter("pause",  "Paused", false);

	private final List<Bouncelet>       s_bouncelets   = new ArrayList<>();
	private final List<IEngineListener> listeners = new ArrayList<>();
	private final Map<Bouncelet, TableItem> b2i = new IdentityHashMap<>();
	private Table                       table;
	private Inspector                   inspector;
	private Timer                       pollTimer = new Timer();
	private final MetaDB                db;
	private final Audio                 audio;
	private final OSC                   osc;
	private final MIDI                  midi;
	private final Parametrizable        engine;
	private       MultiSelection        multiSelection;
	private       boolean               muted;
	private       float                 muteMaster;

	public Engine(MetaDB db, Audio audio, OSC osc, MIDI midi) {
		super("Engine");
		this.db             = db;
		this.audio          = audio;
		this.osc            = osc;
		this.midi           = midi;

		AbletonPush push = midi.getPush();
		this.engine = new Parametrizable("Engine", MASTER, PAUSED) {
			@Override
			public void setVal(Parameter p, float val) {
				super.setVal(p, val);
				osc.send(OSC_ENGINE + p.getName(), val);
				if(p.equals(PAUSED))
					audio.setPaused(val > 0.5f);

				if(push != null)
					push.setColor(PControl.PLAY, audio.isPaused() ? Basic.FULL_BLINK : Basic.FULL);
			}
		};
		if(push != null) {
			push.set(PControl.PLAY,        engine, PAUSED);
			push.set(PControl.KNOB_MASTER, engine, MASTER);
			push.set(PControl.MASTER, ()->{
				push.setColor(PControl.VOLUME, Basic.HALF);
				push.setColor(PControl.MASTER, Basic.FULL);
				push.set(PControl.KNOB_MASTER, engine, MASTER);
			});
			push.set(PControl.VOLUME, ()->{
				push.setColor(PControl.MASTER, Basic.HALF);
				push.setColor(PControl.VOLUME, Basic.FULL);
				push.set(PControl.KNOB_MASTER, audio.getOutGain(), MonitorGain.GAIN);
			});
			push.setColor(PControl.VOLUME, Basic.HALF);
			push.set(PControl.MUTE, ()->{
				if(muted) {
					push.setColor(PControl.MUTE, Basic.FULL);
					setMaster(muteMaster);
					muted = false;
				} else {
					push.setColor(PControl.MUTE, Basic.FULL_BLINK);
					muteMaster = setMaster(0);
					muted = true;
				}
			});
		}

		osc.addHandler("/" + BOUNCELET, this);
		pollTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				cleanup();
				osc.send("/"+BOUNCELET+"s");
			}
		}, 1000, POLL_INTERVAL);

		osc.addHandler("/midi", midi);
		
		Platform.get().addShutdownDispose(this);
	}

	@Override
	public void dispose() {
		pollTimer.cancel();
	}

	private static final String[] COLS   = {"", "Control", "Type", "Name", "Active"};
	private static final int[]    WIDTHS = {8,   56,        250,    250,    50}; 
	private static final Comparator<?>[] CMPS = {
			new Comparator<Bouncelet>() {@Override public int compare(Bouncelet b1, Bouncelet b2) {return Float.compare(b1.getColor().brightness(), b2.getColor().brightness());};},
			new Comparator<Bouncelet>() {@Override public int compare(Bouncelet b1, Bouncelet b2) {return -Float.compare(b1.getColor().brightness(), b2.getColor().brightness());};},

			new Comparator<Bouncelet>() {@Override public int compare(Bouncelet b1, Bouncelet b2) {return Integer.compare(b1.id, b2.id);};},
			new Comparator<Bouncelet>() {@Override public int compare(Bouncelet b1, Bouncelet b2) {return -Integer.compare(b1.id, b2.id);};},

			new Comparator<Bouncelet>() {@Override public int compare(Bouncelet b1, Bouncelet b2) {return b1.getType().compareTo(b2.getType());};},
			new Comparator<Bouncelet>() {@Override public int compare(Bouncelet b1, Bouncelet b2) {return -b1.getType().compareTo(b2.getType());};},

			new Comparator<Bouncelet>() {@Override public int compare(Bouncelet b1, Bouncelet b2) {return b1.getLabel().compareTo(b2.getLabel());};},
			new Comparator<Bouncelet>() {@Override public int compare(Bouncelet b1, Bouncelet b2) {return -b1.getLabel().compareTo(b2.getLabel());};},

			new Comparator<Bouncelet>() {@Override public int compare(Bouncelet b1, Bouncelet b2) {return Float.compare(b1.getActive(), b2.getActive());};},
			new Comparator<Bouncelet>() {@Override public int compare(Bouncelet b1, Bouncelet b2) {return -Float.compare(b1.getActive(), b2.getActive());};},
	};
	@SuppressWarnings("unchecked")
	@Override
	protected void fillContent(Composite panel) {
		Display display = Display.getDefault();

		SashForm sash = new SashForm(panel, SWT.HORIZONTAL | SWT.SMOOTH);
		sash.setLayoutData(GridDataFactory.fill(true, true));

		if(table != null) table.dispose();
		table = new Table(sash, SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setLayoutData(GridDataFactory.fill(true, true));
		table.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		table.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {widgetDefaultSelected(e);}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {selectionChanged();}
		});
		table.setHeaderVisible(true);
		for(int i = 0; i < COLS.length; i++) {
			TableColumn col = new TableColumn(table, SWT.NONE);
			col.setText(COLS[i]);
			col.setWidth(WIDTHS[i]);
			final int idx = i;
			col.addListener(SWT.Selection, e->{
				try {
					TableColumn column = (TableColumn) e.widget;
					if(table.getSortColumn() != column) {
						table.setSortColumn(column);
						if(table.getSortDirection() != SWT.UP && table.getSortDirection() != SWT.DOWN)
							table.setSortDirection(SWT.UP);
					} else {
						table.setSortDirection(table.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
					}
					cmp = (Comparator<Bouncelet>) CMPS[idx*2+(table.getSortDirection() == SWT.UP ? 1 : 0)];
					sort();
				} catch(Throwable t) {
					log.warning(t);
				}
			});
		}

		if(inspector != null) inspector.dispose();
		Composite inspectorPanel = new Composite(sash, SWT.NONE);
		inspectorPanel.setLayoutData(GridDataFactory.fill(true, true));
		inspectorPanel.setLayout(new GridLayout(2,false));

		inspector = new Inspector(audio, inspectorPanel);
		Composite engineUI = (Composite)ParameterWindow.createUI(inspectorPanel, engine, true);
		new Label(engineUI, SWT.NONE).setText("Message");
		Text msgUI = new Text(engineUI, SWT.NONE);
		msgUI.setLayoutData(GridDataFactory.fill(true, false));
		msgUI.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {widgetDefaultSelected(e);}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				osc.send(OSC_ENGINE + "msg", msgUI.getText());
				msgUI.selectAll();
			}
		});

		sash.setWeights(new int[] {70,30});

		multiSelection = new MultiSelection(this, osc, midi, inspector);
		multiSelection.setTable(table);
	}

	private void selectionChanged() {
		try {
			TableItem[] selection = table.getSelection();
			if(selection.length == 0) {
				inspector.clear(inspector.getCurrent());
				return;
			} else if(selection.length == 1) {
				Bouncelet b = (Bouncelet) table.getItem(table.getSelectionIndex()).getData(BOUNCELET);
				inspect(b);
			} else {
				inspect(multiSelection);
			}
		} catch(Throwable t) {
			log.warning(t);
		}
	}

	public void select(Bouncelet b, boolean extendSelection) {
		if(!(extendSelection)) 
			table.deselectAll();
		for(int i = table.getItemCount(); --i >= 0;) {
			if(b == table.getItem(i).getData(BOUNCELET)) {
				table.select(i);
				break;
			}
		}
		selectionChanged();
	}

	public void deselect(Bouncelet bouncelet) {
		int index = 0;
		for(TableItem item : table.getItems()) {
			Bouncelet b = (Bouncelet)item.getData(BOUNCELET);
			if(b == bouncelet)
				table.deselect(index);
			index++;
		}
		selectionChanged();
	}

	private void inspect(Bouncelet b) {
		if(b instanceof MultiSelection) {
			b.setNames(Bouncelet.ACTIVE, Bouncelet.BPM_DOWNER, Bouncelet.PATTERN, Bouncelet.PATTERN_STYLE);
			b.setMins(0f, -8f, 0f,   0f);
			b.setMaxs(1f,  8f, 255f, PatternStyle.values().length);
			b.setVals(1f,  1f, 255f, 0f);
		} else {
			String prefix = "/"+BOUNCELET+"/" + b.getId() + "/"; 
			osc.send(prefix + "names");
			osc.send(prefix + "mins");
			osc.send(prefix + "maxs");
			osc.send(prefix + "vals");
		}
		Bouncelet[] selection = new Bouncelet[table.getSelectionCount()];
		int i = 0;
		for(TableItem item : table.getSelection())
			selection[i++] = (Bouncelet)item.getData(BOUNCELET);
		for(IEngineListener l : listeners) {
			try {
				l.selectionChanged(selection);
			} catch(Throwable t) {
				log.warning(t);
			}
		}
	}

	public void update(int id, String type, String label, float active, RGB color) {
		Bouncelet[] bouncelets = null;
		boolean changed = false;
		synchronized (s_bouncelets) {
			while(id >= s_bouncelets.size()) s_bouncelets.add(null);
			Bouncelet b = s_bouncelets.get(id);
			if(b == null) {
				b = new Bouncelet(this, osc, midi, id, type, label, active, color, inspector);
				changed = true;
			}
			s_bouncelets.set(id, b);
			if(b.getType().equals(type))
				changed |= b.update(label, active, color);
			if(changed) {
				sort();
				if(!(listeners.isEmpty()))
					bouncelets = s_bouncelets.toArray(new Bouncelet[s_bouncelets.size()]);
			}
		}
		if(changed)
			fireBounceletsChanged(bouncelets);
	}

	private void fireBounceletsChanged(Bouncelet[] bouncelets) {
		for(IEngineListener l : listeners) {
			try {
				l.bounceletsChanged(bouncelets);
			} catch(Throwable t) {
				log.warning(t);
			}
		}
	}

	private void cleanup() {
		synchronized (s_bouncelets) {
			long remove = System.currentTimeMillis() - 3 * POLL_INTERVAL;
			for(int i = s_bouncelets.size(); --i >= 0;) {
				Bouncelet b = s_bouncelets.get(i);
				if(b == null) continue;
				if(b.olderThan(remove)) {
					s_bouncelets.set(i, null);
					TableItem item = b2i.get(b);
					if(item != null) {
						Display.getDefault().asyncExec(()->{
							item.dispose();	
							b.dispose();
							synchronized (s_bouncelets) {
								fireBounceletsChanged(s_bouncelets.toArray(new Bouncelet[s_bouncelets.size()]));
							}
						});
					}
					b2i.remove(b);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	Comparator<Bouncelet> cmp = (Comparator<Bouncelet>) CMPS[0];

	private void sort() {
		List<Bouncelet> bs = new ArrayList<>(s_bouncelets.size());
		synchronized (s_bouncelets) {
			for(Bouncelet b : s_bouncelets)
				if(b != null)
					bs.add(b);
		}
		bs.sort(cmp);
		Bouncelet selection = table.getSelectionIndex() < 0 ? null : (Bouncelet) table.getItem(table.getSelectionIndex()).getData(BOUNCELET);
		table.removeAll();
		Display display = table.getDisplay();
		int newSelectionIndex = 0;
		int i = 0;
		for(Bouncelet b : bs) {
			if(b == selection) newSelectionIndex = i;
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData(BOUNCELET, b);
			b2i.put(b, item);
			updateItem(display, b);
			i++;
		}
		if(selection == null) table.deselectAll();
		else				  table.select(newSelectionIndex);
		table.setLinesVisible(table.getItemCount() > 0);
		table.redraw();
	}

	private static final NumberFormat FMT = TextUtilities.decimalFormat(2);
	private void updateItem(Display display, Bouncelet b) {
		TableItem item = b2i.get(b);
		if(!(item.isDisposed())) {
			RGB   rgb   = b.getColor();
			Color color = rgb2color(display, rgb);
			item.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
			item.setBackground(0, color);
			item.setImage(1, b.getControllerImage(display));
			item.setImage(2, db.icon48x48(display, b.getType(), "Bouncelet"));
			item.setText(2, b.getType());
			item.setText(3, b.getLabel());
			item.setText(4, FMT.format(b.getActive()));
		}
	}

	private static final Map<RGB, Color> rgb2color = new HashMap<>();
	public static Color rgb2color(Display display, RGB rgb) {
		Color color = rgb2color.get(rgb);
		if(color == null) {
			color = new Color(display, (int)(rgb.r * 255f), (int)(rgb.g * 255f), (int)(rgb.b * 255f));
			rgb2color.put(rgb, color);
		}
		return color;
	}

	@Override
	public Object[] handle(String[] address, int addrIdx, StringBuilder typeString, long timestamp, Object ... args) {
		synchronized (s_bouncelets) {
			Bouncelet b = null;
			switch(address[address.length-1]) {
			case "names":
				b = s_bouncelets.get(Integer.parseInt(address[address.length-2]));
				b.setNames(args);
				break;
			case "mins": 
				b = s_bouncelets.get(Integer.parseInt(address[address.length-2]));
				b.setMins(args);
				break;
			case "maxs": 
				b = s_bouncelets.get(Integer.parseInt(address[address.length-2]));
				b.setMaxs(args);
				break;
			case "vals": 
				b = s_bouncelets.get(Integer.parseInt(address[address.length-2]));
				b.setVals(args);
				break;
			default:
				Display.getDefault().asyncExec(()->update(
						Integer.parseInt(address[address.length-1]), 
						args[0].toString(), 
						args[1].toString(), 
						((Number)args[2]).floatValue(), 
						new RGB(((Number)args[3]).floatValue(),((Number)args[4]).floatValue(),((Number)args[5]).floatValue())));
				break;
			}
			return null;
		}
	}

	public float setMaster(float val) {
		float result = engine.getVal(MASTER);
		engine.setVal(MASTER, val);
		return result;
	}

	public boolean setPaused(boolean state) {
		boolean result = engine.getVal(PAUSED) > 0.5f;
		engine.setVal(PAUSED, state ? 1 : 0);
		return result;
	}

	public void addEngineListener(IEngineListener l) {
		listeners.add(l);
	}

	public void removeEngineListner(IEngineListener l) {
		CollectionUtilities.removeAll(listeners, l);
	}
}
