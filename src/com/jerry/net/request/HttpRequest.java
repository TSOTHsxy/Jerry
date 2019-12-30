package com.jerry.net.request;

import com.jerry.extend.CodeAble;

import java.nio.charset.Charset;
import java.util.*;

/**
 * HTTP协议请求默认实现
 * 参考tomcat的请求解析方式
 */
public final class HttpRequest extends SimpleRequest implements CodeAble {

    /**
     * 请求字节流
     */
    private byte[] bytes;

    /**
     * 编码字符集
     */
    private Charset charset;

    /**
     * 请求行请求方法
     */
    private String method;

    /**
     * 请求行协议版本
     */
    private String protocol;

    /**
     * 请求参数
     */
    private String args;

    /**
     * 请求参数映射表
     */
    private Map<String, String> argsMap;

    /**
     * 请求头字段映射表
     */
    private Map<String, List<String>> headers = new HashMap<>();

    /**
     * 请求资源
     */
    private byte[] body;

    public HttpRequest(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * @return 请求行请求方法
     */
    public String method() {
        if (failedParsing()) {
            throw new IllegalOperationException();
        }
        return method;
    }

    /**
     * @return 请求行协议版本
     */
    public String protocol() {
        if (failedParsing()) {
            throw new IllegalOperationException();
        }
        return protocol;
    }

    /**
     * @return 请求参数
     */
    public String args() {
        if (failedParsing()) {
            throw new IllegalOperationException();
        }
        return args;
    }

    /**
     * @return 请求参数映射表
     */
    public Map<String, String> argsMap() {
        if (failedParsing()) {
            throw new IllegalOperationException();
        }
        //懒加载模式
        if (argsMap == null) {
            argsMap = parseArgs();
        }
        return argsMap;
    }

    /**
     * @return 请求行
     */
    public String requestLine() {
        if (failedParsing()) {
            throw new IllegalOperationException();
        }
        StringBuilder line = new StringBuilder();
        if (args == null) {
            line.append(String.format("%s %s %s", method, url(), protocol));
        } else {
            line.append(String.format("%s %s?%s %s", method, url(), args, protocol));
        }
        return line.toString();
    }

    /**
     * @return 请求头字段映射表
     */
    public Map<String, List<String>> headersMap() {
        if (failedParsing()) {
            throw new IllegalOperationException();
        }
        return headers;
    }

    /**
     * 获取请求头字段值集合
     *
     * @param key 请求头字段名
     * @return 与字段名对应的字段值集合
     */
    public List<String> headers(String key) {
        if (failedParsing()) {
            throw new IllegalOperationException();
        }
        return headers.get(key);
    }

    /**
     * 获取请求头字段值
     *
     * @param key 请求头字段名
     * @return 与字段名对应的字段值
     */
    public String header(String key) {
        if (failedParsing()) {
            throw new IllegalOperationException();
        }
        List<String> values = headers.get(key);
        if (values != null && values.size() == 1) {
            return values.get(0);
        }
        return null;
    }

    /**
     * @return 请求头字段名集合
     */
    public Set<String> headersName() {
        if (failedParsing()) {
            throw new IllegalOperationException();
        }
        return headers.keySet();
    }

    /**
     * @return 请求资源类型
     */
    public String contentType() {
        return header("Content-Type");
    }

    /**
     * @return 请求资源长度
     */
    public String contentLength() {
        return header("Content-Length");
    }

    /**
     * @return 请求资源编码方式
     */
    public List<String> contentEncoding() {
        return headers.get("Content-Encoding");
    }

    /**
     * @return 缓存控制策略
     */
    public String cacheControl() {
        return header("Cache-Control");
    }

    /**
     * @return 本地缓存标识
     */
    public String eTag() {
        return header("If-None-Match");
    }

    /**
     * @return 用户标识
     */
    public List<String> userAgent() {
        return headers.get("User-Agent");
    }

    /**
     * @return 请求来源
     */
    public String referer() {
        return header("Referer");
    }

    /**
     * @return 客户端主机信息
     */
    public String host() {
        return header("Host");
    }

    /**
     * @return 请求头
     */
    public String headers() {
        if (failedParsing()) {
            throw new IllegalOperationException();
        }
        StringBuilder headers = new StringBuilder();
        for (String key : this.headers.keySet()) {
            headers.append(key).append(": ");
            for (String value : this.headers.get(key)) {
                headers.append(value).append("; ");
            }
            headers.delete(headers.length() - 2, headers.length());
            headers.append("\r\n");
        }
        headers.delete(headers.length() - 2, headers.length());
        return headers.toString();
    }

    /**
     * @return 请求资源
     */
    public byte[] body() {
        return body;
    }

    /**
     * 解析请求并设置请求解析状态
     */
    public void parse() {
        state = parseRequest();
    }

    @Override
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * 逐字节解析请求
     */
    private boolean parseRequest() {
        byte[] bytes = this.bytes;
        int limit = bytes.length;
        int pos = 0;
        //去除行首空白字符
        byte chr;
        do {
            if (pos > limit) {
                return false;
            }
            chr = bytes[pos++];
        } while (chr == Constants.CR || chr == Constants.LF
                || chr == Constants.SP || chr == Constants.HT);
        //解析请求行请求方法
        int start = --pos;
        boolean space = false;
        while (!space) {
            if (failedIndex(pos, limit)) {
                return false;
            }
            if (bytes[pos] == Constants.SP || bytes[pos] == Constants.HT) {
                method = makeString(start, pos - start);
                space = true;
            }
            pos++;
        }
        //去除空白字符
        while (space) {
            if (failedIndex(pos, limit)) {
                return false;
            }
            if (bytes[pos] == Constants.SP || bytes[pos] == Constants.HT) {
                pos++;
            } else {
                space = false;
            }
        }
        //解析请求行请求地址
        int end = 0;
        start = pos;
        int arg = -1;
        while (!space) {
            if (failedIndex(pos, limit)) {
                return false;
            }
            if (bytes[pos] == Constants.SP || bytes[pos] == Constants.HT) {
                end = pos;
                space = true;
            } else if (bytes[pos] == Constants.QUESTION && arg == -1)
                if (pos != start) {
                    arg = pos;
                }
            pos++;
        }
        //分别处理有无请求参数的情况
        if (arg != -1) {
            url = makeString(start, arg - start);
            if ((arg + 1) == end) {
                return false;
            }
            args = makeString(arg + 1, end - arg + 1);
        } else {
            url = makeString(start, end - start);
        }
        //去除空白字符
        while (space) {
            if (failedIndex(pos, limit)) {
                return false;
            }
            if (bytes[pos] == Constants.SP || bytes[pos] == Constants.HT) {
                pos++;
            } else {
                space = false;
            }
        }
        //解析请求行协议版本
        end = 0;
        start = pos;
        boolean tail = false;
        while (!tail) {
            if (pos > limit) {
                return false;
            }
            if (bytes[pos] == Constants.CR) {
                end = pos;
            } else if (bytes[pos] == Constants.LF) {
                if (end == 0) {
                    end = pos;
                }
                tail = true;
            }
            pos++;
        }
        protocol = makeString(start, end - start);
        //循环解析请求头
        String key;
        while (true) {
            //检查请求头是否结束
            while (true) {
                if (bytes[pos] == Constants.CR) {
                    pos++;
                } else if (bytes[pos] == Constants.LF) {
                    pos++;
                    if (method.equals("GET") || pos == limit) {
                        return true;
                    }
                    if (pos > limit) {
                        return false;
                    }
                    body = new byte[limit - pos];
                    System.arraycopy(bytes, pos, body, 0, limit - pos);
                    return true;
                } else {
                    break;
                }
            }
            //去除空白字符
            space = true;
            while (space) {
                if (failedIndex(pos, limit)) {
                    return false;
                }
                if (bytes[pos] == Constants.SP || bytes[pos] == Constants.HT) {
                    pos++;
                } else {
                    space = false;
                }
            }
            //解析请求头字段名
            start = pos;
            boolean colon = false;
            while (!colon) {
                if (failedIndex(pos, limit)) {
                    return false;
                }
                chr = bytes[pos];
                if (chr == Constants.COLON) {
                    end = pos;
                    colon = true;
                }
                pos++;
            }
            key = makeString(start, end - start);
            //去除空白字符
            space = true;
            while (space) {
                if (failedIndex(pos, limit)) {
                    return false;
                }
                if (bytes[pos] == Constants.SP || bytes[pos] == Constants.HT) {
                    pos++;
                } else {
                    space = false;
                }
            }
            //解析字段名对应的字段值
            end = 0;
            start = pos;
            tail = false;
            while (!tail) {
                if (pos > limit) {
                    return false;
                }
                if (bytes[pos] == Constants.CR) {
                    end = pos;
                } else if (bytes[pos] == Constants.LF) {
                    if (end == 0) {
                        end = pos;
                    }
                    tail = true;
                }
                pos++;
            }
            headers.put(key, parseValue(start, end - start));
        }
    }

    /**
     * 检查字节流索引合法性
     */
    private boolean failedIndex(int pos, int limit) {
        if (pos > limit) {
            return true;
        }
        byte[] bytes = this.bytes;
        return bytes[pos] == Constants.CR || bytes[pos] == Constants.LF;
    }

    /**
     * 解析请求参数
     */
    private Map<String, String> parseArgs() {
        Map<String, String> argsMap = new HashMap<>();
        for (String kvp : args.split("&")) {
            String[] kva = kvp.split("=");
            argsMap.put(kva[0], kva[1]);
        }
        return argsMap;
    }

    /**
     * 解析请求头字段值
     */
    private List<String> parseValue(int offset, int length) {
        byte[] bytes = this.bytes;
        String values = new String(bytes, offset, length,
                charset == null ? Constants.charset : charset);
        String[] valueArray = values.split("; *");
        return new ArrayList<>(Arrays.asList(valueArray));
    }

    /**
     * 将字节流转换为字符流
     */
    private String makeString(int offset, int length) {
        byte[] bytes = this.bytes;
        return new String(bytes, offset, length,
                charset == null ? Constants.charset : charset);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) obj;
            return Arrays.equals(bytes, request.bytes);
        }
        return false;
    }

    @Override
    public String toString() {
        return requestLine() + "\r\n" + headers() + "\r\n\r\n" + (body == null ? ""
                : new String(body, charset == null ? Constants.charset : charset));
    }
}
