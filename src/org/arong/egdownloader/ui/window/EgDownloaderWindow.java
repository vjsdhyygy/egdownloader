﻿package org.arong.egdownloader.ui.window;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;

import org.arong.egdownloader.db.DbTemplate;
import org.arong.egdownloader.model.Picture;
import org.arong.egdownloader.model.Setting;
import org.arong.egdownloader.model.Task;
import org.arong.egdownloader.model.TaskList;
import org.arong.egdownloader.spider.Proxy;
import org.arong.egdownloader.ui.ComponentConst;
import org.arong.egdownloader.ui.ComponentUtil;
import org.arong.egdownloader.ui.IconManager;
import org.arong.egdownloader.ui.SwingPrintStream;
import org.arong.egdownloader.ui.listener.MenuMouseListener;
import org.arong.egdownloader.ui.listener.MouseAction;
import org.arong.egdownloader.ui.listener.OperaBtnMouseListener;
import org.arong.egdownloader.ui.menuitem.AddTaskGroupMenuItem;
import org.arong.egdownloader.ui.menuitem.ChangeViewMenuItem;
import org.arong.egdownloader.ui.menuitem.ChangeViewSizeMenuItem;
import org.arong.egdownloader.ui.menuitem.ClearConsoleMenuItem;
import org.arong.egdownloader.ui.menuitem.OpenLogMenuItem;
import org.arong.egdownloader.ui.menuitem.OpenRootMenuItem;
import org.arong.egdownloader.ui.menuitem.ReBuildAllTaskMenuItem;
import org.arong.egdownloader.ui.menuitem.ResetMenuItem;
import org.arong.egdownloader.ui.menuitem.SimpleSearchMenuItem;
import org.arong.egdownloader.ui.menuitem.StartAllTaskMenuItem;
import org.arong.egdownloader.ui.menuitem.StopAllTaskMenuItem;
import org.arong.egdownloader.ui.panel.ConsolePanel;
import org.arong.egdownloader.ui.panel.InfoTabbedPane;
import org.arong.egdownloader.ui.panel.PicturesInfoPanel;
import org.arong.egdownloader.ui.panel.TaskImagePanel;
import org.arong.egdownloader.ui.panel.TaskInfoPanel;
import org.arong.egdownloader.ui.popmenu.MainPopupMenu;
import org.arong.egdownloader.ui.swing.AJButton;
import org.arong.egdownloader.ui.swing.AJLabel;
import org.arong.egdownloader.ui.swing.AJMenu;
import org.arong.egdownloader.ui.swing.AJMenuBar;
import org.arong.egdownloader.ui.swing.AJPanel;
import org.arong.egdownloader.ui.table.TaskingTable;
import org.arong.egdownloader.ui.window.form.AddFormDialog;
import org.arong.egdownloader.ui.work.interfaces.IListenerTask;
import org.arong.egdownloader.ui.work.listenerWork.DeleteTaskWork;
import org.arong.egdownloader.ui.work.listenerWork.StartTaskWork;
import org.arong.egdownloader.ui.work.listenerWork.StopTaskWork;
import org.arong.egdownloader.version.Version;
import org.arong.util.FileUtil;

/**
 * 主线程类
 * 
 * @author 阿荣
 * @since 2014-05-21
 */
public class EgDownloaderWindow extends JFrame {

	private static final long serialVersionUID = 8904976570969033245L;
	
	public String wtitle;//窗口标题
	JWindow minTips;
	
	JMenuBar jMenuBar;// 菜单栏
	public InitWindow initWindow;
	public JFrame settingWindow;
	public JDialog aboutWindow;
	public JDialog addFormWindow;
	public JDialog creatingWindow;
	public JDialog detailWindow;
	public JDialog checkingWindow;
	public JDialog coverWindow2;//漫画封面，鼠标点击弹出
	public SearchCoverWindow coverWindow;//漫画封面，鼠标移动出现
	public JDialog editWindow;
	public JDialog deletingWindow;
	public JDialog zipWindow;
	public JDialog resetAllTaskWindow;
	public JDialog simpleSearchWindow;
	public JDialog countWindow;
	public SearchComicWindow searchComicWindow;
	
	public JPopupMenu tablePopupMenu;
	public TaskingTable runningTable;
	public JScrollPane tablePane;
	public InfoTabbedPane infoTabbedPane;
	public ConsolePanel consolePanel;
	public JPanel emptyPanel;
	public TaskImagePanel taskImagePanel;
	public TaskInfoPanel taskInfoPanel;
	
	
	public Setting setting;
	public TaskList<Task> tasks;
	
