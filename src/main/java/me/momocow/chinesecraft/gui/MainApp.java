package me.momocow.chinesecraft.gui;

import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import me.momocow.chinesecraft.ChineseCraft;
import me.momocow.chinesecraft.mod.ModLangHelper;
import me.momocow.chinesecraft.mod.ModLangHelper.ModState;
import me.momocow.common.JarUtils;
import me.momocow.common.StringUtils;

public class MainApp  extends Application {
	private Image iconImg = new Image(JarUtils.getStream(ChineseCraft.class, "image/icon.png"));
	private File gameDir;
	private Button chooseDir;
	private TextField choosenDir;
	private TextArea progressMsg;
	private ProgressBar progressbar;
	private Button zhtw;
	private Button zhcn;
	private boolean isWorking = false;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		WebView guiContainer = new WebView();
		this.initWebEngine(guiContainer);
		guiContainer.setMinHeight(120);
		
		this.progressMsg = new TextArea();
		this.progressMsg.setEditable(false);
		this.progressMsg.setMinHeight(200);
		
		Label labelChooseDir = new Label("\u9078\u64c7\u904a\u6232\u8def\u5f91");
		labelChooseDir.setAlignment(Pos.CENTER);
		this.choosenDir = new TextField();
		choosenDir.textProperty().addListener((observable, oldValue, newValue) -> {
			this.gameDir = new File(newValue);
		});
		
		this.chooseDir = new Button("\u9078\u64c7");
		chooseDir.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				DirectoryChooser dirChooser = new DirectoryChooser();
				dirChooser.setTitle("\u9078\u64c7\u904a\u6232\u8def\u5f91");
				
				File defaultDir = ChineseCraft.defaultGameDir;
				if (!defaultDir.exists()) {
					defaultDir = new File(System.getProperty("user.dir"));
				}
				dirChooser.setInitialDirectory(defaultDir);
				
				File selected = dirChooser.showDialog(primaryStage);
				
