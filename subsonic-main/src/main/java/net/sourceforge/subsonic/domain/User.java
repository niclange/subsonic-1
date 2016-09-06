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

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

/**
 * Represent a user.
 *
 * @author Sindre Mehus
 */
@Entity
@Data
public class User {

    public static final String USERNAME_ADMIN = "admin";
    public static final String USERNAME_GUEST = "guest";

    @Id
    private final String username;
    private String password;
    private String email;
    private boolean ldapAuthenticated;
    private long bytesStreamed;
    private long bytesDownloaded;
    private long bytesUploaded;

    private boolean isAdminRole;
    private boolean isSettingsRole;
    private boolean isDownloadRole;
    private boolean isUploadRole;
    private boolean isPlaylistRole;
    private boolean isCoverArtRole;
    private boolean isCommentRole;
    private boolean isPodcastRole;
    private boolean isStreamRole;
    private boolean isJukeboxRole;
    private boolean isShareRole;

    public User(String username, String password, String email, boolean ldapAuthenticated,
                long bytesStreamed, long bytesDownloaded, long bytesUploaded) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.ldapAuthenticated = ldapAuthenticated;
        this.bytesStreamed = bytesStreamed;
        this.bytesDownloaded = bytesDownloaded;
        this.bytesUploaded = bytesUploaded;
    }

    public User(String username, String password, String email) {
        this(username, password, email, false, 0, 0, 0);
    }

   

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(username);

        if (isAdminRole) {
            result.append(" [admin]");
        }
        if (isSettingsRole) {
            result.append(" [settings]");
        }
        if (isDownloadRole) {
            result.append(" [download]");
        }
        if (isUploadRole) {
            result.append(" [upload]");
        }
        if (isPlaylistRole) {
            result.append(" [playlist]");
        }
        if (isCoverArtRole) {
            result.append(" [coverart]");
        }
        if (isCommentRole) {
            result.append(" [comment]");
        }
        if (isPodcastRole) {
            result.append(" [podcast]");
        }
        if (isStreamRole) {
            result.append(" [stream]");
        }
        if (isJukeboxRole) {
            result.append(" [jukebox]");
        }
        if (isShareRole) {
            result.append(" [share]");
        }

        return result.toString();
    }
}
