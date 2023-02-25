package sawfowl.wasted.configure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import sawfowl.localeapi.api.TextUtils;
import sawfowl.localeapi.serializetools.SerializedItemStack;
import sawfowl.localeapi.utils.AbstractLocaleUtil;

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
		return getAbstractLocaleUtil(locale).getComponent(json, path);
	}

	public Component getTextWithReplaced(Locale locale, Map<String, String> map, Object... path) {
		return replace(getText(locale, path), map);
	}

	public Component getTextReplaced(Locale locale, Map<String, Component> map, Object... path) {
		return replaceComponent(getText(locale, path), map);
	}

	public Component getTextFromDefault(Object... path) {
		return getAbstractLocaleUtil(org.spongepowered.api.util.locale.Locales.DEFAULT).getComponent(json, path);
	}

	public Component getRandomMessage(Locale locale, ServerPlayer player, Object... path) {
		List<Component> components = getAbstractLocaleUtil(player.locale()).getListComponents(json, path);
		return getAbstractLocaleUtil(locale).getComponent(json, "Basic", "Prefix").append(TextUtils.replace(components.get(random.nextInt(components.size())), Placeholders.PLAYER, toText(player.name()).clickEvent(ClickEvent.suggestCommand("/tell " + player.name() + " "))));
	}

	public Component getRandomMessage(Locale locale, ServerPlayer player, String killer, Object... path) {
		List<Component> components = getAbstractLocaleUtil(player.locale()).getListComponents(json, path);
		return getAbstractLocaleUtil(locale).getComponent(json, "Basic", "Prefix").append(TextUtils.replace(components.get(random.nextInt(components.size())), (new String[] {Placeholders.PLAYER, Placeholders.KILLER}), (new Component[] {toText(player.name()).clickEvent(ClickEvent.suggestCommand("/tell " + player.name() + " ")), toText(killer)})));
	}

	public Component getRandomMessage(Locale locale, ServerPlayer player, String killer, String indirectKiller, Object... path) {
		List<Component> components = getAbstractLocaleUtil(player.locale()).getListComponents(json, path);
		return getAbstractLocaleUtil(locale).getComponent(json, "Basic", "Prefix").append(TextUtils.replace(components.get(random.nextInt(components.size())), (new String[] {Placeholders.PLAYER, Placeholders.KILLER, Placeholders.INDIRECT_KILLER}), (new Component[] {toText(player.name()).clickEvent(ClickEvent.suggestCommand("/tell " + player.name() + " ")), toText(killer), toText(indirectKiller)})));
	}

	public Component getRandomMessage(Locale locale, ServerPlayer player, String killer, ItemStack item, Object... path) {
		List<Component> components = getAbstractLocaleUtil(player.locale()).getListComponents(json, path);
		return getAbstractLocaleUtil(locale).getComponent(json, "Basic", "Prefix").append(TextUtils.replaceToComponents(components.get(random.nextInt(components.size())), (new String[] {Placeholders.PLAYER, Placeholders.KILLER, Placeholders.ITEM}), (new Component[] {toText(player.name()).clickEvent(ClickEvent.suggestCommand("/tell " + player.name() + " ")), toText(killer), item.asComponent().hoverEvent(HoverEvent.showItem((new SerializedItemStack(item).getItemKey()), item.quantity()))})));
	}

	public Component getPrefix(Locale locale) {
		return getAbstractLocaleUtil(locale).getComponent(json, "Basic", "Prefix");
	}

	public boolean containsMessagePath(Object... path) {
		return !getAbstractLocaleUtil(org.spongepowered.api.util.locale.Locales.DEFAULT).getLocaleNode(path).virtual();
	}

	public boolean containsMessagePath(Locale locale, Object... path) {
		return !getAbstractLocaleUtil(locale).getLocaleNode(path).virtual();
	}

	public void addDeathMessage(Component component, ServerPlayer player, Object... path) {
		if(!getAbstractLocaleUtil(org.spongepowered.api.util.locale.Locales.DEFAULT).getLocaleNode(path).virtual()) return;
		component = toText(Placeholders.PLAYER + string(component));
		List<String> messages = new ArrayList<>();
		String message = json ? TextUtils.serializeJson(component) : TextUtils.serializeLegacy(component);
		if(messages.contains(message)) return;
		messages.add(message);
		try {
			getAbstractLocaleUtil(org.spongepowered.api.util.locale.Locales.DEFAULT).getLocaleNode(path).setList(String.class, messages);
			save(org.spongepowered.api.util.locale.Locales.DEFAULT);
		} catch (SerializationException e) {
			e.printStackTrace();
		}
	}

	public void addDeathMessage(Component component, ServerPlayer player, String killer, Object... path) {
		if(!getAbstractLocaleUtil(org.spongepowered.api.util.locale.Locales.DEFAULT).getLocaleNode(path).virtual()) return;
		component = toText(Placeholders.PLAYER + string(component).replace(killer, "") + Placeholders.KILLER);
		List<String> messages = new ArrayList<>();
		String message = json ? TextUtils.serializeJson(component) : TextUtils.serializeLegacy(component);
		if(messages.contains(message)) return;
		messages.add(message);
		try {
			getAbstractLocaleUtil(org.spongepowered.api.util.locale.Locales.DEFAULT).getLocaleNode(path).setList(String.class, messages);
			save(org.spongepowered.api.util.locale.Locales.DEFAULT);
		} catch (SerializationException e) {
			e.printStackTrace();
		}
	}

	public void addDeathMessage(Component component, ServerPlayer player, String killer, String killerCustomName, Object... path) {
		if(!getAbstractLocaleUtil(org.spongepowered.api.util.locale.Locales.DEFAULT).getLocaleNode(path).virtual()) return;
		component = toText(Placeholders.PLAYER + string(component).replace(killer, "").replace(killerCustomName, "") + Placeholders.KILLER);
		List<String> messages = new ArrayList<>();
		String message = json ? TextUtils.serializeJson(component) : TextUtils.serializeLegacy(component);
		if(messages.contains(message)) return;
		messages.add(message);
		try {
			getAbstractLocaleUtil(org.spongepowered.api.util.locale.Locales.DEFAULT).getLocaleNode(path).setList(String.class, messages);
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

	private Component replace(Component component, Map<String, String> map) {
		return TextUtils.replace(component, map);
	}

	private Component replaceComponent(Component component, Map<String, Component> map) {
		return TextUtils.replaceToComponents(component, map);
	}

	private AbstractLocaleUtil getAbstractLocaleUtil(Locale locale) {
		return localeService.getPluginLocales("wasted").getOrDefault(locale, localeService.getPluginLocales("wasted").get(org.spongepowered.api.util.locale.Locales.DEFAULT));
	}

	private Component toText(String string) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
	}

	private boolean check(Locale locale, Component value, String comment, Object... path) {
		return getAbstractLocaleUtil(locale).checkComponent(json, value, comment, path);
	}

	private boolean check(Locale locale, List<Component> value, String comment, Object... path) {
		return getAbstractLocaleUtil(locale).checkListComponents(json, value, comment, path);
	}

	private void save(Locale locale) {
		getAbstractLocaleUtil(locale).saveLocaleNode();
	}

	private String string(Component component) {
		return LegacyComponentSerializer.legacyAmpersand().serialize(component);
	}

}
