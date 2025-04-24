package webserver.mapper;

import handler.ArticleHandler;
import handler.Handler;
import webserver.annotation.RequestMapping;
import webserver.http.common.HttpMethod;
import webserver.http.common.HttpSession;
import webserver.http.exception.HttpException;
import webserver.http.request.HttpRequest;
import webserver.model.Model;
import webserver.resolver.DynamicHandler;
import webserver.resolver.ResolveResponse;
import webserver.resolver.SessionResolver;
import webserver.view.ModelAndView;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static webserver.http.common.HttpConstants.SPACE;
import static webserver.http.response.HttpStatusCode.INTERNAL_SERVER_ERROR;

public class HandlerMapper {

    private static final HandlerMapper instance = new HandlerMapper();
    // 잦은 리플렉션 사용으로 성능 저하를 방지하기 위해 MethodHandles.Lookup을 사용
    private final MethodHandles.Lookup lookup;
    private final Map<String, DynamicHandler> mappings;

    private HandlerMapper() {
        this.mappings = new HashMap<>();
        this.lookup = MethodHandles.lookup();
    }

    public static HandlerMapper getInstance() {
        return instance;
    }

    public void initialize() {
        registerController(Handler.getInstance());
        registerController(new ArticleHandler());
    }

    private void registerController(Object controller) {
        Method[] methods = controller.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                registerMapping(controller, method);
            }
        }
    }

    private void registerMapping(Object controller, Method method) {
        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        validateSignature(method);
        String key = mapping.method() + SPACE + mapping.path();

        try {
            MethodHandle methodHandle = lookup.unreflect(method).bindTo(controller);
            DynamicHandler handler = createDynamicHandler(method, methodHandle);
            mappings.put(key, handler);
        } catch (IllegalAccessException e) {
            throw new HttpException(INTERNAL_SERVER_ERROR);
        }
    }

    private void validateSignature(Method method) {
        for (Class<?> p : method.getParameterTypes()) {
            if (p != HttpRequest.class && p != HttpSession.class && p != Model.class) {
                throw new IllegalStateException(
                        "[ERROR] 지원하지 않는 파라미터 타입: "
                                + method.getName() + " -> " + p.getSimpleName()
                );
            }
        }
    }

    private DynamicHandler createDynamicHandler(Method method, MethodHandle methodHandle) {
        return (request) -> {
            Model model = new Model();
            Object[] args = buildArguments(method, request, model);
            Object ret;
            try {
                ret = methodHandle.invokeWithArguments(args);
            } catch (Throwable t) {
                if (t instanceof HttpException) {
                    throw (HttpException) t;
                }

                throw new HttpException(INTERNAL_SERVER_ERROR);
            }
            // ResolveResponse을 리턴한 경우 그대로
            if (ret instanceof ResolveResponse<?> resolveResponse) {
                return resolveResponse;
            }
            // String 리턴 = 뷰 이름
            if (ret instanceof String viewName) {
                return ResolveResponse.view(viewName, model);
            }
            // ModelAndView 직접 리턴
            if (ret instanceof ModelAndView mav) {
                return ResolveResponse.view(mav);
            }

            throw new HttpException(INTERNAL_SERVER_ERROR);
        };
    }

    private Object[] buildArguments(Method method, HttpRequest request, Model model) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == HttpRequest.class) {
                args[i] = request;
                continue;
            }

            if (paramTypes[i] == HttpSession.class) {
                SessionResolver.injectSession(request);
                args[i] = request.getSession();
            }

            if (paramTypes[i] == Model.class) {
                args[i] = model;
            }
        }

        return args;
    }

    public DynamicHandler getHandler(HttpMethod method, String path) {
        return mappings.get(method + SPACE + path);
    }

}
