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
	int VK_SPACE=GLFW.GLFW_KEY_SPACE;
	int VK_APOSTROPHE=GLFW.GLFW_KEY_APOSTROPHE;
	int VK_COMMA=GLFW.GLFW_KEY_COMMA;
	int VK_MINUS=GLFW.GLFW_KEY_MINUS;
	int VK_PERIOD=GLFW.GLFW_KEY_PERIOD;
	int VK_SLASH=GLFW.GLFW_KEY_SLASH;
	int VK_0=GLFW.GLFW_KEY_0;
	int VK_1=GLFW.GLFW_KEY_1;
	int VK_2=GLFW.GLFW_KEY_2;
	int VK_3=GLFW.GLFW_KEY_3;
	int VK_4=GLFW.GLFW_KEY_4;
	int VK_5=GLFW.GLFW_KEY_5;
	int VK_6=GLFW.GLFW_KEY_6;
	int VK_7=GLFW.GLFW_KEY_7;
	int VK_8=GLFW.GLFW_KEY_8;
	int VK_9=GLFW.GLFW_KEY_9;
	int VK_SEMICOLON=GLFW.GLFW_KEY_SEMICOLON;
	int VK_EQUAL=GLFW.GLFW_KEY_EQUAL;
	int VK_A=GLFW.GLFW_KEY_A;
	int VK_B=GLFW.GLFW_KEY_B;
	int VK_C=GLFW.GLFW_KEY_C;
	int VK_D=GLFW.GLFW_KEY_D;
	int VK_E=GLFW.GLFW_KEY_E;
	int VK_F=GLFW.GLFW_KEY_F;
	int VK_G=GLFW.GLFW_KEY_G;
	int VK_H=GLFW.GLFW_KEY_H;
	int VK_I=GLFW.GLFW_KEY_I;
	int VK_J=GLFW.GLFW_KEY_J;
	int VK_K=GLFW.GLFW_KEY_K;
	int VK_L=GLFW.GLFW_KEY_L;
	int VK_M=GLFW.GLFW_KEY_M;
	int VK_N=GLFW.GLFW_KEY_N;
	int VK_O=GLFW.GLFW_KEY_O;
	int VK_P=GLFW.GLFW_KEY_P;
	int VK_Q=GLFW.GLFW_KEY_Q;
	int VK_R=GLFW.GLFW_KEY_R;
	int VK_S=GLFW.GLFW_KEY_S;
	int VK_T=GLFW.GLFW_KEY_T;
	int VK_U=GLFW.GLFW_KEY_U;
	int VK_V=GLFW.GLFW_KEY_V;
	int VK_W=GLFW.GLFW_KEY_W;
	int VK_X=GLFW.GLFW_KEY_X;
	int VK_Y=GLFW.GLFW_KEY_Y;
	int VK_Z=GLFW.GLFW_KEY_Z;
	int VK_LEFT_BRACKET=GLFW.GLFW_KEY_LEFT_BRACKET;
	int VK_BACKSLASH=GLFW.GLFW_KEY_BACKSLASH;
	int VK_RIGHT_BRACKET=GLFW.GLFW_KEY_RIGHT_BRACKET;
	int VK_GRAVE_ACCENT=GLFW.GLFW_KEY_GRAVE_ACCENT;
	int VK_WORLD_1=GLFW.GLFW_KEY_WORLD_1;
	int VK_WORLD_2=GLFW.GLFW_KEY_WORLD_2;
	int VK_ESCAPE=GLFW.GLFW_KEY_ESCAPE;
	int VK_ENTER=GLFW.GLFW_KEY_ENTER;
	int VK_TAB=GLFW.GLFW_KEY_TAB;
	int VK_BACKSPACE=GLFW.GLFW_KEY_BACKSPACE;
	int VK_INSERT=GLFW.GLFW_KEY_INSERT;
	int VK_DELETE=GLFW.GLFW_KEY_DELETE;
	int VK_RIGHT=GLFW.GLFW_KEY_RIGHT;
	int VK_LEFT=GLFW.GLFW_KEY_LEFT;
	int VK_DOWN=GLFW.GLFW_KEY_DOWN;
	int VK_UP=GLFW.GLFW_KEY_UP;
	int VK_PAGE_UP=GLFW.GLFW_KEY_PAGE_UP;
	int VK_PAGE_DOWN=GLFW.GLFW_KEY_PAGE_DOWN;
	int VK_HOME=GLFW.GLFW_KEY_HOME;
	int VK_END=GLFW.GLFW_KEY_END;
	int VK_CAPS_LOCK=GLFW.GLFW_KEY_CAPS_LOCK;
	int VK_SCROLL_LOCK=GLFW.GLFW_KEY_SCROLL_LOCK;
	int VK_NUM_LOCK=GLFW.GLFW_KEY_NUM_LOCK;
	int VK_PRINT_SCREEN=GLFW.GLFW_KEY_PRINT_SCREEN;
	int VK_PAUSE=GLFW.GLFW_KEY_PAUSE;
	int VK_F1=GLFW.GLFW_KEY_F1;
	int VK_F2=GLFW.GLFW_KEY_F2;
	int VK_F3=GLFW.GLFW_KEY_F3;
	int VK_F4=GLFW.GLFW_KEY_F4;
	int VK_F5=GLFW.GLFW_KEY_F5;
	int VK_F6=GLFW.GLFW_KEY_F6;
	int VK_F7=GLFW.GLFW_KEY_F7;
	int VK_F8=GLFW.GLFW_KEY_F8;
	int VK_F9=GLFW.GLFW_KEY_F9;
	int VK_F10=GLFW.GLFW_KEY_F10;
	int VK_F11=GLFW.GLFW_KEY_F11;
	int VK_F12=GLFW.GLFW_KEY_F12;
	int VK_F13=GLFW.GLFW_KEY_F13;
	int VK_F14=GLFW.GLFW_KEY_F14;
	int VK_F15=GLFW.GLFW_KEY_F15;
	int VK_F16=GLFW.GLFW_KEY_F16;
	int VK_F17=GLFW.GLFW_KEY_F17;
	int VK_F18=GLFW.GLFW_KEY_F18;
	int VK_F19=GLFW.GLFW_KEY_F19;
	int VK_F20=GLFW.GLFW_KEY_F20;
	int VK_F21=GLFW.GLFW_KEY_F21;
	int VK_F22=GLFW.GLFW_KEY_F22;
	int VK_F23=GLFW.GLFW_KEY_F23;
	int VK_F24=GLFW.GLFW_KEY_F24;
	int VK_F25=GLFW.GLFW_KEY_F25;
	int VK_KP_0=GLFW.GLFW_KEY_KP_0;
	int VK_KP_1=GLFW.GLFW_KEY_KP_1;
	int VK_KP_2=GLFW.GLFW_KEY_KP_2;
	int VK_KP_3=GLFW.GLFW_KEY_KP_3;
	int VK_KP_4=GLFW.GLFW_KEY_KP_4;
	int VK_KP_5=GLFW.GLFW_KEY_KP_5;
	int VK_KP_6=GLFW.GLFW_KEY_KP_6;
	int VK_KP_7=GLFW.GLFW_KEY_KP_7;
	int VK_KP_8=GLFW.GLFW_KEY_KP_8;
	int VK_KP_9=GLFW.GLFW_KEY_KP_9;
	int VK_KP_DECIMAL=GLFW.GLFW_KEY_KP_DECIMAL;
	int VK_KP_DIVIDE=GLFW.GLFW_KEY_KP_DIVIDE;
	int VK_KP_MULTIPLY=GLFW.GLFW_KEY_KP_MULTIPLY;
	int VK_KP_SUBTRACT=GLFW.GLFW_KEY_KP_SUBTRACT;
	int VK_KP_ADD=GLFW.GLFW_KEY_KP_ADD;
	int VK_KP_ENTER=GLFW.GLFW_KEY_KP_ENTER;
	int VK_KP_EQUAL=GLFW.GLFW_KEY_KP_EQUAL;
	int VK_LEFT_SHIFT=GLFW.GLFW_KEY_LEFT_SHIFT;
	int VK_LEFT_CONTROL=GLFW.GLFW_KEY_LEFT_CONTROL;
	int VK_LEFT_ALT=GLFW.GLFW_KEY_LEFT_ALT;
	int VK_LEFT_SUPER=GLFW.GLFW_KEY_LEFT_SUPER;
	int VK_RIGHT_SHIFT=GLFW.GLFW_KEY_RIGHT_SHIFT;
	int VK_RIGHT_CONTROL=GLFW.GLFW_KEY_RIGHT_CONTROL;
	int VK_RIGHT_ALT=GLFW.GLFW_KEY_RIGHT_ALT;
	int VK_RIGHT_SUPER=GLFW.GLFW_KEY_RIGHT_SUPER;
	int VK_MENU=GLFW.GLFW_KEY_MENU;
	int VK_LAST=GLFW.GLFW_KEY_LAST;

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
