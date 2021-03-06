package org.arong.egdownloader.ui.panel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.commons.lang.StringUtils;
import org.arong.egdownloader.model.SearchTask;
import org.arong.egdownloader.ui.ComponentConst;
import org.arong.egdownloader.ui.FontConst;
import org.arong.egdownloader.ui.IconManager;
import org.arong.egdownloader.ui.popmenu.SearchWindowPopMenu;
import org.arong.egdownloader.ui.window.EgDownloaderWindow;
import org.arong.util.FileUtil;

public class SearchImagePanel extends JLabel {
	private EgDownloaderWindow mainWindow;
	public boolean iconLoadCompleted; 
	public final static int DEFAULTWIDTH = 16;
	public final static int DEFAULTHEIGHT = 16;
	public SearchImagePanel(final EgDownloaderWindow mainWindow){
		this.mainWindow = mainWindow;
		this.setOpaque(true);
		this.setBackground(Color.BLACK);
		this.setForeground(Color.WHITE);
		this.setFont(FontConst.Microsoft_BOLD_12);
		this.setVerticalTextPosition(JLabel.TOP);
		this.setHorizontalTextPosition(JLabel.CENTER);
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JLabel l = (JLabel) e.getSource();
				mainWindow.searchComicWindow.selectTaskIndex = Integer.parseInt(l.getName()) - 1;
				//左键
				if(e.getButton() == MouseEvent.BUTTON1){}
				//右键
				else if(e.getButton() == MouseEvent.BUTTON3){
					//使之选中
					if(mainWindow.searchComicWindow.popMenu == null){
						mainWindow.searchComicWindow.popMenu = new SearchWindowPopMenu(mainWindow);
					}
					SearchTask task = mainWindow.searchComicWindow.searchTasks.get(mainWindow.searchComicWindow.selectTaskIndex);
					boolean contains = mainWindow.tasks.getTaskMap().containsKey(task.getUrl().replaceAll("https://", "http://")) || mainWindow.tasks.getTaskMap().containsKey(task.getUrl().substring(0, task.getUrl().length() - 1).replaceAll("https://", "http://"));
					if(contains){
						mainWindow.searchComicWindow.popMenu.openPictureItem.setVisible(true);
						mainWindow.searchComicWindow.popMenu.downItem.setVisible(false);
					}else{
						mainWindow.searchComicWindow.popMenu.openPictureItem.setVisible(false);
						mainWindow.searchComicWindow.popMenu.downItem.setVisible(true);
					}
					if(StringUtils.isNotBlank(task.getBtUrl())){
						mainWindow.searchComicWindow.popMenu.openBtPageItem.setVisible(true);
					}else{
						mainWindow.searchComicWindow.popMenu.openBtPageItem.setVisible(false);
					}
					mainWindow.searchComicWindow.popMenu.show(l, e.getPoint().x, e.getPoint().y);
				}
			}

			public void mouseEntered(MouseEvent e) {
				JLabel l = (JLabel) e.getSource();
				l.setBorder(BorderFactory.createLineBorder(Color.PINK, 2));
				l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				if(l.getIcon().getIconWidth() == DEFAULTWIDTH && new File(ComponentConst.CACHE_PATH + "/" + FileUtil.filterDir(mainWindow.searchComicWindow.searchTasks.get(Integer.parseInt(l.getName()) - 1).getUrl())).exists()){
					flush(mainWindow.searchComicWindow.searchTasks.get(Integer.parseInt(l.getName()) - 1));
				}
			}

