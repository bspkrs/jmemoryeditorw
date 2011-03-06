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

package hu.jmemoryeditorw.jna;

import com.sun.jna.Structure;

/**
 * A RECT structure.
 * @author karnok, 2011.03.06.
 */
public class Rect extends Structure {
	/** The left coordinate. */
	public int left;
	/** The top coordinate. */
	public int top;
	/** The right coordinate. */
	public int right;
	/** The bottom coordinate. */
	public int bottom;
}
