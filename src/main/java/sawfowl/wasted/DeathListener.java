package sawfowl.wasted;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.projectile.source.BlockProjectileSource;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.gamerule.GameRules;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import sawfowl.localeapi.api.TextUtils;
import sawfowl.wasted.configure.Placeholders;

public class DeathListener {

	private final Wasted plugin;
	private final Object deathMessages = "DeathMessages";
	private final List<UUID> suicided = new ArrayList<>();
	private final String suicide = "suicide";
	private final Object[] suicidePath = {"DeathMessages", "Suicide"};
	public DeathListener(Wasted plugin) {
		this.plugin = plugin;
	}

	@Listener
	public void commandListener(ExecuteCommandEvent event, @First ServerPlayer player) {
		if(!event.command().equalsIgnoreCase(suicide)) return;
		if(!Sponge.server().commandManager().commandMapping(suicide).isPresent() || !Sponge.server().commandManager().commandMapping(suicide).get().registrar().canExecute(event.commandCause(), Sponge.server().commandManager().commandMapping(suicide).get())) return;
		suicided.add(player.uniqueId());
		Sponge.asyncScheduler().submit(Task.builder().plugin(plugin.getPluginContainer()).delay(Ticks.of(10)).execute(() -> {
			if(suicided.contains(player.uniqueId())) suicided.remove(player.uniqueId());
		}).build());
	}

