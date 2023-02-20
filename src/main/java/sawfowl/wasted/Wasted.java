package sawfowl.wasted;

import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.event.LocaleServiseEvent;
import sawfowl.wasted.configure.Config;

@Plugin("wasted")
public class Wasted {
	private Logger logger;

	private Wasted instance;
	private LocaleService api;
	private PluginContainer pluginContainer;
	private Path configDir;
	private ConfigurationReference<CommentedConfigurationNode> configurationReference;
	private ValueReference<Config, CommentedConfigurationNode> config;

	public Wasted getInstance() {
		return instance;
	}

	public PluginContainer getPluginContainer() {
		return pluginContainer;
	}

	public Logger getLogger() {
		return logger;
	}

	public LocaleService getAPI() {
		return api;
	}

	public Config getConfig() {
		return config.get();
	}

	@Inject
	public Wasted(PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path configDirectory) {
		instance = this;
		this.pluginContainer = pluginContainer;
		configDir = configDirectory;
		logger = LogManager.getLogger("\033[31mWasted\033[0m");
	}

	@Listener
	public void onLocaleServisePostEvent(LocaleServiseEvent.Construct event) {
		try {
			configurationReference = HoconConfigurationLoader.builder().defaultOptions(event.getLocaleService().getConfigurationOptions()).path(configDir.resolve("Config.conf")).build().loadToReference();
			config = configurationReference.referenceTo(Config.class);
			if(!configDir.resolve("Config.conf").toFile().exists()) configurationReference.save();
		} catch (ConfigurateException e) {
			logger.warn(e.getLocalizedMessage());
		}
	}

}
