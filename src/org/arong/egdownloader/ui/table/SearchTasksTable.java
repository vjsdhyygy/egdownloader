package org.arong.egdownloader.ui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.arong.egdownloader.model.SearchTask;
import org.arong.egdownloader.ui.ComponentConst;
import org.arong.egdownloader.ui.CursorManager;
import org.arong.egdownloader.ui.FontConst;
import org.arong.egdownloader.ui.IconManager;
import org.arong.egdownloader.ui.popmenu.SearchWindowPopMenu;
import org.arong.egdownloader.ui.swing.AJLabel;
import org.arong.egdownloader.ui.window.SearchComicWindow;
import org.arong.egdownloader.ui.window.SearchCoverWindow;
/**
 * 搜索结果表格
 * @author dipoo
 * @since 2015-03-11
 */
public class SearchTasksTable extends JTable {

	private static final long serialVersionUID = 8917533573337061263L;
	private List<SearchTask> tasks;
	public SearchComicWindow comicWindow;
	public JPopupMenu popupMenu;//右键菜单
	public int currentRowIndex = -1;
	
	public void changeModel(List<SearchTask> tasks){
		this.tasks = tasks;
		TableModel tableModel = new SearchTaskTableModel(this.tasks);
		this.setModel(tableModel);//设置数据模型
	}
	
