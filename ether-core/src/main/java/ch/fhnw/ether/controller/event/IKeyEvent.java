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

package ch.fhnw.ether.controller.event;

import org.lwjgl.glfw.GLFW;

import ch.fhnw.ether.view.IView;

/**
 * Key event, aligned with the underlying windowing framework.
 * 
 * @author radar
 */
public interface IKeyEvent extends IEvent {
	class KeyEvent extends Event implements IKeyEvent {
		private final int key;
		private final int scanCode;
		private final boolean repeated;
		
		public KeyEvent(IView view, int mods, int key, int scanCode, boolean repeated) {
			super(view, mods);
			this.key = key;
			this.scanCode = scanCode;
			this.repeated = repeated;
		}
		
		@Override
		public int getKey() {
			return key;
		}
		
		@Override
		public int getScanCode() {
			return scanCode;
		}
		
		@Override
		public boolean isRepeated() {
			return repeated;
		}
	}

	int VK_END    = GLFW.GLFW_KEY_END;
	int VK_ENTER  = GLFW.GLFW_KEY_ENTER;
	int VK_ESCAPE = GLFW.GLFW_KEY_ESCAPE;
	int VK_DELETE = GLFW.GLFW_KEY_END;
	int VK_SPACE  = GLFW.GLFW_KEY_SPACE;
	int VK_Z      = GLFW.GLFW_KEY_Z;
	int VK_Y      = GLFW.GLFW_KEY_Y;
	int VK_Q      = GLFW.GLFW_KEY_Q;
	
	/**
	 * Returns keyboard key associated with this event.
	 */
	int getKey();

	/**
	 * Returns (system specific) scancode of the.
	 */
	int getScanCode();

	/**
	 * Returns true if this key is pressed and triggered an auto repeate
	 */
	boolean isRepeated();
}
