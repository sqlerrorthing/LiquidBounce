/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2025 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 *
 */
package net.ccbluex.liquidbounce

import net.ccbluex.liquidbounce.api.ClientUpdate.gitInfo
import net.ccbluex.liquidbounce.api.ClientUpdate.hasUpdate
import net.ccbluex.liquidbounce.api.IpInfoApi
import net.ccbluex.liquidbounce.api.oauth.ClientAccount
import net.ccbluex.liquidbounce.api.oauth.ClientAccountManager
import net.ccbluex.liquidbounce.api.oauth.OAuthClient
import net.ccbluex.liquidbounce.config.AutoConfig
import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.events.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.events.ClientStartEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.Reconnect
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.cosmetic.CosmeticService
import net.ccbluex.liquidbounce.features.itemgroup.ClientItemGroups
import net.ccbluex.liquidbounce.features.itemgroup.groups.headsCollection
import net.ccbluex.liquidbounce.features.misc.AccountManager
import net.ccbluex.liquidbounce.features.misc.FriendManager
import net.ccbluex.liquidbounce.features.misc.proxy.ProxyManager
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.client.ipcConfiguration
import net.ccbluex.liquidbounce.integration.IntegrationListener
import net.ccbluex.liquidbounce.integration.browser.BrowserManager
import net.ccbluex.liquidbounce.integration.interop.ClientInteropServer
import net.ccbluex.liquidbounce.integration.interop.protocol.rest.v1.game.ActiveServerList
import net.ccbluex.liquidbounce.integration.theme.ThemeManager
import net.ccbluex.liquidbounce.integration.theme.component.ComponentOverlay
import net.ccbluex.liquidbounce.lang.LanguageManager
import net.ccbluex.liquidbounce.render.FontManager
import net.ccbluex.liquidbounce.render.ui.ItemImageAtlas
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.utils.aiming.PostRotationExecutor
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.block.ChunkScanner
import net.ccbluex.liquidbounce.utils.client.ErrorHandler
import net.ccbluex.liquidbounce.utils.client.InteractionTracker
import net.ccbluex.liquidbounce.utils.client.TpsObserver
import net.ccbluex.liquidbounce.utils.client.mc
import net.ccbluex.liquidbounce.utils.client.movementbot.MovementBot
import net.ccbluex.liquidbounce.utils.combat.CombatManager
import net.ccbluex.liquidbounce.utils.combat.combatTargetsConfigurable
import net.ccbluex.liquidbounce.utils.input.InputTracker
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager
import net.ccbluex.liquidbounce.utils.mappings.EnvironmentRemapper
import net.ccbluex.liquidbounce.utils.render.WorldToScreen
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.resource.SynchronousResourceReloader
import org.apache.logging.log4j.LogManager
import java.io.File
import kotlin.time.measureTime

/**
 * LiquidBounce
 *
 * A free mixin-based injection hacked-client for Minecraft using FabricMC.
 *
 * @author kawaiinekololis (@team CCBlueX)
 */
object LiquidBounce : EventListener {

    /**
     * CLIENT INFORMATION
     *
     * WARNING: Please read the GNU General Public License
     */
    const val CLIENT_NAME = "LiquidBounce"
    const val CLIENT_AUTHOR = "CCBlueX"
    const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LiquidBounce"

    val clientVersion = gitInfo["git.build.version"]?.toString() ?: "unknown"
    val clientCommit = gitInfo["git.commit.id.abbrev"]?.let { "git-$it" } ?: "unknown"
    val clientBranch = gitInfo["git.branch"]?.toString() ?: "nextgen"

    /**
     * Defines if the client is in development mode.
     * This will enable update checking on commit time instead of semantic versioning.
     *
     * TODO: Replace this approach with full semantic versioning.
     */
    const val IN_DEVELOPMENT = true

    val isIntegrationTesting = !System.getenv("TENACC_TEST_PROVIDER").isNullOrBlank()

    /**
     * Client logger to print out console messages
     */
    val logger = LogManager.getLogger(CLIENT_NAME)!!

    /**
     * Client update information
     */
    val updateAvailable by lazy { hasUpdate() }