	public DbTemplate<Task> taskDbTemplate;
	public DbTemplate<Picture> pictureDbTemplate;
	public DbTemplate<Setting> settingDbTemplate;
	
	Timer netSpeedtimer;
	public int viewModel = 1;//1:表格模式2：图片模式 
	private void setupNetSpeedtimer(final EgDownloaderWindow mainWindow){
		//设置下载速度检测定时器
		TimerTask timerTask = new TimerTask() {
			public void run() {
				//当前一秒内的流量
				Long length = FileUtil.byteLength - FileUtil.oldByteLength;
				//显示到标题栏
				mainWindow.setTitle(mainWindow.wtitle + " (" + FileUtil.showSizeStr(length) + "/S)");
				if(FileUtil.byteLength > 999900000){
					FileUtil.byteLength = 0L;
					FileUtil.oldByteLength = 0L;
				}else{
					FileUtil.oldByteLength = FileUtil.byteLength;
				}
			}
		};
		netSpeedtimer = new Timer(true);
		//1秒执行一次
		netSpeedtimer.schedule(timerTask, 1000, 1000);
	}
	public EgDownloaderWindow(InitWindow initWindow, Setting setting, TaskList<Task> tasks, DbTemplate<Task> taskDbTemplate, DbTemplate<Picture> pictureDbTemplate, DbTemplate<Setting> settingDbTemplate) {
		final EgDownloaderWindow mainWindow = this;
		
		this.initWindow = initWindow;
		this.taskDbTemplate = taskDbTemplate;
		this.pictureDbTemplate = pictureDbTemplate;
		this.settingDbTemplate = settingDbTemplate;
		//加载配置数据
		this.setting = setting;
		//设置代理
		Proxy.init(setting.isUseProxy(), setting.getProxyType(), setting.getProxyIp(), setting.getProxyPort(), setting.getProxyUsername(), setting.getProxyPwd());
		//加载任务列表
		this.tasks = tasks == null ? new TaskList<Task>() : tasks;
		// 设置主窗口
		this.wtitle = Version.NAME + "v" + Version.VERSION + " / " + ("".equals(ComponentConst.groupName) ? "默认空间" : ComponentConst.groupName);
		this.setTitle(this.wtitle);
		
		this.setIconImage(IconManager.getIcon("download").getImage());
		this.getContentPane().setLayout(null);
		//this.setSize(ComponentConst.CLIENT_WIDTH, ComponentConst.CLIENT_HEIGHT);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		//this.setMaximumSize(new Dimension(ComponentConst.CLIENT_WIDTH, ComponentConst.CLIENT_HEIGHT));
		this.setResizable(false);
		this.setBackground(Color.WHITE);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Component newTaskMenu = null;// 菜单：新建
		Component startTasksMenu = null;// 菜单：开始
		Component stopTasksMenu = null;// 菜单：暂停
		Component deleteTasksMenu = null;// 菜单：删除
		Component searchComicMenu = null;//菜单：搜索
		Component settingMenu = null;// 菜单：设置
		Component countMenu = null;// 菜单：统计
		Component aboutMenu = null;// 菜单：关于
		OperaBtnMouseListener newTaskBtnListener = new OperaBtnMouseListener(this, MouseAction.CLICK,
				new IListenerTask() {
			public void doWork(Window mainWindow, MouseEvent e) {
				EgDownloaderWindow this_ = (EgDownloaderWindow) mainWindow;
				this_.setEnabled(false);
				if(this_.creatingWindow != null && this_.creatingWindow.isVisible()){
					this_.creatingWindow.setVisible(true);
					this_.creatingWindow.toFront();
				}else{
					if (this_.addFormWindow == null) {
						this_.addFormWindow = new AddFormDialog(this_);
					}
					this_.addFormWindow.setVisible(true);
					this_.addFormWindow.toFront();
				}
			}
		});
		OperaBtnMouseListener countBtnListener = new OperaBtnMouseListener(this, MouseAction.CLICK,new IListenerTask() {
			public void doWork(Window window, MouseEvent e) {
				EgDownloaderWindow mainWindow = (EgDownloaderWindow)window;
				if (mainWindow.countWindow == null) {
					JDialog countWindow = new CountWindow(mainWindow);
					mainWindow.countWindow = countWindow;
				}
				mainWindow.countWindow.setLocationRelativeTo(mainWindow);
				// 设置关于窗口置于最顶层
				mainWindow.countWindow.toFront();
				((CountWindow)mainWindow.countWindow).showCountPanel();
			}
		});
		
		if(ComponentConst.osname.toLowerCase().contains("linux")){
			newTaskMenu = new AJButton(ComponentConst.ADD_MENU_TEXT, IconManager.getIcon("add"), newTaskBtnListener, false);
			startTasksMenu = new AJButton(ComponentConst.START_MENU_TEXT, 
					IconManager.getIcon("start"), new OperaBtnMouseListener(this, MouseAction.CLICK,new StartTaskWork()), false);
			stopTasksMenu = new AJButton(ComponentConst.STOP_MENU_TEXT,
					IconManager.getIcon("stop"), new OperaBtnMouseListener(this, MouseAction.CLICK,new StopTaskWork()), false);
			deleteTasksMenu = new AJButton(ComponentConst.DELETE_MENU_TEXT,
					IconManager.getIcon("delete"), new OperaBtnMouseListener(this, MouseAction.CLICK,new DeleteTaskWork()), false);
			searchComicMenu = new AJButton(ComponentConst.SEARCH_MENU_TEXT, null
					, new MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							if(searchComicWindow == null){
								searchComicWindow = new SearchComicWindow(mainWindow);
							}
							SearchComicWindow scw = mainWindow.searchComicWindow;
							scw.toFront();
							scw.setVisible(true);
							scw.searchBtn.doClick();
						}
					}, false);
			settingMenu = new AJButton(ComponentConst.SETTING_MENU_TEXT, IconManager.getIcon("setting"),
					new MenuMouseListener(this), false);
			settingMenu.setName(ComponentConst.SETTING_MENU_NAME);
			countMenu = new AJButton(ComponentConst.COUNT_MENU_TEXT, IconManager.getIcon("count"), countBtnListener, false);
			aboutMenu = new AJButton(ComponentConst.ABOUT_MENU_TEXT, IconManager.getIcon("user"),
					new MenuMouseListener(this), false);
			aboutMenu.setName(ComponentConst.ABOUT_MENU_NAME);
		}else{
			newTaskMenu = new AJMenu(ComponentConst.ADD_MENU_TEXT,
					"", IconManager.getIcon("add"), newTaskBtnListener);
			startTasksMenu = new AJMenu(ComponentConst.START_MENU_TEXT,
					"", IconManager.getIcon("start"), new OperaBtnMouseListener(this, MouseAction.CLICK,new StartTaskWork()));
			stopTasksMenu = new AJMenu(ComponentConst.STOP_MENU_TEXT,
					"", IconManager.getIcon("stop"), new OperaBtnMouseListener(this, MouseAction.CLICK,new StopTaskWork()));
			deleteTasksMenu = new AJMenu(ComponentConst.DELETE_MENU_TEXT,
					"", IconManager.getIcon("delete"), new OperaBtnMouseListener(this, MouseAction.CLICK,new DeleteTaskWork()));
			searchComicMenu = new AJMenu(ComponentConst.SEARCH_MENU_TEXT,
					"", new MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							if(searchComicWindow == null){
								searchComicWindow = new SearchComicWindow(mainWindow);
							}
							SearchComicWindow scw = mainWindow.searchComicWindow;
							scw.toFront();
							scw.setVisible(true);
							scw.searchBtn.doClick();
						}
					});
			settingMenu = new AJMenu(ComponentConst.SETTING_MENU_TEXT,
					ComponentConst.SETTING_MENU_NAME, IconManager.getIcon("setting"),
					new MenuMouseListener(this));
			countMenu = new AJMenu(ComponentConst.COUNT_MENU_TEXT,
					"", IconManager.getIcon("count"),countBtnListener);
			aboutMenu = new AJMenu(ComponentConst.ABOUT_MENU_TEXT,
					ComponentConst.ABOUT_MENU_NAME, IconManager.getIcon("user"),
					new MenuMouseListener(this));
		}
		//EHicon
		ImageIcon ehIcon = IconManager.getIcon("eh");
		ehIcon.setImage(ehIcon.getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		((AbstractButton) searchComicMenu).setIcon(ehIcon);
		
		// 菜单：任务组
		JMenu taskGroupMenu = new AJMenu(ComponentConst.TASKGROUP_MENU_TEXT,
				ComponentConst.SETTING_MENU_NAME, IconManager.getIcon("group"));
		taskGroupMenu.add(new AddTaskGroupMenuItem("新建任务组", this, AddTaskGroupMenuItem.ADDACTION));
		taskGroupMenu.add(new AddTaskGroupMenuItem("切换任务组", this, AddTaskGroupMenuItem.CHANGEACTION));
		
		// 菜单：操作
		JMenu operaMenu = new AJMenu(ComponentConst.OPERA_MENU_TEXT,
				"", IconManager.getIcon("opera"));
		JMenu taskMenu = new AJMenu("所有任务",
				"", IconManager.getIcon("task"));
		taskMenu.setForeground(new Color(0,0,85));
		taskMenu.add(new StartAllTaskMenuItem("开始所有任务", this));
		taskMenu.add(new StopAllTaskMenuItem("暂停所有任务", this));
		taskMenu.add(new ResetMenuItem("重置所有任务", this));
		taskMenu.add(new ReBuildAllTaskMenuItem("重建所有任务", this));
		operaMenu.add(taskMenu);
		JMenu sizeMenu = new AJMenu("视图封面", "", IconManager.getIcon("task"));
		sizeMenu.add(new ChangeViewSizeMenuItem("大", this, 1));
		sizeMenu.add(new ChangeViewSizeMenuItem("中√", this, 2));
		sizeMenu.add(new ChangeViewSizeMenuItem("小", this, 3));
		operaMenu.add(sizeMenu);
		operaMenu.add(new ChangeViewMenuItem(" 切换视图", this));
		operaMenu.add(new SimpleSearchMenuItem(" 本地搜索", this));
		operaMenu.add(new OpenRootMenuItem(" 打开根目录", this));
		
		
		// 菜单：控制台
		JMenu consoleMenu = new AJMenu(ComponentConst.CONSOLE_MENU_TEXT,
				"", IconManager.getIcon("select"));
		JMenuItem clearItem = new ClearConsoleMenuItem("清空控制台", this);
		JMenuItem openLogItem = new OpenLogMenuItem("打开日志文件", this);
		consoleMenu.add(clearItem);
		consoleMenu.add(openLogItem);
		
		Component[] menus = new Component[]{
					newTaskMenu, startTasksMenu, stopTasksMenu, deleteTasksMenu, searchComicMenu, taskGroupMenu, settingMenu, operaMenu, consoleMenu, countMenu, aboutMenu
			};
		// 构造菜单栏并添加菜单
		jMenuBar = new AJMenuBar(0, 0, ComponentConst.CLIENT_WIDTH, 30, menus);
		// 正在下载table
		runningTable = new TaskingTable(5, 40, ComponentConst.CLIENT_WIDTH - 20,
				(tasks == null ? 0 :tasks.size()) * 28, this.tasks, this);
		
		
		viewModel = setting.getViewModel();
		if(viewModel == 1){
			tablePane = new JScrollPane(runningTable);
		}else{
			tablePane = new JScrollPane();
			taskImagePanel = new TaskImagePanel(this);
			tablePane.setViewportView(taskImagePanel);
			tablePane.getVerticalScrollBar().setUnitIncrement(20);
			tablePane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
				public void adjustmentValueChanged(AdjustmentEvent e) {
					JScrollBar bar = (JScrollBar) e.getSource();
					if(e.getValue() + bar.getHeight() == bar.getMaximum()){
						taskImagePanel.setPreferredSize(new Dimension((int)taskImagePanel.getPreferredSize().getWidth(), (int)taskImagePanel.getPreferredSize().getHeight() + bar.getUnitIncrement()));
					}
				}
			});
		}
		tablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tablePane.setBounds(new Rectangle(5, 40, ComponentConst.CLIENT_WIDTH - 20, 400));
		tablePane.getViewport().setBackground(new Color(254,254,254));
		
		
		//表格的右键菜单
		tablePopupMenu = new MainPopupMenu(this);
		
		JLabel emptyTableTips = new AJLabel("empty", "", new Color(227,93,81), JLabel.CENTER);
		emptyTableTips.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
		JButton emptyBtn = new AJButton("当前任务组没有下载任务，请点击搜索漫画");
		emptyBtn.setIcon(ehIcon);
		emptyBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(searchComicWindow == null){
					searchComicWindow = new SearchComicWindow(mainWindow);
				}
				SearchComicWindow scw = mainWindow.searchComicWindow;
				scw.toFront();
				scw.setVisible(true);
				scw.searchBtn.doClick();
			}
		});
		emptyPanel = new AJPanel(/*emptyTableTips, */emptyBtn);
		emptyPanel.setBounds(0, 160, (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(), 100);
		emptyPanel.setLayout(new FlowLayout());
		
		
		infoTabbedPane = new InfoTabbedPane(this);
		infoTabbedPane.setBounds(5, ComponentConst.CLIENT_HEIGHT - 240, ComponentConst.CLIENT_WIDTH - 20, 200);
		
		/**
		 * 控制台
		 */
		consolePanel = new ConsolePanel(this);
		/*consolePanel.setEditable(false);
		consolePanel.setAutoscrolls(true);
		consolePanel.setBorder(null);
		consolePanel.setFont(new Font("宋体", Font.BOLD, 13));
		consolePanel.setForeground(new Color(63,127,95));*///
		//consolePanel.setLocation(0, 20);
		//consolePanel.setBackground(Color.GRAY);
		/*TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(219,219,219)), "控制台");
		consolePanel.setBorder(border);*/
		consolePanel.setBounds(5, 5, ComponentConst.CLIENT_WIDTH - 20, 200);
		
		try {
			//将syso信息推送到控制台
			new SwingPrintStream(System.out, consolePanel);
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(this, "控制台初始化错误！");
		}
		
		taskInfoPanel = new TaskInfoPanel(this);
		taskInfoPanel.setBounds(5, 5, ComponentConst.CLIENT_WIDTH - 20, 200);
		
		infoTabbedPane.add("控制台", consolePanel);
		infoTabbedPane.add("任务信息", taskInfoPanel);
		infoTabbedPane.add("图片列表", new PicturesInfoPanel(this));
		/*infoTabbedPane.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				System.out.println(infoTabbedPane.getToolkit());
			}
			
		});*/
		// 添加各个子组件
		ComponentUtil.addComponents(getContentPane(), infoTabbedPane, jMenuBar, tablePane, tablePopupMenu, emptyPanel);
		if(tasks == null || tasks.size() == 0){
			tablePane.setVisible(false);
		}else{
			emptyPanel.setVisible(false);
		}
		
		//聚焦监听
		this.addWindowFocusListener(new WindowAdapter() {
			public void windowGainedFocus(WindowEvent e) {
				EgDownloaderWindow window = (EgDownloaderWindow) e.getSource();
				if (window.aboutWindow != null) {
					window.aboutWindow.dispose();
				}
				if(window.countWindow != null){
					window.countWindow.dispose();
				}
				if(window.creatingWindow != null && window.creatingWindow.isVisible()){
					window.creatingWindow.requestFocus();
				}else if(window.addFormWindow != null && window.addFormWindow.isVisible()){
					window.addFormWindow.requestFocus();
				}else if(window.detailWindow != null && window.detailWindow.isVisible()){
					window.detailWindow.requestFocus();
				}else if(window.editWindow != null && window.editWindow.isVisible()){
					window.editWindow.requestFocus();
				}else if(window.deletingWindow != null && window.deletingWindow.isVisible()){
					window.deletingWindow.requestFocus();
				}else if(window.simpleSearchWindow != null && window.simpleSearchWindow.isVisible()){
					window.simpleSearchWindow.requestFocus();
				}else{
				}
			}

			public void windowLostFocus(WindowEvent e) {
				EgDownloaderWindow window = (EgDownloaderWindow) e.getSource();
				if(window.initWindow.trayMenu != null){
					window.initWindow.trayMenu.setVisible(false);
				}
				/*EgDownloaderWindow window = (EgDownloaderWindow) e.getSource();
				window.consolePane.setVisible(false);*/
			}
		});
		//窗口大小变化监听
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				EgDownloaderWindow window = (EgDownloaderWindow) e.getSource();
				//设置菜单大小
				if(jMenuBar != null){
					jMenuBar.setSize(window.getWidth(), jMenuBar.getHeight());
				}
				//设置表格的大小
				if(runningTable != null){
					int height = window.getHeight() - 280;
					tablePane.setSize(window.getWidth() - 20, height);
					runningTable.setSize(window.getWidth() - 20, height);
					//runningTable.updateUI();
					//设置名称列最多显示字数
					if(window.getWidth() <= ComponentConst.CLIENT_WIDTH){
						TaskingTable.wordNum = 230;
					}else{
						TaskingTable.wordNum = 230 + (window.getWidth() - ComponentConst.CLIENT_WIDTH) * 7 / (2 * 100);
					}
				}
				//设置空提示label位置
				if(emptyPanel != null){
					emptyPanel.setBounds(0, ((window.getHeight() - 280) / 2), window.getWidth(), 100);
				}
				//设置控制台大小
				/*if(consolePane != null){
					consolePane.setBounds(5, window.getHeight() - 240, window.getWidth() - 20, 200);
				}*/
				if(infoTabbedPane != null){
					infoTabbedPane.setBounds(5, window.getHeight() - 240, window.getWidth() - 20, 200);
				}
				if(consolePanel != null){
					consolePanel.setBounds(5, window.getHeight() - 240, window.getWidth() - 20, 200);
				}
			}
		});
		//鼠标动作监听
		this.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				EgDownloaderWindow window = (EgDownloaderWindow) e.getSource();
				if(window.getWidth() < ComponentConst.CLIENT_WIDTH){
					window.setSize(ComponentConst.CLIENT_WIDTH, window.getHeight());
				}
				if(window.getHeight() < ComponentConst.CLIENT_HEIGHT){
					window.setSize(window.getWidth(), ComponentConst.CLIENT_HEIGHT);
				}
			}
		});
		//开启网络下载速度监听
		setupNetSpeedtimer(mainWindow);
	}
	
	protected void processWindowEvent(WindowEvent e) {
		//关闭，询问
		if(e.getID() == WindowEvent.WINDOW_CLOSING){
			int r = JOptionPane.showConfirmDialog(this, "您确定要关闭" + Version.NAME + "吗？", "提示", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
			if(r == JOptionPane.OK_OPTION){
				//保存数据
				this.saveTaskGroupData();
				System.exit(0);
			}
		}
		//最小化，隐藏到托盘
		else if(e.getID() == WindowEvent.WINDOW_ICONIFIED){
			if(initWindow.tray != null){
				if(minTips == null){
					minTips = new JWindow();
					minTips.setSize(150, 30);
					minTips.setLocation((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 200, (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 80);
					minTips.setLayout(new FlowLayout());
					minTips.getContentPane().setBackground(Color.BLACK);
					JLabel tips = new AJLabel(Version.NAME + "已最小化到托盘", Color.WHITE);
					tips.setToolTipText("点击完毕");
					tips.addMouseListener(new MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							minTips.dispose();
						}
					});
					minTips.getContentPane().add(tips);
					minTips.toFront();
					minTips.requestFocus();
					minTips.setVisible(true);
					Timer t = new Timer();
					//10秒后关闭
					t.schedule(new TimerTask() {
						public void run() {
							minTips.dispose();
						}
					}, 10000);
				}
				ComponentUtil.disposeAll(searchComicWindow, settingWindow,
						detailWindow, aboutWindow, addFormWindow,
						creatingWindow, detailWindow, checkingWindow,
						coverWindow2, coverWindow, editWindow, deletingWindow,
						resetAllTaskWindow, countWindow, simpleSearchWindow);
				this.setVisible(false);
				this.dispose();
			}
		}else{
			super.processWindowEvent(e);
		}
	}

	public void saveTaskGroupData(){
		//this.taskDbTemplate.update(this.tasks);
		this.settingDbTemplate.update(this.setting);
	}
	
	public void changeTaskGroup(Setting setting, TaskList<Task> tasks, DbTemplate<Task> taskDbTemplate, DbTemplate<Picture> pictureDbTemplate, DbTemplate<Setting> settingDbTemplate){
		this.taskDbTemplate = taskDbTemplate;
		this.pictureDbTemplate = pictureDbTemplate;
		this.settingDbTemplate = settingDbTemplate;
		//加载配置数据
		this.setting = setting;
		//清空
		this.tasks.clear();
		//加载任务列表
		this.tasks = tasks == null ? new TaskList<Task>() : tasks;
		this.wtitle = Version.NAME + "v" + Version.VERSION + " / " + ("".equals(ComponentConst.groupName) ? "默认空间" : ComponentConst.groupName);
		// 设置主窗口
		this.setTitle(wtitle);
		if(this.tasks.isEmpty()){
			this.tablePane.setVisible(false);
			this.emptyPanel.setVisible(true);
		}else{
			this.tablePane.setVisible(true);
			this.emptyPanel.setVisible(false);
			this.runningTable.changeModel(this);
		}
		this.consolePanel.setText("");//清空控制台
		//开启网络下载速度监听
		netSpeedtimer.cancel();
		setupNetSpeedtimer(this);
	}
}
