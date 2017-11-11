package me.momocow.chinesecraft.mod;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import me.momocow.chinesecraft.util.TriConsumer;
import me.momocow.common.ChineseConverter;
import me.momocow.common.JarUtils;

public class ModLangHelper {
	public static enum ModState {
		SRC_NOT_FOUND, ALREADY_HAS_TARGET, TRANSLATED, NOT_A_MOD, STARTING
	}
	
	private static ChineseConverter converter = ChineseConverter.get();
	
	/**
	 * @param gameDir
	 * @param targetLangIn
	 * @return null or empty string if success; otherwise a reason is returned.
	 */
	public static String convert (File gameDir, String targetLangIn, TriConsumer<Integer, String, ModState> progressObserver) {
		if (!Arrays.asList(new String[]{"zh_TW", "zh_CN"}).contains(targetLangIn)) {
			return "\u76ee\u6a19\u8a9e\u8a00\u932f\u8aa4!";
		}
		
		String srcLang = targetLangIn.equals("zh_TW") ? "zh_CN" : "zh_TW";
		
		File modsDir = new File(gameDir.getAbsolutePath(), "mods");
		if (!modsDir.exists()) {
			return "mods\u8cc7\u6599\u593e\u4e0d\u5b58\u5728!";
		}
		
		File[] mods = modsDir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		    	name = name.toLowerCase();
		        return name.endsWith(".zip") || name.endsWith(".jar");
		    }
		});
		
		progressObserver.accept(mods.length, "", ModState.NOT_A_MOD);
		
		
		for (File mod : mods) {
			progressObserver.accept(mods.length, mod.getName(), ModState.STARTING);

			boolean hasTarget = false;
			String srcAsset = "", targetAsset = "";
			for (String assets : JarUtils.listFiles(mod, "assets")) { 
				if (assets.endsWith("lang/" + srcLang + ".lang")) {
					srcAsset = assets;
				} else if (assets.endsWith("lang/" + targetLangIn + ".lang")){
					hasTarget = true;
					break;
				}
			}
			
			if (hasTarget) {
				progressObserver.accept(mods.length, mod.getName(), ModState.ALREADY_HAS_TARGET);
				continue;
			}
			
			if (!srcAsset.isEmpty()) {
				String originalContent = JarUtils.getTextResource(mod, srcAsset);
				String converted = "";
				switch (targetLangIn) {
					case "zh_TW":
						converted = converter.toTW(originalContent);
						break;
					case "zh_CN":
						converted = converter.toCN(originalContent);
				}

				if (converted.length() != originalContent.length()) {
					return "\u7e41\u7c21\u8f49\u63db\u524d\u5f8c\u5b57\u6578\u4e0d\u7b26!";
				}
				
				targetAsset = srcAsset.replaceAll("zh_[TC][WN]\\.lang$", targetLangIn + ".lang");
				JarUtils.appendFile(mod, targetAsset, converted);
				progressObserver.accept(mods.length, mod.getName(), ModState.TRANSLATED);
			} else {
				progressObserver.accept(mods.length, mod.getName(), ModState.SRC_NOT_FOUND);
			}
		}
		
		return null;
	}
}
