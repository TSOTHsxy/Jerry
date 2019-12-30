package com.jerry.agent.valve;

import com.jerry.agent.response.HttpResponse;
import com.jerry.agent.response.State;
import com.jerry.net.request.HttpRequest;

/**
 * 默认响应阀门
 * 提供默认响应内容
 */
public final class DefaultValve extends SimpleValve {

    /**
     * 默认index页面标题
     */
    private static final String HTML_TITLE_INDEX = "Welcome to Jerry";

    /**
     * 默认index页面内容
     */
    private static final String HTML_BODY_INDEX = "If you see this page," +
            "the Jerry is working.<br><i>Thank you for using Jerry.</i>";

    /**
     * 默认页面模板
     */
    private static final String HTML = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "   <meta charset=\"UTF-8\">\n" +
            "   <title>%s %s</title>\n" +
            "   <style>\n" +
            "       h1, p {\n" +
            "           text-align: center;\n" +
            "       }\n" +
            "   </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "   <h1>%s</h1>\n" +
            "   <p>%s</p>\n" +
            "</body>\n" +
            "</html>";

    @Override
    void doProcess(HttpRequest request, HttpResponse response) {
        String html;
        State state;
        switch (state = response.state()) {
            case READY:
            case NOT_FOUND:
                if ("/".equals(request.url())) {
                    response.status(
                            State.READY.code(),
                            State.READY.reason());
                    html = String.format(
                            HTML,
                            "",
                            HTML_TITLE_INDEX,
                            HTML_TITLE_INDEX,
                            HTML_BODY_INDEX
                    );
                    break;
                }
                response.status(
                        State.NOT_FOUND.code(),
                        State.NOT_FOUND.reason()
                );
                html = String.format(
                        HTML,
                        State.NOT_FOUND.code(),
                        State.NOT_FOUND.reason(),
                        State.NOT_FOUND.reason(),
                        State.NOT_FOUND.explain()
                );
                break;
            case BAD_REQUEST:
            case METHOD_NOT_ALLOWED:
            case REQUEST_URI_TOO_LARGE:
            case INTERNAL_SERVER_ERROR:
            case NOT_IMPLEMENTED:
            case HTTP_VERSION_NOT_SUPPORTED:
                response.status(state.code(), state.reason());
                html = String.format(
                        HTML,
                        state.code(),
                        state.reason(),
                        state.reason(),
                        state.explain()
                );
                break;
            default:
                return;
        }
        response.body(html.getBytes())
                .contentType("text/html")
                .contentLength(String.valueOf(html.getBytes().length))
                .state(State.COMPLETE);
    }
}
