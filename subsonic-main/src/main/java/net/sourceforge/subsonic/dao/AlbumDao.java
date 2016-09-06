/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */
package net.sourceforge.subsonic.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import net.sourceforge.subsonic.domain.Album;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.MusicFolder;
import net.sourceforge.subsonic.util.FileUtil;

/**
 * Provides database services for albums.
 *
 * @author Sindre Mehus
 */
public class AlbumDao extends AbstractDao implements AlbumDaoInterface {

    private static final String COLUMNS = "id, path, name, artist, song_count, duration_seconds, cover_art_path, " +
                                          "year, genre, play_count, last_played, comment, created, last_scanned, present, folder_id";

    private final RowMapper rowMapper = new AlbumMapper();

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#getAlbum(java.lang.String, java.lang.String)
	 */
    @Override
	public Album getAlbum(String artistName, String albumName) {
        return queryOne("select " + COLUMNS + " from album where artist=? and name=?", rowMapper, artistName, albumName);
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#getAlbumForFile(net.sourceforge.subsonic.domain.MediaFile)
	 */
    @Override
	public Album getAlbumForFile(MediaFile file) {

        // First, get all albums with the correct album name (irrespective of artist).
        List<Album> candidates = query("select " + COLUMNS + " from album where name=?", rowMapper, file.getAlbumName());
        if (candidates.isEmpty()) {
            return null;
        }

        // Look for album with the correct artist.
        for (Album candidate : candidates) {
            if (ObjectUtils.equals(candidate.getArtist(), file.getArtist()) && FileUtil.exists(candidate.getPath())) {
                return candidate;
            }
        }

        // Look for album with the same path as the file.
        for (Album candidate : candidates) {
            if (ObjectUtils.equals(candidate.getPath(), file.getParentPath())) {
                return candidate;
            }
        }

        // No appropriate album found.
        return null;
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#getAlbum(int)
	 */
    @Override
	public Album getAlbum(int id) {
        return queryOne("select " + COLUMNS + " from album where id=?", rowMapper, id);
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#getAlbumsForArtist(java.lang.String, java.util.List)
	 */
    @Override
	public List<Album> getAlbumsForArtist(final String artist, final List<MusicFolder> musicFolders) {
        if (musicFolders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Object> args = new HashMap<String, Object>() {{
            put("artist", artist);
            put("folders", MusicFolder.toIdList(musicFolders));
        }};
        return namedQuery("select " + COLUMNS + " from album where artist = :artist and present and folder_id in (:folders) " +
                          "order by name",
                          rowMapper, args);
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#createOrUpdateAlbum(net.sourceforge.subsonic.domain.Album)
	 */
    @Override
	public synchronized void createOrUpdateAlbum(Album album) {
        String sql = "update album set " +
                     "path=?," +
                     "song_count=?," +
                     "duration_seconds=?," +
                     "cover_art_path=?," +
                     "year=?," +
                     "genre=?," +
                     "play_count=?," +
                     "last_played=?," +
                     "comment=?," +
                     "created=?," +
                     "last_scanned=?," +
                     "present=?, " +
                     "folder_id=? " +
                     "where artist=? and name=?";

        int n = update(sql, album.getPath(), album.getSongCount(), album.getDurationSeconds(), album.getCoverArtPath(), album.getYear(),
                       album.getGenre(), album.getPlayCount(), album.getLastPlayed(), album.getComment(), album.getCreated(),
                       album.getLastScanned(), album.isPresent(), album.getFolderId(), album.getArtist(), album.getName());

        if (n == 0) {

            update("insert into album (" + COLUMNS + ") values (" + questionMarks(COLUMNS) + ")", null, album.getPath(),
                   album.getName(), album.getArtist(), album.getSongCount(), album.getDurationSeconds(),
                   album.getCoverArtPath(), album.getYear(), album.getGenre(), album.getPlayCount(), album.getLastPlayed(),
                   album.getComment(), album.getCreated(), album.getLastScanned(), album.isPresent(), album.getFolderId());
        }

        int id = queryForInt("select id from album where artist=? and name=?", null, album.getArtist(), album.getName());
        album.setId(id);
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#getAlphabetialAlbums(int, int, boolean, java.util.List)
	 */
    @Override
	public List<Album> getAlphabetialAlbums(final int offset, final int count, boolean byArtist, final List<MusicFolder> musicFolders) {
        if (musicFolders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Object> args = new HashMap<String, Object>() {{
            put("folders", MusicFolder.toIdList(musicFolders));
            put("count", count);
            put("offset", offset);
        }};
        String orderBy = byArtist ? "artist, name" : "name";
        return namedQuery("select " + COLUMNS + " from album where present and folder_id in (:folders) " +
                          "order by " + orderBy + " limit :count offset :offset", rowMapper, args);
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#getMostFrequentlyPlayedAlbums(int, int, java.util.List)
	 */
    @Override
	public List<Album> getMostFrequentlyPlayedAlbums(final int offset, final int count, final List<MusicFolder> musicFolders) {
        if (musicFolders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Object> args = new HashMap<String, Object>() {{
            put("folders", MusicFolder.toIdList(musicFolders));
            put("count", count);
            put("offset", offset);
        }};
        return namedQuery("select " + COLUMNS + " from album where play_count > 0 and present and folder_id in (:folders) " +
                          "order by play_count desc limit :count offset :offset", rowMapper, args);
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#getMostRecentlyPlayedAlbums(int, int, java.util.List)
	 */
    @Override
	public List<Album> getMostRecentlyPlayedAlbums(final int offset, final int count, final List<MusicFolder> musicFolders) {
        if (musicFolders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Object> args = new HashMap<String, Object>() {{
            put("folders", MusicFolder.toIdList(musicFolders));
            put("count", count);
            put("offset", offset);
        }};
        return namedQuery("select " + COLUMNS + " from album where last_played is not null and present and folder_id in (:folders) " +
                          "order by last_played desc limit :count offset :offset", rowMapper, args);
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#getNewestAlbums(int, int, java.util.List)
	 */
    @Override
	public List<Album> getNewestAlbums(final int offset, final int count, final List<MusicFolder> musicFolders) {
        if (musicFolders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Object> args = new HashMap<String, Object>() {{
            put("folders", MusicFolder.toIdList(musicFolders));
            put("count", count);
            put("offset", offset);
        }};
        return namedQuery("select " + COLUMNS + " from album where present and folder_id in (:folders) " +
                          "order by created desc limit :count offset :offset", rowMapper, args);
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#getStarredAlbums(int, int, java.lang.String, java.util.List)
	 */
    @Override
	public List<Album> getStarredAlbums(final int offset, final int count, final String username, final List<MusicFolder> musicFolders) {
        if (musicFolders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Object> args = new HashMap<String, Object>() {{
            put("folders", MusicFolder.toIdList(musicFolders));
            put("count", count);
            put("offset", offset);
            put("username", username);
        }};
        return namedQuery("select " + prefix(COLUMNS, "album") + " from starred_album, album where album.id = starred_album.album_id and " +
                          "album.present and album.folder_id in (:folders) and starred_album.username = :username " +
                          "order by starred_album.created desc limit :count offset :offset",
                          rowMapper, args);
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#getAlbumsByGenre(int, int, java.lang.String, java.util.List)
	 */
    @Override
	public List<Album> getAlbumsByGenre(final int offset, final int count, final String genre, final List<MusicFolder> musicFolders) {
        if (musicFolders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Object> args = new HashMap<String, Object>() {{
            put("folders", MusicFolder.toIdList(musicFolders));
            put("count", count);
            put("offset", offset);
            put("genre", genre);
        }};
        return namedQuery("select " + COLUMNS + " from album where present and folder_id in (:folders) " +
                          "and genre = :genre limit :count offset :offset", rowMapper, args);
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#getAlbumsByYear(int, int, int, int, java.util.List)
	 */
    @Override
	public List<Album> getAlbumsByYear(final int offset, final int count, final int fromYear, final int toYear,
                                       final List<MusicFolder> musicFolders) {
        if (musicFolders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Object> args = new HashMap<String, Object>() {{
            put("folders", MusicFolder.toIdList(musicFolders));
            put("count", count);
            put("offset", offset);
            put("fromYear", fromYear);
            put("toYear", toYear);
        }};
        if (fromYear <= toYear) {
            return namedQuery("select " + COLUMNS + " from album where present and folder_id in (:folders) " +
                              "and year between :fromYear and :toYear order by year limit :count offset :offset",
                              rowMapper, args);
        } else {
            return namedQuery("select " + COLUMNS + " from album where present and folder_id in (:folders) " +
                              "and year between :toYear and :fromYear order by year desc limit :count offset :offset",
                              rowMapper, args);
        }
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#markNonPresent(java.util.Date)
	 */
    @Override
	public void markNonPresent(Date lastScanned) {
        int minId = queryForInt("select top 1 id from album where last_scanned != ? and present", 0, lastScanned);
        int maxId = queryForInt("select max(id) from album where last_scanned != ? and present", 0, lastScanned);

        final int batchSize = 1000;
        for (int id = minId; id <= maxId; id += batchSize) {
            update("update album set present=false where id between ? and ? and last_scanned != ? and present", id, id + batchSize, lastScanned);
        }
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#expunge()
	 */
    @Override
	public void expunge() {
        int minId = queryForInt("select top 1 id from album where not present", 0);
        int maxId = queryForInt("select max(id) from album where not present", 0);

        final int batchSize = 1000;
        for (int id = minId; id <= maxId; id += batchSize) {
            update("delete from album where id between ? and ? and not present", id, id + batchSize);
        }
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#starAlbum(int, java.lang.String)
	 */
    @Override
	public void starAlbum(int albumId, String username) {
        unstarAlbum(albumId, username);
        update("insert into starred_album(album_id, username, created) values (?,?,?)", albumId, username, new Date());
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#unstarAlbum(int, java.lang.String)
	 */
    @Override
	public void unstarAlbum(int albumId, String username) {
        update("delete from starred_album where album_id=? and username=?", albumId, username);
    }

    /* (non-Javadoc)
	 * @see net.sourceforge.subsonic.dao.AlbumDaoInterface#getAlbumStarredDate(int, java.lang.String)
	 */
    @Override
	public Date getAlbumStarredDate(int albumId, String username) {
        return queryForDate("select created from starred_album where album_id=? and username=?", null, albumId, username);
    }

    private static class AlbumMapper implements ParameterizedRowMapper<Album> {
        public Album mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Album(
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getInt(5),
                    rs.getInt(6),
                    rs.getString(7),
                    rs.getInt(8) == 0 ? null : rs.getInt(8),
                    rs.getString(9),
                    rs.getInt(10),
                    rs.getTimestamp(11),
                    rs.getString(12),
                    rs.getTimestamp(13),
                    rs.getTimestamp(14),
                    rs.getBoolean(15),
                    rs.getInt(16));
        }
    }
}
