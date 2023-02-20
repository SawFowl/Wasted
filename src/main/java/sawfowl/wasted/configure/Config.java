package sawfowl.wasted.configure;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class Config {

	public Config() {}

	@Setting("Debug")
	private boolean debug = true;
	@Setting("JsonLocales")
	private boolean jsonLocales = true;
	@Setting("ConsoleDeathMessage")
	private boolean consoleDeathMessage = true;

	public boolean isDebug() {
		return debug;
	}

	public boolean isJsonLocales() {
		return jsonLocales;
	}

	public boolean isConsoleDeathMessage() {
		return consoleDeathMessage;
	}

}
