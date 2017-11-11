package me.momocow.chinesecraft;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import me.momocow.chinesecraft.gui.MainApp;
import me.momocow.common.JarUtils;

public class ChineseCraft {	
	public static String version;
	public static File mainJar = JarUtils.getJarFile(ChineseCraft.class);
	public static File defaultGameDir = new File(System.getenv("APPDATA") , ".minecraft");
	public static File appdata = new File (System.getenv("APPDATA"), "ChineseCraft");
	
	public static void main (String[] args) {
		ChineseCraft.init();
		Application.launch(MainApp.class, args);
	}
	
	public static void makeCache (File gameDir) {
		if (!appdata.exists()) {
			appdata.mkdirs();
		}
		
		List<String> content = new ArrayList<>();
		content.add("gameDir=" + gameDir.getAbsolutePath());
		
		File cacheFile = new File(appdata.getAbsolutePath(), "latest.dat");
		try {
			Files.write(cacheFile.toPath(), content, Charset.forName("UTF-8"));
		} catch (IOException e) { }
	}

	private static void init() {
		version = JarUtils.getManifest(mainJar, "Implementation-Version");

		File cacheFile = new File(appdata.getAbsolutePath(), "latest.dat");
		if (cacheFile.exists()) {
			try {
				List<String> cachedLines = Files.readAllLines(cacheFile.toPath());
				for (String line : cachedLines) {
					if (line.contains("gameDir=")) {
						String[] tokens = line.split("=");
						defaultGameDir = new File(tokens[1].trim());
					}
				}
			} catch (IOException e) {  }
		}
	}
}
