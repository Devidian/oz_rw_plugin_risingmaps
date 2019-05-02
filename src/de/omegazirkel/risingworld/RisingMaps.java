package de.omegazirkel.risingworld;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import de.omegazirkel.risingworld.tools.Colors;
import de.omegazirkel.risingworld.tools.FileChangeListener;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.PluginChangeWatcher;
import net.risingworld.api.Plugin;
import net.risingworld.api.Server;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerCommandEvent;
import net.risingworld.api.events.player.PlayerDisconnectEvent;
// import net.risingworld.api.events.player.PlayerEnterChunkEvent;
import net.risingworld.api.events.player.PlayerGenerateMapTileEvent;
// import net.risingworld.api.events.player.PlayerEnterWorldpartEvent;
import net.risingworld.api.events.player.PlayerSpawnEvent;
import net.risingworld.api.objects.Player;
// import net.risingworld.api.utils.Vector2i;
// import net.risingworld.api.utils.Vector3f;
// import net.risingworld.api.utils.Vector3i;
// import net.risingworld.api.utils.Utils.ByteUtils;
import net.risingworld.api.utils.Utils.FileUtils;

public class RisingMaps extends Plugin implements Listener, FileChangeListener {

	static final String pluginVersion = "0.2.0-SNAPSHOT";
	static final String pluginName = "RisingMaps";
	static final String pluginCMD = "rm";

	static final de.omegazirkel.risingworld.tools.Logger log = new de.omegazirkel.risingworld.tools.Logger("[OZ.RM]");
	static final Colors c = Colors.getInstance();
	private static I18n t = null;

	// Settings
	static int logLevel = 0;
	static boolean restartOnUpdate = true;
	static boolean sendPluginWelcome = false;

	static String tileRoot = "";
	static String webURL = "";
	// END Settings

	static boolean flagRestart = false;

	@Override
	public void onEnable() {
		t = t != null ? t : new I18n(this);
		registerEventListener(this);
		this.initSettings();

		try {
			PluginChangeWatcher WU = new PluginChangeWatcher(this);
			File f = new File(getPath());
			WU.watchDir(f);
			WU.startListening();
		} catch (IOException ex) {
			log.out(ex.getMessage(), 999);
		}

		log.out(pluginName + " Plugin is enabled", 10);
	}

	@Override
	public void onDisable() {
		log.out(pluginName + " Plugin is disabled", 10);
	}

	@EventMethod
	public void onPlayerSpawn(PlayerSpawnEvent event) {
		if (sendPluginWelcome) {
			Player player = event.getPlayer();
			String lang = player.getSystemLanguage();
			player.sendTextMessage(t.get("MSG_PLUGIN_WELCOME", lang));
		}
	}

	@EventMethod
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		Server server = getServer();

