package io.github.jsixface.codexvert.api

import io.github.jsixface.codexvert.logger
import io.github.jsixface.codexvert.utils.CodecUtils.parseMediaInfo
import io.github.jsixface.common.TrackType
import io.github.jsixface.common.VideoFile
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.PathWalkOption
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.io.path.walk

typealias VideoList = Map<String, VideoFile>


@OptIn(ExperimentalPathApi::class)
class VideoApi {

    private val logger = logger()

    fun refreshDirs(): Boolean {
        val data = SavedData.load()
        val scan = mutableMapOf<String, VideoFile>()
        data.settings.libraryLocations.forEach { l ->
            val loc = Path(l)
            loc.walk(PathWalkOption.FOLLOW_LINKS).forEach { p ->
                if (p.isDirectory().not() && data.settings.videoExtensions.contains(p.extension.lowercase())) {
                    scan[p.pathString] = VideoFile(
                        path = p.pathString,
                        fileName = p.name,
                        modifiedTime = p.getLastModifiedTime().toMillis()
                    )
                }
            }
        }
        val toParse: VideoList = consolidateData(data.details, scan)
        val deleted = removeDeleted(data.details, scan)
        parseMediaFiles(data.details, toParse)
        return if (toParse.isNotEmpty() || deleted) {
            logger.info("Saving new data")
            data.save()
            true
        } else false
    }

    fun getVideos(): VideoList = SavedData.load().details

    private fun parseMediaFiles(details: MutableMap<String, VideoFile>, toParse: VideoList) {
        toParse.values.forEach { videoFile ->
            parseMediaInfo(videoFile.path)?.let { tracks ->
                details[videoFile.path] = videoFile.copy(
                    videos = tracks.filter { t -> t.type == TrackType.Video },
                    audios = tracks.filter { t -> t.type == TrackType.Audio },
                    subtitles = tracks.filter { t -> t.type == TrackType.Subtitle },
                )
            }
        }
    }

    private fun consolidateData(data: MutableMap<String, VideoFile>, scan: VideoList): VideoList {
        val toScan = scan.filterValues { data[it.path]?.modifiedTime != it.modifiedTime }
        toScan.forEach { data[it.key] = it.value } // Source object change
        return toScan
    }

    private fun removeDeleted(data: MutableMap<String, VideoFile>, scan: VideoList): Boolean {
        val toDelete = data.keys.filter { !scan.containsKey(it) }
        toDelete.forEach { data.remove(it) }
        return toDelete.isNotEmpty()
    }
}