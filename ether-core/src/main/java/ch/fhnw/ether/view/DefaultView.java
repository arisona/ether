/*
 * Copyright (c) 2013 - 2016 Stefan Muller Arisona, Simon Schubiger
 * Copyright (c) 2013 - 2016 FHNW & ETH Zurich
 * All rights reserved.
 *
 * Contributions by: Filip Schramka, Samuel von Stachelski
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *  Neither the name of FHNW / ETH Zurich nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.fhnw.ether.view;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.controller.event.IEventScheduler.IAction;
import ch.fhnw.ether.controller.event.IKeyEvent.KeyEvent;
import ch.fhnw.ether.controller.event.IPointerEvent;
import ch.fhnw.ether.controller.event.IPointerEvent.PointerEvent;
import ch.fhnw.ether.platform.IMonitor;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.view.IWindow.IKeyListener;
import ch.fhnw.ether.view.IWindow.IPointerListener;
import ch.fhnw.ether.view.IWindow.IWindowListener;
import ch.fhnw.util.Viewport;
import ch.fhnw.util.math.Vec2;

/**
 * Default view class that implements some basic functionality. Use as base for
 * more complex implementations.
 * 
 * @author radar
 */
public class DefaultView implements IView {

	private final Config viewConfig;

	private final IController controller;

	private volatile IWindow window;

	private volatile Viewport viewport = new Viewport(0, 0, 1, 1);

	private boolean enabled = true;

	public DefaultView(IController controller, IMonitor monitor, Config viewConfig, String title) {
		this(controller, monitor, monitor.getX() , monitor.getY(),  monitor.getWidth(), monitor.getHeight(), viewConfig, title);
	}

	public DefaultView(IController controller, int x, int y, int w, int h, Config viewConfig, String title) {
		this(controller, null, x, y, w, h, viewConfig, title);
	}

	private DefaultView(IController controller, IMonitor monitor, int x, int y, int w, int h, Config viewConfig, String title) {
		this.controller = controller;
		this.viewConfig = viewConfig;

		Platform.get().runOnMainThread(() -> {
			window = IWindow.create(new Vec2(16, 16), title != null ? title : "", viewConfig.getViewType() == ViewType.INTERACTIVE_VIEW);

			window.setWindowListener(windowListener);
			window.setKeyListener(keyListener);
			window.setPointerListener(pointerListener);

			// note: we open the window initially at a smaller size, and then
			// resize in order to trigger the window listener.
			window.setSize(new Vec2(w, h));
			if (x != -1)
				window.setPosition(new Vec2(x, y));
			window.setVisible(true);			
			window.setFullscreen(monitor);
		});

		runOnSceneThread(t -> controller.viewCreated(this));
	}

	@Override
	public void dispose() {
		Platform.get().runOnMainThread(() -> {
			window.dispose();
			window = null;
		});
	}

	@Override
	public final boolean isEnabled() {
		return enabled;
	}

	@Override
	public final void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public Config getConfig() {
		return viewConfig;
	}

	@Override
	public final IController getController() {
		return controller;
	}

	@Override
	public final Viewport getViewport() {
		return viewport;
	}

	@Override
	public IWindow getWindow() {
		return window;
	}

	@Override
	public String toString() {
		return "[view " + hashCode() + "]";
	}

	private void runOnSceneThread(IAction action) {
		if (controller.isSceneThread())
			action.run(controller.getScheduler().getTime());
		else
			controller.run(action);
	}

	private IWindowListener windowListener = new IWindowListener() {
		@Override
		public void windowCloseRequest(IWindow window) {
			dispose();
			runOnSceneThread(time -> {
				controller.viewDisposed(DefaultView.this);
			});
		}

		@Override
		public void windowRefresh(IWindow window) {
			controller.repaint();
		}

		@Override
		public void windowFocusChanged(IWindow w, boolean focused) {
			if (focused)
				runOnSceneThread(time -> controller.viewGainedFocus(DefaultView.this));
			else 
				runOnSceneThread(time -> controller.viewLostFocus(DefaultView.this));
		}

		@Override
		public void windowResized(IWindow window, Vec2 windowSize, Vec2 framebufferSize) {
			viewport = new Viewport(0, 0, framebufferSize.x, framebufferSize.y);
			runOnSceneThread(time -> controller.viewResized(DefaultView.this));
		}
	};

	private IKeyListener keyListener = new IKeyListener() {
		@Override
		public void keyPressed(IWindow window, int mods, int key, int scancode, boolean repeat) {
			runOnSceneThread(time -> controller.keyPressed(new KeyEvent(DefaultView.this, mods, key, scancode, repeat)));
		}

		@Override
		public void keyReleased(IWindow window, int mods, int key, int scancode) {
			runOnSceneThread(time -> controller.keyReleased(new KeyEvent(DefaultView.this, mods, key, scancode, false)));
		}
	};

	private IPointerListener pointerListener = new IPointerListener() {
		@Override
		public void pointerEntered(IWindow window, int mods, Vec2 position) {
			runOnSceneThread(time -> controller.pointerEntered(ptre(mods, position, Vec2.ZERO, 0, 0)));
		}

		@Override
		public void pointerExited(IWindow window, int mods, Vec2 position) {
			runOnSceneThread(time -> controller.pointerExited(ptre(mods, position, Vec2.ZERO, 0, 0)));
		}

		@Override
		public void pointerPressed(IWindow window, int mods, Vec2 position, int button, boolean repeat) {
			runOnSceneThread(time -> controller.pointerPressed(ptre(mods, position, Vec2.ZERO, button, repeat ? 2 : 1)));
		}

		@Override
		public void pointerReleased(IWindow window, int mods, Vec2 position, int button) {
			runOnSceneThread(time -> controller.pointerReleased(ptre(mods, position, Vec2.ZERO, button, 0)));
		}

		@Override
		public void pointerClicked(IWindow window, int mods, Vec2 position, int button) {
			runOnSceneThread(time -> controller.pointerClicked(ptre(mods, position, Vec2.ZERO, button, 1)));
		}

		@Override
		public void pointerMoved(IWindow window, int mods, Vec2 position) {
			runOnSceneThread(time -> controller.pointerMoved(ptre(mods, position, Vec2.ZERO, 0, 0)));
		}

		@Override
		public void pointerDragged(IWindow window, int mods, Vec2 position) {
			runOnSceneThread(time -> controller.pointerDragged(ptre(mods, position, Vec2.ZERO, 0, 0)));
		}

		@Override
		public void pointerWheelMoved(IWindow window, int mods, Vec2 position, Vec2 scroll) {
			runOnSceneThread(time -> controller.pointerScrolled(ptre(mods, position, scroll, 0, 0)));
		}
	};

	IPointerEvent ptre(int mods, Vec2 position, Vec2 scroll, int button, int count) {
		return new PointerEvent(this, mods, button, count, position.x, position.y, scroll.x, scroll.y);
	}
}
