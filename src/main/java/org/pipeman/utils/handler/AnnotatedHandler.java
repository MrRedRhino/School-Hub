package org.pipeman.utils.handler;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.validation.Validation;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AnnotatedHandler implements Handler {
    private final List<Function<Context, ?>> arguments = new ArrayList<>();
    private final Method method;

    public AnnotatedHandler(Class<?> clazz, String methodName) {
        method = getMethod(clazz, methodName);

        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) throw new IllegalArgumentException("Method has no arguments");

        if (parameters[0].getType() != Context.class) {
            throw new IllegalArgumentException("Method must have a context parameter");
        }

        for (int i = 1; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getAnnotations().length != 1) {
                throw new IllegalArgumentException("Parameter " + parameter.getName() + " must have exactly one annotation");
            }

            QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
            if (queryParam != null) {
                arguments.add(context -> context.queryParamAsClass(queryParam.value(), parameter.getType()).get());
                continue;
            }

            PathParam pathParam = parameter.getAnnotation(PathParam.class);
            if (pathParam != null) {
                arguments.add(context -> context.pathParamAsClass(pathParam.value(), parameter.getType()).get());
                continue;
            }

            BodyParam bodyParam = parameter.getAnnotation(BodyParam.class);
            if (bodyParam != null) {
                arguments.add(context -> {
                    JSONObject body = new JSONObject(context.body());
                    Object value = body.opt(bodyParam.value());
                    return context.appData(Validation.ValidationKey)
                            .validator(bodyParam.value(), parameter.getType(), value == null ? null : String.valueOf(value))
                            .get();
                });
            }
        }
    }

    private static Method getMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Method not found: " + methodName);
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {
        if (arguments.isEmpty()) {
            method.invoke(null, context);
        } else {
            Object[] args = new Object[arguments.size() + 1];
            args[0] = context;
            for (int i = 0; i < arguments.size(); i++) {
                args[i + 1] = arguments.get(i).apply(context);
            }
            method.invoke(null, args);
        }
    }
}
