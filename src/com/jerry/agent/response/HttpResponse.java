package com.jerry.agent.response;

import java.util.*;

/**
 * HTTP协议响应默认实现
 */
public final class HttpResponse extends SimpleResponse {

    /**
     * 响应行协议版本
     */
    private String protocol;

    /**
     * 响应行状态码
     */
    private String code;

    /**
     * 响应行状态码描述
     */
    private String reason;

    /**
     * 响应头字段映射表
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * 响应资源
     */
    private byte[] body;

    /**
     * 设置响应行协议版本
     *
     * @param protocol 响应行协议版本
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * @return 响应行协议版本
     */
    public String protocol() {
        return protocol;
    }

    /**
     * 设置响应状态
     *
     * @param code   响应行状态码
     * @param reason 响应行状态码描述
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse status(String code, String reason) {
        this.code = code;
        this.reason = reason;
        return this;
    }

    /**
     * @return 响应行状态码
     */
    public String code() {
        return code;
    }

    /**
     * @return 响应行状态码描述
     */
    public String reason() {
        return reason;
    }

    /**
     * @return 响应行
     */
    public String responseLine() {
        return String.format("%s %s %s", protocol, code, reason);
    }

    /**
     * 添加响应头字段
     *
     * @param key   响应头字段名
     * @param value 响应头字段值
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    /**
     * @return 响应头字段映射表
     */
    public Map<String, String> headersMap() {
        return headers;
    }

    /**
     * 通过响应头字段名获取对应的字段值
     *
     * @param key 响应头字段名
     * @return 与字段名对应的字段值
     */
    public String header(String key) {
        return headers.get(key);
    }

    /**
     * @return 响应头字段名集合
     */
    public Set<String> headersName() {
        return headers.keySet();
    }

    /**
     * @return 响应头
     */
    public String headers() {
        StringBuilder headers = new StringBuilder();
        for (String key : this.headers.keySet()) {
            headers.append(String.format("%s: %s\r\n", key, this.headers.get(key)));
        }
        headers.delete(headers.length() - 2, headers.length());
        return headers.toString();
    }

    /**
     * 设置响应资源类型
     *
     * @param contentType 响应资源类型
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse contentType(String contentType) {
        header("Content-Type", contentType);
        return this;
    }

    /**
     * @return 响应资源类型
     */
    public String contentType() {
        return headers.get("Content-Type");
    }

    /**
     * 设置响应资源长度
     *
     * @param contentLength 响应资源长度
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse contentLength(String contentLength) {
        header("Content-Length", contentLength);
        return this;
    }

    /**
     * @return 响应资源长度
     */
    public String contentLength() {
        return headers.get("Content-Length");
    }

    /**
     * 设置响应资源编码方式
     *
     * @param contentEncoding 响应资源编码方式
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse contentEncoding(String contentEncoding) {
        header("Content-Encoding", contentEncoding);
        return this;
    }

    /**
     * @return 响应资源编码方式
     */
    public String contentEncoding() {
        return headers.get("ContentEncoding");
    }

    /**
     * 设置缓存控制策略
     *
     * @param cacheControl 缓存控制策略
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse cacheControl(String cacheControl) {
        header("Cache-Control", cacheControl);
        return this;
    }

    /**
     * @return 缓存控制策略
     */
    public String cacheControl() {
        return headers.get("Cache-Control");
    }

    /**
     * 设置响应资源标识
     *
     * @param eTag 响应资源标识
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse eTag(String eTag) {
        header("ETag", eTag);
        return this;
    }

    /**
     * @return 响应资源标识
     */
    public String eTag() {
        return headers.get("ETag");
    }

    /**
     * 设置服务器响应时间戳
     *
     * @param date 服务器响应时间戳
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse date(String date) {
        header("Date", date);
        return this;
    }

    /**
     * @return 服务器响应时间戳
     */
    public String date() {
        return headers.get("Date");
    }

    /**
     * 设置响应资源最后被修改的时间
     *
     * @param lastModified 响应资源最后被修改的时间
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse lastModified(String lastModified) {
        header("Last-Modified", lastModified);
        return this;
    }

    /**
     * @return 响应资源最后被修改的时间
     */
    public String lastModified() {
        return headers.get("Last-Modified");
    }

    /**
     * 设置重定向目标url
     *
     * @param url 重定向目标url
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse location(String url) {
        header("Location", url);
        return this;
    }

    /**
     * @return 重定向目标url
     */
    public String location() {
        return headers.get("Location");
    }

    /**
     * 设置服务器标识
     *
     * @param serverIdentity 服务器标识
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse serverIdentity(String serverIdentity) {
        header("Server", serverIdentity);
        return this;
    }

    /**
     * @return 服务器标识
     */
    public String serverIdentity() {
        return headers.get("Server");
    }

    /**
     * 设置响应资源
     *
     * @param body 响应资源
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse body(byte[] body) {
        this.body = body;
        return this;
    }

    /**
     * @return 响应资源
     */
    public byte[] body() {
        return body;
    }

    /**
     * 重定向到目标url
     *
     * @param url 目标url
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse redirect(String url) {
        status("307", "Internal Redirect");
        location(url);
        return this;
    }

    /**
     * 使用客户端本地缓存
     *
     * @param eTag 响应资源标识
     * @return 当前对象实例(用以支持链式调用)
     */
    public HttpResponse useCache(String eTag) {
        status("304", "Not Modified");
        eTag(eTag);
        return this;
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = (responseLine() + "\r\n" + headers() + "\r\n\r\n").getBytes();
        if (body != null) {
            byte[] result = new byte[bytes.length + body.length];
            System.arraycopy(bytes, 0, result, 0, bytes.length);
            System.arraycopy(body, 0, result, bytes.length, body.length);
            return result;
        }
        return bytes;
    }

    @Override
    public int hashCode() {
        return headers.hashCode() ^ Arrays.hashCode(body) ^ 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HttpResponse) {
            HttpResponse hr = (HttpResponse) obj;
            return protocol.equals(hr.protocol)
                    && code.equals(hr.code) && reason.equals(hr.reason)
                    && headers.equals(hr.headers) && Arrays.equals(body, hr.body);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder response = new StringBuilder();
        response.append(responseLine())
                .append("\r\n")
                .append(headers())
                .append("\r\n\r\n");
        if (body != null) {
            response.append(new String(body));
        }
        return response.toString();
    }
}