    /**
     * Should be executed to start the client.
     */
    @Suppress("unused")
    val startHandler = handler<ClientStartEvent> {
        runCatching {
            logger.info("Launching $CLIENT_NAME v$clientVersion by $CLIENT_AUTHOR")
            logger.debug("Loading from cloud: '$CLIENT_CLOUD'")

            // Load mappings
            EnvironmentRemapper

            // Load translations
            LanguageManager.loadDefault()

            // Initialize client features
            EventManager

            // Config
            ConfigSystem
            combatTargetsConfigurable

            ChunkScanner
            InputTracker

            // Features
            ModuleManager
            CommandManager
            ScriptManager
            RotationManager
            InteractionTracker
            CombatManager
            FriendManager
            ProxyManager
            AccountManager
            InventoryManager
            WorldToScreen
            Reconnect
            ActiveServerList
            ConfigSystem.root(ClientItemGroups)
            ConfigSystem.root(LanguageManager)
            ConfigSystem.root(ClientAccountManager)
            BrowserManager
            FontManager
            PostRotationExecutor
            TpsObserver
            MovementBot

            // Register commands and modules
            CommandManager.registerInbuilt()
            ModuleManager.registerInbuilt()

            // Load user scripts
            ScriptManager.loadAll()

            // Load theme and component overlay
            ThemeManager
            ComponentOverlay.insertComponents()

            // Load config system from disk
            ConfigSystem.loadAll()

            // Netty WebSocket
            ClientInteropServer.start()

            // Initialize browser
            logger.info("Refresh Rate: ${mc.window.refreshRate} Hz")

            IntegrationListener
            BrowserManager.initBrowser()

            // Register resource reloader
            val resourceManager = mc.resourceManager
            val clientResourceReloader = ClientResourceReloader()
            if (resourceManager is ReloadableResourceManagerImpl) {
                resourceManager.registerReloader(clientResourceReloader)
            } else {
                logger.warn("Failed to register resource reloader!")

                // Run resource reloader directly as fallback
                clientResourceReloader.reload(resourceManager)
            }

            ItemImageAtlas
        }.onSuccess {
            logger.info("Successfully loaded client!")
        }.onFailure(ErrorHandler::fatal)
    }

    /**
     * Resource reloader which is executed on client start and reload.
     * This is used to run async tasks without blocking the main thread.
     *
     * For now this is only used to check for updates and request additional information from the internet.
     *
     * @see SynchronousResourceReloader
     * @see ResourceReloader
     */
    class ClientResourceReloader : SynchronousResourceReloader {

        override fun reload(manager: ResourceManager) {
            runCatching {
                // Queue fonts of all themes
                // TODO: Will be removed with PR #3884 as it is not needed anymore
                ThemeManager.themesFolder.listFiles()
                    ?.filter { file -> file.isDirectory }
                    ?.forEach { file ->
                        runCatching {
                            val assetsFolder = File(file, "assets")
                            if (!assetsFolder.exists()) {
                                return@forEach
                            }

                            FontManager.queueFolder(assetsFolder)
                        }.onFailure {
                            logger.error("Failed to queue fonts from theme '${file.name}'.", it)
                        }
                    }

                // Load fonts
                val duration = measureTime {
                    FontManager.createGlyphManager()
                }

                logger.info("Completed loading fonts in ${duration.inWholeMilliseconds} ms.")
                logger.info("Fonts: [ ${FontManager.fontFaces.joinToString { face -> face.name }} ]")
            }.onFailure(ErrorHandler::fatal)

            // Check for newest version
            if (updateAvailable) {
                logger.info("Update available! Please download the latest version from https://liquidbounce.net/")
            }

            runCatching {
                ipcConfiguration.let {
                    logger.info("Loaded Discord IPC configuration.")
                }
            }.onFailure {
                logger.error("Failed to load Discord IPC configuration.", it)
            }

            // Refresh local IP info
            logger.info("Refreshing local IP info...")
            IpInfoApi

            // Check if client account is available
            if (ClientAccountManager.clientAccount != ClientAccount.EMPTY_ACCOUNT) {
                OAuthClient.runWithScope {
                    runCatching {
                        ClientAccountManager.clientAccount.renew()
                    }.onFailure {
                        logger.error("Failed to renew client account token.", it)
                        ClientAccountManager.clientAccount = ClientAccount.EMPTY_ACCOUNT
                    }.onSuccess {
                        logger.info("Successfully renewed client account token.")
                        ConfigSystem.storeConfigurable(ClientAccountManager)
                    }
                }
            }

            // Refresh cosmetic service
            CosmeticService.refreshCarriers(force = true) {
                logger.info("Successfully loaded ${CosmeticService.carriers.size} cosmetics carriers.")
            }

            // Load Head collection
            headsCollection

            // Load settings list from API
            runCatching {
                logger.info("Loading settings list from API...")
                AutoConfig.configs
            }.onSuccess {
                logger.info("Loaded ${it.size} settings from API.")
            }.onFailure {
                logger.error("Failed to load settings list from API", it)
            }
        }
    }

    /**
     * Should be executed to stop the client.
     */
    @Suppress("unused")
    val shutdownHandler = handler<ClientShutdownEvent> {
        logger.info("Shutting down client...")

        ConfigSystem.storeAll()
        ChunkScanner.ChunkScannerThread.stopThread()

        // Shutdown browser as last step
        BrowserManager.shutdownBrowser()
    }

}
