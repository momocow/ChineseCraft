package me.momocow.chinesecraft.gui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GuiLobbyPane extends VBox {
	public GuiLobbyPane() {
		super(20);
		
		Label labelTarget = new Label("\u76ee\u6a19\u8a9e言");
		
		Button targetTraditional = new Button("\u7e41\u9ad4\u4e2d\u6587");
		Button targetSimplified = new Button("\u7c21\u9ad4\u4e2d\u6587");
		
		HBox container = new HBox(targetTraditional, targetSimplified);
		
		this.getChildren().addAll(labelTarget, container);
	}
}