			public void mouseExited(MouseEvent e) {
				JLabel l = (JLabel) e.getSource();
				l.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
				l.setCursor(Cursor.getDefaultCursor());
			}
		});
	}
	public SearchImagePanel(int index, final EgDownloaderWindow mainWindow){
		this(mainWindow);
		this.setName((index + 1)+ "");
	}
	public void flush(SearchTask task){
		flush(task, 0);
	}
	
	public void flushTitle(){
		
	}
	
	public void flush(SearchTask task, final long delay){
		this.setForeground(Color.WHITE);
		boolean contains = mainWindow.tasks.getTaskMap().containsKey(task.getUrl().replaceAll("https://", "http://")) || mainWindow.tasks.getTaskMap().containsKey(task.getUrl().substring(0, task.getUrl().length() - 1).replaceAll("https://", "http://"));
		if(contains){this.setForeground(Color.RED);}
		this.setText(genText(task));
		this.setToolTipText(task.getName() + (StringUtils.isNotBlank(task.getUploader()) ? "[" + task.getUploader() + "]" : ""));
		final SearchImagePanel this_ = this;
		
		final String path = ComponentConst.CACHE_PATH + "/" + FileUtil.filterDir(task.getUrl());
		File cover = new File(path);
		if(cover == null || !cover.exists()){
			this.setSize(DEFAULTWIDTH, DEFAULTHEIGHT);
			this.setIcon(IconManager.getIcon("loading"));
			new Thread(new Runnable() {
				public void run() {
					if(delay > 0){
						try {
							Thread.sleep(delay);
						} catch (InterruptedException e) {}
					}
					File cover = new File(path);
					int i = 1;
					ImageIcon icon = null;
					while(!cover.exists() && i < 60){
						i ++;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {}
						cover = new File(path);
					}
					if(!cover.exists()){ 
						this_.setText(this_.getText() + ">加载失败");
						this_.setIcon(null);
						return;
					}
					icon = new ImageIcon(path);
					icon.getImage().flush();//解决加载图片不完全问题
					while(icon.getIconWidth() == -1 && icon.getImage().getWidth(icon.getImageObserver()) == -1){
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {}
						try {
							icon = new ImageIcon(path);
							icon.getImage().flush();//解决加载图片不完全问题
						} catch (Exception e) {this_.setText(this_.getText() + ">加载失败");return;}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
					int width = icon.getIconWidth(), height = icon.getIconHeight();
					if(width == -1){
						width = icon.getImage().getWidth(icon.getImageObserver());
						height = icon.getImage().getHeight(icon.getImageObserver());
					}
					this_.setSize(width + 4, height + 4);
					icon.getImage().flush();
					this_.setIcon(icon);
					this_.iconLoadCompleted = true;
				}
			}).start();
		}else{
			ImageIcon icon = null;
			icon = new ImageIcon(path);
			icon.getImage().flush();//解决加载图片不完全问题
			if(icon.getIconWidth() == -1 && icon.getImage().getWidth(icon.getImageObserver()) == -1){
				this.setSize(DEFAULTWIDTH, DEFAULTHEIGHT);
				this.setIcon(IconManager.getIcon("loading"));
			}else{
				if(icon.getIconWidth() == -1){
					this.setSize(icon.getImage().getWidth(icon.getImageObserver()) + 4, icon.getImage().getHeight(icon.getImageObserver()) + 4);
				}else{
					this.setSize(icon.getIconWidth() + 4, icon.getIconHeight() + 4);
				}
				icon.getImage().flush();//解决加载图片不完全问题
				this.setIcon(icon);
			}
		}
	}
	
	public String genText(SearchTask task){
		return "<html><small><font color='#f2e986'>" + task.getRating() + "分</font> " + 
				(StringUtils.isNotBlank(task.getType()) ? 
						(ComponentConst.typeColorMap.get(task.getType().toUpperCase()) == null ? String.format(ComponentConst.typeColorMap.get("other"), task.getType()) : ComponentConst.typeColorMap.get(task.getType().toUpperCase())) : "") +
						" " + (StringUtils.isBlank(task.getDate()) ? "" : task.getDate().substring(2)) + (StringUtils.isBlank(task.getFilenum()) ? "" : " <font color='#f2e986'>" + task.getFilenum() + "P</font>") + "</small></html>";
	}
}
