/*
 * Classname            : hu.jmemoryeditorw.gui.Main
 * Version information  : 1.0
 * Date                 : 2008.12.16.
 * Copyright notice     : Karnok David
 */
package hu.jmemoryeditorw.gui;

import hu.jmemoryeditorw.jna.Kernel32;
import hu.jmemoryeditorw.jna.MemoryBasicInformation;
import hu.jmemoryeditorw.jna.WinAPIHelper;
import hu.jmemoryeditorw.jna.WinAPIHelper.ProcessData;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.jna.ptr.IntByReference;

/**
 * @author karnokd, 2008.12.16.
 * @version $Revision 1.0$
 */
public class Main extends JFrame {
	/** Annotation for value saving. */
	@Retention(RetentionPolicy.RUNTIME)
	public @interface SaveValue { }
	/** Annotation for indication of disable component upon find new. */
	@Retention(RetentionPolicy.RUNTIME)
	public @interface DisableOnFind { }
	/** The record for found values. */
	public static class ValueFound {
		/** The address of the finding. */
		public int address;
		/** The current value. */
		public byte[] value;
		/** The previous value. */
		public byte[] prevValue;
		/** The type of the value (i.e. byte, int, double, etc. */
		public int valueType;
		/** Constructor. */
		public ValueFound() {
			
		}
		/** Copy constructor. */
		public ValueFound(ValueFound that) {
			this.address = that.address;
			this.value = that.value.clone();
			this.valueType = that.valueType;
			this.prevValue = that.prevValue;
		}
		@Override
		public String toString() {
			if (prevValue != null) {
				return String.format("%08X - %s <-- %s", address, foundValueToString(value, valueType), foundValueToString(prevValue, valueType));
			}
			return String.format("%08X - %s", address, foundValueToString(value, valueType));
		}
	}
	/** Record for the value hold operation. */
	public static class ValueHold {
		/** The memory address. */
		public int address;
		/** The current value. */
		public byte[] value;
		/** The value on hold. */
		public byte[] holdValue;
		/** Value type. */
		public int valueType;
		/** Hold enabled. */
		public boolean enabled;
		/** Address valid. */
		public boolean valid;
		/** Hold type: exactly, at least, at most. */
		public int holdType;
		/** The refresh cycle. */
		public int cycleTime;
		/** The remainging cycles. */
		public int remainingCycles;
		/** The process id. */
		public int processId;
		/** Constructor. */
		public ValueHold() {
			
		}
		/** Copy constructor. */
		public ValueHold(ValueHold that) {
			synchronized(that) {
				this.address = that.address;
				this.value = that.value.clone();
				this.holdValue = that.holdValue.clone();
				this.valueType = that.valueType;
				this.enabled = that.enabled;
				this.valid = that.valid;
				this.cycleTime = that.cycleTime;
				this.remainingCycles = that.remainingCycles;
				this.holdType = that.holdType;
				this.processId = that.processId;
			}
		}
		@Override
		public String toString() {
			synchronized(this) {
				return String.format("[%s][%s] %08X - %s <- %s (%s, %d %s)", enabled ? "Y" : "N", 
						valid ? "Y" : "N", address, foundValueToString(value, valueType),
						foundValueToString(holdValue, valueType), 
						holdType == 0 ? "Exactly" : (holdType == 1 ? "At least" : "At most"),
						cycleTime * 10, "ms");
			}
		}
	}
	/** */
	private static final long serialVersionUID = 4184565190048070573L;
	/** The current list of processes available. */
	private final List<ProcessData> processList = Lists.newArrayList();
	/** The list of selected process's memory structure. */
	private final List<MemoryBasicInformation> currentMemory = Lists.newArrayList();
	/** The on-screen combobox for process selection. */
	@DisableOnFind
	private JComboBox processBox;
	/** The memory layout list. */
	@DisableOnFind
	private JList memoryList;
	/** Search value start. */
	@SaveValue
	@DisableOnFind
	private JTextField valueStart;
	/** Search value end. */
	@SaveValue
	@DisableOnFind
	private JTextField valueEnd;
	/** Value type. */
	@SaveValue
	@DisableOnFind
	private JComboBox valueType;
	/** Address start. */
	@SaveValue
	@DisableOnFind
	private JTextField addressStart;
	/** Address end. */
	@SaveValue
	@DisableOnFind
	private JTextField addressEnd;
	/** The list of found addresses/values. */
	@DisableOnFind
	private JList resultList;
	/** The result list label. */
	private JLabel resultListLabel;
	/** Filter changed values. */
	@SaveValue
	@DisableOnFind
	private JRadioButton filterChanged;
	/** Filter unchanged values. */
	@SaveValue
	@DisableOnFind
	private JRadioButton filterUnchanged;
	/** Filter increased values. */
	@SaveValue
	@DisableOnFind
	private JRadioButton filterIncreased;
	/** Filter decreased values. */
	@SaveValue
	@DisableOnFind
	private JRadioButton filterDecreased;
	/** Filter radio group button. */
	private ButtonGroup filterGroup;
	/** Find new button. */
	@DisableOnFind
	private JButton findNewButton;
	/** Find based on values. */
	@DisableOnFind
	private JButton findButton;
	/** Filter based on the filter group. */
	@DisableOnFind
	private JButton filterButton;
	/** Result history. */
	@DisableOnFind
	private JComboBox resultHistory;
	/** Clear history. */
	@DisableOnFind
	private JButton clearHistory;
	/** Search progress label. */
	private JLabel progressLabel;
	/** Search progress bar. */
	private JProgressBar progress;
	/** The default text field color. */
	private Color textFieldColor;
	/** Current address for value editing. */
	@SaveValue
	@DisableOnFind
	private JComboBox addressRW;
	/** Current value of editing. */
	@SaveValue
	@DisableOnFind
	private JComboBox valueRW;
	/** Hold value type. */
	@SaveValue
	@DisableOnFind
	private JComboBox holdType;
	/** Hold button type. */
	@DisableOnFind
	private JButton holdButton;
	/** Perform the read. */
	@DisableOnFind
	private JButton readButton;
	/** Perform the write. */
	@DisableOnFind
	private JButton writeButton;
	/** Clear read+write history. */
	@DisableOnFind
	private JButton clearButton;
	/** The bottom status line. */
	private JLabel statusLine;
	/** The current background finder task. */
	private ValueFinder valueFinder;
	/** The result limit count. */
	@SaveValue
	@DisableOnFind
	private JTextField limitCount;
	/** The result list model. */
	private DefaultListModel resultListModel;
	/** The history list model. */
	private DefaultComboBoxModel historyListModel;
	/** The list for current findings. */
	//private final List<ValueFound> currentFindings = Lists.newArrayList();
	/** Value difference finder. */
	private ValueDiffFinder valueDiffFinder;
	/** The label for the memory list. */
	private JLabel memoryLabel;
	/** The button for the memory refresh. */
	@DisableOnFind
	private JButton memoryRefresh;
	/** Popup menu for the results list. */
	private JPopupMenu resultPopup;
	/** The hold list model. */
	private DefaultListModel holdListModel;
	/** The hold list. */
	@DisableOnFind
	private JList holdList;
	/** The hold list label. */
	private JLabel holdListLabel;
	/** The hold cycle time. */
	@SaveValue
	@DisableOnFind
	private JTextField cycleTime;
	/** The hold thread. */
	private Thread holdThread;
	/** Hold list menu. */
	private JPopupMenu holdMenu;
	/**
	 * Constructor. Initializes the layout.
	 */
	public Main() {
		super();
		init();
	}
//	/** The custom list model with option to suppress change events. */
//	class CustomListModel extends DefaultListModel {
//		/** Flag to enable or disable notification*/
//		private boolean allowNotification = true;
//		@Override
//		protected void fireContentsChanged(Object source, int index0, int index1) {
//			if (allowNotification) {
//				super.fireContentsChanged(source, index0, index1);
//			}
//		}
//		@Override
//		protected void fireIntervalAdded(Object source, int index0, int index1) {
//			if (allowNotification) {
//				super.fireIntervalAdded(source, index0, index1);
//			}
//		}
//		@Override
//		protected void fireIntervalRemoved(Object source, int index0, int index1) {
//			if (allowNotification) {
//				super.fireIntervalRemoved(source, index0, index1);
//			}
//		}
//		/** Begin changes. */
//		public void begin() {
//			allowNotification = false;
//		}
//		/** Done changes. */
//		public void end() {
//			allowNotification = true;
//			super.fireContentsChanged(this, 0, getSize() - 1);
//		}
//	}
	class ProcessListRenderer extends DefaultListCellRenderer {
		/** */
		private static final long serialVersionUID = -9175136304178115221L;
		@Override
		public Component getListCellRendererComponent(
		                       JList list,
		                       Object value,
		                       int index,
		                       boolean isSelected,
		                       boolean cellHasFocus) {
			JLabel lbl = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof Integer) {
				int idx = ((Integer)value);
				if (idx >= 0 && idx < processList.size()) {
					ProcessData pd = processList.get(idx);
					lbl.setIcon(pd.icon16);
					if (pd.WindowText.length() > 70) {
						lbl.setText(pd.WindowText.substring(0, 70) + "... (" + pd.ProcessID + ")");
					} else {
						lbl.setText(pd.WindowText + " (" + pd.ProcessID + ")");
					}
				} else {
					lbl.setText("");
					lbl.setIcon(null);
				}
			}
			return lbl;
		}
	}
	/** The history list entry. */
	public static class HistoryListEntry {
		/** List of found values. */
		public final List<ValueFound> values = Lists.newArrayList();
		/** The search index. */
		public int index;
		/** The search timestamp. */
		public Date timestamp = new Date();
		private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");
		@Override
		public String toString() {
			return "#" + index + " " + sdf.format(timestamp) + " (" + values.size() + ")";
		}
	}
	/**
	 * Builds the layout.
	 */
	private void init() {
		setTitle("Memory Editor v2.0 - Java Implementation");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				doWindowClosing();
			}
		});
		textFieldColor = new JTextField().getBackground();
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		c.setLayout(gl);
		
		JLabel processLabel = new JLabel("Processes:");
		processBox = new JComboBox();
		processBox.setRenderer(new ProcessListRenderer());
		processBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSelectProcess();
			}
		});
		JButton processRefresh = new JButton("Refresh");
		processRefresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshProcesses();
			}
		});
		memoryList = new JList(new DefaultListModel());
		memoryList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, processLabel.getFont().getSize()));
		JScrollPane memoryListScroll = new JScrollPane(memoryList);
		//memoryListScroll.setPreferredSize(new Dimension(200, 100));
		
		valueStart = new JTextField(10);
		valueStart.addKeyListener(NONEMPTY_KEY_LISTENER);
		valueStart.setHorizontalAlignment(JTextField.TRAILING);
		JLabel valueStartLabel = new JLabel("Value start:");
		
		valueEnd = new JTextField(10);
		valueEnd.addKeyListener(EMPTY_KEY_LISTENER);
		valueEnd.setHorizontalAlignment(JTextField.TRAILING);
		JLabel valueEndLabel = new JLabel("Value end:");
		addressStart = new JTextField(10);
		addressStart.addKeyListener(EMPTY_KEY_LISTENER);
		addressStart.setHorizontalAlignment(JTextField.TRAILING);
		JLabel addressStartLabel = new JLabel("Address start:");
		addressEnd = new JTextField(10);
		addressEnd.addKeyListener(EMPTY_KEY_LISTENER);
		addressEnd.setHorizontalAlignment(JTextField.TRAILING);
		JLabel addressEndLabel = new JLabel("Address end:");
		
		valueType = new JComboBox(new String[] { "Byte", "Short", "Int", "Long", "Float", "Double" });
		JLabel valueTypeLabel = new JLabel("Value type");
		
		resultPopup = new JPopupMenu();
		JMenuItem 
		mi = new JMenuItem("Use item");
		mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { 
			doSetAddress(); 
		}});
		resultPopup.add(mi);
		
		mi = new JMenuItem("Re-read item(s)");
		mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { 
			doRereadItems(); 
		}});
		resultPopup.add(mi);
		mi = new JMenuItem("Remove item(s)");
		mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { 
			doRemoveItems(resultList);
			setNumberOfResults(resultList.getModel().getSize());
			addToHistory();
		}});
		
		resultPopup.add(mi);
		mi = new JMenuItem("Retain item(s)");
		mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { 
			doRetainItems(resultList); 
			setNumberOfResults(resultList.getModel().getSize());
			addToHistory();
		}});
		resultPopup.add(mi);

		holdMenu = new JPopupMenu();

		mi = new JMenuItem("Use");
		mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { 
			doHoldUse(); 
		}});
		holdMenu.add(mi);
		mi = new JMenuItem("Enable");
		mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { 
			doHoldEnable(); 
		}});
		holdMenu.add(mi);
		mi = new JMenuItem("Disable");
		mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { 
			doHoldDisable(); 
		}});
		holdMenu.add(mi);
		
		mi = new JMenuItem("Remove item(s)");
		mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { 
			doRemoveItems(holdList); 
			setNumberOfHolds(holdList.getModel().getSize());
		}});
		
		holdMenu.add(mi);
		mi = new JMenuItem("Retain item(s)");
		mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { 
			doRetainItems(holdList); 
			setNumberOfHolds(holdList.getModel().getSize());
		}});
		holdMenu.add(mi);

		
		resultListModel = new DefaultListModel();
		resultList = new JList(resultListModel);
		resultList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, processLabel.getFont().getSize()));
		resultList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybePopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				maybePopup(e);
			}
			/** Perform popup if this is a popup event. */
			private void maybePopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					// get the item index under the mouse
					int idx = resultList.locationToIndex(e.getPoint());
					if (idx >= 0) {
						if (!resultList.isSelectedIndex(idx)) {
							resultList.setSelectedIndices(new int[0]);
							resultList.setSelectedIndex(idx);
						}
					}
					resultPopup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					doSetAddress();
				}
			}
		});
		
		resultListLabel = new JLabel();
		setNumberOfResults(0);
		JScrollPane resultListScroll = new JScrollPane(resultList);
		
		filterChanged = new JRadioButton("Changed");
		filterUnchanged = new JRadioButton("Unchanged");
		filterIncreased = new JRadioButton("Increased");
		filterDecreased = new JRadioButton("Decreased");
		filterGroup = new ButtonGroup();
		filterGroup.add(filterChanged);
		filterGroup.add(filterUnchanged);
		filterGroup.add(filterIncreased);
		filterGroup.add(filterDecreased);
		
		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new GridLayout(2, 2));
		filterPanel.add(filterChanged);
		filterPanel.add(filterUnchanged);
		filterPanel.add(filterIncreased);
		filterPanel.add(filterDecreased);
		filterPanel.setBorder(new TitledBorder("Filter"));
		
		findNewButton = new JButton("Find New");
		findNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFindNew();
			}
		});
		findButton = new JButton("Find");
		findButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFind();
			}
		});
		filterButton = new JButton("Filter");
		filterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFilter();
			}
		});
		
		progress = new JProgressBar(JProgressBar.HORIZONTAL, 0, 1000);
		progressLabel = new JLabel();
		
		historyListModel = new DefaultComboBoxModel();
		resultHistory = new JComboBox(historyListModel);
		resultHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doHistoryChanged();
			}
		});
		JLabel resultHistoryLabel = new JLabel("History");
		
		clearHistory = new JButton("Clear");
		clearHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doClearHistory();
			}
		});
		
		addressRW = new JComboBox();
		addressRW.setEditable(true);
		addressRW.getEditor().getEditorComponent().addKeyListener(NONEMPTY_KEY_LISTENER);
		JLabel addressLabel = new JLabel("Address:");
		valueRW = new JComboBox();
		valueRW.setEditable(true);
		valueRW.getEditor().getEditorComponent().addKeyListener(NONEMPTY_KEY_LISTENER);
		JLabel valueLabel = new JLabel("Value:");
		readButton = new JButton("Read");
		readButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doRead();
			}
		});
		writeButton = new JButton("Write");
		writeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doWrite();
			}
		});
		clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doClearRW();
			}
		});
		JLabel holdTypeLabel = new JLabel("Hold value:");
		holdType = new JComboBox(new String[] { "Exactly", "At least", "At most" });
		holdButton = new JButton("Hold");
		holdButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doHold();
			}
		});
		//JSeparator sep1 = new JSeparator(JSeparator.HORIZONTAL);
		statusLine = new JLabel();
		statusLine.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		limitCount = new JTextField(10);
		limitCount.setText("65536");
		limitCount.setHorizontalAlignment(JTextField.TRAILING);
		limitCount.addKeyListener(NONEMPTY_KEY_LISTENER);
		JLabel limitLabel = new JLabel("Result limit:");
		
		memoryLabel = new JLabel();
		memoryRefresh = new JButton("Refresh");
		memoryRefresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSelectProcess();
			}
		});
		holdListLabel = new JLabel();
		holdListModel = new DefaultListModel();
		holdList = new JList(holdListModel);
		holdList.setFont(resultList.getFont());
		JScrollPane holdListScroll = new JScrollPane(holdList);
		holdList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybePopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				maybePopup(e);
			}
			/** Perform popup if this is a popup event. */
			private void maybePopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					// get the item index under the mouse
					int idx = holdList.locationToIndex(e.getPoint());
					if (idx >= 0) {
						if (!holdList.isSelectedIndex(idx)) {
							holdList.setSelectedIndices(new int[0]);
							holdList.setSelectedIndex(idx);
						}
					}
					holdMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					doHoldUse();
				}
			}
		});
		setNumberOfHolds(0);
		
		cycleTime = new JTextField(8);
		cycleTime.setText("10");
		JLabel cycleTimeLabel = new JLabel("Cycle time ");
		JLabel cycleUOMLabel = new JLabel(" x 10 ms");
		
		holdThread = new Thread(new Runnable() {
			@Override
			public void run() {
				doHoldLoop();
			}
		});
		holdThread.start();
		
		setEnableControls(false);
		processBox.setEnabled(true);
		//----------------------------------------------------------------------------------------------
		// set up layout
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(processLabel)
				.addComponent(processBox)
				.addComponent(processRefresh)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup(Alignment.LEADING, false)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(memoryLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(memoryRefresh)
					)
					.addComponent(memoryListScroll, 180, 180, 180)
				)
				.addGroup(
					gl.createParallelGroup()
					.addGroup(
						gl.createSequentialGroup()
						.addGroup(
							gl.createParallelGroup()
							.addComponent(addressStartLabel)
							.addComponent(valueStartLabel)
							.addComponent(valueTypeLabel)
						)
						.addGroup(
							gl.createParallelGroup()
							.addComponent(addressStart)
							.addComponent(valueStart)
							.addComponent(valueType)
						)
						.addGroup(
							gl.createParallelGroup()
							.addComponent(addressEndLabel)
							.addComponent(valueEndLabel)
							.addComponent(limitLabel)
						)
						.addGroup(
							gl.createParallelGroup()
							.addComponent(addressEnd)
							.addComponent(valueEnd)
							.addComponent(limitCount)
						)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addGroup(
							gl.createParallelGroup()
							.addGroup(
								gl.createSequentialGroup()
								.addComponent(findNewButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(findButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(filterButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							)
							.addGroup(
								gl.createSequentialGroup()
								.addComponent(resultHistoryLabel)
								.addComponent(resultHistory)
								.addComponent(clearHistory)
							)
						)
						.addComponent(filterPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addGroup(
							gl.createParallelGroup()
							.addGroup(
								gl.createSequentialGroup()
								.addComponent(resultListLabel)
								.addComponent(progress)
								.addComponent(progressLabel)
							)
							.addComponent(resultListScroll)
						)
						.addGroup(
							gl.createParallelGroup()
							.addComponent(holdListLabel)
							.addComponent(holdListScroll)
						)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(addressLabel)
						.addComponent(addressRW)
						.addComponent(valueLabel)
						.addComponent(valueRW)
						.addComponent(readButton)
						.addComponent(writeButton)
						.addComponent(clearButton)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(holdTypeLabel)
						.addComponent(holdType)
						.addComponent(holdButton)
						.addComponent(cycleTimeLabel)
						.addComponent(cycleTime)
						.addComponent(cycleUOMLabel)
					)
				)
			)
			.addComponent(statusLine, 0, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(processLabel)
				.addComponent(processBox)
				.addComponent(processRefresh)
			)
			.addGroup(
				gl.createParallelGroup()
				.addGroup(
					gl.createSequentialGroup()
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(memoryLabel)
						.addComponent(memoryRefresh)
					)
					.addComponent(memoryListScroll)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(addressStartLabel)
						.addComponent(addressStart)
						.addComponent(addressEndLabel)
						.addComponent(addressEnd)
					)
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(valueStartLabel)
						.addComponent(valueStart)
						.addComponent(valueEndLabel)
						.addComponent(valueEnd)
					)
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(valueTypeLabel)
						.addComponent(valueType)
						.addComponent(limitLabel)
						.addComponent(limitCount)
					)
					.addGroup(
						gl.createParallelGroup(Alignment.CENTER)
						.addGroup(
							gl.createSequentialGroup()
							.addGroup(
								gl.createParallelGroup(Alignment.BASELINE)
								.addComponent(findNewButton)
								.addComponent(findButton)
								.addComponent(filterButton)
							)
							.addGroup(
								gl.createParallelGroup(Alignment.BASELINE)
								.addComponent(resultHistoryLabel)
								.addComponent(resultHistory)
								.addComponent(clearHistory)
							)
						)
						.addComponent(filterPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(
						gl.createParallelGroup(Alignment.CENTER)
						.addComponent(resultListLabel)
						.addComponent(progress)
						.addComponent(progressLabel)
						.addComponent(holdListLabel)
					)
					.addGroup(
						gl.createParallelGroup()
						.addComponent(resultListScroll)
						.addComponent(holdListScroll)
					)
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(addressLabel)
						.addComponent(addressRW)
						.addComponent(valueLabel)
						.addComponent(valueRW)
						.addComponent(readButton)
						.addComponent(writeButton)
						.addComponent(clearButton)
					)
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(holdTypeLabel)
						.addComponent(holdType)
						.addComponent(holdButton)
						.addComponent(cycleTimeLabel)
						.addComponent(cycleTime)
						.addComponent(cycleUOMLabel)
					)
				)
			)
			.addComponent(statusLine)
		);
		gl.linkSize(SwingConstants.HORIZONTAL, findNewButton, findButton, filterButton);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				refreshProcesses();
				pack();
				setLocationRelativeTo(null);
				initConfig();
				setVisible(true);
			}
		});
	}
	/** Sets the status line value to the last error. */
	private void statusLineToLastError() {
		int i = Kernel32.INSTANCE.GetLastError();
		String s = WinAPIHelper.getLastErrorStr(i);
		statusLine.setText(String.format("%d - %s", i, s));
	}
	/** An key listener for emptyable text field. */
	private final KeyListener EMPTY_KEY_LISTENER = new KeyListener() {
		@Override
		public void keyPressed(KeyEvent e) {
			
		}
		@Override
		public void keyReleased(KeyEvent e) {
			colorField(e.getComponent(), true);
		}
		@Override
		public void keyTyped(KeyEvent e) {
			
		}
	};
	/** An key listener for non emptyable text field. */
	private final KeyListener NONEMPTY_KEY_LISTENER = new KeyListener() {
		@Override
		public void keyPressed(KeyEvent e) {
			
		}
		@Override
		public void keyReleased(KeyEvent e) {
			colorField(e.getComponent(), false);
		}
		@Override
		public void keyTyped(KeyEvent e) {
			
		}
	};
	/**
	 * Tests the field of valid hexadecimal number or valid double value.
	 * @param field the field to test.
	 * @param allowEmpty allow the field to be empty?
	 */
	private void colorField(Component field, boolean allowEmpty) {
		String s = "";
		if (field instanceof JTextComponent) {
			s = ((JTextComponent)field).getText();
		}
		if (s == null || s.length() == 0) {
			if (allowEmpty) {
				field.setBackground(textFieldColor);
			} else {
				field.setBackground(Color.YELLOW);
			}
		} else {
			boolean dollar = s.startsWith("$");
			boolean zerox = s.startsWith("0x") || s.startsWith("0X");
			if (dollar || zerox) {
				try {
					Integer.parseInt(s.substring(dollar ? 1 : 2), 16);
					field.setBackground(textFieldColor);
				} catch (NumberFormatException ex) {
					field.setBackground(Color.YELLOW);
				}
			} else {
				try {
					Double.parseDouble(s.replace(',', '.'));
					field.setBackground(textFieldColor);
				} catch (NumberFormatException ex) {
					field.setBackground(Color.YELLOW);
				}
			}
		}
	}
	/**
	 * Action for process selection change.
	 */
	private void doSelectProcess() {
		DefaultListModel mdl = (DefaultListModel)memoryList.getModel();
		int idx = processBox.getSelectedIndex();
		currentMemory.clear();
		long sum = 0;
		if (idx >= 0) {
			ProcessData pd = processList.get(idx);
			currentMemory.addAll(WinAPIHelper.queryProcessMemory(pd));
			for (MemoryBasicInformation mi : currentMemory) {
				mdl.addElement(String.format("%08X (%d kB)", mi.BaseAddress, mi.RegionSize));
				sum += mi.RegionSize;
			}
			setEnableControls(true);
		} else {
			mdl.clear();
		}
		memoryLabel.setText((sum / 1024 / 1024) + " MB");
		statusLineToLastError();
	}
