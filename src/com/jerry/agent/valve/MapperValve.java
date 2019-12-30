package com.jerry.agent.valve;

import com.jerry.agent.adapter.Adapter;
import com.jerry.agent.annotation.root;
import com.jerry.agent.annotation.url;
import com.jerry.agent.response.HttpResponse;
import com.jerry.agent.response.State;
import com.jerry.extend.Lifecycle;
import com.jerry.logger.LogAble;
import com.jerry.logger.LogStyle;
import com.jerry.logger.LogUtils;
import com.jerry.net.request.HttpRequest;
import com.jerry.utils.Resources;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 映射阀门
 * 建立请求和对象方法之间的映射
 */
public final class MapperValve extends SimpleValve implements Lifecycle, LogAble {

    private static Logger logger
            = Logger.getLogger(MapperValve.class.getSimpleName());

    private static boolean logSetup = false;

    /**
     * 请求映射表
     */
    private Map<String, Mapper> mappers = new HashMap<>();

    /**
     * 映射对象
     */
    private Object object;

    /**
     * 数据适配器映射表
     */
    private Map<Class<?>, Adapter<?>> adapters = new HashMap<>();

    /**
     * 文件类型-响应资源类型映射表
     */
    private Map<String, String> contentTypes = new HashMap<>();

    /**
     * 资源管理工具
     */
    private Resources resources = new Resources();

    /**
     * 静态资源根目录
     */
    private String root;

    /**
     * 添加数据适配器
     *
     * @param clazz   数据适配器类型
     * @param adapter 数据适配器
     */
    public void addAdapter(Class<?> clazz, Adapter adapter) {
        adapters.put(clazz, adapter);
    }

    /**
     * 添加数据适配器映射表
     *
     * @param adapters 数据适配器映射表
     */
    public void addAdapters(Map<Class<?>, Adapter<?>> adapters) {
        this.adapters.putAll(adapters);
    }

    /**
     * 添加文件类型-响应资源类型映射
     *
     * @param suffix 文件后缀
     * @param type   响应资源类型
     */
    public void addContentType(String suffix, String type) {
        contentTypes.put(suffix, type);
    }

    /**
     * 添加文件类型-响应资源类型映射表
     *
     * @param contentTypes 文件类型-响应资源类型映射表
     */
    public void addContentTypes(Map<String, String> contentTypes) {
        this.contentTypes.putAll(contentTypes);
    }

    /**
     * 绑定映射对象
     *
     * @param object 映射对象
     */
    public void bindObject(Object object) {
        this.object = object;
    }

    @Override
    void doProcess(HttpRequest request, HttpResponse response) {
        State state;
        switch (state = response.state()) {
            case READY:
                //从请求映射表中取出请求地址对应的映射对象
                //如果不存在映射关系则当作静态资源请求处理
                Mapper mapper = mappers.get(request.url());
                if (mapper == null) {
                    requestStaticResources(request, response);
                    break;
                }
                //检查请求方法是否被授权
                String method = mapper.action.toUpperCase();
                if (method.equals("ALL") || method.equals(request.method())) {
                    invoke(mapper, request, response, state);
                } else {
                    handleException(request, response, State.METHOD_NOT_ALLOWED);
                }
                break;
            case BAD_REQUEST:
            case NOT_FOUND:
            case METHOD_NOT_ALLOWED:
            case REQUEST_URI_TOO_LARGE:
            case INTERNAL_SERVER_ERROR:
            case NOT_IMPLEMENTED:
            case HTTP_VERSION_NOT_SUPPORTED:
                handleException(request, response, state);
                break;
        }
    }

    @Override
    public boolean start() {
        if (object == null) {
            logger.severe("Mapping object missing.");
            return false;
        }
        initMappers();
        if (mappers.isEmpty()) {
            logger.warning("There is no mapping in the current valve.");
            return false;
        }
        if (adapters.isEmpty()) {
            logger.warning("No adapter is added to the current valve.");
            return false;
        }
        if (contentTypes.isEmpty()) {
            logger.warning("No contentType " +
                    "mapping has been added to the current valve.");
        }
        logger.config("MapperValve startup now.");
        return true;
    }

    @Override
    public void stop() {
        logger.config("MapperValve stop now.");
    }

    @Override
    public void setLogStyle(LogStyle logStyle) {
        if (logSetup) {
            return;
        }
        LogUtils.initLogger(logger, logStyle);
        resources.setLogStyle(logStyle);
        logSetup = true;
    }

