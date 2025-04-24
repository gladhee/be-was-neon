package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.common.HttpMethod;
import webserver.http.exception.HttpException;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;
import webserver.http.response.StatusLine;
import webserver.mapper.HandlerMapper;
import webserver.resolver.*;
import webserver.util.Convertor;
import webserver.view.ModelAndView;
import webserver.view.TemplateEngine;

import java.io.IOException;

public class Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);
    private static final String ERROR_PATH = "/error/";
    private static final String HTML_EXT = ".html";
    private final HttpRequest request;

    public Dispatcher(HttpRequest request) {
        this.request = request;
    }

    public HttpResponse dispatch() throws IOException {
        Resolver resolver = determineResolver();
        try {
            ResolveResponse<?> resolveResponse = resolver.resolve();

            SessionResolver.addSessionCookieIfNew(request, resolveResponse);
            return buildHttpResponse(resolveResponse);
        } catch (HttpException e) {
            logger.error("Error during request processing: {}", e.getMessage());
            String errorLocation = ERROR_PATH + e.getStatusCode() + HTML_EXT;
            ResolveResponse<?> errorResponse = ResolveResponse.redirect(errorLocation);

            return buildHttpResponse(errorResponse);
        }
    }

    private Resolver determineResolver() {
        String path = request.getPath();
        HttpMethod method = request.getMethod();
        DynamicHandler handler = HandlerMapper.getInstance().getHandler(method, path);

        if (handler == null) {
            logger.debug("Dispatching to ResourceResolver");
            return new ResourceResolver(request);
        }

        logger.debug("Dispatching to DynamicResolver");
        return new DynamicResolver(request, handler);
    }

    private HttpResponse buildHttpResponse(ResolveResponse<?> responseEntity) {
        if (responseEntity.isView()) {
            return buildResponseByTemplate(responseEntity);
        }

        return new HttpResponse(
                new StatusLine(responseEntity.getStatusCode()),
                responseEntity.getHeaders(),
                Convertor.convertToByteArray(responseEntity.getBody())
        );
    }

    private HttpResponse buildResponseByTemplate(ResolveResponse<?> responseEntity) {
        ModelAndView mav = responseEntity.getModelAndView();
        TemplateEngine templateEngine = new TemplateEngine(mav, request.getSession());
        String html = templateEngine.render();

        return new HttpResponse(
                new StatusLine(responseEntity.getStatusCode()),
                responseEntity.getHeaders(),
                Convertor.convertToByteArray(html)
        );
    }

}
