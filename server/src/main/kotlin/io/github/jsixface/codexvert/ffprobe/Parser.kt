package io.github.jsixface.codexvert.ffprobe

import io.github.jsixface.codexvert.db.IVideoFilesRepo
import io.github.jsixface.codexvert.logger
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.PathWalkOption
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.walk


interface IParser {
    suspend fun parseVideoFile(file: Path)
    suspend fun parseAll(locations: List<String>, extensions: List<String>): Boolean
}

class Parser(private val repo: IVideoFilesRepo) : IParser {
    private val logger = logger()

    override suspend fun parseVideoFile(file: Path) {
        val p = ProbeUtils.parseMediaInfo(file) ?: return
        val entity = repo.getFile(file)
        if (entity == null) {
            repo.create(p, file)
        } else {
            repo.update(p, entity)
        }
    }

    override suspend fun parseAll(locations: List<String>, extensions: List<String>): Boolean {
        val videos = locations.map { Path(it) }.flatMap { loc ->
            loc.walk(PathWalkOption.FOLLOW_LINKS)
                .filter { it.isDirectory().not() }
                .filter { extensions.contains(it.extension.lowercase()) }
        }.associateWith { it.getLastModifiedTime().toMillis() }
            .mapKeys { it.key.toAbsolutePath().toString() }
        val entries = repo.getAll().associateBy { it.path }

        val deleted = entries - videos.keys
        val modified = videos.filter { it.value != entries[it.key]?.modified }
        logger.info("Total files: ${videos.size}, Deleted: ${deleted.size}, Added/Modified: ${modified.size}")
        modified.keys.forEach { parseVideoFile(Path(it)) }
        deleted.values.forEach { repo.delete(it.id.value) }
        return deleted.isNotEmpty() || modified.isNotEmpty()
    }

}