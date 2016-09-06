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
package net.sourceforge.subsonic.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import lombok.Data;


/**
 * @author Sindre Mehus
 * @version $Id$
 */
@Entity
@Data
public class Album {

	@Id
	@GeneratedValue
    private int id;
    private String path;
    private String name;
    private String artist;
    private int songCount;
    private int durationSeconds;
    private String coverArtPath;
    private Integer year;
    private String genre;
    private int playCount;
    private Date lastPlayed;
    private String comment;
    private Date created;
    private Date lastScanned;
    private boolean present;
    private Integer folderId;
    
    @OneToMany
    @JoinColumn(name="albumId", referencedColumnName="id")
    private List<StarredAlbum> starredAlbums;
    
    public Album() {
    }

    public Album(int id, String path, String name, String artist, int songCount, int durationSeconds, String coverArtPath,
            Integer year, String genre, int playCount, Date lastPlayed, String comment, Date created, Date lastScanned,
            boolean present, Integer folderId) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.artist = artist;
        this.songCount = songCount;
        this.durationSeconds = durationSeconds;
        this.coverArtPath = coverArtPath;
        this.year = year;
        this.genre = genre;
        this.playCount = playCount;
        this.lastPlayed = lastPlayed;
        this.comment = comment;
        this.created = created;
        this.lastScanned = lastScanned;
        this.folderId = folderId;
        this.present = present;
    }

   
}
