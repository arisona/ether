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

import java.io.File;

import ch.fhnw.ether.platform.IMonitor;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.util.IDisposable;
import ch.fhnw.util.math.Vec2;

/**
 * Abstraction of a window with the most basic operations.
 * @author radar
 *
 */
public interface IWindow extends IDisposable {
	/**
	 * Opqaue abstraction of underlying rendering context (e.g. an OpenGL context).
	 * @see IWindow#acquireContext() for details how to acquire and release.
	 */
	interface IContext extends AutoCloseable {
	}

	int MOD_SHIFT = 1;
	int MOD_CONTROL = 2;
	int MOD_ALT = 4;
	int MOD_SUPER = 8;

	enum PointerMode {
		NORMAL,
		HIDDEN,
		DISABLED
	}
	
	interface IWindowListener {
		void windowCloseRequest(IWindow window);

		void windowRefresh(IWindow window);

		void windowFocusChanged(IWindow window, boolean focused);

		void windowResized(IWindow window, Vec2 windowSize, Vec2 framebufferSize);
	}
	
	interface IKeyListener {
		void keyPressed(IWindow window, int mods, int key, int scancode, boolean repeat);

		void keyReleased(IWindow window, int mods, int key, int scancode);
	}

	interface IPointerListener {
		void pointerEntered(IWindow window, int mods, Vec2 position);

		void pointerExited(IWindow window, int mods, Vec2 position);

		void pointerPressed(IWindow window, int mods, Vec2 position, int button, boolean repeat);

		void pointerReleased(IWindow window, int mods, Vec2 position, int button);

		void pointerClicked(IWindow window, int mods, Vec2 position, int button);

		void pointerMoved(IWindow window, int mods, Vec2 position);

		void pointerDragged(IWindow window, int mods, Vec2 position);

		void pointerWheelMoved(IWindow window, int mods, Vec2 position, Vec2 scroll);
	}
	
	class WindowAdapter implements IWindowListener {
		@Override public void windowCloseRequest(IWindow window) {}
		@Override public void windowRefresh(IWindow window) {}
		@Override public void windowFocusChanged(IWindow window, boolean focused) {}
		@Override public void windowResized(IWindow window, Vec2 windowSize, Vec2 framebufferSize) {}		
	}
	
	class KeyAdapter implements IKeyListener {
		@Override public void keyPressed(IWindow window, int mods, int key, int scancode, boolean repeat) {}
		@Override public void keyReleased(IWindow window, int mods, int key, int scancode) {}
	}
	
	class PointerAdapter implements IPointerListener {
		@Override public void pointerEntered(IWindow window, int mods, Vec2 position) {}
		@Override public void pointerExited(IWindow window, int mods, Vec2 position) {}
		@Override public void pointerPressed(IWindow window, int mods, Vec2 position, int button, boolean repeat) {}
		@Override public void pointerReleased(IWindow window, int mods, Vec2 position, int button) {}
		@Override public void pointerClicked(IWindow window, int mods, Vec2 position, int button) {}
		@Override public void pointerMoved(IWindow window, int mods, Vec2 position) {}
		@Override public void pointerDragged(IWindow window, int mods, Vec2 position) {}
		@Override public void pointerWheelMoved(IWindow window, int mods, Vec2 position, Vec2 scroll) {}
	}
	
	/**
	 * Dispose this window and free all resources associated with it.
	 * 
	 * Call from main thread only.
	 */
	@Override
	void dispose();
	
	/**
	 * Returns true if this window has been disposed.
	 * 
	 * May be called from any thread.
	 */
	boolean isDisposed();
	
	/**
	 * Acquire this window's render context. Note that this is an
	 * auto-closeable, thus needs to be used with
	 * 
	 * <pre>
	 * {@code
	 * try (IContext ctx = window.acquireContext()) {
	 *   // ...
	 * } catch (Throwable t) {}
	 *   // ...
	 * }
	 * </pre>
	 * 
	 * thus the context will be automatically released when getting out of
	 * scope.
	 * 
	 * May be called from any thread.
	 */
	IContext acquireContext();
	
	/**
	 * Release this window's render context. Use only explicitly if you can't
	 * use try with resource.
	 * 
	 * May be called from any thread.
	 */
	void releaseContext();

	/**
	 * Swap buffers.
	 * 
	 * May be called from any thread.
	 */
	void swapBuffers();	

	/**
	 * Get this window's title.
	 * 
	 * May be called from any thread.
	 */
	String getTitle();
	
	/**
	 * Set this window's title.
	 * 
	 * Call from main thread only.
	 */
	void setTitle(String title);

	/**
	 * Returns true if this window is visible.
	 * 
	 * May be called from any thread.
	 */
	boolean isVisible();
	
	/**
	 * Shows / hides this window.
	 * 
	 * Call from main thread only.
	 */
	void setVisible(boolean visible);

	/**
	 * Get this window's position (in screen units).
	 * 
	 * May be called from any thread.
	 */
	Vec2 getPosition();
	
	/**
	 * Set this window's position (in screen units).
	 * 
	 * Call from main thread only.
	 */
	void setPosition(Vec2 position);

	/**
	 * Get this window's size (in screen units).
	 * 
	 * May be called from any thread.
	 */
	Vec2 getSize();
	
	/**
	 * Set this window's size (in screen units)
	 * 
	 * Call from main thread only.
	 */
	void setSize(Vec2 size);

	/**
	 * Get this window's framebuffer size (in pixel units);
	 * 
	 * May be called from any thread.
	 */
	Vec2 getFramebufferSize();
	
	/**
	 * Enable or disable this window as a fullscreen window on the given monitor.
	 * 
	 * Call from main thread only.
	 * 
	 * @param monitor The monitor to use for fullscreen or null to exit fullscreen mode.
	 */
	void setFullscreen(IMonitor monitor);

	/**
	 * Set pointer mode for this window.
	 * 
	 * Call from main thread only.
	 */
	void setPointerMode(PointerMode mode);

	/**
	 * Warp pointer to x y (in pixel units), in right-handed window coordinates
	 * (origin bottom left).
	 * 
	 * Call from main thread only.
	 */
	void setPointerPosition(float x, float y);
		
	/**
	 * Set pointer icon.
	 * 
	 * Call from main thread only.
	 */
	void setPointerIcon(File file, int hotX, int hotY);
	
	/**
	 * Set window listener (or pass null to reset).
	 * 
	 * Call from main thread only.
	 */
	void setWindowListener(IWindowListener windowListener);
	
	/**
	 * Set key listener (or pass null to reset).
	 * 
	 * Call from main thread only.
	 */
	void setKeyListener(IKeyListener keyListener);
	
	/**
	 * Set pointer listener (or pass null to reset).
	 * 
	 * Call from main thread only.
	 */
	void setPointerListener(IPointerListener pointerListener);
	
	/**
	 * Create window.
	 * 
	 * Call from main thread only.
	 */
	static IWindow create(Vec2 size, String title, boolean decorated) {
		return Platform.get().createWindow(size, title, decorated);
	}
	
	/**
	 * Create full screen window.
	 * 
	 * Call from main thread only.
	 */
	static IWindow create(IMonitor monitor, String title) {
		return Platform.get().createWindow(monitor, title);
	}

}
