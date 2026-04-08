package sawfowl.wasted;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocalesList;
import sawfowl.localeapi.api.TextUtils;
import sawfowl.localeapi.api.Translation;
import sawfowl.localeapi.api.config.ReferencedConfig;
import sawfowl.localeapi.api.config.locale.PluginLocale;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.api.services.ConfigurationService;
import sawfowl.localeapi.api.services.LocaleService;
import sawfowl.wasted.configure.Config;
import sawfowl.wasted.configure.Placeholders;

@Plugin("wasted")
public class Wasted {
	private Logger logger;

	private Wasted instance;
	private PluginContainer pluginContainer;
	private ReferencedConfig<Config> config;
	private LocalesList<Translation> locales;

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

	public LocalesList<Translation> getLocales() {
		return locales;
	}

	@Inject
	public Wasted(PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path configDirectory) {
		instance = this;
		this.pluginContainer = pluginContainer;
		logger = LogManager.getLogger("\033[31mWasted\033[0m");
		locales = LocaleService.getInstance().createLocales(pluginContainer);
		if(!locales.contains(Locales.DEFAULT)) generateDefault();
		if(!locales.contains(Locales.RU_RU)) generateRu();
		config = ConfigurationService.getInstance().createReferencedConfig(pluginContainer, Config.class).setPath(configDirectory).setName("Config").setType(ConfigTypes.HOCON).setItemStackSerializerType(ItemStackSerializerType.JSON).build();
	}

	@Listener
	public void onServerStarted(StartedEngineEvent<Server> event) {
		Sponge.eventManager().registerListeners(pluginContainer, new DeathListener(instance), MethodHandles.lookup());
	}

	@Listener
	public void onReload(RefreshGameEvent event) {
		config.load();
	}

	private void generateDefault() {
		PluginLocale locale = locales.createSimpleTranslation(ConfigTypes.HOCON, Locales.DEFAULT);
		locale.addIfNotExist(TextUtils.deserializeLegacy("&7[&4Wasted&7]&r "), null, "Basic", "Prefix");
		locale.addIfNotExist(TextUtils.deserializeLegacy("&aPlugin has been reloaded."), null, "Basic", "Reload");
		locale.addIfNotExist(TextUtils.deserializeLegacy("&cThis command can only be executed by the player."), null, "Basic", "OnlyPlayer");
		locale.addIfNotExist(TextUtils.deserializeLegacy(Placeholders.PLAYER + " committed suicide"), null, "DeathMessages", "Suicide");
		locale.save();
	}

	private void generateRu() {
		PluginLocale locale = locales.createSimpleTranslation(ConfigTypes.HOCON, Locales.RU_RU);
		locale.addIfNotExist(TextUtils.deserializeLegacy("&7[&4Wasted&7]&r "), null, "Basic", "Prefix");
		locale.addIfNotExist(TextUtils.deserializeLegacy("&aПлагин перезагружен."), null, "Basic", "Reload");
		locale.addIfNotExist(TextUtils.deserializeLegacy("&cЭта команда может быть выполненна только игроком."), null, "Basic", "OnlyPlayer");
		locale.addIfNotExist(TextUtils.deserializeLegacy(Placeholders.PLAYER + " покончил жизнь самоубийством"), null, "DeathMessages", "Suicide");
		locale.save();
	}

}
