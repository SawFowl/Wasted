# Wasted

####  ***[LocaleAPI](https://ore.spongepowered.org/Semenkovsky_Ivan/LocaleAPI) - required.***

Plugin for setting custom death messages. \
The plugin supports work with mods. \
The plugin takes into account the in-game rule of showing death messages.

All messages are stored in localization files -> `{CONFIG_DIR}/localeapi/wasted/*`.
Initially, the plugin does not contain death messages. They will be added to the localization by default with each new unique player death.
If you'd like, you can provide me with a default localization file filled with all the vanilla death messages and I'll add it to the next release.



##### Commands: 
/sponge plugins refresh wasted - Reload plugin config.

##### Placeholders: 
%player% - The Player Who Died \
%killer% - Player Killer \
%indirect-killer%- Killer player who indirectly used %killer% \
%item% - An object in the hands of a murderer.

##### API7 Docs -> [GitHub](https://github.com/SawFowl/Wasted/blob/API7/README.md)
