package sawfowl.wasted.configure;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class Config {

	public Config() {}

	@Setting("JsonLocales")
	private boolean jsonLocales = false;
	@Setting("ConsoleDeathMessage")
	private boolean consoleDeathMessage = true;

	public boolean isJsonLocales() {
		return jsonLocales;
	}

	public boolean isConsoleDeathMessage() {
		return consoleDeathMessage;
	}

}