				String shownDirStr = selected != null ? selected.getAbsolutePath() : "";
				choosenDir.setText(shownDirStr);
			}
		});
		
		this.zhcn = new Button("\u7b80\u4f53\u4e2d\u6587");
		this.zhtw = new Button("\u7e41\u9ad4\u4e2d\u6587");
		
		this.zhcn.setOnAction(event -> {
			this.convert("zh_CN");
		});
		
		this.zhtw.setOnAction(event -> {
			this.convert("zh_TW");
		});
		
		HBox buttons = new HBox(this.zhcn, this.zhtw);
		this.zhcn.prefWidthProperty().bind(buttons.widthProperty().divide(2));
		this.zhtw.prefWidthProperty().bind(buttons.widthProperty().divide(2));
		
		this.zhcn.prefHeightProperty().bind(buttons.heightProperty());
		this.zhtw.prefHeightProperty().bind(buttons.heightProperty());
		
		buttons.setMinHeight(80);
		buttons.setStyle("-fx-font-size: 24;");
		
		this.progressbar = new ProgressBar(0f);
		this.progressbar.setDisable(true);
		HBox progress = new HBox(this.progressbar);
		this.progressbar.prefWidthProperty().bind(progress.widthProperty());
		progress.setAlignment(Pos.TOP_CENTER);
		
		HBox localAccessBar = new HBox(labelChooseDir, choosenDir, chooseDir);
		localAccessBar.setMinHeight(40);
		localAccessBar.setStyle("-fx-font-size: 20;");
		HBox.setHgrow(choosenDir, Priority.ALWAYS);
		VBox vbox = new VBox(guiContainer, buttons, localAccessBar, progressMsg, progress);
		
		primaryStage.setTitle("ChineseCraft v" + ChineseCraft.version);
		primaryStage.getIcons().add(iconImg);
		primaryStage.setScene(new Scene(vbox, 640, 540));
		primaryStage.setResizable(false);
		primaryStage.sizeToScene();
		primaryStage.show();
	}
	
	public void convert (String choice) {
		if (this.gameDir == null || !this.gameDir.exists()) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("\u6ce8\u610f!");
			alert.setHeaderText(null);
			alert.setContentText("\u4f60\u4e26\u672a\u9078\u64c7\u4e00\u500b\u6709\u6548\u7684\u904a\u6232\u8def\u5f91!");
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(this.getIcon());

			alert.showAndWait();
			return;
		}
		
		if (this.isWorking ) {
			return;
		}
		
		this.chooseDir.setDisable(true);
		this.choosenDir.setDisable(true);
		this.progressbar.setDisable(false);
		this.zhcn.setDisable(true);
		this.zhtw.setDisable(true);

		Task<String> task = new Task<String>() {
			private int processedMods = 0;
			
			@Override
			protected String call() throws Exception {
				long start = System.currentTimeMillis();
				String ret = ModLangHelper.convert(MainApp.this.gameDir, choice, (modCount, currentMod, state) -> {
					if (state == ModState.NOT_A_MOD ) {
						this.updateProgress(processedMods, modCount);
					}
					
					if (state == ModState.STARTING ) {
						int tmpDone = this.processedMods + 1;
						Platform.runLater(new Runnable() {
							public void run() {
								MainApp.this.progressMsg.appendText("(" + tmpDone + "/"+ modCount + ") " + currentMod + "...  ");
							}
						});
					}
					
					if (state != ModState.NOT_A_MOD && state != ModState.STARTING) {
						this.processedMods ++;
						this.updateProgress(processedMods, modCount);
					}
					
					if (state == ModState.ALREADY_HAS_TARGET) {
						Platform.runLater(new Runnable() {
							public void run() {
								MainApp.this.progressMsg.appendText("(\u76ee\u6a19\u8a9e\u8a00\u5df2\u5b58\u5728!)\n");
							}
						});
					} else if (state == ModState.SRC_NOT_FOUND) {
						Platform.runLater(new Runnable() {
							public void run() {
								MainApp.this.progressMsg.appendText("(\u6c92\u6709\u53ef\u7528\u4f86\u6e90!)\n");
							}
						});
					} else if (state == ModState.TRANSLATED) {
						Platform.runLater(new Runnable() {
							public void run() {
								MainApp.this.progressMsg.appendText("(\u5df2\u7ffb\u8b6f!)\n");
							}
						});
					}
				});
				
				long elapsed = System.currentTimeMillis() - start;
				int elapsedHour = (int) (elapsed / 3600000);
				int elapsedMinute = (int) ((elapsed - elapsedHour * 3600000) / 60000);
				int elapsedSecond = (int) ((elapsed - elapsedMinute * 60000) / 1000);
				
				String elapsedMsg = "\u8f49\u63db\u5b8c\u6210 (\u5171 " + processedMods + " \u500b\u6a21\u7d44, \u8017\u6642 "
						+ elapsedHour + " \u5c0f\u6642 " + elapsedMinute + " \u5206 " + elapsedSecond + " \u79d2)!\n";
				
				Platform.runLater(new Runnable() {
					public void run() {
						MainApp.this.progressMsg.appendText(elapsedMsg);
					}
				});
				
				return ret;
			}
		};
		task.setOnSucceeded(e -> {
			String result = task.getValue();
			
			AlertType resType = AlertType.INFORMATION;
			String resTitle = "\u5b8c\u6210";
			
			if (result != null && !result.isEmpty()) {
				resType = AlertType.WARNING;
				resTitle = "\u6ce8\u610f";
			} else {
				result = "\u8f49\u63db\u5b8c\u7562!";
				ChineseCraft.makeCache(this.gameDir);
			}
			
			Alert alert = new Alert(resType);
			alert.setTitle(resTitle);
			alert.setHeaderText(null);
			alert.setContentText(result);
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(this.getIcon());
			alert.showAndWait();
			
			this.progressbar.progressProperty().unbind();
						
			this.chooseDir.setDisable(false);
			this.choosenDir.setDisable(false);
			this.progressbar.setDisable(true);
			this.zhcn.setDisable(false);
			this.zhtw.setDisable(false);
			this.progressbar.setProgress(0);
			this.choosenDir.setText("");
			this.gameDir = null;
			this.isWorking = false;
			
			this.progressMsg.appendText("*******************************************************\n");
		});
		
		this.progressbar.progressProperty().bind(task.progressProperty());
		new Thread(task).start();
		this.isWorking = true;
	}

	public File getGameDir () {
		return this.gameDir;
	}
	
	public Image getIcon () {
		return this.iconImg;
	}

	private String replaceResourcePaths(String html) { 
		html = StringUtils.replaceEachBy(html, "%(.*?)%", (matched, memorized)->{
			 return ChineseCraft.class.getClassLoader().getResource(memorized.get(0)).toString();
		 });
		return html;
	}
	
	private void initWebEngine (WebView guiContainer) {
		WebEngine engine = guiContainer.getEngine();
		String content = JarUtils.getTextResource(ChineseCraft.mainJar, "index.html");
		engine.loadContent(replaceResourcePaths(content));
	}
}
