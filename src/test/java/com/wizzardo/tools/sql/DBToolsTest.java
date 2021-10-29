package com.wizzardo.tools.sql;

import com.wizzardo.tools.misc.Stopwatch;
import com.wizzardo.tools.misc.Unchecked;
import com.wizzardo.tools.sql.generated.ArtistTable;
import com.wizzardo.tools.sql.generated.Tables;
import com.wizzardo.tools.sql.model.Artist;
import com.wizzardo.tools.sql.model.Song;
import com.wizzardo.tools.sql.query.Field;
import com.wizzardo.tools.sql.query.Generator;
import com.wizzardo.tools.sql.query.QueryBuilder;
import com.wizzardo.tools.sql.query.QueryBuilder.TIMESTAMP;
import org.junit.Before;
import org.junit.Test;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class DBToolsTest {
    protected DBTools service;

    @Before
    public void init() {
        service = new DBTools();
        final SQLiteConnectionPoolDataSource dataSource = new SQLiteConnectionPoolDataSource();
        dataSource.setUrl("jdbc:sqlite::memory:");
        dataSource.getConfig().setBusyTimeout(10000);
        Unchecked.run(new Unchecked.UncheckedRunnable() {
            @Override
            public void run() throws Exception {
                dataSource.getConfig().setDatePrecision("MILLISECONDS");
            }
        });
        service.migrationsListPath = "sql/migrations.txt";
        service.dataSource = new SimpleConnectionPool(dataSource, 8);
        service.migrate();
    }

    @Test
    public void test_1() {
        service.withBuilder(new DBTools.Mapper<QueryBuilder.WrapConnectionStep, Object>() {
            @Override
            public Object map(QueryBuilder.WrapConnectionStep c) throws SQLException {
                c.insertInto(Tables.ARTIST).fields(Arrays.asList((Field) Tables.ARTIST.NAME)).values(new Artist(0, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "test")).executeUpdate();
                ResultSet resultSet = c.select(Arrays.asList(Tables.ARTIST.ID, Tables.ARTIST.NAME)).from(Tables.ARTIST).executeQuery();
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getInt(Tables.ARTIST.ID.getName())).isEqualTo(1);
                assertThat(resultSet.getString(Tables.ARTIST.NAME.getName())).isEqualTo("test");
                assertThat(resultSet.next()).isFalse();
                return null;
            }
        });
    }

    @Test
    public void test_fetch() {
        List<Artist> artists = service.withBuilder(new DBTools.Mapper<QueryBuilder.WrapConnectionStep, List<Artist>>() {
            @Override
            public List<Artist> map(QueryBuilder.WrapConnectionStep c) throws SQLException {
                c.insertInto(Tables.ARTIST).values(new Artist(0, TIMESTAMP.now(), TIMESTAMP.now(), "artist 1")).executeUpdate();
                long id = service.getLastInsertedId(c.getConnection());
                assertThat(id).describedAs("id of inserted row").isEqualTo(1);

                c.insertInto(Tables.ARTIST).values(new Artist(0, TIMESTAMP.now(), TIMESTAMP.now(), "artist 2")).executeUpdate();
                return c.select()
                        .from(Tables.ARTIST)
                        .fetchInto(Artist.class);
            }
        });

        assertThat(artists).hasSize(2)
                .extracting("id", "name")
                .contains(tuple(1L, "artist 1"), tuple(2L, "artist 2"));

        assertThat(artists).hasSize(2).extracting("dateCreated").doesNotContainNull();
    }

    @Test
    public void test_update() {
        Artist artist = service.withBuilder(new DBTools.Mapper<QueryBuilder.WrapConnectionStep, Artist>() {
            @Override
            public Artist map(QueryBuilder.WrapConnectionStep c) throws SQLException {
                c.insertInto(Tables.ARTIST).values(new Artist(0, TIMESTAMP.now(), TIMESTAMP.now(), "artist 1")).executeUpdate();
                long id = service.getLastInsertedId(c.getConnection());
                assertThat(id).describedAs("id of inserted row").isEqualTo(1);

                Unchecked.run(new Unchecked.UncheckedRunnable() {
                    @Override
                    public void run() throws Exception {
                        Thread.sleep(1);
                    }
                });

                int rowsUpdated = c.update(Tables.ARTIST)
                        .set(Tables.ARTIST.DATE_UPDATED.eq(TIMESTAMP.now()))
                        .set(Tables.ARTIST.NAME.eq("the ARTIST"))
                        .where(Tables.ARTIST.ID.eq(1))
                        .executeUpdate();
                assertThat(rowsUpdated).describedAs("updated rows").isEqualTo(1);

                return c.select()
                        .from(Tables.ARTIST)
                        .where(Tables.ARTIST.ID.eq(1))
                        .fetchOneInto(Artist.class);
            }
        });

        assertThat(artist)
                .extracting("id", "name")
                .contains(1L, "the ARTIST");

        assertThat(artist.dateUpdated).describedAs("should be updated")
                .isNotNull()
                .isNotEqualTo(artist.dateCreated)
                .isCloseTo(new Date(), 100);
    }

    @Test
    public void test_alias() {
        ArtistTable a = Tables.ARTIST.as("a");
        String sql = QueryBuilder.withConnection(null)
                .select()
                .from(a)
                .where(a.ID.eq(1))
                .toSql();

        assertThat(sql).isEqualTo("select * from artist as a where a.id=?");
    }

    @Test
    public void test_inner_select() {
        String sql = QueryBuilder.withConnection(null)
                .select(Field.of(QueryBuilder.withConnection(null)
                                .select(new Field("count(s.*)"))
                                .from(Tables.SONG.as("s"))
                                .where(Tables.SONG.as("s").ARTIST_ID.eq(Tables.ARTIST.ID))
                        , "songs")
                )
                .from(Tables.ARTIST)
                .where(Tables.ARTIST.ID.eq(1))
                .toSql();

        assertThat(sql).isEqualTo("select (select count(s.*) from song as s where s.artist_id=artist.id) as songs from artist where artist.id=?");
    }

    public static class ArtistSongs {
        long id;
        String songs;
    }

    @Test
    public void test_inner_select_2() throws SQLException {
        final Artist artist = new Artist(0, TIMESTAMP.now(), TIMESTAMP.now(), "artist 1");
        artist.id = service.insertInto(artist, Tables.ARTIST);

        Song song1 = new Song(0, TIMESTAMP.now(), TIMESTAMP.now(), "song 1", artist.id, Song.Genre.ROCK);
        song1.id = service.insertInto(song1, Tables.SONG);

        Song song2 = new Song(0, TIMESTAMP.now(), TIMESTAMP.now(), "song 2", artist.id, Song.Genre.METAL);
        song2.id = service.insertInto(song2, Tables.SONG);


        ArtistSongs artistSongs = service.withBuilder(new DBTools.Mapper<QueryBuilder.WrapConnectionStep, ArtistSongs>() {
            @Override
            public ArtistSongs map(QueryBuilder.WrapConnectionStep c) throws SQLException {
                QueryBuilder.WhereStep query = c.select(Tables.ARTIST.ID, Field.of(c
                                        .select(Field.invoke("group_concat", Tables.SONG.as("s").ID))
                                        .from(Tables.SONG.as("s"))
                                        .where(Tables.SONG.as("s").ARTIST_ID.eq(Tables.ARTIST.ID)))
                                .as("songs")
                        )
                        .from(Tables.ARTIST)
                        .where(Tables.ARTIST.ID.eq(artist.id));

                assertThat(query.toSql()).isEqualTo("select artist.id, (select group_concat(s.id) from song as s where s.artist_id=artist.id) as songs from artist where artist.id=?");
                return query.fetchOneInto(ArtistSongs.class);
            }
        });
        assertThat(artistSongs.id).isEqualTo(artist.id);
        assertThat(artistSongs.songs).isEqualTo("1,2");
    }

    @Test
    public void benchmark_building_sql() {
        Stopwatch stopwatch = new Stopwatch("building_sql", true);
        int n = 100000;
        int sum = 0;
        for (int i = 0; i < n; i++) {
            String sql = QueryBuilder.withConnection(null)
                    .select(Tables.SONG.FIELDS)
                    .from(Tables.SONG)
                    .join(Tables.ARTIST).on(Tables.ARTIST.ID.eq(Tables.SONG.ARTIST_ID))
                    .where(Tables.ARTIST.ID.eq(1))
                    .toSql();
            sum += sql.length();
        }
        stopwatch.stop();
        assertThat(stopwatch.getDuration()).isLessThan(150000000);
        assertThat(sum).isNotEqualTo(0);
    }

}