	public SearchTasksTable(int x, int y, int width, int height, final List<SearchTask> tasks_, SearchComicWindow comicWindow_){
		this.comicWindow = comicWindow_;
		this.tasks = (tasks_ == null ? new ArrayList<SearchTask>() : tasks_);
		
		if(this.tasks.size() > ComponentConst.MAX_TASK_PAGE){
			height = ComponentConst.MAX_TASK_PAGE * 25;
		}
		this.setBounds(x, y, width, height);
//		this.setShowGrid(true);//显示单元格边框
//		this.setCellSelectionEnabled(false);//选择单元格
		this.setCursor(CursorManager.getPointerCursor());//光标变手型
		this.getTableHeader().setReorderingAllowed(false);//不可移动列
		this.setBackground(Color.WHITE);
//		this.setOpaque(false);//设为透明
		TableModel tableModel = new SearchTaskTableModel(this.tasks);
		this.setModel(tableModel);//设置数据模型
		this.setRowHeight(300);
//		TaskTableCellRenderer renderer = new TaskTableCellRenderer();
//		renderer.setHorizontalAlignment(JLabel.CENTER);   
		this.setDefaultRenderer(Object.class, new TableCellRenderer() {
			private Color c = new Color(47,110,178);
			private Color uploaderColor = Color.getHSBColor(122, 255, 122);
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				
				if(isSelected){
					c = Color.BLUE;
				}else{
					c = Color.DARK_GRAY;
				}
				if(value == null) return null;
				
				SearchTasksTable tb = (SearchTasksTable) table;
				TableColumn tc = tb.getColumnModel().getColumn(column);
				if(column == 0){//类型
					tc.setPreferredWidth(105);
					tc.setMaxWidth(150);
					
					JLabel l = new AJLabel("", c, JLabel.LEFT);
					if(tasks.get(row).getType() != null){
						ImageIcon icon = IconManager.getIcon(tasks.get(row).getType().toLowerCase());
						if(icon != null){
							l.setIcon(icon);
						}else{
							l.setText(tasks.get(row).getType());
						}
					}
					return l;
				}else if(column == 1){//名称
					SearchTask task = tasks.get(row);
					boolean contains = comicWindow.mainWindow.tasks.getTaskMap().containsKey(task.getUrl().replaceAll("https://", "http://")) || comicWindow.mainWindow.tasks.getTaskMap().containsKey(task.getUrl().substring(0, task.getUrl().length() - 1).replaceAll("https://", "http://"));
					tc.setPreferredWidth(700);
					tc.setMaxWidth(1800);
					JLabel l = new AJLabel((contains ? "【已存在】" : "") + value.toString(), c, isSelected ? FontConst.Microsoft_BOLD_11 : FontConst.Microsoft_PLAIN_11, JLabel.LEFT);
					if(task.getBtUrl() != null){
						try{
							l.setIcon(IconManager.getIcon("t"));
						}catch (Exception e) {
							
						}
					}
					l.setToolTipText(value.toString());
					return l;
				}else if(column == 2){//图片个数
					tc.setPreferredWidth(60);
					tc.setMaxWidth(80);
					return new AJLabel("   " + value.toString(), c, FontConst.Microsoft_PLAIN_11, JLabel.LEFT);
				}else if(column == 3){//评分
					tc.setPreferredWidth(80);
					tc.setMaxWidth(120);
					return new AJLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;" + value.toString() + "★</html>", c, FontConst.Microsoft_PLAIN_11, JLabel.LEFT);
				}else if(column == 4){//上传者
					tc.setPreferredWidth(100);
					tc.setMaxWidth(150);
					JLabel l = new AJLabel(value.toString(), c, FontConst.Microsoft_PLAIN_11, JLabel.LEFT);
					l.setForeground(uploaderColor);
					l.setToolTipText("点击搜索该上传者的上传的漫画");
					return l;
				}else if(column == 5){//发布时间
					tc.setPreferredWidth(100);
					tc.setMaxWidth(150);
					return new AJLabel(value.toString(), c, FontConst.Microsoft_PLAIN_11, JLabel.LEFT);
				}else{
					return null;
				}
			}
		});//设置渲染器
//		this.getTableHeader().setDefaultRenderer(new TaskTableHeaderRenderer());
		//单元格监听
		this.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e){
				final SearchTasksTable table = comicWindow.searchTable;
				int rowIndex = table.rowAtPoint(e.getPoint());
				int columnIndex = table.columnAtPoint(e.getPoint());
				if(columnIndex == 0){
					SearchTask task = table.getTasks().get(rowIndex);
					//切换行
					if(rowIndex != currentRowIndex){
						currentRowIndex = rowIndex;
						if(comicWindow.coverWindow == null){
							comicWindow.coverWindow = new SearchCoverWindow(comicWindow);
						}
						comicWindow.coverWindow.showCover(task, new Point(e.getXOnScreen() + 50, e.getYOnScreen()));
					}
				}else{
					if(comicWindow.coverWindow != null){
						comicWindow.coverWindow.setVisible(false);
						currentRowIndex = -1;
					}
				}
			}
		});
		this.addMouseListener(new MouseAdapter() {
			
			public void mouseExited(MouseEvent e) {
				if(comicWindow.coverWindow != null){
					comicWindow.coverWindow.setVisible(false);
					currentRowIndex = -1;
				}
			}
			public void mouseClicked(MouseEvent e) {
				final SearchTasksTable table = (SearchTasksTable)e.getSource();
				//获取点击的行数
				int rowIndex = table.rowAtPoint(e.getPoint());
				comicWindow.selectTaskIndex = rowIndex;
				int columnIndex = table.columnAtPoint(e.getPoint());
				//左键
				if(e.getButton() == MouseEvent.BUTTON1){
					//点击上传者
					if(columnIndex == 4){
						comicWindow.doSearch("uploader:" + tasks.get(rowIndex).getUploader());
					}
				}
				//右键
				else if(e.getButton() == MouseEvent.BUTTON3){
					//使之选中
					table.setRowSelectionInterval(rowIndex, rowIndex);
					if(comicWindow.popMenu == null){
						comicWindow.popMenu = new SearchWindowPopMenu(comicWindow.mainWindow);
					}
					SearchTask task = comicWindow.searchTasks.get(comicWindow.selectTaskIndex);
					boolean contains = comicWindow.mainWindow.tasks.getTaskMap().containsKey(task.getUrl().replaceAll("https://", "http://")) || comicWindow.mainWindow.tasks.getTaskMap().containsKey(task.getUrl().substring(0, task.getUrl().length() - 1).replaceAll("https://", "http://"));
					if(contains){
						comicWindow.popMenu.openPictureItem.setVisible(true);
						comicWindow.popMenu.downItem.setVisible(false);
					}else{
						comicWindow.popMenu.openPictureItem.setVisible(false);
						comicWindow.popMenu.downItem.setVisible(true);
					}
					if(StringUtils.isNotBlank(task.getBtUrl())){
						comicWindow.popMenu.openBtPageItem.setVisible(true);
					}else{
						comicWindow.popMenu.openBtPageItem.setVisible(false);
					}
					comicWindow.popMenu.show(table, e.getPoint().x, e.getPoint().y);
				}
			}
		});
	}
	
	public List<SearchTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<SearchTask> tasks) {
		this.tasks = tasks;
	}

}