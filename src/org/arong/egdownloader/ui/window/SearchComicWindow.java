package org.arong.egdownloader.ui.window;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.arong.egdownloader.model.ScriptParser;
import org.arong.egdownloader.model.SearchTask;
import org.arong.egdownloader.ui.ComponentConst;
import org.arong.egdownloader.ui.ComponentUtil;
import org.arong.egdownloader.ui.CursorManager;
import org.arong.egdownloader.ui.IconManager;
import org.arong.egdownloader.ui.panel.SearchImagePanel;
import org.arong.egdownloader.ui.popmenu.SearchWindowPopMenu;
import org.arong.egdownloader.ui.swing.AJButton;
import org.arong.egdownloader.ui.swing.AJCheckBox;
import org.arong.egdownloader.ui.swing.AJLabel;
import org.arong.egdownloader.ui.swing.AJPager;
import org.arong.egdownloader.ui.swing.AJTextField;
import org.arong.egdownloader.ui.table.SearchTasksTable;
import org.arong.egdownloader.ui.work.SearchComicWorker;
import org.arong.util.FileUtil;

import com.sun.awt.AWTUtilities;

/**
 * 显示绅士站漫画列表窗口
 * @author dipoo
 * @since 2015-03-11
 */
public class SearchComicWindow extends JFrame {

