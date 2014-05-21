package org.arong.book.ui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.arong.book.ui.CursorManager;
/**
 * 封装JMenuItem,使构造函数可以设置文本,name值,鼠标监听或者添加其他子项<br>
 * 如果有子项，则子项之间默认设有分割线
 * @author 阿荣
 * @since 2013-9-1
 *
 */
public class AJMenuItem extends JMenuItem implements ActionListener{

	private static final long serialVersionUID = -1883326507604845312L;
	
	public AJMenuItem(String text, String name, MouseListener mouseListener){
		super(text);
		this.setName(name);
		this.setCursor(CursorManager.getPointerCursor());
		this.addMouseListener(mouseListener);
	}
	
	public AJMenuItem(String text, String name, Component... components){
		super(text);
		this.setName(name);
		this.setCursor(CursorManager.getPointerCursor());
		for (int i = 0; i < components.length; i++) {
			this.add(components[i]);
			if(i != components.length - 1){
				this.add(new JSeparator());
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		System.out.println(e);
	}
}