    /**
     * 通过反射初始化请求映射
     */
    private void initMappers() {
        //遍历对象方法
        for (Method method : object.getClass().getDeclaredMethods()) {
            //遍历方法注解
            for (Annotation annotation : method.getDeclaredAnnotations()) {
                if (annotation instanceof url) {
                    //建立请求映射
                    method.setAccessible(true);
                    url anUrl = (url) annotation;
                    mappers.put(anUrl.value(), new Mapper(method, anUrl.type(), anUrl.method()));
                    logger.config(String.format("Establish address mapping: %s", anUrl.value()));
                }
            }
        }
        //遍历对象字段
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            //遍历字段注解
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (annotation instanceof root) {
                    try {
                        field.setAccessible(true);
                        //反射获取字段值
                        Object value = field.get(object);
                        if (value instanceof String) {
                            root = (String) value;
                            break;
                        }
                    } catch (IllegalAccessException e) {
                        logger.warning(e.getMessage());
                    }
                }
            }
        }
        //如果未设置静态资源根目录
        //则将静态资源根目录初始化为当前工作目录
        if (root == null) {
            root = System.getProperty("user.dir");
        }
        logger.config(String.format("Set static resource root: %s", root));
    }

    /**
     * 处理静态资源请求
     */
    private void requestStaticResources(HttpRequest request, HttpResponse response) {
        //检查静态资源请求的合法性
        //直接使用静态资源URL请求静态资源视为不合法
        String referer = request.header("Referer");
        if (referer == null) {
            handleException(request, response, State.NOT_FOUND);
            return;
        }
        String path = root + request.url();
        File file = new File(path);
        if (!file.exists()) {
            handleException(request, response, State.NOT_FOUND);
            return;
        }
        byte[] fileBytes = resources.readFile(file);
        if (fileBytes == null) {
            logger.warning(String.format(request.addr() + " : Failed to read file %s.", path));
            handleException(request, response, State.INTERNAL_SERVER_ERROR);
            return;
        }
        //获取静态资源文件后缀
        //只截取最后一次出现的小数点后的字符
        String fileType = path.substring(path.lastIndexOf(".") + 1);
        fillResponse(request, response, fileBytes, State.READY,
                fileType.equals(path) ? "application/octet-stream" : contentTypes.get(fileType));
    }

    /**
     * 通过反射处理请求生成响应
     */
    @SuppressWarnings("unchecked")
    private void invoke(Mapper mapper, HttpRequest request, HttpResponse response, State state) {
        //获取映射方法的参数列表并检查参数个数
        Class[] classes = mapper.method.getParameterTypes();
        if (classes.length > 2) {
            logger.severe(String.format("Too many " +
                    "parameters of method: %s.", mapper.method.getName()));
            dispatchException(request, response, state);
            return;
        }
        //检查参数类型并构建实参数组
        Object[] args = new Object[classes.length];
        for (int i = 0; i < classes.length; i++) {
            if (classes[i] == HttpRequest.class) {
                args[i] = request;
            } else if (classes[i] == HttpResponse.class) {
                args[i] = response;
            } else {
                logger.severe(String.format("Unsupported " +
                        "parameter type: %s.", classes[i].getName()));
                dispatchException(request, response, state);
                return;
            }
        }
        try {
            //反射调用映射方法并获取返回值
            Object object = mapper.method.invoke(this.object, args);
            if (object == null) {
                logger.severe(String.format("Method %s " +
                        "return value is null.", mapper.method));
                dispatchException(request, response, state);
                return;
            }
            //返回值的类型为HttpResponse则表示在映射方法中手动设置响应信息
            //此时补充响应行状态码和状态码描述并直接返回
            if (object instanceof HttpResponse) {
                if (response.code() == null || response.reason() == null) {
                    response.status(state.code(), state.reason());
                }
                return;
            }
            //获取数据适配器
            Adapter adapter = adapters.get(object.getClass());
            if (adapter != null) {
                //通过适配器将返回值转换为字节数组
                byte[] bytes = adapter.toBytes(object);
                fillResponse(request, response, bytes, state, mapper.contentType);
                return;
            } else {
                logger.severe(String.format("Adapter of type %s " +
                        "is missing.", object.getClass().getName()));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.severe(e.getMessage());
        }
        dispatchException(request, response, state);
    }

    /**
     * 请求/响应异常分发
     */
    private void dispatchException(HttpRequest request, HttpResponse response, State state) {
        if (state.isThrow()) {
            response.state(state);
        } else {
            handleException(request, response, State.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理请求/响应异常
     */
    private void handleException(HttpRequest request, HttpResponse response, State state) {
        Mapper mapper = mappers.get(state.code());
        if (mapper == null) {
            response.state(state);
            return;
        }
        invoke(mapper, request, response, state);
    }

    /**
     * 填充响应对象
     */
    private void fillResponse(HttpRequest request, HttpResponse response,
                              byte[] bytes, State state, String contentType) {
        //对客户端已缓存的资源直接使用本地缓存
        //仅提供ETag实现
        String requestMD5 = request.eTag();
        String responseMD5 = resources.md5(bytes);
        if (responseMD5 != null) {
            if (requestMD5 != null && requestMD5.equals(responseMD5)) {
                response.useCache(responseMD5)
                        .state(State.COMPLETE);
                return;
            }
        } else {
            logger.warning(String.format("%s : Failed to " +
                    "calculate MD5 value of data.", request.addr()));
        }
        //对支持gzip编码的客户端的响应数据使用gzip编码
        boolean isGzip = false;
        String acceptEncoding = request.header("Accept-Encoding");
        if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
            byte[] gzipBytes = resources.gzip(bytes);
            if (gzipBytes != null) {
                response.body(gzipBytes)
                        .contentEncoding("gzip")
                        .contentLength(String.valueOf(gzipBytes.length));
                isGzip = true;
            } else {
                logger.warning(String.format("%s : Gzip " +
                        "compression failed.", request.addr()));
            }
        }
        if (!isGzip) {
            response.body(bytes).contentLength(String.valueOf(bytes.length));
        }
        //设置响应资源标识用于支持缓存功能
        if (responseMD5 != null) {
            response.eTag(responseMD5);
        }
        response.status(state.code(), state.reason())
                .contentType(contentType)
                .state(State.COMPLETE);
    }

    /**
     * 请求映射对象
     */
    private static final class Mapper {

        /**
         * 映射方法
         */
        Method method;

        /**
         * 响应资源类型
         */
        String contentType;

        /**
         * 授权的请求方法类型
         */
        String action;

        public Mapper(Method method, String contentType, String action) {
            this.method = method;
            this.contentType = contentType;
            this.action = action;
        }
    }
}
