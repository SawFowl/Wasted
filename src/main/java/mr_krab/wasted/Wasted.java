/*
 * Wasted - Plugin for changing death messages.
 * Copyright (C) 2018 Mr_Krab
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wasted is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
 
package mr_krab.wasted;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.ShulkerBullet;
import org.spongepowered.api.entity.living.golem.Shulker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.google.inject.Inject;
import mr_krab.wasted.utils.ConfigUtil;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@SuppressWarnings("unused")
@Plugin(id = "wasted",
		name = "Wasted",
		version = "0.8.0-S7.1-BETA",
		authors = "Mr_Krab")
public class Wasted {

	@Inject
	@DefaultConfig(sharedRoot = false)
	private Path defaultConfig;
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	@Inject
	@ConfigDir(sharedRoot = false)
	public File configFile;
	@Inject
	@DefaultConfig(sharedRoot = false)
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	public ConfigurationNode rootNode;
	@Inject
	private Logger logger;

	private static Wasted instance;
	private ConfigUtil configUtil;

	boolean replace;
	boolean console;
	boolean debug;

	public Path getConfigDir() {
		return configDir;
	}
	public Logger getLogger() {
		return logger;
	}
	public static Wasted getInstance() {
		return instance;
	}

	public MessageChannel toPlayers() {
		return MessageChannel.TO_PLAYERS;
	}

	public void sendMessage(Text message){
		String prefix = TextSerializers.formattingCode('§').serialize(
				TextSerializers.FORMATTING_CODE.deserialize(rootNode.getNode("Death-Messages", "Message-Prefix").getString()));
		if(console) {
			logger.info(message.toPlain());
		}
		String send = prefix + message.toPlain();
		toPlayers().send(Text.of(send));
	}

	public Text messageBuilder(List<String> strings, String player) {
		int n = (int) Math.floor(Math.random() * strings.size());
		String message = strings.get(n);
		return Text.of(TextSerializers.formattingCode('§').serialize(
				TextSerializers.FORMATTING_CODE.deserialize(message.replace("%player%", player))));
	}

	public Text messageBuilderPvP(List<String> strings, String player, String killer) {
		int n = (int) Math.floor(Math.random() * strings.size());
		String message = strings.get(n);
		return Text.of(TextSerializers.formattingCode('§').serialize(
				TextSerializers.FORMATTING_CODE.deserialize(message.replace("%player%", player).replace("%killer%", killer))));
	}

	public void load() {
		configLoader = HoconConfigurationLoader.builder().setPath(configDir.resolve("config.conf")).build();
		try {
			rootNode = configLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		configUtil.checkConfigVersion();
		replace = rootNode.getNode("Replace-Standart-Messages").getBoolean();
		console = rootNode.getNode("Console-Death-Message").getBoolean();
		debug = rootNode.getNode("Debug").getBoolean();
	}

	public void commandRegister() {

		CommandSpec commandDebug = CommandSpec.builder()
				.permission("wasted.admin")
				.executor((src, args) -> {
					if(debug) {
						debug = false;
						src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize("&cDebug OFF"));
					} else {
						debug = true;
						src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize("&aDebug ON"));
					}
					return CommandResult.success();
				})
				.build();

		CommandSpec commandReload = CommandSpec.builder()
				.permission("wasted.admin")
				.executor((src, args) -> {
					load();
					src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(rootNode.getNode("Reload-Message").getString()));
					return CommandResult.success();
				})
				.build();

		CommandSpec commandWasted = CommandSpec.builder()
				.permission("wasted.admin")
				.child(commandReload, "reload")
				.child(commandDebug, "debug")
				.executor((src, args) -> {
					if(src instanceof Player) {
					src.sendMessage(Text.of(
							   Text.builder(" §2/wasted reload").onClick(TextActions.runCommand("/wasted reload")), Text.NEW_LINE,
							   Text.builder(" §2/wasted debug").onClick(TextActions.runCommand("/wasted debug"))));
					} else {
						src.sendMessage(Text.of(Text.NEW_LINE, "§2wasted reload", Text.NEW_LINE, "§2wasted debug"));
					}
					return CommandResult.success();
				})
				.build();

		Sponge.getCommandManager().register(this, commandWasted, "wasted");
	}

	@Listener
	public void onPreInitialization(GamePreInitializationEvent event) {
		logger = (Logger)LoggerFactory.getLogger("\033[31mWasted\033[0m");
		logger.info("\n\n"
				+ "§4║╦║ ╔╗ ╔╗ ═╦═ ╔═ ╦╗\n"
				+ "§4║║║ ╠╣ ╚╗  ║  ╠═ ║║		§eLoading...\n"
				+ "§4╚╩╝ ║║ ╚╝  ║  ╚═ ╩╝\n");
		instance = this;
		configUtil = new ConfigUtil();
		configUtil.saveConfig();
		load();
		commandRegister();
	}
	
	@Listener
	public void onReload(GameReloadEvent event) {
		load();
		Sponge.getServer().getConsole().sendMessage(
				TextSerializers.FORMATTING_CODE.deserialize(rootNode.getNode("Reload-Message").getString()));
	}

	@Listener
	public void onPlayerDeath(DestructEntityEvent.Death event, @Getter("getTargetEntity") Player player) {
		event.clearMessage();
		Text prefix = Text.of(TextSerializers.formattingCode('§').serialize(
				TextSerializers.FORMATTING_CODE.deserialize(rootNode.getNode("Death-Messages", "Message-Prefix").getString())));
		if(debug) {
			logger.info("Cause root -> " + event.getCause().root().toString());
		}
		List<String> stringList = rootNode.getNode("Death-Messages", "Other", "Unknown").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
		Text message = messageBuilder(stringList, player.getName());
		Optional<FallingBlockDamageSource> optFallingBlockDamageSource = event.getCause().first(FallingBlockDamageSource.class);
		if(optFallingBlockDamageSource.isPresent()) {
			FallingBlockDamageSource damageSource = optFallingBlockDamageSource.get();
			String blockID = damageSource.getFallingBlockData().blockState().get().getType().getId();
			if(debug) {
				logger.info("FallingBlockDamageSource -> " + damageSource);
				logger.info("Block id -> " + blockID);
			}
			if(rootNode.getNode("Death-Messages", "FallingBlock", blockID).isVirtual()) {
				stringList = rootNode.getNode("Death-Messages", "FallingBlock", "Unknown").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
				message = messageBuilder(stringList, player.getName());
			} else {
				stringList = rootNode.getNode("Death-Messages", "FallingBlock", blockID).getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
				message = messageBuilder(stringList, player.getName());
			}
			if(replace) {
				event.setMessage(prefix, message);
				return;
			}
			sendMessage(message);
			return;
		}
		Optional<EntityDamageSource> optEntityDamageSource = event.getCause().first(EntityDamageSource.class);
		if (optEntityDamageSource.isPresent()) {
			EntityDamageSource damageSource = optEntityDamageSource.get();
			Entity killer = damageSource.getSource();
			if(killer.getType().getId().equals("customnpcs:customnpc")) {
				String npcName = damageSource.getSource().toString().replace('\'', '%');
				npcName = npcName.split("%")[1].split("%")[0];
				if(debug) {
					logger.info("CustomNpc found -> " + damageSource.getSource());
					logger.info("CustomNpc NPC name -> " + npcName);
				}
				if(!rootNode.getNode("Death-Messages", "NamedMob", npcName).isVirtual()) {
					stringList = rootNode.getNode("Death-Messages", "NamedMob", npcName).getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
					message = messageBuilder(stringList, player.getName());
					if(replace) {
						event.setMessage(prefix, message);
						return;
					}
					sendMessage(message);
					return;
				} else {
					logger.info("Missing config node NamedMob {\"" + npcName +"\": [messages] }");
				}
			}
			if(killer instanceof Projectile) {
				Projectile projectile = (Projectile) killer;
				ProjectileSource source = projectile.getShooter();
				if(debug) {
					logger.info("EntityDamageSource -> " + damageSource);
					logger.info("Projectile id -> " + projectile.getType().getId());
					logger.info("Projectile Source id -> " + source.toString());
				}
				if(source instanceof Player) {
					Player playerKiller = (Player) source;
					if(!playerKiller.getName().equals(player.getName())) {
						if(rootNode.getNode("Death-Messages", "PvP", projectile.getType().getId()).isVirtual()) {
							stringList = rootNode.getNode("Death-Messages", "PvP", "Melee", "Unregistered-weapons").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
							message = messageBuilderPvP(stringList, player.getName(), playerKiller.getName());
						} else {
							stringList = rootNode.getNode("Death-Messages", "PvP", projectile.getType().getId()).getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
							message = messageBuilderPvP(stringList, player.getName(), playerKiller.getName());
						}
					} else {
						if(rootNode.getNode("Death-Messages", "PvP", "Self", projectile.getType().getId()).isVirtual()) {
							stringList = rootNode.getNode("Death-Messages", "PvP", "Self", "Unknown").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
							message = messageBuilder(stringList, player.getName());
						} else {
							stringList = rootNode.getNode("Death-Messages", "PvP", "Self", projectile.getType().getId()).getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
							message = messageBuilder(stringList, player.getName());
						}
					}
					if(replace) {
						event.setMessage(prefix, message);
						return;
					}
					sendMessage(message);
					return;
				} else if(source instanceof Entity) {
					killer = (Entity) source;
					logger.info("Killer id -> " + killer.getType().getId());
				} else if(source instanceof TileEntity) {
					TileEntity tile = (TileEntity) projectile.getShooter();
					String node = tile.getType().getId() + "*" + projectile.getType().getId();
					if(debug) {
						logger.info("TileEntity id -> " + tile.getType().getId());
					}
					if(!rootNode.getNode("Death-Messages", "TileEntity", node).isVirtual()) {
						stringList = rootNode.getNode("Death-Messages", "TileEntity", node).getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
						message = messageBuilder(stringList, player.getName());
					} else {
						if(debug) {
							logger.info("Missing config node -> " + node);
						}
						stringList = rootNode.getNode("Death-Messages", "TileEntity", "Unknown").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
						message = messageBuilder(stringList, player.getName());
					}
					if(replace) {
						event.setMessage(prefix, message);
						return;
					}
					sendMessage(message);
					return;
				}
			}
			if(killer instanceof ShulkerBullet) {
				Optional<IndirectEntityDamageSource> optDamageSource = event.getCause().first(IndirectEntityDamageSource.class);
				if(optDamageSource.isPresent()) {
					IndirectEntityDamageSource indirectEntityDamageSource = optDamageSource.get();
					killer = indirectEntityDamageSource.getIndirectSource();
				}
			}
			if (killer instanceof Player) {
				Player playerKiller = (Player) killer;
				if(playerKiller.getItemInHand(HandTypes.MAIN_HAND).get().isEmpty()) {
					stringList = rootNode.getNode("Death-Messages", "PvP", "Hand").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
					message = messageBuilderPvP(stringList, player.getName(), playerKiller.getName());
				} else {
					if(debug) {
						logger.info("Item id -> " + playerKiller.getItemInHand(HandTypes.MAIN_HAND).get().getType().getId());
					}
					if(player.getWorld().getName().equals(killer.getWorld().getName())) {
						if(player.getPosition().distanceSquared(playerKiller.getPosition()) <= 4) {
							if(rootNode.getNode("Death-Messages", "PvP", "Ignore-Weapons").getBoolean()) {
								stringList = rootNode.getNode("Death-Messages", "PvP", "Melee", "Weapon-Ignored").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
								message = messageBuilderPvP(stringList, player.getName(), playerKiller.getName());
							} else {
								if(rootNode.getNode("Death-Messages", "PvP", "Melee", playerKiller.getItemInHand(HandTypes.MAIN_HAND).get().getType().getId()).isVirtual()) {
									stringList = rootNode.getNode("Death-Messages", "PvP", "Melee", "Unregistered-weapons").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
									message = messageBuilderPvP(stringList, player.getName(), playerKiller.getName());
								} else {
									stringList = rootNode.getNode("Death-Messages", "PvP", "Melee", playerKiller.getItemInHand(HandTypes.MAIN_HAND).get().getType().getId()).getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
									message = messageBuilderPvP(stringList, player.getName(), playerKiller.getName());
								}
							}
						} else {
							if(rootNode.getNode("Death-Messages", "PvP", "Ignore-Weapons").getBoolean()) {
								stringList = rootNode.getNode("Death-Messages", "PvP", "Distance", "Weapon-Ignored").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
								message = messageBuilderPvP(stringList, player.getName(), playerKiller.getName());
							} else {
								if(rootNode.getNode("Death-Messages", "PvP", "Melee", playerKiller.getItemInHand(HandTypes.MAIN_HAND).get().getType().getId()).isVirtual()) {
									stringList = rootNode.getNode("Death-Messages", "PvP", "Distance", "Unregistered-weapons").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
									message = messageBuilderPvP(stringList, player.getName(), playerKiller.getName());
								} else {
									stringList = rootNode.getNode("Death-Messages", "PvP", "Distance", playerKiller.getItemInHand(HandTypes.MAIN_HAND).get().getType().getId()).getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
									message = messageBuilderPvP(stringList, player.getName(), playerKiller.getName());
								}
							}
						}
					}
				}
			} else {
				if(debug) {
					logger.info("Killer id -> " + killer.getType().getId());
					if(!killer.getType().getName().isEmpty()) {
						logger.info("Killer name -> " + killer.getType().getName());
						logger.info("DamageSource -> " + damageSource.getSource());
					}
				}
				if(!rootNode.getNode("Death-Messages", "Mob", killer.getType().getId()).isVirtual()) {
					stringList = rootNode.getNode("Death-Messages", "Mob", killer.getType().getId()).getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
					message = messageBuilder(stringList, player.getName());
				} else {
					stringList = rootNode.getNode("Death-Messages", "Mob", "Unknown").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
					message = messageBuilder(stringList, player.getName());
				}
				if(!killer.getType().getName().isEmpty()) {
					if(!rootNode.getNode("Death-Messages", "NamedMob", killer.getType().getName()).isVirtual()) {
						stringList = rootNode.getNode("Death-Messages", "NamedMob", killer.getType().getName()).getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
						message = messageBuilder(stringList, player.getName());
					}
				}
			}
			if(replace) {
				event.setMessage(prefix, message);
				return;
			}
			sendMessage(message);
			return;
		}
		Optional<BlockDamageSource> optBlockDamageSource = event.getCause().first(BlockDamageSource.class);
		if(optBlockDamageSource.isPresent()) {
			BlockDamageSource damageSource = optBlockDamageSource.get();
			BlockType block = damageSource.getLocation().getBlockType();
			if(!block.getId().isEmpty()) {
				if(debug) {
					logger.info("BlockDamageSource -> " + damageSource);
					logger.info("Block killer id -> " + block.getId());
				}
				if(!rootNode.getNode("Death-Messages", "Block", block.getId()).isVirtual()) {
					stringList = rootNode.getNode("Death-Messages", "Block", block.getId()).getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
					message = messageBuilder(stringList, player.getName());
				} else {
					stringList = rootNode.getNode("Death-Messages", "Block", "Unknown").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
					message = messageBuilder(stringList, player.getName());
				} 
				if(replace) {
					event.setMessage(prefix, message);
					return;
				}
				sendMessage(message);
				return;
			} 
		}
		Optional<DamageSource> optDamageSource = event.getCause().first(DamageSource.class);
		if(optDamageSource.isPresent()) {
			DamageSource damageSource = optDamageSource.get();
			if(!rootNode.getNode("Death-Messages", "Other", "DamageName").isVirtual()) {
				if(event.getCause().root() instanceof DamageSource) {
					if(!damageSource.toString().isEmpty()) {
						String damageName = damageSource.toString().split("Name=")[1].split(",")[0];
						if(debug) {
							logger.info("DamageSource -> " + damageSource);
							logger.info("Damage name -> " + damageName);
						}
						if(!rootNode.getNode("Death-Messages", "Other", "DamageName", damageName).isVirtual()) {
							stringList = rootNode.getNode("Death-Messages", "Other", "DamageName", damageName).getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
							message = messageBuilder(stringList, player.getName());
							if(replace) {
								event.setMessage(prefix, message);
								return;
							}
							sendMessage(message);
							return;
						}
					}
				}
			}
			DamageType damage = damageSource.getType();
			if(debug) {
				logger.info("DamageType id -> " + damage.getId());
			}
			if(!rootNode.getNode("Death-Messages", "Other", "DamageType", damage.getId()).isVirtual()) {
				stringList = rootNode.getNode("Death-Messages", "Other", "DamageType", damage.getId()).getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
				message = messageBuilder(stringList, player.getName());
		
			}
		}
		if(replace) {
			event.setMessage(prefix, message);
			return;
		}
		sendMessage(message);
	}
}