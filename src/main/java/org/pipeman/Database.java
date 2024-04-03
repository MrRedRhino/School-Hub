package org.pipeman;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.pipeman.rest.NewsApi;
import org.pipeman.rest.SupervisionApi;
import org.pipeman.rest.TodoApi;
import org.pipeman.rest.User;
import org.pipeman.rest.reservation.Live;
import org.pipeman.substitution_plan.PlanCache;
import org.pipeman.substitution_plan.notifications.Subscriber;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

public class Database {
    private static final Jdbi JDBI;
    private static final JedisPooled jedis;

    static {
        HikariConfig hikariConfig = new HikariConfig();
        Config config = Config.get();
        hikariConfig.setUsername(config.dbUser);
        hikariConfig.setPassword(config.dbPassword);
        hikariConfig.setJdbcUrl(config.dbUrl);

        JDBI = Jdbi.create(new HikariDataSource(hikariConfig));
        JDBI.registerRowMapper(ConstructorMapper.factory(User.Data.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(Subscriber.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(TodoApi.Todo.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(SupervisionApi.Supervision.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(Live.Reservation.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(PlanCache.PlanAccount.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(NewsApi.NewsData.class));
        JDBI.installPlugin(new Jackson2Plugin());

        String[] split = config.redisUrl.split("@");
        DefaultJedisClientConfig builder = DefaultJedisClientConfig.builder()
                .user(config.redisUser)
                .password(config.redisPassword)
                .database(Integer.parseInt(split[0]))
                .build();
        jedis = new JedisPooled(HostAndPort.from(split[1]), builder);
    }

    public static JedisPooled getJedis() {
        return jedis;
    }

    public static Jdbi getJdbi() {
        return JDBI;
    }
}
