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

import ch.fhnw.ether.view.IView;

/**
 * Pointer (mouse) event, aligned with the underlying windowing framework.
 * 
 * Main difference to other frameworks is the right handed coordinate system
 * used, i.e. (0, 0) refers to the bottom-left corner of the client window.
 * 
 * @author radar
 */
public interface IPointerEvent extends IEvent {
	int BUTTON_1 = 0;
	int BUTTON_2 = 1;
	int BUTTON_3 = 2;
	
	final class PointerEvent extends Event implements IPointerEvent {
		private final int button;
		private final int clickCount;
		private final float x;
		private final float y;
		private final float scrollX;
		private final float scrollY;
		
		public PointerEvent(IView view, int mods, int button, int clickCount, float x, float y, float scrollX, float scrollY) {
			super(view, mods);
			this.button = button;
			this.clickCount = clickCount;
			this.x = x;
			this.y = y;
			this.scrollX = scrollX;
			this.scrollY = scrollY;
		}
		
		@Override
		public int getButton() {
			return button;
		}
		
		@Override
		public int getClickCount() {
			return clickCount;
		}
		
		@Override
		public float getX() {
			return x;
		}
		
		@Override
		public float getY() {
			return y;
		}
		
		@Override
		public float getScrollX() {
			return scrollX;
		}
		
		@Override
		public float getScrollY() {
			return scrollY;
		}
		
		@Override
		public String toString() {
			return "("+x+","+y+")("+scrollX+","+scrollY+") "+clickCount + " " + button;
		}
	}

	/**
	 * Returns the button pressed for this event.
	 */
	int getButton();

	/**
	 * Returns the button's click count.
	 */
	int getClickCount();

	/**
	 * Returns the pointer's x coordinate.
	 */
	float getX();

	/**
	 * Returns the pointer's y coordinate.
	 */
	float getY();
	
	/**
	 * Returns scroll amount in x direction.
	 */
	float getScrollX();
	
	/**
	 * Returns scroll amount in y direction.
	 */
	float getScrollY();
}
