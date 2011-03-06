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

import hu.jmemoryeditorw.gui.Main.DisableOnFind;
import hu.jmemoryeditorw.gui.Main.ProcessListRenderer;
import hu.jmemoryeditorw.jna.Kernel32;
import hu.jmemoryeditorw.jna.Rect;
import hu.jmemoryeditorw.jna.User32;
import hu.jmemoryeditorw.jna.WinAPIHelper;
import hu.jmemoryeditorw.jna.WinAPIHelper.ProcessData;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.common.collect.Lists;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Cheat program to emulate subsequent clicks in a 
 * hidden object game.
 * @author karnok, 2011.03.06.
 */
public class HogClicker extends JFrame {
	/** */
	private static final long serialVersionUID = -7964917223246676090L;
	/** The list of points to click. */
	List<Point> ps = new ArrayList<Point>();
	/** The list of points to click. */
	JList<String> points = new JList<String>();
	/** The current list of processes available. */
	final List<ProcessData> processList = Lists.newArrayList();
	/** The on-screen combobox for process selection. */
	@DisableOnFind
	JComboBox processBox;
	/** The status label. */
	JLabel statusLine;
	/** Refresh the process listing. */
	JButton refreshProcess;
	/** Remove a point. */
	JButton removePoint;
	/** Clear all points. */
	JButton clearPoint;
	/** Capture a screenshot of the current process. */
	JButton capture;
	/** Move a point up. */
	JButton up;
	/** Move a point down. */
	JButton down;
	/** The delay between clicks. */
	JTextField delay;
	/** The main screen. */
	MainScreen main;
	/** The zoomed screen. */
	ZoomedScreen zoom;
	/** Execute the clicks. */
	JButton execute;
	/** The current window handle. */
	int currentWindow;
	/** The points list model. */
	DefaultListModel<String> pointsModel;
	/** The highlighter box size. */
	JSpinner boxSize;
	/** The box color. */
	JButton boxColor;
	/** The zoomed screen popped out. */
	ZoomedScreen popupZoom;
	/** The zoom screen popped out. */
	JFrame popupFrame;
	/** Top-down ordering. */
	JButton topDown;
	/** Construct the GUI. */
	public HogClicker() {
		super("Hidden Object Game Clicker");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		processBox = new JComboBox();
		processBox.setRenderer(new ProcessListRenderer(processList));
		
		refreshProcess = new JButton("Refresh");
		refreshProcess.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getProcessList();
			}
		});
		removePoint = new JButton("Remove");
		removePoint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doRemovePoint();
			}
		});
		clearPoint = new JButton("Clear");
		clearPoint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doClearPoint();
			}
		});
		capture = new JButton("Capture");
		capture.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doCapture();
			}
		});

		up = new JButton("Up");
		up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doMoveUp();
			}
		});
		down = new JButton("Down");
		down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doMoveDown();
			}
		});
		delay = new JTextField(5);
		delay.setText("1000");
		JLabel delayLabel = new JLabel("Delay (ms):");
		delayLabel.setForeground(Color.WHITE);
		
		main = new MainScreen();
		main.points = ps;
		main.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				doMainMove(e);
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				zoom.location = e.getPoint();
				zoom.repaint();
			}
		});
		main.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				doMouseClicked(e);
			}
		});
		
		boxSize = new JSpinner();
		boxSize.getModel().setValue(main.size);
		boxSize.getModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				doBoxSizeChanged();
			}
		});
		
		JLabel boxSizeLabel = new JLabel("Size:");
		boxSizeLabel.setForeground(Color.WHITE);
		
		boxColor = new JButton("Color");
		boxColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSelectBoxColor();
			}
		});
		
		zoom = new ZoomedScreen();
		zoom.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				doPopupZoom();
			}
		});
		
		popupZoom = new ZoomedScreen();
		
		execute = new JButton("Execute");
		execute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExecute();
			}
		});
		
		statusLine = new JLabel();
		
		Container c = getContentPane();
		
		
		JLabel processLabel = new JLabel("Process:");
		processLabel.setForeground(Color.WHITE);		
		pointsModel = new DefaultListModel<String>();
		points.setModel(pointsModel);
		
		JScrollPane sp = new JScrollPane(points);
		
		JScrollPane mainScroll = new JScrollPane(main);
		mainScroll.getHorizontalScrollBar().setUnitIncrement(16);
		mainScroll.getHorizontalScrollBar().setBlockIncrement(48);
		mainScroll.getVerticalScrollBar().setUnitIncrement(16);
		mainScroll.getVerticalScrollBar().setBlockIncrement(48);

		c.setBackground(Color.DARK_GRAY);
		mainScroll.setBackground(Color.DARK_GRAY);
		main.setBackground(Color.DARK_GRAY);

		topDown = new JButton("TD");
		topDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doTopDown();
			}
		});
		
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(processLabel)
				.addComponent(processBox)
				.addComponent(refreshProcess)
				.addComponent(capture)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(removePoint)
						.addComponent(clearPoint)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(up)
						.addComponent(down)
						.addComponent(topDown)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(delayLabel)
						.addComponent(delay, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(boxSizeLabel)
						.addComponent(boxSize, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(boxColor)
					)
					.addComponent(sp, 128, 128, 128)
					.addComponent(execute)
					.addComponent(zoom, 128, 128, 128)
				)
				.addComponent(mainScroll, 1, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			)
			.addComponent(statusLine)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(processLabel)
				.addComponent(processBox)
				.addComponent(refreshProcess)
				.addComponent(capture)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.LEADING)
				.addGroup(
					gl.createSequentialGroup()
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(removePoint)
						.addComponent(clearPoint)
					)
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(up)
						.addComponent(down)
						.addComponent(topDown)
					)
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(delayLabel)
						.addComponent(delay, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(boxSizeLabel)
						.addComponent(boxSize, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(boxColor)
					)
					.addComponent(sp)
					.addComponent(execute)
					.addComponent(zoom, 128, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				)
				.addComponent(mainScroll, 1, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			)
			.addComponent(statusLine)
		);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				getProcessList();
			}
		});
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (popupFrame != null) {
					popupFrame.dispose();
					popupFrame = null;
				}
			}
		});
		
		pack();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				HogClicker c = new HogClicker();
				c.setLocationRelativeTo(null);
				c.setVisible(true);
			}
		});
	}
	void getProcessList() {
		processList.clear();
		processList.addAll(WinAPIHelper.getProcesses());
		Integer[] data = new Integer[processList.size()];
		for (int i = 0; i < data.length; i++) {
			data[i] = i;
		}
		processBox.setModel(new DefaultComboBoxModel(data));
		processBox.setSelectedIndex(-1);
		statusLineToLastError();
	}
	/** Sets the status line value to the last error. */
	void statusLineToLastError() {
		int i = Kernel32.INSTANCE.GetLastError();
		String s = WinAPIHelper.getLastErrorStr(i);
		statusLine.setText(String.format("%d - %s", i, s));
	}
	/** The main screen showing the entire game window. */
	class MainScreen extends JComponent {
		/** The image. */
		BufferedImage image;
		/** The points to highlight. */
		List<Point> points;
		/** The currently selected point. */
		Point selected;
		/** The box size around a point. */
		int size = 3;
		/** The box color. */
		Color boxColor = Color.RED;
		/**
		 * 
		 */
		private static final long serialVersionUID = -7401191429904399320L;
		@Override
		public Dimension preferredSize() {
			return image != null ? new Dimension(image.getWidth(), image.getHeight()) : new Dimension(640, 480);
		}
		@Override
		public void paint(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			if (image != null) {
				g.drawImage(image, 0, 0, null);
				Point last = null;
				for (Point p : points) {
					if (p != selected) {
						g.setColor(boxColor);
					} else {
						g.setColor(new Color(255 - boxColor.getRed(), 255 - boxColor.getGreen(), 255 - boxColor.getBlue()));
					}
					g.drawRect(p.x - size, p.y - size, 2 * size, 2 * size);
					if (last != null) {
						g.drawLine(last.x, last.y, p.x, p.y);
					}
					last = p;
				}
			}
		}
		
	}
	List<Integer> findProcessWindows(final int processId) {
		final List<Integer> targets = new ArrayList<Integer>();
		User32.INSTANCE.EnumWindows(new User32.EnumWindowsProc() {
			@Override
			public boolean callback(int hWnd, Pointer arg) {
				IntByReference pid = new IntByReference();
				User32.INSTANCE.GetWindowThreadProcessId(hWnd, pid);
				if (User32.INSTANCE.IsWindowVisible(hWnd)
						&& processId == pid.getValue()) {
					targets.add(hWnd);
				}
				return true;
			}
		}, null);
		return targets;
	}
	/**
	 * A zoomed screen showing a part of the game window where the mouse
	 * is currently.
	 */
	class ZoomedScreen extends JComponent {
		/** The image. */
		BufferedImage image;
		/** The current location within the image. */
		Point location;
		/** The zoom level. */
		int zoom = 2;
		/**
		 * 
		 */
		private static final long serialVersionUID = 7070324347539386539L;
		@Override
		public void paint(Graphics g) {
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			if (image != null && location != null) {
				g.drawImage(image, 
						0, 0, getWidth(), getHeight(), 
						location.x - getWidth() / 2 /zoom, 
						location.y - getHeight() / 2 / zoom,
						location.x + getWidth() / 2 /zoom, 
						location.y + getHeight() / 2 / zoom,
						null);
			}
		}
	}
	/** Handle the capture button. */
	void doCapture() {
		if (processBox.getSelectedIndex() < 0) {
			return;
		}
		ProcessData pd = processList.get(processBox.getSelectedIndex());
		JPopupMenu mnu = new JPopupMenu();
		
		List<Integer> windows = findProcessWindows(pd.ProcessID);
		statusLineToLastError();
		if (windows.size() == 1) {
			screenCapture(windows.get(0));
		} else {
			char[] name = new char[64 * 1024];
			for (final Integer wnd : windows) {
				Arrays.fill(name, '\0');
				User32.INSTANCE.GetWindowTextW(wnd, name, name.length);
				StringBuilder n = new StringBuilder();
				for (int c = 0; c < name.length; c++) {
					if (name[c] == '\0') {
						break;
					}
					n.append(name[c]);
				}
				JMenuItem mi = new JMenuItem(n.toString());
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						screenCapture(wnd);
					}
				});
				mnu.add(mi);
			}
			mnu.show(capture, 0, capture.getHeight());
		}
	}
	/**
	 * Handle the mouse click on the main window
	 * @param e the event
	 */
	protected void doMouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) && !e.isShiftDown()) {
			Point p = e.getPoint();
			ps.add(p);
			pointsModel.addElement(p.x + ", " + p.y);
		} else
		if (SwingUtilities.isLeftMouseButton(e) && e.isShiftDown()) {
			for (int i = ps.size() - 1; i >= 0; i--) {
				Point p = ps.get(i);
				int dx = p.x - e.getX();
				int dy = p.y - e.getY();
				if (dx * dx + dy * dy <= main.size * main.size ) {
					ps.remove(i);
					pointsModel.removeElementAt(i);
				}
			}
		}
		repaint();
	}
	/** The box size changed event. */
	void doBoxSizeChanged() {
		main.size = Math.max(1, (Integer)boxSize.getValue());
		main.repaint();
	}
	/** The color selection event. */
	void doSelectBoxColor() {
		Color c = JColorChooser.showDialog(this, "Select a box color", main.boxColor);
		if (c != null) {
			main.boxColor = c;
			main.repaint();
		}
	}
	/** Remove the selected points. */
	void doRemovePoint() {
		int[] idxs = points.getSelectedIndices();
		for (int i = idxs.length - 1; i >= 0; i--) {
			ps.remove(idxs[i]);
			pointsModel.removeElementAt(idxs[i]);
		}
		repaint();
	}
	/** Remove all points. */
	void doClearPoint() {
		pointsModel.clear();
		ps.clear();
		repaint();
	}
	/**
	 * Move selected items up.
	 */
	void doMoveUp() {
		int[] idxs = points.getSelectedIndices();
		if (idxs.length == 0 || idxs[0] == 0) {
			return;
		}
		for (int i = 0; i < idxs.length; i++) {
			int idx = idxs[i];
			Point p = ps.remove(idx);
			String pstr = pointsModel.remove(idx);
			
			ps.add(idx - 1, p);
			pointsModel.add(idx - 1, pstr);
			idxs[i]--;
		}
		points.setSelectedIndices(idxs);
		repaint();
	}
	/**
	 * Move selected items down.
	 */
	void doMoveDown() {
		int[] idxs = points.getSelectedIndices();
		if (idxs.length == 0 || idxs[idxs.length - 1] == ps.size() - 1) {
			return;
		}
		for (int i = 0; i < idxs.length; i++) {
			int idx = idxs[i];
			Point p = ps.remove(idx);
			String pstr = pointsModel.remove(idx);
			
			ps.add(idx + 1, p);
			pointsModel.add(idx + 1, pstr);
			idxs[i] += 1;
		}
		points.setSelectedIndices(idxs);
		repaint();
	}
	/**
	 * Execute the mouse clicks.
	 */
	void doExecute() {
		execute.setEnabled(false);
		if (popupFrame != null) {
			popupFrame.setVisible(false);
		}
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				Rect r = new Rect();
				User32.INSTANCE.GetWindowRect(currentWindow, r);
				User32.INSTANCE.SetForegroundWindow(currentWindow);
				try {
					int d = Integer.parseInt(delay.getText());
					Thread.sleep(d);
					Robot rob = new Robot();
					for (Point p : ps) {
						rob.mouseMove(p.x + r.left, p.y + r.top);
						rob.mousePress(InputEvent.BUTTON1_DOWN_MASK);
						Thread.sleep(100);
						rob.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
						Thread.sleep(d);
					}
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				} catch (AWTException ex) {
					ex.printStackTrace();
				}
				return null;
			}
			@Override
			protected void done() {
				execute.setEnabled(true);
			}
		};
		worker.execute();
	}

	/**
	 * Perform the screen capture by bringing the window to front
	 * and taking a screenshot.
	 * @param wnd the window
	 */
	void screenCapture(final Integer wnd) {
		currentWindow = wnd;
		Rect r = new Rect();
		User32.INSTANCE.GetWindowRect(wnd, r);
		try {
			User32.INSTANCE.SetForegroundWindow(wnd);
			Robot robot = new Robot();
			BufferedImage img = robot.createScreenCapture(new Rectangle(r.left, r.top, r.right - r.left + 1, r.bottom - r.top + 1));
			main.image = img;
			zoom.image = img;
			popupZoom.image = img;
			main.revalidate();
			requestFocus();
			toFront();
			repaint();
			popupZoom.repaint();
		} catch (AWTException ex) {
			ex.printStackTrace();
		} finally {
			statusLineToLastError();
		}
	}
	/** Display a floating zoom window. */
	void doPopupZoom() {
		if (popupFrame == null) {
			popupFrame = new JFrame("Zoom");
			popupFrame.setAlwaysOnTop(true);
			popupFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			final JSlider slider = new JSlider(1, 10, popupZoom.zoom);
			slider.setOrientation(JSlider.HORIZONTAL);
			slider.setSnapToTicks(true);
			slider.setMinorTickSpacing(1);
			slider.setPaintLabels(true);
			slider.setPaintTicks(true);
			slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					popupZoom.zoom = slider.getValue();
					popupZoom.repaint();
				}
			});
			
			popupFrame.getContentPane().add(slider, BorderLayout.NORTH);
			popupFrame.getContentPane().add(popupZoom, BorderLayout.CENTER);
			popupZoom.setPreferredSize(new Dimension(96, 96));
			popupFrame.pack();
		}
		popupFrame.setVisible(true);
		popupFrame.setLocationRelativeTo(zoom);
		popupFrame.toFront();
	}
	/**
	 * Move the viewpoint of the zoomed window.
	 * @param e the event
	 */
	void doMainMove(MouseEvent e) {
		zoom.location = e.getPoint();
		zoom.repaint();
		popupZoom.location = zoom.location;
		popupZoom.repaint();
	}
	/** Reorder points top-down. */
	void doTopDown() {
		Collections.sort(ps, new Comparator<Point>() {
			@Override
			public int compare(Point o1, Point o2) {
				int c = Integer.compare(o1.y, o2.y);
				if (c == 0) {
					c = Integer.compare(o1.x, o2.x);
				}
				return c;
			}
		});
		pointsModel.clear();
		for (Point p : ps) {
			pointsModel.addElement(p.x + ", " + p.y);
		}
	}
}
