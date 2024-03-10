package sawfowl.wasted.configure;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.configurate.serialize.SerializationException;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.PluginLocale;
import sawfowl.localeapi.api.Text;
import sawfowl.localeapi.api.TextUtils;
import sawfowl.localeapi.api.serializetools.itemstack.SerializedItemStackPlainNBT;

public class Locales {

	private final LocaleService localeService;
	private final boolean json;
	private String[] localesTags;
	private Random random = new Random();
	public Locales(LocaleService localeService, boolean json) {
		this.localeService = localeService;
		this.json = json;
		localeService.createPluginLocale("wasted", ConfigTypes.HOCON, org.spongepowered.api.util.locale.Locales.DEFAULT);
		localeService.createPluginLocale("wasted", ConfigTypes.HOCON, org.spongepowered.api.util.locale.Locales.RU_RU);
		generateDefault();
		generateRu();
		localeService.localesExist("wasted");
	}

	public LocaleService getLocaleService() {
		return localeService;
	}

	public String[] getLocalesTags() {
		if(localesTags != null) return localesTags;
		return localesTags = localeService.getLocalesList().stream().map(Locale::toLanguageTag).collect(Collectors.toList()).stream().toArray(String[]::new);
	}

	public Component getText(Locale locale, Object... path) {
		return getPluginLocale(locale).getComponent(path);
	}

	public Component getTextFromDefault(Object... path) {
		return getPluginLocale(org.spongepowered.api.util.locale.Locales.DEFAULT).getComponent(path);
	}

	public Component getRandomMessage(Locale locale, ServerPlayer player, Object... path) {
		List<Component> components = getPluginLocale(player.locale()).getListComponents(path);
		return getPluginLocale(locale).getComponent("Basic", "Prefix").append(Text.of(components.get(random.nextInt(components.size()))).replace(Placeholders.PLAYER, toText(player.name()).clickEvent(ClickEvent.suggestCommand("/tell " + player.name() + " "))).get());
	}

	public Component getRandomMessage(Locale locale, ServerPlayer player, String killer, Object... path) {
		List<Component> components = getPluginLocale(player.locale()).getListComponents(path);
		return getPluginLocale(locale).getComponent("Basic", "Prefix").append(Text.of(components.get(random.nextInt(components.size()))).replace(new String[] {Placeholders.PLAYER, Placeholders.KILLER}, toText(player.name()).clickEvent(ClickEvent.suggestCommand("/tell " + player.name() + " ")), toText(killer)).get());
	}

	public Component getRandomMessage(Locale locale, ServerPlayer player, String killer, String indirectKiller, Object... path) {
		List<Component> components = getPluginLocale(player.locale()).getListComponents(path);
		return getPluginLocale(locale).getComponent("Basic", "Prefix").append(Text.of(components.get(random.nextInt(components.size()))).replace(new String[] {Placeholders.PLAYER, Placeholders.KILLER, Placeholders.INDIRECT_KILLER}, toText(player.name()).clickEvent(ClickEvent.suggestCommand("/tell " + player.name() + " ")), toText(killer), toText(indirectKiller)).get());
	}

	public Component getRandomMessage(Locale locale, ServerPlayer player, String killer, ItemStack item, Object... path) {
		List<Component> components = getPluginLocale(player.locale()).getListComponents(path);
		return getPluginLocale(locale).getComponent("Basic", "Prefix").append(Text.of(components.get(random.nextInt(components.size()))).replace(new String[] {Placeholders.PLAYER, Placeholders.KILLER, Placeholders.ITEM}, toText(player.name()).clickEvent(ClickEvent.suggestCommand("/tell " + player.name() + " ")), toText(killer), item.asComponent().hoverEvent(HoverEvent.showItem((new SerializedItemStackPlainNBT(item).getItemKey()), item.quantity()))).get());
	}

	public Component getPrefix(Locale locale) {
		return getPluginLocale(locale).getComponent("Basic", "Prefix");
	}

	public boolean isVirtualMessagePath(Object... path) {
		return getPluginLocale(org.spongepowered.api.util.locale.Locales.DEFAULT).getLocaleNode(path).virtual();
	}