//	/**
//	 * Returns true if the given text component does not contain any value.
//	 * @param c the text component to test
//	 * @return true if the given text component does not contain any value.
//	 */
//	private boolean isEmpty(JTextComponent c) {
//		return c.getText() == null || c.getText().length() == 0;
//	}
	/**
	 * Perform the closing operation.
	 */
	private void doWindowClosing() {
		if (valueFinder != null) {
			valueFinder.cancel(true);
		}
		if (valueDiffFinder != null) {
			valueDiffFinder.cancel(true);
		}
		holdThread.interrupt();
		doneConfig();
		dispose();
	}
	/**
	 * Saves or loads the values for various fields annotated with savevalue.
	 * @param save if true the values are saved
	 * @param p the properties to load/save from
	 */
	private void saveLoadValues(boolean save, Properties p) {
		Class<?> clazz = this.getClass();
		for (Field f : clazz.getDeclaredFields()) {
			SaveValue a = f.getAnnotation(SaveValue.class);
			if (a != null) {
				try {
					Object o = f.get(this);
					if (o instanceof JTextField) {
						JTextField v = (JTextField)o;
						if (save) {
							String s = v.getText();
							p.setProperty(f.getName(), s != null ? s : "");
						} else {
							v.setText(p.getProperty(f.getName()));
							for (KeyListener kl : v.getKeyListeners()) {
								if (kl == NONEMPTY_KEY_LISTENER || kl == EMPTY_KEY_LISTENER) {
									kl.keyReleased(new KeyEvent(v, 0, 0, 0, 0, '\0'));
								}
							}
						}
					} else
					if (o instanceof JComboBox) {
						JComboBox v = (JComboBox)o;
						if (save) {
							if (v.isEditable()) {
								p.setProperty(f.getName(), v.getSelectedItem() != null ? v.getSelectedItem().toString() : "");
							} else {
								p.setProperty(f.getName(), Integer.toString(v.getSelectedIndex()));
							}
						} else {
							String s = p.getProperty(f.getName());
							if (v.isEditable()) {
								v.setSelectedItem(s);
								for (KeyListener kl : v.getEditor().getEditorComponent().getKeyListeners()) {
									if (kl == NONEMPTY_KEY_LISTENER || kl == EMPTY_KEY_LISTENER) {
										kl.keyReleased(new KeyEvent(v.getEditor().getEditorComponent(), 0, 0, 0, 0, '\0'));
									}
								}
							} else {
								v.setSelectedIndex(s != null && s.length() > 0 ? Integer.parseInt(s) : -1);
							}
						}
					} else
					if (o instanceof JRadioButton) {
						JRadioButton v = (JRadioButton)o;
						if (save) {
							p.setProperty(f.getName(), v.isSelected() ? "true" : "false");
						} else {
							v.setSelected("true".equals(p.getProperty(f.getName())));
						}
					}
					
				} catch (NumberFormatException ex) {
					// ignored
				} catch (IllegalArgumentException ex) {
					// ignored
				} catch (IllegalAccessException ex) {
					// ignored
				}
			}
		}
	}
	/** Initialize the window based on the configuration file. */
	private void initConfig() {
		try {
			FileInputStream in = new FileInputStream("config.xml");
			try {
				Properties p = new Properties();
				p.loadFromXML(in);
				// set window statuses
				String winstat = p.getProperty("WindowStatus");
				setExtendedState(Integer.parseInt(winstat));
				if (getExtendedState() == JFrame.NORMAL) {
					Rectangle rect = new Rectangle();
					rect.x = Integer.parseInt(p.getProperty("X"));
					rect.y = Integer.parseInt(p.getProperty("Y"));
					rect.width = Integer.parseInt(p.getProperty("Width"));
					rect.height = Integer.parseInt(p.getProperty("Height"));
					setBounds(rect);
				}
				saveLoadValues(false, p);
			} finally {
				in.close();
			}
		} catch (IOException ex) {
			// ignore
		}
	}
	/** Save the window state to configuration file. */
	private void doneConfig() {
		try {
			Properties p = new Properties();
			p.setProperty("WindowStatus", Integer.toString(getExtendedState()));
			Rectangle rect = getBounds();
			p.setProperty("X", Integer.toString(rect.x));
			p.setProperty("Y", Integer.toString(rect.y));
			p.setProperty("Width", Integer.toString(rect.width));
			p.setProperty("Height", Integer.toString(rect.height));
			FileOutputStream out = new FileOutputStream("config.xml");
			saveLoadValues(true, p);
			try {
				p.storeToXML(out, "");
			} finally {
				out.close();
			}
		} catch (IOException ex) {
			
		}
	}
	/** Refreshes the list of processes. */
	private void refreshProcesses() {
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
	/**
	 * Tries to find the specified bytes of value in the buffer. If wrap is non-null and the
	 * wrap array is one less longer than the value array, an automatic data wrap is performed as if
	 * the wrap precedes the actual buffer
	 * @param buffer the buffer to search in
	 * @param end the length of the search from index 0, must be less than buffer.length
	 * @param value the value to find
	 * @param wrap if not null, the search is performed on the wrap + buffer virtual array and
	 * @param firstTime if true the wrapping is not performed (i.e. first check of a multi-buffer find)
	 * after the search is completed, the last bytes of the buffer is copied into the wrap array.
	 * @return the list if offsets, where the value is found, if the value is found starting from the wrap buffer,
	 * a negative index will show the relative starting position from the buffer
	 */
	public static List<Integer> findBytes(byte[] buffer, int len, byte[] value, byte[] wrap, boolean firstTime) {
		List<Integer> result = Lists.newArrayList();
		// the easy case, find values within the buffer only
		if (wrap != null && !firstTime) {
			// create a small combined buffer from wrap and the beginning of buffer
			byte[] cb = new byte[2 * wrap.length];
			System.arraycopy(wrap, 0, cb, 0, wrap.length);
			System.arraycopy(buffer, 0, cb, wrap.length, wrap.length);
			findBytes(cb, 0, cb.length, value, result, -wrap.length);
		}
		findBytes(buffer, 0, len, value, result, 0);
		// memorize the current buffer's last part
		if (wrap != null) {
			System.arraycopy(buffer, len - wrap.length - 1, wrap, 0, wrap.length);
		}
		return result;
	}
	/** Find the addresses which are have value in the specified range. */
	public static Map<Integer, byte[]> findByteRange(byte[] buffer, int len, byte[] valueStart, byte[] valueEnd, byte[] wrap, boolean firstTime) {
		Map<Integer, byte[]> result = Maps.newLinkedHashMap();
		if (wrap != null && !firstTime) {
			// create a small combined buffer from wrap and the beginning of buffer
			byte[] cb = new byte[2 * wrap.length];
			System.arraycopy(wrap, 0, cb, 0, wrap.length);
			System.arraycopy(buffer, 0, cb, wrap.length, wrap.length);
			findBytesRange(cb, 0, cb.length, valueStart, valueEnd, result, -wrap.length);
		}
		findBytesRange(buffer, 0, len, valueStart, valueEnd, result, 0);
		// memorize the current buffer's last part
		if (wrap != null) {
			System.arraycopy(buffer, len - wrap.length - 1, wrap, 0, wrap.length);
		}
		return result;
	}
	/** Find addresses of values in specified range. */
	private static void findBytesRange(byte[] buffer, int start, int len, byte[] valueStart, byte[] valueEnd, Map<Integer, byte[]> result, int bias) {
		outerloop:
			for (int i = start, count = len - valueStart.length + 1; i < count; i++) {
				for (int j = valueStart.length - 1; j >= 0; j--) {
					int b = buffer[i + j] & 0xFF;
					int v1 = valueStart[j] & 0xFF;
					int v2 = valueEnd[j] &0xFF;
					if (b < v1 || b > v2) {
						continue outerloop;
					}
				}
				byte[] value = new byte[valueStart.length];
				System.arraycopy(buffer, i, value, 0, value.length);
				result.put(i + bias, value);
			}
	}
	/**
	 * Find the specified value in the given buffer from the given starting position.
	 * THe multiple results are biased by the given value (i.e. address correction)
	 * @param buffer the buffer
	 * @param start the start index
	 * @param len the length of the searchable area
	 * @param value the value to find
	 * @param result the list of offsets
	 * @param bias the offset bias
	 */
	private static void findBytes(byte[] buffer, int start, int len, byte[] value, List<Integer> result, int bias) {
		outerloop:
		for (int i = start, count = len - value.length + 1; i < count; i++) {
			for (int j = 0; j < value.length; j++) {
				if (buffer[i + j] != value[j]) {
					continue outerloop;
				}
			}
			result.add(i + bias);
		}
	}
	/** Worker task to find values. */
	private class ValueFinder extends SwingWorker<Void, int[]> {
		/** The read buffer size. */
		private static final int BUFFER_SIZE = 64 * 1024;
		/** Start memory address. */
		private final int startAddress;
		/** End memory address. */
		private final int endAddress;
		/** The process identifier. */
		private final int processId;
		/** The value to find. */
		private final byte[] value;
		/** The optional end range value. */
		private final byte[] endValue;
		/** The value type. */
		private final int valueType;
		/** The layout of the memory to search in. */
		private final List<MemoryBasicInformation> memInfo = Lists.newArrayList();
		/** The allowed number of findings. */
		private final int limit;
		/** The found addresses. */
		private final List<ValueFound> currentValues = Lists.newArrayList();
		/**
		 * Constructor. Sets the private fields.
		 * @param startAddress
		 * @param endAddress
		 * @param processId
		 * @param value
		 * @param endValue
		 * @param limit
		 * @param memInfo
		 * @param valueType
		 */
		public ValueFinder(int startAddress, int endAddress, int processId, 
				byte[] value, byte[] endValue, int limit, List<MemoryBasicInformation> memInfo, int valueType) {
			this.startAddress = startAddress;
			this.endAddress = endAddress;
			this.processId = processId;
			this.value = value.clone();
			this.endValue = endValue != null ? endValue.clone() : null;
			this.limit = limit;
			this.memInfo.addAll(memInfo);
			this.valueType = valueType;
		}
		@Override
		protected Void doInBackground() throws Exception {
			Kernel32 k32 = Kernel32.INSTANCE;
			int hProcess = k32.OpenProcess(Kernel32.PROCESS_VM_READ, false, processId);
			if (hProcess != 0) {
				byte[] buffer = new byte[BUFFER_SIZE];
				byte[] wrap = new byte[value.length - 1];
				int found = 0;
				for (MemoryBasicInformation mi : memInfo) {
					if (isCancelled() || found >= limit) {
						break;
					}
					if (mi.BaseAddress >= startAddress && mi.BaseAddress <= endAddress) {
						int remainingSize = mi.RegionSize;
						int readOffset = 0;
						IntByReference bytesRead = new IntByReference();
						while (remainingSize > 0 && !isCancelled() && found < limit) {
							publish(new int[] { mi.BaseAddress + readOffset, found });
							int readSize = remainingSize > buffer.length ? buffer.length : remainingSize;
							if (k32.ReadProcessMemory(hProcess, mi.BaseAddress + readOffset, buffer, readSize, bytesRead)) {
								if (endValue == null) {
									for (Integer i : findBytes(buffer, bytesRead.getValue(), value, wrap, readOffset == 0)) {
										if (isCancelled() || found >= limit) {
											break;
										}
										ValueFound vf = new ValueFound();
										vf.valueType = valueType;
										vf.address = mi.BaseAddress + readOffset + i;
										vf.value = value.clone();
										currentValues.add(vf);
										found++;
										publish(new int[] { mi.BaseAddress + readOffset + i, found });
									}
								} else {
									for (Map.Entry<Integer, byte[]> e : findByteRange(buffer, bytesRead.getValue(), value, endValue, wrap, readOffset == 0).entrySet()) {
										if (isCancelled() || found >= limit) {
											break;
										}
										ValueFound vf = new ValueFound();
										vf.valueType = valueType;
										vf.address = mi.BaseAddress + readOffset + e.getKey();
										vf.value = e.getValue();
										currentValues.add(vf);
										found++;
										publish(new int[] { mi.BaseAddress + readOffset + e.getKey(), found });
									}
								}
								readOffset += bytesRead.getValue();
								remainingSize -= bytesRead.getValue();
							} else {
								remainingSize = 0;
							}
						}
					}
				}
				k32.CloseHandle(hProcess);
			}
			return null;
		}
		@Override
		protected void done() {
			statusLineToLastError();
			progress.setValue(0);
			findNewButton.setText("Find new");
			resultListModel.clear();
			for (ValueFound vf : currentValues) {
				resultListModel.addElement(vf);
			}
			setNumberOfResults(resultListModel.size());
			valueFinder = null;
			if (resultListModel.size() > 0) {
				addToHistory();
			}
			setEnableControls(true);
		}
		@Override
		protected void process(List<int[]> chunks) {
			for (int[] sp : chunks) {
				progress.setValue((int)(sp[0] * 1000L / (endAddress - startAddress)));
				progressLabel.setText(String.format("%d MB", sp[0] / 1024 / 1024));
				setNumberOfResults(sp[1]);
			}
		}
	}
	/** Converts the contents of the given text component into an array of native byte representation. */
	private byte[] getValueBytes(String s, int type) {
		byte[] result = null;
		if (s == null) {
			return new byte[0];
		}
		int radix = 10;
		if (s.startsWith("$")) {
			s = s.substring(1);
			radix = 16;
		} else
		if (s.startsWith("0x") || s.startsWith("0X")) {
			s = s.substring(2);
			radix = 16;
		}
		switch (valueType.getSelectedIndex()) {
		// BYTE
		case 0:
			result = new byte[1];
			try {
				int val = Integer.parseInt(s, radix);
				result[0] = (byte)(val & 0xFF);
			} catch (NumberFormatException ex) {
				// ignored
			}
			break;
		// SHORT
		case 1:
			try {
				int val = Integer.parseInt(s, radix);
				result = new byte[2];
				result[0] = (byte)(val & 0xFF);
				result[1] = (byte)((val >> 8) & 0xFF);
			} catch (NumberFormatException ex) {
				// ignored
			}
			break;
		// INT
		case 2:
			try {
				long val = Long.parseLong(s, radix);
				result = new byte[4];
				result[0] = (byte)(val & 0xFFL);
				result[1] = (byte)((val >> 8) & 0xFFL);
				result[2] = (byte)((val >> 16) & 0xFFL);
				result[3] = (byte)((val >> 24) & 0xFFL);
			} catch (NumberFormatException ex) {
				
			}
			break;
		// LONG
		case 3:
			try {
				BigInteger val = new BigInteger(s, radix);
				result = new byte[8];
				byte[] vb = val.toByteArray();
				int start = vb.length < 8 ? 0 : vb.length - 8;
				int len = vb.length > 8 ? 8 : vb.length;
				System.arraycopy(vb, start, result, 0, len);
				reverse(result);
			} catch (NumberFormatException ex) {
				
			}
			break;
		// FLOAT
		case 4:
			try {
				int val = Float.floatToRawIntBits(Float.parseFloat(s));
				result = new byte[4];
				result[0] = (byte)(val & 0xFFL);
				result[1] = (byte)((val >> 8) & 0xFFL);
				result[2] = (byte)((val >> 16) & 0xFFL);
				result[3] = (byte)((val >> 24) & 0xFFL);
			} catch (NumberFormatException ex) {
				
			}
			break;
		// DOUBLE
		case 5:
			long val = Double.doubleToRawLongBits(Double.parseDouble(s));
			result = new byte[8];
			result[0] = (byte)(val & 0xFFL);
			result[1] = (byte)((val >> 8) & 0xFFL);
			result[2] = (byte)((val >> 16) & 0xFFL);
			result[3] = (byte)((val >> 24) & 0xFFL);
			result[4] = (byte)((val >> 32) & 0xFFL);
			result[5] = (byte)((val >> 40) & 0xFFL);
			result[6] = (byte)((val >> 48) & 0xFFL);
			result[7] = (byte)((val >> 56) & 0xFFL);
			break;
		}
		return result;
	}
	/**
	 * Reverse the array.
	 * @param a
	 */
	private static void reverse(byte[] a) {
		int left = 0;
		int right = a.length - 1;
		while (left < right) {
			byte tmp = a[left];
			a[left] = a[right];
			a[right] = tmp;
			left++;
			right--;
		}
	}
	private void doFindNew() {
		if (valueFinder != null) {
			valueFinder.cancel(true);
			valueFinder = null;
			findNewButton.setText("Find new");
			setEnableControls(true);
			return;
		}
		if (processBox.getSelectedIndex() < 0) {
			return;
		}
		setEnableControls(false);
		findNewButton.setText("Stop");
		findNewButton.setEnabled(true);
		resultListModel.clear();
		byte[] valueStartBytes = null;
		if (valueStart.getBackground() == textFieldColor) {
			valueStartBytes = getValueBytes(valueStart.getText(), valueType.getSelectedIndex());
			byte[] valueEndBytes = null;
			if (valueEnd.getBackground() == textFieldColor && !"".equals(valueEnd.getText())) {
				valueEndBytes = getValueBytes(valueEnd.getText(), valueType.getSelectedIndex());
			}
			int startAddressValue = 0;
			String s = addressStart.getText();
			if (s == null || s.isEmpty() || addressStart.getBackground() != textFieldColor) {
				startAddressValue = currentMemory.get(0).BaseAddress;
			} else {
				startAddressValue = (int)getValue(s);
			}
			int endAddressValue = 0;
			s = addressEnd.getText();
			if (s == null || s.isEmpty() || addressEnd.getBackground() != textFieldColor) {
				endAddressValue = currentMemory.get(currentMemory.size() - 1).BaseAddress;
			} else {
				endAddressValue = (int)getValue(s);
			}
			s = limitCount.getText();
			int limit = 65536;
			if (s == null || s.isEmpty() || limitCount.getBackground() != textFieldColor) {
				limit = (int)getValue(s);
			}
			int processId = processList.get(processBox.getSelectedIndex()).ProcessID;
			valueFinder = new ValueFinder(startAddressValue, endAddressValue, processId, 
					valueStartBytes, valueEndBytes, limit, currentMemory, valueType.getSelectedIndex());
			valueFinder.execute();
		}
	}
	/**
	 * Convert a string containing a decimal or hexadecimal value into a long.
	 * @param s
	 * @return
	 */
	private static long getValue(String s) {
		long result = 0;
		if (s != null) {
			if (s.startsWith("$")) {
				s = s.substring(1);
				result = Long.parseLong(s, 16);
			} else
			if (s.startsWith("0x") || s.startsWith("0X")) {
				s = s.substring(2);
				result = Long.parseLong(s, 16);
			} else {
				result = Long.parseLong(s);
			}
		}
		return result;
	}
	/** History combobox changed. */
	private void doHistoryChanged() {
		if (resultHistory.getSelectedIndex() >= 0) {
			HistoryListEntry e = (HistoryListEntry)resultHistory.getSelectedItem();
			resultListModel.setSize(e.values.size());
			for (int i = 0; i < e.values.size(); i++) {
				resultListModel.set(i, e.values.get(i));
			}
			setNumberOfResults(e.values.size());
		}
	}
	/** Enable or disable controls which are marked as to be disabled when searching. */
	private void setEnableControls(boolean enable) {
		for (Field f : getClass().getDeclaredFields()) {
			DisableOnFind a = f.getAnnotation(DisableOnFind.class);
			if (a != null) {
				try {
					Object o = f.get(this);
					if (o instanceof Component) {
						((Component)o).setEnabled(enable);
					}
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}
		}
	}
	/** Sets the result list label. */
	private void setNumberOfResults(int count) {
		resultListLabel.setText("Number of items found: " + count);
	}
	/** Sets the result list label. */
	private void setNumberOfHolds(int count) {
		holdListLabel.setText("Number of items: " + count);
	}
	/** Action for Clear history button. */
	private void doClearHistory() {
		historyListModel.removeAllElements();
		resultListModel.clear();
	}
	/**
	 * Converts the given bytes of values to a number using the value type.
	 * @param value
	 * @param valueType
	 * @return
	 */
	private static Object foundValueToString(byte[] value, int valueType) {
		byte[] val = value.clone();
		reverse(val);
		switch (valueType) {
		// BYTE
		case 0:
			return value[0];
		// SHORT
		case 1:
			return new BigInteger(val).shortValue();
		// INT
		case 2:
			return new BigInteger(val).intValue();
		// LONG
		case 3:
			return new BigInteger(val).longValue();
		// FLOAT
		case 4:
			int v = (val[0] << 24) | (val[1] << 16) | (val[2] << 8) | val[3];
			return Float.intBitsToFloat(v);
		// DOUBLE
		case 5:
			long l = (val[0] << 56) | (val[1] << 48) | (val[2] << 40) | (val[3] << 32)
				| (val[4] << 24) | (val[5] << 16) | (val[6] << 16) | val[7];
			return Double.longBitsToDouble(l);
		}
		return Arrays.toString(value);
	}
	/** Compare two arrays. */
	public static int compare(byte[] value1, byte[] value2) {
		int len = value1.length > value2.length ? value2.length : value1.length;
		for (int i = len - 1; i >= 0; i--) {
			int v1 = value1[i] & 0xFF;
			int v2 = value2[i] & 0xFF;
			if (v1 > v2) {
				return 1;
			} else
			if (v1 < v2) {
				return -1;
			}
		}
		return 0;
	}
	private void doFind() {
		setEnableControls(false);
		try {
			byte[] val1 = getValueBytes(valueStart.getText(), valueType.getSelectedIndex());
			byte[] val2 = getValueBytes(valueEnd.getText(), valueType.getSelectedIndex());
			for (int i = resultListModel.size() - 1; i >= 0; i--) {
				ValueFound vf = (ValueFound)resultListModel.get(i);
				if (val2 != null) {
					if (compare(val1, vf.value) > 0 || compare(vf.value, val2) > 0) {
						resultListModel.remove(i);
					}
				} else {
					if (compare(vf.value, val1) != 0) {
						resultListModel.remove(i);
					}
				}
			}
			addToHistory();
		} finally {
			setEnableControls(true);
		}
	}
	/** Perform the difference checking. */
	public class ValueDiffFinder extends SwingWorker<Void, Integer> {
		/** The process id. */
		private final int processId;
		/** The change to check for. */
		private final int diffMode;
		/** The map of current values. */
		private final List<ValueFound> currentValues = Lists.newArrayList();
		/** The original size. */
		private final int originalSize;
		/** Constructor. */
		public ValueDiffFinder(int processId, int diffMode, Enumeration<?> values) {
			this.processId = processId;
			this.diffMode = diffMode;
			int i = 0;
			while (values.hasMoreElements()) {
				currentValues.add(new ValueFound((ValueFound)values.nextElement()));
				i++;
			}
			originalSize = i;
		}
		@Override
		protected Void doInBackground() throws Exception {
			Kernel32 k32 = Kernel32.INSTANCE;
			int hProcess = k32.OpenProcess(Kernel32.PROCESS_VM_READ, false, processId);
			if (hProcess != 0) {
				for (int i = currentValues.size() - 1; i >= 0; i--) {
					if (isCancelled()) {
						break;
					}
					ValueFound e = currentValues.get(i);
					byte[] oldValue = e.value;
					byte[] newValue = new byte[oldValue.length];
					IntByReference bread = new IntByReference();
					if (k32.ReadProcessMemory(hProcess, e.address, newValue, newValue.length, bread)) {
						switch (diffMode) {
						// CHANGED
						case 0:
							if (compare(oldValue, newValue) == 0) {
								currentValues.remove(i);
							} else {
								e.prevValue = e.value.clone();
								e.value = newValue;
							}
							break;
						// UNCHANGED
						case 1:
							if (compare(oldValue, newValue) != 0) {
								currentValues.remove(i);
							}
							break;
						// INCREASED
						case 2:
							if (compare(oldValue, newValue) >= 0) {
								currentValues.remove(i);
							} else {
								e.prevValue = e.value.clone();
								e.value = newValue;
							}
							break;
						// DECREASED
						case 3:
							if (compare(oldValue, newValue) <= 0) {
								currentValues.remove(i);
							} else {
								e.prevValue = e.value.clone();
								e.value = newValue;
							}
							break;
						}
					} else {
						currentValues.remove(i);
					}
					publish(i);
				}
				k32.CloseHandle(hProcess);
			}
			publish(0);
			return null;
		}
		@Override
		protected void done() {
			statusLineToLastError();
			resultListModel.clear();
			for (ValueFound vf : currentValues) {
				resultListModel.addElement(vf);
			}
			addToHistory();
			valueDiffFinder = null;
			filterButton.setText("Filter");
			setEnableControls(true);
		}
		@Override
		protected void process(List<Integer> chunks) {
			if (originalSize > 0) {
				for (int i : chunks) {
					progress.setValue((int)(i * 1000L / originalSize));
				}
			} else {
				progress.setValue(0);
			}
		}
	}
	/**
	 * Add current findings to the history.
	 */
	private void addToHistory() {
		HistoryListEntry e = new HistoryListEntry();
		e.index = historyListModel.getSize() + 1;
		for (int i = 0; i < resultListModel.size(); i++) {
			e.values.add((ValueFound)resultListModel.get(i));
		}
		historyListModel.addElement(e);
		resultHistory.setSelectedIndex(historyListModel.getSize() -1);
	}
	/** Perform the filtering operation. */
	private void doFilter() {
		if (valueDiffFinder != null) {
			valueDiffFinder.cancel(true);
			valueDiffFinder = null;
			filterButton.setText("Filter");
			setEnableControls(true);
		}
		setEnableControls(false);
		filterButton.setText("Stop");
		filterButton.setEnabled(true);
		Enumeration<AbstractButton> e = filterGroup.getElements();
		int diffMode = -1;
		while (e.hasMoreElements()) {
			diffMode++;
			if (e.nextElement().isSelected()) {
				break;
			}
		}
		valueDiffFinder = new ValueDiffFinder(processList.get(processBox.getSelectedIndex()).ProcessID, diffMode, resultListModel.elements());
		valueDiffFinder.execute();
	}
	/** Do set the address+value field according to the selection in the result list. */
	private void doSetAddress() {
		// save current values into the histories
		ValueFound sel = (ValueFound)resultList.getSelectedValue();
		if (sel != null) {
			historizeCombo(addressRW);
			
			addressRW.setSelectedItem(String.format("$%08X", sel.address));
			for (KeyListener kl : addressRW.getEditor().getEditorComponent().getKeyListeners()) {
				if (kl == NONEMPTY_KEY_LISTENER || kl == EMPTY_KEY_LISTENER) {
					kl.keyReleased(new KeyEvent(addressRW.getEditor().getEditorComponent(), 0, 0, 0, 0, '\0'));
				}
			}
			
			historizeCombo(valueRW);
			valueRW.setSelectedItem(foundValueToString(sel.value, sel.valueType).toString());
			for (KeyListener kl : valueRW.getEditor().getEditorComponent().getKeyListeners()) {
				if (kl == NONEMPTY_KEY_LISTENER || kl == EMPTY_KEY_LISTENER) {
					kl.keyReleased(new KeyEvent(valueRW.getEditor().getEditorComponent(), 0, 0, 0, 0, '\0'));
				}
			}
		}
	}
	/**
	 * Historize the current value of the combobox.
	 */
	private void historizeCombo(JComboBox cb) {
		Object o = cb.getSelectedItem();
		if (o != null && !"".equals(o)) {
			int found = -1;
			for (int i = 0; i < cb.getItemCount(); i++) {
				if (o.equals(cb.getItemAt(i))) {
					found = i;
					break;
				}
			}
			if (found < 0) {
				cb.addItem(o);
			} else {
				cb.setSelectedIndex(found);
			}
		}
	}
	/** Perform read. */
	private void doRead() {
		if (addressRW.getSelectedItem() != null) {
			historizeCombo(addressRW);
			int address = (int)getValue(addressRW.getSelectedItem().toString());
			byte[] value = readMemoryValue(address);
			if (value.length > 0) {
				valueRW.setSelectedItem(foundValueToString(value, valueType.getSelectedIndex()).toString());
				valueRW.getEditor().getEditorComponent().setBackground(new Color(128, 255, 128));
			} else {
				valueRW.setSelectedItem("");
				valueRW.getEditor().getEditorComponent().setBackground(new Color(255, 128, 128));
			}
		}
	}
	/** Read the process memory. */
	private byte[] readMemoryValue(int address) {
		Kernel32 k32 = Kernel32.INSTANCE;
		int hProcess = k32.OpenProcess(Kernel32.PROCESS_VM_READ, false, processList.get(processBox.getSelectedIndex()).ProcessID);
		if (hProcess != 0) {
			byte[] buffer = getValueBytes("0", valueType.getSelectedIndex());
			IntByReference bread = new IntByReference();
			k32.ReadProcessMemory(hProcess, address, buffer, buffer.length, bread);
			statusLineToLastError();
			k32.CloseHandle(hProcess);
			if (bread.getValue() > 0) {
				return buffer;
			}
		} else {
			statusLineToLastError();
		}
		return new byte[0];
	}
	/** Write the contents of the address+value fields. */
	private void doWrite() {
		if (addressRW.getSelectedItem() != null && valueRW.getSelectedItem() != null) {
			int address = (int)getValue(addressRW.getSelectedItem().toString());
			byte[] value = getValueBytes(valueRW.getSelectedItem().toString(), valueType.getSelectedIndex());
			if (writeMemoryValue(address, value)) {
				valueRW.getEditor().getEditorComponent().setBackground(new Color(128, 255, 128));
			} else {
				valueRW.getEditor().getEditorComponent().setBackground(new Color(255, 128, 128));
			}
		}
	}
	/** Read the process memory. */
	private boolean writeMemoryValue(int address, byte[] value) {
		Kernel32 k32 = Kernel32.INSTANCE;
		int hProcess = k32.OpenProcess(Kernel32.PROCESS_VM_OPERATION | Kernel32.PROCESS_VM_WRITE, false, processList.get(processBox.getSelectedIndex()).ProcessID);
		if (hProcess != 0) {
			IntByReference bread = new IntByReference();
			k32.WriteProcessMemory(hProcess, address, value, value.length, bread);
			statusLineToLastError();
			k32.CloseHandle(hProcess);
			if (bread.getValue() > 0) {
				return true;
			}
		} else {
			statusLineToLastError();
		}
		return false;
	}
	/** Re-read selected items. */
	private void doRereadItems() {
		for (int i : resultList.getSelectedIndices()) {
			ValueFound vf = (ValueFound)resultListModel.get(i);
			vf.prevValue = vf.value;
			vf.value = readMemoryValue(vf.address);
			resultListModel.set(i, vf);
		}
	}
	/** Remove selected items from results and historize the remaining. */
	private void doRemoveItems(JList list) {
		int[] idxs = list.getSelectedIndices();
		if (idxs.length > 0) {
			Arrays.sort(idxs);
			DefaultListModel mdl = (DefaultListModel)list.getModel();
			synchronized (mdl) {
				for (int i = idxs.length - 1; i >= 0; i--) {
					mdl.remove(idxs[i]);
				}
			}
		}
	}
	/** Retain only the selected items. */
	private void doRetainItems(JList list) {
		int[] idxs1 = list.getSelectedIndices();
		if (idxs1.length > 0) {
			Arrays.sort(idxs1);
			DefaultListModel mdl = (DefaultListModel)list.getModel();
			synchronized (mdl) {
				int[] idxs = new int[idxs1.length + 2];
				System.arraycopy(idxs1, 0, idxs, 1, idxs1.length);
				idxs[0] = -1;
				idxs[idxs.length - 1] = mdl.size();
				for (int i = idxs.length - 1; i >= 1; i--) {
					for (int j = idxs[i] - 1; j > idxs[i - 1]; j--) {
						mdl.remove(j);
					}
				}
			}
		}
	}
	/** Clear address+value history. */
	private void doClearRW() {
		addressRW.removeAllItems();
		valueRW.removeAllItems();
		addressRW.setSelectedItem("");
		valueRW.setSelectedItem("");
	}
	/** Perform the hold loop. */
	private void doHoldLoop() {
		Kernel32 k32 = Kernel32.INSTANCE;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				break;
			}
			synchronized (holdListModel) {
				for (int i = 0; i < holdListModel.size(); i++) {
					ValueHold vh1 = (ValueHold)holdListModel.getElementAt(i);
					ValueHold vh = new ValueHold(vh1);
					if (vh.enabled && --vh.remainingCycles <= 0) {
						vh.remainingCycles = vh.cycleTime;
						int hProcess = k32.OpenProcess(Kernel32.PROCESS_VM_OPERATION | Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_VM_WRITE, false, vh.processId);
						if (hProcess != 0) {
							IntByReference bread = new IntByReference();
							if (k32.ReadProcessMemory(hProcess, vh.address, vh.value, vh.value.length, bread)) {
								vh.valid = true;
								switch (vh.holdType) {
								// EXACTLY
								case 0:
									if (compare(vh.value, vh.holdValue) != 0) {
										if (!k32.WriteProcessMemory(hProcess, vh.address, vh.holdValue, vh.holdValue.length, bread)) {
											vh.valid = false;
										}
									}
									break;
								// AT LEAST
								case 1:
									if (compare(vh.value, vh.holdValue) < 0) {
										if (!k32.WriteProcessMemory(hProcess, vh.address, vh.holdValue, vh.holdValue.length, bread)) {
											vh.valid = false;
										}
									}
									break;
								// AT MOST
								case 2:
									if (compare(vh.value, vh.holdValue) > 0) {
										if (!k32.WriteProcessMemory(hProcess, vh.address, vh.holdValue, vh.holdValue.length, bread)) {
											vh.valid = false;
										}
									}
									break;
								}
							} else {
								vh.valid = false;
							}
							k32.CloseHandle(hProcess);
						} else {
							vh.valid = false;
						}
					}
					holdListModel.setElementAt(vh, i);
				}
			}
		}
	}
	/** Add current address to hold. */
	private void doHold() {
		if (processBox.getSelectedIndex() >= 0 && valueType.getSelectedIndex() >= 0
				&& holdType.getSelectedIndex() >= 0 && addressRW.getSelectedItem() != null
				&& valueRW.getSelectedItem() != null) {
			ValueHold vh = new ValueHold();
			synchronized (vh) {
				vh.processId = processList.get(processBox.getSelectedIndex()).ProcessID;
				vh.address = (int)getValue(addressRW.getSelectedItem().toString());
				vh.valueType = valueType.getSelectedIndex();
				vh.value = getValueBytes(valueRW.getSelectedItem().toString(), vh.valueType);
				vh.holdValue = vh.value.clone();
				vh.holdType = holdType.getSelectedIndex();
				vh.enabled = true;
				vh.cycleTime = (int)getValue(cycleTime.getText());
				vh.remainingCycles = vh.cycleTime;
			}
			synchronized(holdListModel) {
				holdListModel.addElement(vh);
				 setNumberOfHolds(holdListModel.getSize());
			}
		}
	}
	/** Select object. */
	private void doHoldUse() {
		ValueHold vh = (ValueHold)holdList.getSelectedValue();
		if (vh != null) {
			synchronized (vh) {
				addressRW.setSelectedItem(String.format("$%08X", vh.address));
				valueRW.setSelectedItem(foundValueToString(vh.value, vh.valueType).toString());
			}
		}
	}
	/** Enable items. */
	private void doHoldEnable() {
		int[] idxs = holdList.getSelectedIndices();
		for (int i : idxs) {
			ValueHold vh = (ValueHold)holdListModel.getElementAt(i);
			synchronized (vh) {
				vh.enabled = true;
			}
			holdListModel.setElementAt(vh, i);
		}
	} 
	/**  Disable items. */
	private void doHoldDisable() {
		int[] idxs = holdList.getSelectedIndices();
		for (int i : idxs) {
			ValueHold vh = (ValueHold)holdListModel.getElementAt(i);
			synchronized (vh) {
				vh.enabled = false;
			}
			holdListModel.setElementAt(vh, i);
		}
	} 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Main();
			}
		});

	}

}
