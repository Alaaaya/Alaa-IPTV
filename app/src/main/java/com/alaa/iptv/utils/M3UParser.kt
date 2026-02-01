package com.alaa.iptv.utils

import com.alaa.iptv.data.models.Channel
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object M3UParser {
    
    fun parse(inputStream: InputStream): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(InputStreamReader(inputStream))
        
        var line: String?
        var currentChannel: TempChannel? = null
        var streamId = 0
        
        while (reader.readLine().also { line = it } != null) {
            val trimmedLine = line?.trim() ?: continue
            
            if (trimmedLine.startsWith("#EXTINF:")) {
                currentChannel = parseExtInf(trimmedLine)
                currentChannel.streamId = (++streamId).toString()
            } else if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                currentChannel?.let {
                    it.url = trimmedLine
                    channels.add(it.toChannel())
                }
                currentChannel = null
            }
        }
        
        reader.close()
        return channels
    }
    
    fun parseFromString(content: String): List<Channel> {
        return parse(content.byteInputStream())
    }
    
    private fun parseExtInf(line: String): TempChannel {
        val channel = TempChannel()
        
        // Extract tvg-id
        val tvgIdPattern = """tvg-id="([^"]*)"""".toRegex()
        tvgIdPattern.find(line)?.let {
            channel.epgChannelId = it.groupValues[1]
        }
        
        // Extract tvg-name
        val tvgNamePattern = """tvg-name="([^"]*)"""".toRegex()
        tvgNamePattern.find(line)?.let {
            channel.tvgName = it.groupValues[1]
        }
        
        // Extract tvg-logo
        val tvgLogoPattern = """tvg-logo="([^"]*)"""".toRegex()
        tvgLogoPattern.find(line)?.let {
            channel.logo = it.groupValues[1]
        }
        
        // Extract group-title (category)
        val groupTitlePattern = """group-title="([^"]*)"""".toRegex()
        groupTitlePattern.find(line)?.let {
            channel.categoryName = it.groupValues[1]
        }
        
        // Extract channel name (after last comma)
        val nameIndex = line.lastIndexOf(',')
        if (nameIndex != -1 && nameIndex < line.length - 1) {
            channel.name = line.substring(nameIndex + 1).trim()
        }
        
        return channel
    }
    
    private data class TempChannel(
        var streamId: String = "",
        var name: String = "",
        var tvgName: String = "",
        var logo: String? = null,
        var epgChannelId: String? = null,
        var categoryName: String? = null,
        var url: String = ""
    ) {
        fun toChannel(): Channel {
            return Channel(
                streamId = streamId,
                num = streamId,
                name = name.ifEmpty { tvgName.ifEmpty { "Unknown Channel" } },
                streamType = "live",
                streamIcon = logo,
                epgChannelId = epgChannelId,
                added = null,
                categoryId = categoryName?.hashCode()?.toString(),
                categoryName = categoryName,
                customSid = null,
                tvArchive = 0,
                directSource = url,
                tvArchiveDuration = 0
            )
        }
    }
}
