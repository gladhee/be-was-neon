package webserver.http.exception;

import webserver.http.response.HttpStatusCode;

public class HttpException extends RuntimeException {

    private final HttpStatusCode statusCode;

    public HttpException(HttpStatusCode statusCode) {
        super(statusCode.toString());
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode.getStatusCode();
    }

}