	private static final long serialVersionUID = -3912589805632312855L;
	public EgDownloaderWindow mainWindow;
	public SearchTagWindow searchTagWindow;
	public SearchCoverWindow coverWindow;
	public JTextField keyField;
	public JComboBox language;
	private JLabel loadingLabel;
	public JLabel totalLabel;
	public JButton searchBtn;
	public JButton leftBtn;
	public JButton rightBtn;
	public JButton tagBtn;
	public JButton changeViewBtn;
	private JButton clearCacheBtn;
	public SearchTasksTable searchTable;
	public JScrollPane tablePane;
	public JPanel picturePane;
	public JPanel optionPanel;
	public AJPager pager;
	public boolean haveBt;//是否有bt下载文件
	public String key = " ";//搜索条件的字符串
	public List<SearchTask> searchTasks = new ArrayList<SearchTask>();
	public Map<String, Map<String, List<SearchTask>>> datas = new HashMap<String, Map<String, List<SearchTask>>>();//任务数据缓存
	public Map<String, String> keyPage = new HashMap<String, String>();//分页信息缓存
	public Map<String, String> pageInfo = new HashMap<String, String>();//总页数缓存
	public List<String> keyList = new ArrayList<String>();//关键字缓存
	private Font font = new Font("宋体", 0, 12); 
	public String page = "1";
	public SearchWindowPopMenu popMenu;
	public int viewModel = 2;//2为图片浏览；1为表格浏览
	public int selectTaskIndex = 0;//操作的任务索引
	public int f_cats = 0;
	public String f_sto = "";
	public SearchComicWindow(final EgDownloaderWindow mainWindow){
		this.mainWindow = mainWindow;
		viewModel = mainWindow.setting.getSearchViewModel();
		this.setSize(ComponentConst.CLIENT_WIDTH, ComponentConst.CLIENT_HEIGHT);
		this.setTitle("搜索里站漫画");
		this.setIconImage(IconManager.getIcon("eh").getImage());
		this.setLayout(null);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		AWTUtilities.setWindowOpaque(this, true);
		//this.getContentPane().setBackground(Color.LIGHT_GRAY);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);//全屏
		//this.setResizable(false);
		this.setLocationRelativeTo(mainWindow);  
		JLabel keyLabel = new AJLabel("关键字", Color.BLUE, 10, 20, 50, 30);
		keyField = new AJTextField("", 60, 20, 400, 30);
		keyField.setText("language:chinese");
		keyField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					searchBtn.doClick();
				}
			}
		});
		
		keyList.add(",1");
		
		loadingLabel = new AJLabel("正在加载数据", "loading.gif", Color.BLACK, JLabel.LEFT);
		loadingLabel.setBounds(630, 20, 120, 30);
		loadingLabel.setVisible(false);
		
		totalLabel = new AJLabel("", "", Color.BLACK, JLabel.LEFT);
		totalLabel.setBounds(630, 20, 300, 30);
		totalLabel.setVisible(false);
		
		searchBtn = new AJButton("搜索", "", new ActionListener() {
			
			public void actionPerformed(ActionEvent ae) {
				mainWindow.searchComicWindow.toFront();
				search(page);
			}
			
		}, 470, 20, 60, 30);
		leftBtn = new JButton(IconManager.getIcon("left"));
		leftBtn.setBounds(540, 20, 30, 30);
		leftBtn.setToolTipText("后退");
		leftBtn.setFocusable(false);
		leftBtn.setCursor(CursorManager.getPointerCursor());
		leftBtn.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				changeKeyList(true);
				String key = keyList.get(keyList.size() - 1);
				keyField.setText(key.substring(0, key.lastIndexOf(",")));
				page = key.substring(key.lastIndexOf(",") + 1, key.length());
				searchBtn.doClick();
			}
		});
		rightBtn = new JButton(IconManager.getIcon("right"));
		rightBtn.setBounds(580, 20, 30, 30);
		rightBtn.setToolTipText("前进");
		rightBtn.setFocusable(false);
		rightBtn.setCursor(CursorManager.getPointerCursor());
		rightBtn.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				changeKeyList(false);
				String key = keyList.get(keyList.size() - 1);
				keyField.setText(key.substring(0, key.lastIndexOf(",")));
				page = key.substring(key.lastIndexOf(",") + 1, key.length());
				searchBtn.doClick();
			}
		});
		final SearchComicWindow this_ = this;
		tagBtn = new AJButton("选择标签", "",  new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if(searchTagWindow == null){
					searchTagWindow = new SearchTagWindow(this_);
				}
				searchTagWindow.setVisible(true);
			}
		}, this.getWidth() - 150, 20, 60, 30);
		clearCacheBtn = new AJButton("清理缓存", "",  new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				datas.clear();
				pageInfo.clear();
				keyPage.clear();
				JOptionPane.showMessageDialog(this_, "清理成功");
			}
		}, this.getWidth() - 80, 20, 60, 30);
		tagBtn.setUI(AJButton.blueBtnUi);
		clearCacheBtn.setUI(AJButton.blueBtnUi);
		
		/* 分类条件 */
		optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		optionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(Integer.parseInt("bababa", 16)), 1), "条件过滤"));
		optionPanel.setBounds(6, 55, ComponentConst.CLIENT_WIDTH - 23, 65);
		
		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox jcb = (JCheckBox) e.getItem();
				try{
					if (jcb.isSelected()) {// 判断是否被选择
						if(f_cats == 0) f_cats = 1022;
						f_cats -= Integer.parseInt(jcb.getName());
					} else {
						f_cats += Integer.parseInt(jcb.getName());
					}
				}catch(Exception e1){}

			}
		};
		
		JCheckBox c1 = new AJCheckBox("2", "Doujinshi", Color.BLUE, font, true, itemListener);
		JCheckBox c2 = new AJCheckBox("4", "Manga", Color.BLUE, font, true, itemListener);
		JCheckBox c3 = new AJCheckBox("8", "Artist CG", Color.BLUE, font, true, itemListener);
		JCheckBox c4 = new AJCheckBox("16", "Game CG", Color.BLUE, font, true, itemListener);
		JCheckBox c5 = new AJCheckBox("512", "Western", Color.BLUE, font, true, itemListener);
		JCheckBox c6 = new AJCheckBox("256", "Non-H", Color.BLUE, font, true, itemListener);
		JCheckBox c7 = new AJCheckBox("32", "Image Set", Color.BLUE, font, true, itemListener);
		JCheckBox c8 = new AJCheckBox("64", "Cosplay", Color.BLUE, font, true, itemListener);
		JCheckBox c9 = new AJCheckBox("128", "Asian Porn", Color.BLUE, font, true, itemListener);
		JCheckBox c10 = new AJCheckBox("1", "Misc", Color.BLUE, font, true, itemListener);
		JCheckBox c11 = new AJCheckBox("BT", Color.RED, font, false);//
		c11.setToolTipText("是否可以下载BT文件");
		c11.setName("sto");
		c11.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox jcb = (JCheckBox) e.getItem();
				f_sto = jcb.isSelected() ? "on" : "";
			}
		});
		language = new JComboBox(new String[]{"全部", "中文", "日文", "英文", "韩文", "法文"});
		language.setSelectedIndex(1);
		language.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String key = keyField.getText();
				String[] keys = key.split(" ");
				if(keys[0].indexOf("language:") != -1){
					key = "";
					for(int i = 1; i < keys.length; i ++){
						key += keys[i];
						if(i != keys.length - 1){
							key += "";
						}
					}
				}
				switch(language.getSelectedIndex()){
					case 0:
						keyField.setText(key);
						break;
					case 1:
						keyField.setText("language:chinese " + key);
						break;
					case 2:
						keyField.setText("language:japanese " + key);
						break;	
					case 3:
						keyField.setText("language:english " + key);
						break;
					case 4:
						keyField.setText("language:korean " + key);
						break;
					case 5:
						keyField.setText("language:french " + key);
						break;
				}
			}
		});
		final JCheckBox c12 = new AJCheckBox("All", Color.RED, font, true);
		c12.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Component[] cs = optionPanel.getComponents();
				for(int i = 0; i < cs.length; i ++){
					if(cs[i] instanceof JCheckBox){
						((JCheckBox)cs[i]).setSelected(c12.isSelected());
					}
				}
			}
		});
		changeViewBtn = new AJButton("切换视图", "",  new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				viewModel = viewModel == 1 ? 2 : 1;
				mainWindow.setting.setSearchViewModel(viewModel);
				ScriptParser.searchScriptFile = null;
				searchBtn.doClick();
			}
		}, 0, 0, 60, 30);
		ComponentUtil.addComponents(optionPanel, language, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, changeViewBtn);
		/* 分类条件 end*/
		
		pager = new AJPager(20, ComponentConst.CLIENT_HEIGHT - 80, ComponentConst.CLIENT_WIDTH, ComponentConst.CLIENT_HEIGHT, new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JButton btn = (JButton) e.getSource();
				page = btn.getName();
				search(Integer.parseInt(btn.getName()) + "");
			}
		});
		pager.setVisible(false);
		
		
		ComponentUtil.addComponents(this.getContentPane(), keyLabel, keyField, searchBtn, leftBtn, rightBtn, loadingLabel, totalLabel, tagBtn, clearCacheBtn, optionPanel, pager);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { 
				//关闭后显示主界面
				mainWindow.setVisible(true);
				mainWindow.setEnabled(true);
				JFrame w = (JFrame)e.getSource();
				w.dispose();
			}
			public void windowGainedFocus(WindowEvent e) {
				//SearchComicWindow window = (SearchComicWindow) e.getSource();
				//window.mainWindow.consolePane.setVisible(false);
			}
			public void windowActivated(WindowEvent e) {
				//picturePane = null;
				//showResult("4", 1);
			}
		});
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				SearchComicWindow this_ = (SearchComicWindow) e.getSource();
				this_.dispose();
			}
		});
		
		//窗口大小变化监听
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				SearchComicWindow window = (SearchComicWindow) e.getSource();
				//设置清理缓存按钮位置
				if(clearCacheBtn != null){
					tagBtn.setLocation(window.getWidth() - 150, clearCacheBtn.getY());
					clearCacheBtn.setLocation(window.getWidth() - 80, clearCacheBtn.getY());
				}
				//设置分类条件大小
				if(optionPanel != null){
					optionPanel.setSize(window.getWidth() - 23, optionPanel.getHeight());
				}
				//设置表格的大小
				if(searchTable != null){
					int height = window.getHeight() - 210;
					tablePane.setSize(window.getWidth() - 20, height);
					searchTable.setSize(window.getWidth() - 20, height + 20);
				}
				//设置图片面板大小
				if(picturePane != null){
					int height = window.getHeight() - 210;
					tablePane.setSize(window.getWidth() - 20, height);
					
					int hr = (int)(tablePane.getWidth() / 260);
					int zr = (int)(25 / hr) + 1;
					picturePane.setPreferredSize(new Dimension(tablePane.getWidth() - 40,  zr * 500));
				}
				//设置分页面板大小
				if(pager != null){
					pager.setBounds(pager.getX(), window.getHeight() - 80, window.getWidth() - 20, pager.getHeight());
				}
			}
		});
		
		//鼠标动作监听
		this.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				SearchComicWindow window = (SearchComicWindow) e.getSource();
				if(window.getWidth() < ComponentConst.CLIENT_WIDTH){
					window.setSize(ComponentConst.CLIENT_WIDTH, window.getHeight());
				}
				if(window.getHeight() < ComponentConst.CLIENT_HEIGHT){
					window.setSize(window.getWidth(), ComponentConst.CLIENT_HEIGHT);
				}
			}
		});
		
		//检测是否存在缓存目录,不存在则创建
		FileUtil.ifNotExistsThenCreate(ComponentConst.CACHE_PATH);
		
	}
	
	public void search(String page){
		showLoading();
		String keyText = keyField.getText().trim();
		//如果当前的关键字与上一个不相同，则添加进去
		if(! (keyText + "," + page).equals(keyList.get(keyList.size() - 1))){
			keyList.add(keyText + "," + page);
		}
		
		String k = parseOption() + keyText;
		if(datas.containsKey(k) && datas.get(k).containsKey(page)){
			searchTasks = datas.get(k).get(page);
			if(viewModel == 1){
				showResult(pageInfo.get(k), Integer.parseInt(page));
			}else{
				showResult2(pageInfo.get(k), Integer.parseInt(page));
			}
			totalLabel.setText(keyPage.get(k));
			hideLoading();
		}else{
			//设置为不可用
			leftBtn.setEnabled(false);
			rightBtn.setEnabled(false);
			
			key = k;
			String exurl = (mainWindow.setting.isHttps() ? "https" : "http") + 
					"://exhentai.org/?advsearch=1&f_sname=on&f_stags=on&f_sh=on&f_spf=&f_spt=&page=" + 
					(Integer.parseInt(page) - 1) + "&f_cats=" + f_cats + "&f_sto=" + f_sto;
			if(!keyText.equals("")){
				//过滤key
				try {
					keyText = URLEncoder.encode(keyText, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					
				}
				exurl = exurl + "&f_search=" + keyText;
			}
			new SearchComicWorker(mainWindow, exurl, Integer.parseInt(page)).execute();
		}
	}
	
	
	public String parseOption(){
		Component[] cs = optionPanel.getComponents();
		String option = "";
		JCheckBox jc = null;
		for(int i = 0; i < cs.length; i++){
			if(cs[i] instanceof JCheckBox){
				jc = (JCheckBox) cs[i];
				if(jc.isSelected()){
					if(jc.getName() != null){
						option += "&f_" + jc.getName().toLowerCase() + "=1";
					}else{
						option += "&f_" + jc.getText().toLowerCase() + "=1";
					}
				}
			}
		}
		return option;
	}
	
	public void doSearch(String text){
		if(text == null || "".equals(text)){
			return;
		}
		String key = text;
		switch(language.getSelectedIndex()){
			case 0:
				break;
			case 1:
				key = "language:chinese$ " + key;
				break;
			case 2:
				key = "language:english$ " + key;
				break;
			case 3:
				key = "language:korean$ " + key;
				break;
			case 4:
				key = "language:french$ " + key;
				break;
			case 5:
				key = "language:spanish$ " + key;
				break;	
		}
		if(key.equals(keyField.getText())){
			return;
		}
		page = "1";
		keyField.setText(key);
		searchBtn.doClick();
	}
	public void showLoading(){
		totalLabel.setVisible(false);
		loadingLabel.setVisible(true);
		searchBtn.setEnabled(false);
		if(tablePane != null){
			tablePane.setVisible(false);
		}
		pager.setVisible(false);
	}
	
	public void hideLoading(){
		loadingLabel.setVisible(false);
		searchBtn.setEnabled(true);
		totalLabel.setVisible(true);
		if(tablePane != null){
			tablePane.setVisible(true);
		}
		pager.setVisible(true);
	}
	
	public void setTotalInfo(String totalPage, String totalTasks){
		totalLabel.setText("共搜索到 " + totalPage + " 页,总计 " + totalTasks + " 本漫画");
	}
	
	public SearchImagePanel[] picLabels = new SearchImagePanel[25];
	
	//二次搜索刷新使用
	public void updateTaskInfo(){
		if(viewModel == 1){
			searchTable.updateUI();
		}else{
			for(int i = 0; i < searchTasks.size(); i ++){
				final SearchImagePanel coverLabel = picLabels[i];
				coverLabel.setText(coverLabel.genText(searchTasks.get(i)));
			}
			
		}
	}
	
	public void showResult(String totalPage, Integer currentPage){
		if(picturePane != null){mainWindow.searchComicWindow.getContentPane().remove(tablePane);tablePane = null;}
		if(searchTable == null){
			searchTable = new SearchTasksTable(5, 130, this.getWidth() - 20,
					this.getHeight() - 210, searchTasks, this);
		}
		if(tablePane == null){
			tablePane = new JScrollPane(searchTable);
			mainWindow.searchComicWindow.getContentPane().add(tablePane);
			tablePane.setBounds(5, 130, this.getWidth() - 20, this.getHeight() - 210);
			tablePane.getViewport().setBackground(new Color(254,254,254));
		}
		
		searchTable.setVisible(true);
		searchTable.changeModel(searchTasks);
		searchTable.updateUI();
		JScrollBar jScrollBar = tablePane.getVerticalScrollBar();
		jScrollBar.setValue(jScrollBar.getMinimum());//滚动到最前
		if(totalPage != null && currentPage != null){
			mainWindow.searchComicWindow.pager.change(Integer.parseInt(totalPage), currentPage);
			mainWindow.searchComicWindow.pager.setVisible(true);
		}
	}
	
	public void showResult2(final String totalPage, final Integer currentPage){
		if(picLabels[0] == null){
			for(int i = 0; i < 25; i ++){
				SearchImagePanel coverLabel = new SearchImagePanel(i, mainWindow);
				picLabels[i] = coverLabel;
			}
		}
		if(searchTable != null){
			mainWindow.searchComicWindow.getContentPane().remove(tablePane);
			tablePane = null;
		}
		if(tablePane == null){
			tablePane = new JScrollPane();
			tablePane.setBounds(5, 130, this.getWidth() - 20, this.getHeight() - 210);
			tablePane.getViewport().setBackground(new Color(254,254,254));
			mainWindow.searchComicWindow.getContentPane().add(tablePane);
		}
		if(picturePane == null){
			picturePane = new JPanel();
			//picturePane.setBackground(Color.LIGHT_GRAY);
			picturePane.setLayout(new FlowLayout(FlowLayout.CENTER));
			picturePane.setBounds(10, 5, tablePane.getWidth() - 20, 250 * 6);
			int hr = (int)(tablePane.getWidth() / 260);
			int zr = (int)(25 / hr) + 1;
			picturePane.setPreferredSize(new Dimension(tablePane.getWidth() - 40,  zr * 500));
		}else{
			picturePane.removeAll();
		}
		picturePane.setVisible(true);
		tablePane.setViewportView(picturePane);
		
		for(int i = 0; i < searchTasks.size(); i ++){
			final SearchImagePanel coverLabel = picLabels[i];
			coverLabel.flush(searchTasks.get(i), 200 * i);
			ComponentUtil.addComponents(picturePane, coverLabel);
		}
		
		JScrollBar jScrollBar = tablePane.getVerticalScrollBar();
		jScrollBar.setValue(0);//滚动到最前
		jScrollBar.setUnitIncrement(20);
		if(totalPage != null && currentPage != null){
			mainWindow.searchComicWindow.pager.change(Integer.parseInt(totalPage), currentPage);
			mainWindow.searchComicWindow.pager.setVisible(true);
		}
	}
	
	public void dispose() {
		mainWindow.setEnabled(true);
		mainWindow.setVisible(true);
		if(searchTagWindow != null && searchTagWindow.isVisible()){
			searchTagWindow.dispose();
		}
		super.dispose();
	}
	//根据前进或后退改变keyList的元素排序
	private void changeKeyList(boolean left){
		if(left){
			String e = keyList.remove(keyList.size() - 1);
			keyList.add(0, e);
		}else{
			String e = keyList.remove(0);
			keyList.add(keyList.size(), e);
		}
	}
}
