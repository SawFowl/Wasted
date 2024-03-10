package sawfowl.wasted.configure;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class Config {

	public Config() {}
	@Setting("ConsoleDeathMessage")
	@Comment("All messages are stored in localization files.")
	private boolean consoleDeathMessage = true;

	public boolean isConsoleDeathMessage() {
		return consoleDeathMessage;
	}

}
