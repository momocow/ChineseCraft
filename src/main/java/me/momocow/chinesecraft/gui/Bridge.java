package me.momocow.chinesecraft.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class Bridge implements IWebViewBridge {
	private MainApp app;
	
	public Bridge (MainApp appIn) {
		this.app = appIn;
	}
	
	@Override
	public void target(String buttonId) {
		if (this.app.getGameDir() == null || !this.app.getGameDir().exists()) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("\u6ce8\u610f!");
			alert.setHeaderText(null);
			alert.setContentText("\u4f60\u4e26\u672a\u9078\u64c7\u4e00\u500b\u6709\u6548\u7684\u904a\u6232\u8def\u5f91!");
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(this.app.getIcon());

			alert.showAndWait();
			return;
		}
		
		this.app.convert(buttonId);
	}

}
