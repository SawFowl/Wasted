package mr_krab.wasted.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;

import mr_krab.wasted.Wasted;

public class ConfigUtil {
	
	private final static Wasted plugin = Wasted.getInstance();

	public ConfigUtil() {
		this.saveConfig();
	}

	public void saveConfig() {
		String path = "/assets/" + "wasted" + "/";
		File config = new File(plugin.configFile + File.separator + "config.conf");
		if (!config.exists()) {
        	try {
        		plugin.getLogger().info("Save config");
        		URI u = getClass().getResource(path + "config.conf").toURI();
				FileSystem jarFS = FileSystems.newFileSystem(URI.create(u.toString().split("!")[0]), new HashMap<>());
				Files.copy(jarFS.getPath(u.toString().split("!")[1]), new File(plugin.configFile + File.separator + "config.conf").toPath());
				jarFS.close();
        	} catch (IOException ex) {
        		plugin.getLogger().error("Failed to save config");
        		ex.printStackTrace();
        	} catch (URISyntaxException e) {
        		plugin.getLogger().error("Failed to save config");
				e.printStackTrace();
			}
		}
	}

  	// Check the configuration version.
	public void checkConfigVersion() {
		String path = "/assets/" + "wasted" + "/";
		File oldConfig = new File(plugin.getConfigDir() + File.separator + "config.conf");
		File renamedConfig = new File(plugin.getConfigDir() + File.separator + "ConfigOld.conf");
		if (!plugin.rootNode.getNode("Config-Version").isVirtual()) {
			// This part can be supplemented.
			if (plugin.rootNode.getNode("Config-Version").getInt() != 1) {
				plugin.getLogger().warn("Attention!!! The version of your configuration file does not match the current one!");
				if(oldConfig.exists()) {
					oldConfig.renameTo(renamedConfig);
				}
				plugin.getLogger().warn("Your config has been replaced with the default config. Old config see in the file ConfigOld.txt.");
	        	try {
	        		URI u = getClass().getResource(path + "config.conf").toURI();
					FileSystem jarFS = FileSystems.newFileSystem(URI.create(u.toString().split("!")[0]), new HashMap<>());
					Files.copy(jarFS.getPath(u.toString().split("!")[1]), new File(plugin.configFile + File.separator + "config.conf").toPath());
					jarFS.close();
	        	} catch (IOException ex) {
	        		ex.printStackTrace();
	        	} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		} else {
			// Action in the absence of the configuration version.
			plugin.getLogger().warn("Attention!!! The version of your configuration file does not match the current one!");
			if(oldConfig.exists()) {
				oldConfig.renameTo(renamedConfig);
			}
			plugin.getLogger().warn("Your config has been replaced with the default config. Old config see in the file ConfigOld.txt.");
        	try {
        		URI u = getClass().getResource(path + "config.conf").toURI();
				FileSystem jarFS = FileSystems.newFileSystem(URI.create(u.toString().split("!")[0]), new HashMap<>());
				Files.copy(jarFS.getPath(u.toString().split("!")[1]), new File(plugin.configFile + File.separator + "config.conf").toPath());
				jarFS.close();
        	} catch (IOException ex) {
        		ex.printStackTrace();
        	} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
	
}
