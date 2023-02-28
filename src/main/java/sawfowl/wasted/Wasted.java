package sawfowl.wasted;

import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

import sawfowl.localeapi.event.LocaleServiseEvent;
import sawfowl.wasted.configure.Config;
import sawfowl.wasted.configure.Locales;

@Plugin("wasted")
public class Wasted {
	private Logger logger;

	private Wasted instance;
	private PluginContainer pluginContainer;
	private Path configDir;
	private ConfigurationReference<CommentedConfigurationNode> configurationReference;
	private ValueReference<Config, CommentedConfigurationNode> config;
	private Locales locales;
	ConfigurationOptions options;

	public Wasted getInstance() {
		return instance;
	}

	public PluginContainer getPluginContainer() {
		return pluginContainer;
	}

	public Logger getLogger() {
		return logger;
	}

	public Config getConfig() {
		return config.get();
	}

	public Locales getLocales() {
		return locales;
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
		options = event.getLocaleService().getConfigurationOptions();
		try {
			configurationReference = HoconConfigurationLoader.builder().defaultOptions(options).path(configDir.resolve("Config.conf")).build().loadToReference();
			config = configurationReference.referenceTo(Config.class);
			if(!configDir.resolve("Config.conf").toFile().exists()) configurationReference.save();
		} catch (ConfigurateException e) {
			logger.warn(e.getLocalizedMessage());
		}
		locales = new Locales(event.getLocaleService(), getConfig().isJsonLocales());
	}

	@Listener
	public void onServerStarted(StartedEngineEvent<Server> event) {
		Sponge.eventManager().registerListeners(pluginContainer, new DeathListener(instance));
	}

	@Listener
	public void onReload(RefreshGameEvent event) {
		try {
			configurationReference.load();
			config = configurationReference.referenceTo(Config.class);
		} catch (ConfigurateException e) {
			logger.warn(e.getLocalizedMessage());
		}
	}

}
