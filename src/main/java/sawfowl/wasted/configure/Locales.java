package sawfowl.wasted.configure;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.configurate.serialize.SerializationException;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.TextUtils;
import sawfowl.localeapi.utils.AbstractLocaleUtil;

public class Locales {

	private final LocaleService localeService;
	private final boolean json;
	private String[] localesTags;
	private Random random = new Random();
	public Locales(LocaleService localeService, boolean json) {
		this.localeService = localeService;
		this.json = json;
		localeService.createPluginLocale("wasted", ConfigTypes.JSON, org.spongepowered.api.util.locale.Locales.DEFAULT);
		localeService.createPluginLocale("wasted", ConfigTypes.JSON, org.spongepowered.api.util.locale.Locales.RU_RU);
		generateDefault();
		generateRu();
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

	public Component getRandomMessage(ServerPlayer player, Object... path) {
		List<Component> components = getAbstractLocaleUtil(player.locale()).getListComponents(json, path);
		return TextUtils.replace(components.get(random.nextInt(components.size())), (new String[] {"%player%"}), (new String[] {player.name()}));
	}

	public void addDeathMessage(Component component, ServerPlayer player, Object... path) {
		List<String> messages = getAbstractLocaleUtil(org.spongepowered.api.util.locale.Locales.DEFAULT).getListStrings(path);
		component = TextUtils.replace(component, (new String[] {player.name()}), (new String[] {"%player%"}));
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
		if(save) save(locale);
	}

	private void generateRu() {
		Locale locale = org.spongepowered.api.util.locale.Locales.RU_RU;
		boolean save = check(locale, toText("&7[&4Wasted&7]&r "), null, "Basic", "Prefix");
		save = check(locale, toText("&aПлагин перезагружен."), null, "Basic", "Reload");
		if(save) save(locale);
	}

	private Component replace(Component component, Map<String, String> map) {
		return TextUtils.replace(component, map);
	}

	private Component replaceComponent(Component component, Map<String, Component> map) {
		return TextUtils.replaceToComponents(component, map);
	}

	private AbstractLocaleUtil getAbstractLocaleUtil(Locale locale) {
		return localeService.getPluginLocales("wasted").get(locale);
	}

	private Component toText(String string) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
	}

	private boolean check(Locale locale, Component value, String comment, Object... path) {
		return getAbstractLocaleUtil(locale).checkComponent(json, value, comment, path);
	}

	private void save(Locale locale) {
		getAbstractLocaleUtil(locale).saveLocaleNode();
	}}
