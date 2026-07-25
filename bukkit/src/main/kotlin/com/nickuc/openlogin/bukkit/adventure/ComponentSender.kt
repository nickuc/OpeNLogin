/*
 * The MIT License (MIT)
 *
 * Copyright © 2020 - 2026 - OpenLogin Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.nickuc.openlogin.bukkit.adventure

import com.nickuc.openlogin.common.util.MessageParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.Duration
import net.kyori.adventure.title.Title as AdventureTitle
import com.nickuc.openlogin.common.model.Title as CommonTitle

object ComponentSender {

    private val kickSerializer = LegacyComponentSerializer.legacySection()

    @JvmStatic
    fun send(sender: CommandSender, component: Component) {
        AudienceProvider.get().sender(sender).sendMessage(component)
    }

    @JvmStatic
    fun send(sender: CommandSender, miniMessage: String) {
        send(sender, MessageParser.parse(miniMessage))
    }

    @JvmStatic
    fun sendActionbar(player: Player, component: Component) {
        AudienceProvider.get().player(player).sendActionBar(component)
    }

    @JvmStatic
    fun sendActionbar(player: Player, miniMessage: String) {
        sendActionbar(player, MessageParser.parse(miniMessage))
    }

    @JvmStatic
    fun sendTitle(player: Player, title: CommonTitle) {
        if (title.title.isEmpty() && title.subtitle.isEmpty()) {
            resetTitle(player)
            return
        }

        val titleComponent = if (title.title.isEmpty()) Component.empty() else MessageParser.parse(title.title)
        val subtitleComponent = if (title.subtitle.isEmpty()) Component.empty() else MessageParser.parse(title.subtitle)
        val times = AdventureTitle.Times.times(
            Duration.ofMillis(title.fadeIn * 50L),
            Duration.ofMillis(title.stay * 50L),
            Duration.ofMillis(title.fadeOut * 50L)
        )

        AudienceProvider.get().player(player).showTitle(AdventureTitle.title(titleComponent, subtitleComponent, times))
    }

    @JvmStatic
    fun resetTitle(player: Player) {
        AudienceProvider.get().player(player).resetTitle()
    }

    @JvmStatic
    fun toKickString(component: Component): String = kickSerializer.serialize(component)

    @JvmStatic
    fun toKickString(miniMessage: String): String = toKickString(MessageParser.parse(miniMessage))
}
