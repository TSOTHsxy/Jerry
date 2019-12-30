package com.jerry.agent.response;

/**
 * 请求处理状态枚举
 */
public enum State implements Additional {

    /**
     * 未就绪状态
     * 当响应对象未进行初始化时处于此状态
     */
    UNREADY {
        @Override
        public String code() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String reason() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String explain() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isThrow() {
            throw new UnsupportedOperationException();
        }
    },

    /**
     * 待检查状态
     * 当响应对象初始化后处于此状态
     */
    CHECKED {
        @Override
        public String code() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String reason() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String explain() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isThrow() {
            throw new UnsupportedOperationException();
        }
    },

    /**
     * 已就绪状态
     * 当请求检查完成等待处理时处于此状态
     */
    READY {
        @Override
        public String code() {
            return "200";
        }

        @Override
        public String reason() {
            return "OK";
        }

        @Override
        public String explain() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isThrow() {
            return false;
        }
    },

    /**
     * 400状态
     * 当请求异常时处于此状态
     */
    BAD_REQUEST {
        @Override
        public String code() {
            return "400";
        }

        @Override
        public String reason() {
            return "Bad Request";
        }

        @Override
        public String explain() {
            return "The browser (or proxy) sent a request " +
                    "that this server could not understand.";
        }

        @Override
        public boolean isThrow() {
            return false;
        }
    },

    /**
     * 404状态
     * 当请求地址不存在时处于此状态
     */
    NOT_FOUND {
        @Override
        public String code() {
            return "404";
        }

        @Override
        public String reason() {
            return "Not Found";
        }

        @Override
        public String explain() {
            return "The requested URL was not found on the server. If you " +
                    "entered the URL manually please check your spelling and try again.";
        }

        @Override
        public boolean isThrow() {
            return false;
        }
    },

    /**
     * 405状态
     * 当请求方法未授权时处于此状态
     */
    METHOD_NOT_ALLOWED {
        @Override
        public String code() {
            return "405";
        }

        @Override
        public String reason() {
            return "Method Not Allowed";
        }

        @Override
        public String explain() {
            return "The method is not allowed for the requested URL.";
        }

        @Override
        public boolean isThrow() {
            return false;
        }
    },

    /**
     * 414状态
     * 当请求地址过长时处于此状态
     */
    REQUEST_URI_TOO_LARGE {
        @Override
        public String code() {
            return "414";
        }

        @Override
        public String reason() {
            return "Request URI Too Long";
        }

        @Override
        public String explain() {
            return "The length of the requested URL exceeds the capacity " +
                    "limit for this server. The request cannot be processed.";
        }

        @Override
        public boolean isThrow() {
            return false;
        }
    },

    /**
     * 500状态
     * 当服务器出现错误时处于此状态
     */
    INTERNAL_SERVER_ERROR {
        @Override
        public String code() {
            return "500";
        }

        @Override
        public String reason() {
            return "Internal Server Error";
        }

        @Override
        public String explain() {
            return "The server encountered an internal error and was " +
                    "unable to complete your request. Either the server " +
                    "is overloaded or there is an error in the application.";
        }

        @Override
        public boolean isThrow() {
            return true;
        }
    },

    /**
     * 501状态
     * 当服务器不支持请求的功能时处于此状态
     */
    NOT_IMPLEMENTED {
        @Override
        public String code() {
            return "501";
        }

        @Override
        public String reason() {
            return "Not Implemented";
        }

        @Override
        public String explain() {
            return "The server does not support " +
                    "the action requested by the browser.";
        }

        @Override
        public boolean isThrow() {
            return true;
        }
    },

    /**
     * 505状态
     * 当服务器不支持请求的协议版本时处于此状态
     */
    HTTP_VERSION_NOT_SUPPORTED {
        @Override
        public String code() {
            return "505";
        }

        @Override
        public String reason() {
            return "HTTP Version Not Supported";
        }

        @Override
        public String explain() {
            return "The server does not support the HTTP " +
                    "protocol version used in the request.";
        }

        @Override
        public boolean isThrow() {
            return true;
        }
    },

    /**
     * 完成状态
     * 当请求处理完毕时处于此状态
     * 此时响应对象已构建完成
     */
    COMPLETE {
        @Override
        public String code() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String reason() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String explain() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isThrow() {
            throw new UnsupportedOperationException();
        }
    }
}
