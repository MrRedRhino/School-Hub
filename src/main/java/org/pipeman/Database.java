package org.pipeman;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.pipeman.rest.LoginApi;
import org.pipeman.rest.SupervisionApi;
import org.pipeman.rest.TodoApi;
import org.pipeman.rest.User;
import org.pipeman.rest.reservation.Live;
import org.pipeman.substitution_plan.notifications.Subscriber;

public class Database {
    private static final Jdbi JDBI;

    static {
        HikariConfig config = new HikariConfig();
        config.setUsername(Config.get().dbUser);
        config.setPassword(Config.get().dbPassword);
        config.setJdbcUrl(Config.get().dbUrl);

        JDBI = Jdbi.create(new HikariDataSource(config));
        JDBI.registerRowMapper(ConstructorMapper.factory(User.Data.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(Subscriber.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(TodoApi.Todo.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(SupervisionApi.Supervision.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(Live.Reservation.class));
        JDBI.installPlugin(new Jackson2Plugin());
    }

    public static Jdbi getJdbi() {
        return JDBI;
    }
}