	@Listener
	public void onDeath(DestructEntityEvent.Death event) {
		if(!(event.entity() instanceof ServerPlayer) || !event.cause().first(DamageSource.class).isPresent()) return;
		ServerPlayer player = (ServerPlayer) event.entity();
		if(!player.world().properties().gameRule(GameRules.SHOW_DEATH_MESSAGES.get())) return;
		event.setMessageCancelled(true);
		Component message = event.message();
		DamageSource damageSource = event.cause().first(DamageSource.class).get();
		if(suicided.contains(player.uniqueId())) {
			suicided.remove(player.uniqueId());
			sendMessage(message, player, suicidePath);
			return;
		}
		if(damageSource instanceof EntityDamageSource) {
			Entity source = ((EntityDamageSource) damageSource).source();
			if(source instanceof Projectile) {
				Projectile projectile = (Projectile) source;
				if(projectile.shooter().isPresent()) {
					ProjectileSource projectileSource = projectile.shooter().get().get();
					if(projectileSource instanceof Entity) {
						if(projectileSource instanceof ServerPlayer) {
							ServerPlayer killer = (ServerPlayer) projectileSource;
							ItemStack handItem = killer.itemInHand(HandTypes.MAIN_HAND);
							Object[] path = {deathMessages, "PvP", "Projectile", entityId(projectile).asString()};
							if(!handItem.type().equals(ItemTypes.AIR.get())) path = new Object[] {deathMessages, "PvP", "Projectile", "Weapon", entityId(projectile).asString()};
							if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, killer.name(), getPlayerName(killer), path);
							sendMessage(message, player, getPlayerName(player), handItem, path);
						} else {
							source = (Entity) projectileSource;
							Object[] path = {deathMessages, "Mobs", "Projectile", entityId(source).asString(), entityId(projectile).asString()};
							if(source.get(Keys.CUSTOM_NAME).isPresent()) {
								path = new Object[] {deathMessages, "Mobs", "Projectile", "CustomNames", string(source.get(Keys.CUSTOM_NAME).get()), entityId(projectile).asString()};
								if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, TextUtils.clearDecorations(source.get(Keys.CUSTOM_NAME).get()), path);
								sendMessage(message, player, string(source.get(Keys.CUSTOM_NAME).get()), path);
							} else {
								if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, string(source.type().asComponent()), path);
								sendMessage(message, player, path);
							}
						}
					} else if(projectileSource instanceof BlockProjectileSource) {
						BlockState block = ((BlockProjectileSource) projectileSource).block();
						Object[] path = {deathMessages, "Blocks", "Projectile", blockID(block), entityId(projectile).asString()};
						if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, string(block.type().asComponent()), path);
						sendMessage(message, player, path);
					} else {
						Object[] path = {deathMessages, "Blocks", "Projectile", "UnknownSource", entityId(projectile).asString()};
						if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, string(source.type().asComponent()), path);
						sendMessage(message, player, path);
					}
				} else {
					Object[] path = {deathMessages, "Blocks", "Projectile", "UnknownSource", entityId(projectile).asString()};
					if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, string(source.type().asComponent()), path);
					sendMessage(message, player, path);
				}
			} else {
				if(source instanceof ServerPlayer) {
					ServerPlayer killer = (ServerPlayer) source;
					ItemStack handItem = killer.itemInHand(HandTypes.MAIN_HAND);
					Object[] path = {deathMessages, "PvP", "Melee"};
					if(!handItem.type().equals(ItemTypes.AIR.get())) path = new Object[] {deathMessages, "PvP", "Melee", "Weapon"};
					if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, killer.name(), getPlayerName(killer), path);
					sendMessage(message, player, getPlayerName(player), handItem, path);
				} else {
					Object[] path = {deathMessages, "Mobs", "Melee", entityId(source).asString()};
					if(source.get(Keys.CUSTOM_NAME).isPresent()) {
						path = new Object[] {deathMessages, "Mobs", "Melee", "CustomNames", string(source.get(Keys.CUSTOM_NAME).get())};
						if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, TextUtils.clearDecorations(source.get(Keys.CUSTOM_NAME).get()), path);
						sendMessage(message, player, string(source.get(Keys.CUSTOM_NAME).get()), path);
					} else {
						if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, string(source.type().asComponent()), path);
						sendMessage(message, player, path);
					}
				}
			}
		} else if(damageSource instanceof IndirectEntityDamageSource) {
			IndirectEntityDamageSource indirectEntityDamageSource = (IndirectEntityDamageSource) damageSource;
			Entity indirectSource = indirectEntityDamageSource.indirectSource();
			Entity source = indirectEntityDamageSource.source();
			if(indirectSource instanceof ServerPlayer) {
				ServerPlayer indirectKiller = (ServerPlayer) indirectSource;
				if(source instanceof ServerPlayer) {
					ServerPlayer killer = (ServerPlayer) source;
					Object[] path = {deathMessages, "PvP", "Melee", "IndirectPlayer"}; 
					if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(TextUtils.replace(message, (new String[] {killer.name(), getPlayerName(killer), indirectKiller.name(), getPlayerName(indirectKiller)}), (new String[] {Placeholders.KILLER, Placeholders.KILLER, Placeholders.INDIRECT_KILLER, Placeholders.INDIRECT_KILLER})), player, path);
					sendMessage(message, player, string(source.get(Keys.CUSTOM_NAME).get()), string(indirectSource.get(Keys.CUSTOM_NAME).get()), path);
				} else {
					Object[] path = {deathMessages, "PvP", "Melee", "IndirectPlayer", "Source", entityId(source)};
					if(source.get(Keys.CUSTOM_NAME).isPresent()) {
						path = new Object[] {deathMessages, "PvP", "Melee", "IndirectPlayer", "CustomNames", string(source.get(Keys.CUSTOM_NAME).get())};
						if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(TextUtils.replace(message, (new String[] {string(source.get(Keys.CUSTOM_NAME).get()), indirectKiller.name(), getPlayerName(indirectKiller)}), (new String[] {Placeholders.KILLER, Placeholders.INDIRECT_KILLER, Placeholders.INDIRECT_KILLER})), player, path);
						sendMessage(message, player, string(source.get(Keys.CUSTOM_NAME).get()), string(indirectSource.get(Keys.CUSTOM_NAME).get()), path);
					} else {
						if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(TextUtils.replace(message, (new String[] {indirectKiller.name(), getPlayerName(indirectKiller)}), (new String[] {Placeholders.INDIRECT_KILLER, Placeholders.INDIRECT_KILLER})), player, path);
						sendMessage(message, player, string(indirectSource.get(Keys.CUSTOM_NAME).get()), path);
					}
				}
			} else if(source instanceof ServerPlayer) {
				ServerPlayer killer = (ServerPlayer) source;
				Object[] path = {deathMessages, "PvP", "Melee", "IndirectEntity", entityId(indirectSource)};
				if(indirectSource.get(Keys.CUSTOM_NAME).isPresent()) {
					path = new Object[] {deathMessages, "PvP", "Melee", "IndirectEntity", "CustomNames", indirectSource.get(Keys.CUSTOM_NAME).get()};
					if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(TextUtils.replace(message, (new String[] {player.name(),  getPlayerName(killer), string(indirectSource.get(Keys.CUSTOM_NAME).get())}), (new String[] {Placeholders.KILLER, Placeholders.KILLER, Placeholders.INDIRECT_KILLER})), player, path);
					sendMessage(message, player, string(source.get(Keys.CUSTOM_NAME).get()), string(indirectSource.get(Keys.CUSTOM_NAME).get()), path);
				} else {
					if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(TextUtils.replace(message, (new String[] {player.name(),  getPlayerName(killer)}), (new String[] {Placeholders.KILLER, Placeholders.KILLER})), player, path);
					sendMessage(message, player, string(indirectSource.get(Keys.CUSTOM_NAME).get()), path);
				}
			} else {
				Object[] path = {deathMessages, "Mobs", "Melee", "IndirectEntity", "Source", entityId(source), entityId(indirectSource)};
				if(source.get(Keys.CUSTOM_NAME).isPresent()) {
					if(indirectSource.get(Keys.CUSTOM_NAME).isPresent()) {
						path = new Object[] {deathMessages, "Mobs", "Melee", "IndirectEntity", "CustomNames", string(source.get(Keys.CUSTOM_NAME).get()), indirectSource.get(Keys.CUSTOM_NAME).get()};
					} else path = new Object[] {deathMessages, "Mobs", "Melee", "IndirectEntity", "CustomNames", string(source.get(Keys.CUSTOM_NAME).get()), entityId(indirectSource)};
				} else if(indirectSource.get(Keys.CUSTOM_NAME).isPresent()) path = new Object[] {deathMessages, "PvP", "Melee", "IndirectEntity", "CustomNames", entityId(source), indirectSource.get(Keys.CUSTOM_NAME).get()};
				if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, string(source.type().asComponent()), path);
				sendMessage(message, player, path);
			}
		} else {
			Optional<Entity> optLastAtacker = player.get(Keys.LAST_ATTACKER);
			if(damageSource instanceof FallingBlockDamageSource) {
				String blockId = ((FallingBlockDamageSource) damageSource).blockState().key().key().asString();
				Object[] path = {deathMessages, "FallingBlocks", "Simple", blockId};
				if(optLastAtacker.isPresent()) {
					if(optLastAtacker.get() instanceof ServerPlayer) {
						ServerPlayer killer = (ServerPlayer) optLastAtacker.get();
						path = new Object[] {deathMessages, "FallingBlocks", "PvP", blockId};
						if(!killer.itemInHand(HandTypes.MAIN_HAND).type().equals(ItemTypes.AIR.get())) {
							if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(TextUtils.replace(message, (new String[] {killer.name(), getPlayerName(killer), itemId(killer.itemInHand(HandTypes.MAIN_HAND)).asString()}), (new String[] {Placeholders.KILLER, Placeholders.KILLER, Placeholders.ITEM})), player, path);
							sendMessage(message, player, getPlayerName(killer), killer.itemInHand(HandTypes.MAIN_HAND), path);
						} else {
							if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(TextUtils.replace(message, (new String[] {killer.name(), getPlayerName(killer)}), (new String[] {Placeholders.KILLER, Placeholders.KILLER})), player, path);
							sendMessage(message, player, getPlayerName(killer), path);
						}
					} else {
						path = new Object[] {deathMessages, "FallingBlocks", "Mobs", blockId};
						if(optLastAtacker.get().get(Keys.CUSTOM_NAME).isPresent()) {
							path = new Object[] {deathMessages, "FallingBlocks", "Mobs", blockId, string(optLastAtacker.get().get(Keys.CUSTOM_NAME).get())};
							if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, path);
							sendMessage(message, player, string(optLastAtacker.get().get(Keys.CUSTOM_NAME).get()), path);
						} else {
							path = new Object[] {deathMessages, "FallingBlocks", "Mobs", blockId, entityId(optLastAtacker.get())};
							if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, string(((FallingBlockDamageSource) damageSource).blockState().get().type().asComponent()), path);
							sendMessage(message, player, path);
						}
					}
				} else {
					if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, path);
					sendMessage(message, player, path);
				}
			} else if(damageSource instanceof BlockDamageSource) {
				String blockId = blockID(((BlockDamageSource) damageSource).blockSnapshot().state()).asString();
				Object[] path = {deathMessages, "BlockDamage", "Simple", blockId};
				if(optLastAtacker.isPresent()) {
					if(optLastAtacker.get() instanceof ServerPlayer) {
						ServerPlayer killer = (ServerPlayer) optLastAtacker.get();
						path = new Object[] {deathMessages, "BlockDamage", "PvP", blockId};
						if(!killer.itemInHand(HandTypes.MAIN_HAND).type().equals(ItemTypes.AIR.get())) {
							if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(TextUtils.replace(message, (new String[] {killer.name(), getPlayerName(killer), itemId(killer.itemInHand(HandTypes.MAIN_HAND)).asString()}), (new String[] {Placeholders.KILLER, Placeholders.KILLER, Placeholders.ITEM})), player, path);
							sendMessage(message, player, getPlayerName(killer), killer.itemInHand(HandTypes.MAIN_HAND), path);
						} else {
							if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(TextUtils.replace(message, (new String[] {killer.name(), getPlayerName(killer)}), (new String[] {Placeholders.KILLER, Placeholders.KILLER})), player, path);
							sendMessage(message, player, getPlayerName(killer), path);
						}
					} else {
						path = new Object[] {deathMessages, "BlockDamage", "Mobs", blockId};
						if(optLastAtacker.get().get(Keys.CUSTOM_NAME).isPresent()) {
							path = new Object[] {deathMessages, "BlockDamage", "Mobs", blockId, string(optLastAtacker.get().get(Keys.CUSTOM_NAME).get())};
							if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, path);
							sendMessage(message, player, string(optLastAtacker.get().get(Keys.CUSTOM_NAME).get()), path);
						} else {
							path = new Object[] {deathMessages, "BlockDamage", "Mobs", blockId, entityId(optLastAtacker.get())};
							if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, string(optLastAtacker.get().type().asComponent()), path);
							sendMessage(message, player, path);
						}
					}
				} else {
					if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, path);
					sendMessage(message, player, path);
				}
			} else {
				DamageType damageType = damageSource.type();
				String damageId = damageTypeId(damageType).asString();
				Object[] path = {deathMessages, "DamageTypes", "Simple", damageId};
				if(optLastAtacker.isPresent()) {
					if(optLastAtacker.get() instanceof ServerPlayer) {
						ServerPlayer killer = (ServerPlayer) optLastAtacker.get();
						path = new Object[] {deathMessages, "DamageTypes", "PvP", damageId};
						if(!killer.itemInHand(HandTypes.MAIN_HAND).type().equals(ItemTypes.AIR.get())) {
							if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(TextUtils.replace(message, itemId(killer.itemInHand(HandTypes.MAIN_HAND)).asString(), Placeholders.ITEM), player, path);
							sendMessage(message, player, getPlayerName(killer), killer.itemInHand(HandTypes.MAIN_HAND), path);
						} else {
							if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, killer.name(), getPlayerName(killer), path);
							sendMessage(message, player, getPlayerName(killer), path);
						}
					} else {
						path = new Object[] {deathMessages, "DamageTypes", "Mobs", damageId};
						if(optLastAtacker.get().get(Keys.CUSTOM_NAME).isPresent()) {
							path = new Object[] {deathMessages, "DamageTypes", "Mobs", damageId, string(optLastAtacker.get().get(Keys.CUSTOM_NAME).get())};
							if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, string(optLastAtacker.get().get(Keys.CUSTOM_NAME).get()), path);
							sendMessage(message, player, string(optLastAtacker.get().get(Keys.CUSTOM_NAME).get()), path);
						} else {
							path = new Object[] {deathMessages, "DamageTypes", "Mobs", damageId, entityId(optLastAtacker.get())};
							if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, string(optLastAtacker.get().type().asComponent()), path);
							sendMessage(message, player, path);
						}
					}
				} else {
					if(!plugin.getLocales().containsMessagePath(path)) plugin.getLocales().addDeathMessage(message, player, path);
					sendMessage(message, player, path);
				}
			}
		}
	}

	private static ResourceKey entityId(Entity entity) {
		return Sponge.game().registry(RegistryTypes.ENTITY_TYPE).valueKey(entity.type());
	}

	private static ResourceKey blockID(BlockState block) {
		return Sponge.game().registry(RegistryTypes.BLOCK_TYPE).valueKey(block.type());
	}

	private static ResourceKey damageTypeId(DamageType damageType) {
		return Sponge.game().registry(RegistryTypes.DAMAGE_TYPE).valueKey(damageType);
	}

	private static ResourceKey itemId(ItemStack itemStack) {
		return Sponge.game().registry(RegistryTypes.ITEM_TYPE).valueKey(itemStack.type());
	}

	private Collection<ServerPlayer> getOnlinePlayers() {
		return Sponge.server().onlinePlayers();
	}

	private Component text(String string) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
	}

	private String string(Component component) {
		return LegacyComponentSerializer.legacyAmpersand().serialize(component);
	}

	private String getPlayerName(ServerPlayer player) {
		return TextUtils.clearDecorations(string(player.get(Keys.CUSTOM_NAME).orElse(text(player.name()))));
	}

	private void sendMessage(Component message, ServerPlayer player, String killer, ItemStack item, Object... path) {
		Sponge.asyncScheduler().submit(Task.builder().plugin(plugin.getPluginContainer()).execute(() -> {
			if(plugin.getConfig().isConsoleDeathMessage()) Sponge.systemSubject().sendMessage(!plugin.getLocales().containsMessagePath(path) ? plugin.getLocales().getPrefix(plugin.getLocales().getLocaleService().getSystemOrDefaultLocale()).append(message) : plugin.getLocales().getRandomMessage(plugin.getLocales().getLocaleService().getSystemOrDefaultLocale(), player, killer, item, path));
			getOnlinePlayers().forEach(reciever -> {
				reciever.sendMessage(!plugin.getLocales().containsMessagePath(reciever.locale(), path) ? plugin.getLocales().getPrefix(reciever.locale()).append(message) : plugin.getLocales().getRandomMessage(reciever.locale(), player, killer, item, path));
			});
		}).build());
	}

	private void sendMessage(Component message, ServerPlayer player, String killer, Object... path) {
		Sponge.asyncScheduler().submit(Task.builder().plugin(plugin.getPluginContainer()).execute(() -> {
			if(plugin.getConfig().isConsoleDeathMessage()) Sponge.systemSubject().sendMessage(!plugin.getLocales().containsMessagePath(path) ? plugin.getLocales().getPrefix(plugin.getLocales().getLocaleService().getSystemOrDefaultLocale()).append(message) : plugin.getLocales().getRandomMessage(plugin.getLocales().getLocaleService().getSystemOrDefaultLocale(), player, killer, path));
			getOnlinePlayers().forEach(reciever -> {
				reciever.sendMessage(!plugin.getLocales().containsMessagePath(reciever.locale(), path) ? plugin.getLocales().getPrefix(reciever.locale()).append(message) : plugin.getLocales().getRandomMessage(reciever.locale(), player, killer, path));
			});
		}).build());
	}

	private void sendMessage(Component message, ServerPlayer player, String killer, String indirectKiller, Object... path) {
		Sponge.asyncScheduler().submit(Task.builder().plugin(plugin.getPluginContainer()).execute(() -> {
			if(plugin.getConfig().isConsoleDeathMessage()) Sponge.systemSubject().sendMessage(!plugin.getLocales().containsMessagePath(path) ? plugin.getLocales().getPrefix(plugin.getLocales().getLocaleService().getSystemOrDefaultLocale()).append(message) : plugin.getLocales().getRandomMessage(plugin.getLocales().getLocaleService().getSystemOrDefaultLocale(), player, killer, indirectKiller, path));
			getOnlinePlayers().forEach(reciever -> {
				reciever.sendMessage(!plugin.getLocales().containsMessagePath(reciever.locale(), path) ? plugin.getLocales().getPrefix(reciever.locale()).append(message) : plugin.getLocales().getRandomMessage(reciever.locale(), player, killer, indirectKiller, path));
			});
		}).build());
	}

	private void sendMessage(Component message, ServerPlayer player, Object... path) {
		Sponge.asyncScheduler().submit(Task.builder().plugin(plugin.getPluginContainer()).execute(() -> {
			if(plugin.getConfig().isConsoleDeathMessage()) Sponge.systemSubject().sendMessage(!plugin.getLocales().containsMessagePath(path) ? plugin.getLocales().getPrefix(plugin.getLocales().getLocaleService().getSystemOrDefaultLocale()).append(message) : plugin.getLocales().getRandomMessage(plugin.getLocales().getLocaleService().getSystemOrDefaultLocale(), player, path));
			getOnlinePlayers().forEach(reciever -> {
				reciever.sendMessage(!plugin.getLocales().containsMessagePath(reciever.locale(), path) ? plugin.getLocales().getPrefix(reciever.locale()).append(message) : plugin.getLocales().getRandomMessage(reciever.locale(), player, path));
			});
		}).build());
	}

}