	public boolean isVirtualMessagePath(Locale locale, Object... path) {
		return getPluginLocale(locale).getLocaleNode(path).virtual();
	}

	public void addDeathMessage(Component component, ServerPlayer player, Object... path) {
		if(!isVirtualMessagePath(org.spongepowered.api.util.locale.Locales.DEFAULT, path)) return;
		try {
			getPluginLocale(org.spongepowered.api.util.locale.Locales.DEFAULT).getLocaleNode(path).setList(Component.class, Arrays.asList(clear(component)));
			save(org.spongepowered.api.util.locale.Locales.DEFAULT);
		} catch (SerializationException e) {
			e.printStackTrace();
		}
	}

	public void addDeathMessage(Component component, ServerPlayer player, String killer, Object... path) {
		if(!isVirtualMessagePath(org.spongepowered.api.util.locale.Locales.DEFAULT, path)) return;
		try {
			getPluginLocale(org.spongepowered.api.util.locale.Locales.DEFAULT).getLocaleNode(path).setList(Component.class, Arrays.asList(Text.of(clear(component)).replace(killer, Placeholders.KILLER).get()));
			save(org.spongepowered.api.util.locale.Locales.DEFAULT);
		} catch (SerializationException e) {
			e.printStackTrace();
		}
	}

	public void addDeathMessage(Component component, ServerPlayer player, String killer, String killerCustomName, Object... path) {
		if(isVirtualMessagePath(org.spongepowered.api.util.locale.Locales.DEFAULT, path)) return;
		try {
			getPluginLocale(org.spongepowered.api.util.locale.Locales.DEFAULT).getLocaleNode(path).setList(Component.class, Arrays.asList(Text.of(clear(component)).replace(killer, "").replace(killerCustomName, Placeholders.KILLER).get()));
			save(org.spongepowered.api.util.locale.Locales.DEFAULT);
		} catch (SerializationException e) {
			e.printStackTrace();
		}
	}

	private void generateDefault() {
		Locale locale = org.spongepowered.api.util.locale.Locales.DEFAULT;
		boolean save = check(locale, toText("&7[&4Wasted&7]&r "), null, "Basic", "Prefix");
		save = check(locale, toText("&aPlugin has been reloaded."), null, "Basic", "Reload");
		save = check(locale, toText("&cThis command can only be executed by the player."), null, "Basic", "OnlyPlayer");
		save = check(locale, Arrays.asList(toText(Placeholders.PLAYER + " committed suicide")), null, "DeathMessages", "Suicide");
		if(save) save(locale);
	}

	private void generateRu() {
		Locale locale = org.spongepowered.api.util.locale.Locales.RU_RU;
		boolean save = check(locale, toText("&7[&4Wasted&7]&r "), null, "Basic", "Prefix");
		save = check(locale, toText("&aПлагин перезагружен."), null, "Basic", "Reload");
		save = check(locale, toText("&cЭта команда может быть выполненна только игроком."), null, "Basic", "OnlyPlayer");
		save = check(locale, Arrays.asList(toText(Placeholders.PLAYER + " покончил жизнь самоубийством")), null, "DeathMessages", "Suicide");
		if(save) save(locale);
	}

	private PluginLocale getPluginLocale(Locale locale) {
		return localeService.getPluginLocales("wasted").containsKey(locale) ? localeService.getPluginLocales("wasted").get(locale) : localeService.getPluginLocales("wasted").get(org.spongepowered.api.util.locale.Locales.DEFAULT);
	}

	private Component toText(String string) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
	}

	private boolean check(Locale locale, Component value, String comment, Object... path) {
		return getPluginLocale(locale).checkComponent(json, value, comment, path);
	}

	private boolean check(Locale locale, List<Component> value, String comment, Object... path) {
		return getPluginLocale(locale).checkListComponents(json, value, comment, path);
	}

	private void save(Locale locale) {
		getPluginLocale(locale).saveLocaleNode();
	}

	private Component clear(Component component) {
		return TextUtils.deserializeLegacy(Placeholders.PLAYER + TextUtils.serializeLegacy(component));
	}

}
