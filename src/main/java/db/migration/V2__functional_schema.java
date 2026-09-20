// Créditos: oEnzoRibas
package db.migration;

import org.flywaydb.core.api.migration.*;
import java.sql.*;
import java.util.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Frozen migration: no dependency on application entities/services.
 * MariaDB DDL commits implicitly; rehearse on a restored copy and keep a full backup.
 */
public class V2__functional_schema extends BaseJavaMigration {
    @Override public Integer getChecksum() { return 2026091902; }
    @Override public boolean canExecuteInTransaction() { return false; }

    @Override public void migrate(Context context) throws Exception {
        Connection c = context.getConnection();
        // Fail before DDL for incompatible data instead of deleting or deduplicating it.
        Map<String, String> episodes = new LinkedHashMap<>();
        Set<String> ids = new HashSet<>();
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT id, video_url FROM episodes")) {
            while (rs.next()) {
                String videoId = youtubeId(rs.getString("video_url"));
                if (!ids.add(videoId)) throw new SQLException("Episódios duplicados no YouTube: " + videoId + ". Corrija na cópia antes do deploy.");
                episodes.put(rs.getString("id"), videoId);
            }
        }
        for (String table : List.of("users", "posts", "episodes")) {
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT id FROM " + table + " WHERE 1=0")) {
                if (!rs.getMetaData().getColumnTypeName(1).equalsIgnoreCase("UUID"))
                    throw new SQLException("UUID nativo obrigatório em " + table + ".id. Não altere IDs sem um plano de conversão validado.");
            }
        }
        execute(c, "CREATE TABLE IF NOT EXISTS contatos (id UUID PRIMARY KEY, nome VARCHAR(255) NOT NULL, email VARCHAR(255) NOT NULL, assunto VARCHAR(255) NOT NULL, mensagem TEXT NOT NULL)");
        execute(c, "CREATE TABLE IF NOT EXISTS sugestoes (id UUID PRIMARY KEY, nome VARCHAR(255) NOT NULL, email VARCHAR(255) NOT NULL, tema VARCHAR(2000) NOT NULL)");
        execute(c, "ALTER TABLE users ADD COLUMN role VARCHAR(16) NOT NULL DEFAULT 'USER'");
        execute(c, "ALTER TABLE posts MODIFY COLUMN description TEXT NOT NULL");
        execute(c, "UPDATE posts SET tags = '' WHERE tags IS NULL");
        execute(c, "ALTER TABLE posts MODIFY COLUMN tags VARCHAR(2048) NOT NULL DEFAULT ''");
        execute(c, "ALTER TABLE posts ADD COLUMN image_content_type VARCHAR(100)");
        execute(c, "ALTER TABLE episodes MODIFY COLUMN description TEXT NOT NULL");
        execute(c, "ALTER TABLE episodes ADD COLUMN youtube_id VARCHAR(11)");
        execute(c, "ALTER TABLE episodes ADD COLUMN thumbnail_url VARCHAR(512)");
        execute(c, "ALTER TABLE contatos MODIFY COLUMN mensagem TEXT NOT NULL");
        execute(c, "ALTER TABLE sugestoes MODIFY COLUMN tema VARCHAR(2000) NOT NULL");
        for (String table : List.of("contatos", "sugestoes"))
            execute(c, "ALTER TABLE " + table + " ADD COLUMN created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP");
        for (String table : List.of("posts", "episodes")) {
            execute(c, "UPDATE " + table + " SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL");
            execute(c, "ALTER TABLE " + table + " MODIFY COLUMN created_at TIMESTAMP(6) NOT NULL");
        }
        try (PreparedStatement update = c.prepareStatement("UPDATE episodes SET youtube_id = ?, video_url = ? WHERE id = ?")) {
            for (var entry : episodes.entrySet()) {
                update.setString(1, entry.getValue());
                update.setString(2, "https://www.youtube.com/watch?v=" + entry.getValue());
                update.setObject(3, UUID.fromString(entry.getKey()));
                update.executeUpdate();
            }
        }
        execute(c, "ALTER TABLE episodes MODIFY COLUMN youtube_id VARCHAR(11) NOT NULL");
        execute(c, "CREATE UNIQUE INDEX uk_episode_youtube_id ON episodes(youtube_id)");
        // Only inspect signatures, without loading entire legacy BLOBs into memory.
        try (Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT id, SUBSTRING(image, 1, 12) AS signature FROM posts WHERE image IS NOT NULL");
             PreparedStatement update = c.prepareStatement("UPDATE posts SET image_content_type = ? WHERE id = ?")) {
            while (rs.next()) {
                update.setString(1, mime(rs.getBytes("signature")));
                update.setObject(2, UUID.fromString(rs.getString("id")));
                update.executeUpdate();
            }
        }
        for (String table : List.of("posts", "episodes", "contatos", "sugestoes"))
            execute(c, "CREATE INDEX idx_" + table + "_created ON " + table + "(created_at, id)");
    }

    private static void execute(Connection c, String sql) throws SQLException {
        try (Statement statement = c.createStatement()) { statement.execute(sql); }
    }

    private static String youtubeId(String value) throws SQLException {
        try {
            URI uri = URI.create(value);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            String path = uri.getPath();
            String id = null;
            if (!Set.of("http", "https").contains(uri.getScheme()) || uri.getUserInfo() != null || uri.getPort() != -1)
                throw new IllegalArgumentException();
            if (host.equals("youtu.be")) id = path.substring(1);
            else if (Set.of("youtube.com", "www.youtube.com", "m.youtube.com").contains(host)) {
                if (path.equals("/watch") && uri.getRawQuery() != null) {
                    for (String item : uri.getRawQuery().split("&")) {
                        String[] pair = item.split("=", 2);
                        if (pair.length == 2 && pair[0].equals("v")) {
                            if (id != null) throw new IllegalArgumentException();
                            id = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                        }
                    }
                } else for (String prefix : List.of("/embed/", "/shorts/", "/live/"))
                    if (path.startsWith(prefix)) id = path.substring(prefix.length());
            }
            if (id == null || !id.matches("[A-Za-z0-9_-]{11}")) throw new IllegalArgumentException();
            return id;
        } catch (RuntimeException e) {
            throw new SQLException("Há URL de episódio incompatível. Corrija na cópia do banco antes do deploy.", e);
        }
    }

    private static String mime(byte[] b) {
        if (b.length >= 3 && (b[0] & 255) == 255 && (b[1] & 255) == 216 && (b[2] & 255) == 255) return "image/jpeg";
        if (b.length >= 8 && (b[0] & 255) == 137 && b[1] == 80 && b[2] == 78 && b[3] == 71) return "image/png";
        if (b.length >= 6 && new String(b, 0, 3, StandardCharsets.US_ASCII).equals("GIF")) return "image/gif";
        if (b.length >= 12 && new String(b, 0, 4, StandardCharsets.US_ASCII).equals("RIFF")
            && new String(b, 8, 4, StandardCharsets.US_ASCII).equals("WEBP")) return "image/webp";
        return "application/octet-stream";
    }
}