		if (flagRestart) {
			int playersLeft = server.getPlayerCount() - 1;
			if (playersLeft == 0) {
				log.out("Last player left the server, shutdown now due to flagRestart is set", 100); // INFO LEVEL
				server.shutdown();
			} else if (playersLeft > 1) {
				this.broadcastMessage("BC_PLAYER_REMAIN", playersLeft);
			}
		}
	}

	@EventMethod
	public void onPlayerGenerateMapTile(PlayerGenerateMapTileEvent event) {
		Player player = event.getPlayer();
		int tileX = event.getX();
		int tileY = event.getY();
		final File destinationFile = new File(tileRoot + "mt_" + tileX + "_" + tileY);
		String compareHash = FileUtils.getMd5(destinationFile);
		// executeDelayed(4f, () -> {
		player.requestMapTileRaw(tileX, tileY, compareHash, (byte[] tile) -> {
			if (tile == null) {
				// player.sendTextMessage("no tile returned");
			} else {
				FileUtils.writeBytesToFile(tile, destinationFile);
			}
		});
		// });
	}

	@EventMethod
	public void onPlayerCommand(PlayerCommandEvent event) {
		Player player = event.getPlayer();
		String command = event.getCommand();
		String lang = event.getPlayer().getSystemLanguage();
		String[] cmd = command.split(" ");

		if (cmd[0].equals("/" + pluginCMD)) {
			// Invalid number of arguments (0)
			if (cmd.length < 2) {
				player.sendTextMessage(c.error + pluginName + ":>" + c.text
						+ t.get("MSG_CMD_ERR_ARGUMENTS", lang).replace("PH_CMD", c.error + command + c.text)
								.replace("PH_COMMAND_HELP", c.command + "/" + pluginCMD + " help\n" + c.text));
				return;
			}
			String option = cmd[1];
			switch (option) {
			case "info":
				String infoMessage = t.get("CMD_INFO", lang);
				player.sendTextMessage(c.okay + pluginName + ":> " + infoMessage);
				break;
			case "help":
				String helpMessage = t.get("MSG_CMD_INFO", lang)
						.replace("PH_CMD_HELP", c.command + "/" + pluginCMD + " help" + c.text)
						.replace("PH_CMD_INFO", c.command + "/" + pluginCMD + " info" + c.text)
						.replace("PH_CMD_STATUS", c.command + "/" + pluginCMD + " status" + c.text);
				player.sendTextMessage(c.okay + pluginName + ":> " + helpMessage);
				break;
			case "status":
				String statusMessage = t.get("MSG_CMD_STATUS", lang)
						.replace("PH_VERSION", c.okay + pluginVersion + c.text)
						.replace("PH_LANGUAGE",
								c.comment + player.getLanguage() + " / " + player.getSystemLanguage() + c.text)
						.replace("PH_USEDLANG", c.info + t.getLanguageUsed(lang) + c.text)
						.replace("PH_LANG_AVAILABLE", c.okay + t.getLanguageAvailable() + c.text)
						.replace("PH_MAP_URL", c.info + webURL + c.text);
				player.sendTextMessage(c.okay + pluginName + ":> " + statusMessage);
				break;
			default:
				player.sendTextMessage(c.error + pluginName + ":> " + c.text
						+ t.get("MSG_CMD_ERR_UNKNOWN_OPTION", lang).replace("PH_OPTION", option));
				break;
			}
		}

	}

	/** */
	private void initSettings() {
		Properties settings = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(getPath() + "/settings.properties");
			settings.load(new InputStreamReader(in, "UTF8"));
			in.close();

			// fill global values
			logLevel = Integer.parseInt(settings.getProperty("logLevel", "0"));
			sendPluginWelcome = settings.getProperty("sendPluginWelcome", "true").contentEquals("true");
			tileRoot = settings.getProperty("tileRoot", this.getPath() + "/tiles/");
			webURL = settings.getProperty("webURL", "");

			// restart settings
			restartOnUpdate = settings.getProperty("restartOnUpdate").contentEquals("true");
			log.out(pluginName + " Plugin settings loaded", 10);
		} catch (Exception ex) {
			log.out("Exception on initSettings: " + ex.getMessage(), 100);
		}
	}

	// All stuff for plugin updates

	/**
	 *
	 * @param i18nIndex
	 * @param playerCount
	 */
	private void broadcastMessage(String i18nIndex, int playerCount) {
		getServer().getAllPlayers().forEach((player) -> {
			try {
				String lang = player.getSystemLanguage();
				player.sendTextMessage(c.warning + pluginName + ":> " + c.text
						+ t.get(i18nIndex, lang).replace("PH_PLAYERS", playerCount + ""));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void onFileChangeEvent(Path file) {
		if (file.toString().endsWith("jar")) {
			if (restartOnUpdate) {
				Server server = getServer();

				if (server.getPlayerCount() > 0) {
					flagRestart = true;
					this.broadcastMessage("BC_UPDATE_FLAG", server.getPlayerCount());
				} else {
					log.out("onFileCreateEvent: <" + file + "> changed, restarting now (no players online)", 100);
				}

			} else {
				log.out("onFileCreateEvent: <" + file + "> changed but restartOnUpdate is false", 0);
			}
		} else {
			log.out("onFileCreateEvent: <" + file + ">", 0);
		}
	}

	@Override
	public void onFileCreateEvent(Path file) {
		if (file.toString().endsWith("settings.properties")) {
			this.initSettings();
		} else {
			log.out(file.toString() + " was changed", 0);
		}
	}
}