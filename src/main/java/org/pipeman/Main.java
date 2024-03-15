package org.pipeman;

import com.fasterxml.jackson.databind.module.SimpleModule;
import de.mkammerer.snowflakeid.SnowflakeIdGenerator;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import io.javalin.json.JavalinJackson;
import org.pipeman.rest.*;
import org.pipeman.rest.reservation.ReservationApi;
import org.pipeman.substitution_plan.PdfDataSerializer;
import org.pipeman.substitution_plan.PlanData;

import java.time.LocalDate;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {
    public static final SnowflakeIdGenerator ID_GENERATOR = SnowflakeIdGenerator.createDefault(1);

    public static void main(String[] args) {
        SslPlugin sslPlugin = new SslPlugin(sslConfig -> {
            sslConfig.http2 = true;
            sslConfig.secure = false;
            sslConfig.insecurePort = Config.get().apiPort;
        });
        Javalin.create(config -> {
            config.registerPlugin(sslPlugin);

            config.validation.register(LocalDate.class, s -> {
                String[] split = s.split("-");
                int year = Integer.parseInt(split[0]);
                int month = Integer.parseInt(split[1]);
                int day = Integer.parseInt(split[2]);
                return LocalDate.of(year, month, day);
            });

            config.showJavalinBanner = false;

//            config.bundledPlugins.enableCors(c -> c.addRule(CorsPluginConfig.CorsRule::anyHost));

            config.router.apiBuilder(() -> {
                path("api", () -> {
                    path("login", () -> {
                        post(LoginApi::sendCode);
                        post("verify", LoginApi::login);
                    });

                    path("account", () -> {
                        get(LoginApi::getAccount);
                        post("logout", LoginApi::logout);

                        patch("settings", LoginApi::patchSettings);
                        patch("course-filter", LoginApi::patchCourseFilter);
                    });

                    path("subscriptions", () -> {
                        put(SubscriptionApi::addSubscription);
                        delete(SubscriptionApi::removeSubscription);
                    });

                    path("plans", () -> {
                        get(SubstitutionApi::getPlans);

                        path("{class}", () -> {
                            put(SubstitutionApi::addAccount);
                            delete(SubstitutionApi::removeAccount);
                            get("today", SubstitutionApi::getPlanToday);
                            get("tomorrow", SubstitutionApi::getPlanTomorrow);
                        });
                    });

                    path("books", () -> {
                        get("", BookApi::listBooks);
                        get("search-completions", BookApi::completions);

                        get("{book}", BookApi::getBook);
                        get("{book}/{page}", BookApi::getPage);
                        get("{book}/{page}/summary", BookApi::getSummary);
                        get("{book}/{page}/annotations", BookApi::getAnnotation);
                        put("{book}/{page}/annotations", BookApi::putAnnotation);
                    });

                    path("todos", () -> {
                        get(TodoApi::getTodos);
                        put(TodoApi::putTodo);
                        patch("{id}", TodoApi::patchTodo);
                        put("{id}", TodoApi::putCompletedTodo);
                        delete("{id}", TodoApi::completeTodo);
                    });

                    path("supervisions", () -> {
                        get(SupervisionApi::getSupervisions);
                        delete("{date}", SupervisionApi::deleteSupervision);
                        put("{date}", SupervisionApi::putSupervision);
                    });

                    path("reservations", () -> {
                        sse("live", ReservationApi::sse);
                        put("{seat}", ReservationApi::reserveSeat);
                        delete("{seat}", ReservationApi::removeReservation);
                    });

                    get("news", News::getNews);
                });
            });
        }).start();

        SimpleModule module = new SimpleModule();
        module.addSerializer(PlanData.class, new PdfDataSerializer());
        JavalinJackson.Companion.defaultMapper().registerModule(module);
    }
}
