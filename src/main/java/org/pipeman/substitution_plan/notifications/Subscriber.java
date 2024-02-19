package org.pipeman.substitution_plan.notifications;

import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Subscription.Keys;
import org.json.JSONObject;

import java.util.Set;

public class Subscriber {
    private final Set<String> filter;
    private final Subscription subscription;

    public Subscriber(String endpoint, String key, String auth, Set<String> filter) {
        this.filter = filter;
        Keys keys = new Keys(key, auth);
        subscription = new Subscription(endpoint, keys);
    }

    public Set<String> filter() {
        return filter;
    }

    public Subscription subscription() {
        return subscription;
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "filter=" + filter +
                ", endpoint=" + subscription.endpoint +
                '}';
    }
}
