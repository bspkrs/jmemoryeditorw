/*
 * Copyright 2011 David Karnok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.jmemoryeditorw.gui;

import java.awt.Robot;
import java.awt.event.InputEvent;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.win32.StdCallLibrary;

/**
 * If the user holds down mouse 4 or 5, the program emits fast
 * left or right clicks.
 * @author karnok, 2012.05.31.
 */
public class FastClick {
	
	public static class WinPoint extends Structure {
		public int x;
		public int y;
	}
	interface User32 extends StdCallLibrary {
		/**
		 * Returns the mouse position into the supplied 8 byte
		 * record structure of two integers.
		 * @param pointOut
		 * @return
		 */
		boolean GetCursorPos(WinPoint pointOut);
		/**
		 * Retrieve the key state of the given virtual key.
		 * @param key the key to check, see VK_* constants.
		 * @return the state flags
		 */
		short GetAsyncKeyState(int key);
		int VK_XBUTTON1 = 5;
		int VK_XBUTTON2 = 6;
	}
	/**
	 * Main program.
	 * @param args the arguments.
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		Robot r = new Robot();
		
		User32 u32 = (User32)Native.loadLibrary("User32", User32.class);
		
		WinPoint wp = new WinPoint();
		
		while (!Thread.currentThread().isInterrupted()) {
			if (!u32.GetCursorPos(wp)) {
				System.out.println(Kernel32.INSTANCE.GetLastError());
			} else {
				int state1 = u32.GetAsyncKeyState(User32.VK_XBUTTON1);
				if ((state1 & 0x8000) != 0) {
					r.mousePress(InputEvent.BUTTON1_MASK);
					Thread.sleep(10);
					r.mouseRelease(InputEvent.BUTTON1_MASK);
				}
				int state2 = u32.GetAsyncKeyState(User32.VK_XBUTTON2);
				if ((state2 & 0x8000) != 0) {
					r.mousePress(InputEvent.BUTTON3_MASK);
					Thread.sleep(10);
					r.mouseRelease(InputEvent.BUTTON3_MASK);
				}
			}
			Thread.sleep(50);
		}
	}
}
