package com.jerry.net.connector;

import com.jerry.extend.Lifecycle;
import com.jerry.agent.response.Response;
import com.jerry.net.request.Request;
import com.jerry.extend.CodeAble;
import com.jerry.logger.LogAble;
import com.jerry.net.producer.Producer;
import com.jerry.utils.CacheQueue;
import com.jerry.logger.LogStyle;
import com.jerry.logger.LogUtils;
import com.jerry.utils.ChannelUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;

/**
 * 连接器NIO实现
 */
public final class NIOConnector extends Connector implements LogAble, CodeAble {

    private final static Logger logger
            = Logger.getLogger(NIOConnector.class.getSimpleName());

    private static boolean logSetup = false;

    /**
     * 线程控制变量
     * 将该变量设为{@code false}可以停止所有的子线程
     */
    private volatile boolean running = true;

    /**
     * 请求生成器缓存队列
     */
    private CacheQueue<Producer> cacheQueue = new CacheQueue<>();

    /**
     * 同步事务队列
     */
    private BlockingQueue<Work> workQueue = new LinkedBlockingQueue<>();

    /**
     * 异步事务队列
     */
    private Queue<Work> asyncWorkQueue = new ConcurrentLinkedQueue<>();

    /**
     * 事务对象缓存队列
     */
    private CacheQueue<Work> workCache = new CacheQueue<>();

    /**
     * Reader线程组
     */
    private List<Reader> readers = new ArrayList<>();

    /**
     * Reader线程组轮询索引
     */
    private int rotation = 0;

    /**
     * 异步Writer线程
     */
    private Writer writer;

    @Override
    public boolean start() {
        if (processor == null || factory == null) {
            logger.severe("Connector missing required parameters.");
            return false;
        }
        if (factory instanceof Lifecycle && !((Lifecycle) factory).start()) {
            return false;
        }
        if (processor instanceof Lifecycle && !((Lifecycle) processor).start()) {
            return false;
        }
        try {
            int maxThreads = maxThreads();
            //启动Reader线程组
            Reader reader;
            for (int i = 0; i < maxThreads; i++) {
                reader = new Reader(i);
                readers.add(reader);
                reader.start();
            }
            //启动Worker线程组
            for (int i = 0; i < maxThreads; i++) {
                new Worker(i).start();
            }
            //启动异步Writer线程
            writer = new Writer();
            writer.start();
            //启动Acceptor线程
            Acceptor acceptor = new Acceptor();
            acceptor.register(port());
            acceptor.start();
            logger.config("Connector startup now.");
            return true;
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        stop();
        return false;
    }

    @Override
    public void stop() {
        running = false;
        if (factory instanceof Lifecycle) {
            ((Lifecycle) factory).stop();
        }
        if (processor instanceof Lifecycle) {
            ((Lifecycle) processor).stop();
        }
        logger.config("Connector stop now.");
    }

    @Override
    public void setLogStyle(LogStyle logStyle) {
        if (logStyle.disabled() || logSetup) {
            return;
        }
        if (factory != null && factory instanceof LogAble) {
            ((LogAble) factory).setLogStyle(logStyle);
        }
        LogUtils.initLogger(logger, logStyle);
        if (processor != null && processor instanceof LogAble) {
            ((LogAble) processor).setLogStyle(logStyle);
        }
        logSetup = true;
    }

    @Override
    public void setCharset(Charset charset) {
        if (factory != null && factory instanceof CodeAble) {
            ((CodeAble) factory).setCharset(charset);
        }
        if (processor != null && processor instanceof CodeAble) {
            ((CodeAble) processor).setCharset(charset);
        }
    }

    /**
     * 以轮询方式从Reader线程组获取一个Reader线程对象
     */
    private Reader getReader() {
        if (rotation == Integer.MAX_VALUE) {
            rotation = 0;
        }
        return readers.get((rotation++) % maxThreads());
    }

    /**
     * Acceptor线程
     * 监听指定端口的连接请求
     */
    private class Acceptor extends Thread {

        /**
         * 通道选择器
         */
        private Selector selector;

        /**
         * 连接监听通道
         */
        private ServerSocketChannel channel;

        Acceptor() throws IOException {
            super("Acceptor");
            selector = Selector.open();
            channel = ServerSocketChannel.open();
        }

        @Override
        public void run() {
            while (running) {
                try {
                    int state = selector.select();
                    if (state == 0) {
                        continue;
                    }
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        if (key.isValid() && key.isAcceptable()) {
                            doAccept(key);
                        }
                    }
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }
            }
        }

        /**
         * 处理到达的连接请求
         */
        private void doAccept(SelectionKey selectionKey) throws IOException {
            ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = channel.accept();
            if (socketChannel == null) {
                logger.warning("Failed to get socket channel.");
                return;
            }
            socketChannel.configureBlocking(false);
            Socket socket = socketChannel.socket();
            socket.setTcpNoDelay(!useNagle);
            socket.setKeepAlive(keepAlive);
            getReader().register(socketChannel);
        }

        /**
         * 为指定端口注册连接请求监听
         */
        private void register(int port) throws IOException {
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(port), backlog());
            channel.register(selector, SelectionKey.OP_ACCEPT);
        }
    }

    /**
     * Reader线程
     * 监听通道可读事件
     */
    private class Reader extends Thread {

        /**
         * 通道选择器
         */
        private Selector readSelector;

        /**
         * 线程同步控制变量
         */
        private boolean locked = false;

        Reader(int i) throws IOException {
            super("Reader-" + i);
            readSelector = Selector.open();
        }

        @Override
        public void run() {
            while (running) {
                try {
                    readSelector.select();
                    while (locked) {
                        synchronized (this) {
                            this.wait(1000);
                        }
                    }
                    Iterator<SelectionKey> it = readSelector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        if (key.isValid() && key.isReadable()) {
                            doRead(key);
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    logger.severe(e.getMessage());
                }
            }
        }

        /**
         * 处理可读通道
         */
        private void doRead(SelectionKey selectionKey) throws IOException {
            Producer producer = (Producer) selectionKey.attachment();
            if (producer == null) {
                logger.warning("Failed to get request producer.");
                return;
            }
            int readBytes = producer.readOnChannel();
            if (readBytes != -1) {
                //生成请求对象
                Request request = producer.makeRequest();
                if (request == null) {
                    logger.warning("Failed to get request object.");
                    return;
                }
                //生成事务对象
                Work work = workCache.get();
                if (work == null) {
                    work = new Work();
                }
                work.reset(producer.channel(), request);
                try {
                    //递交事务
                    workQueue.put(work);
                } catch (InterruptedException e) {
                    logger.warning(e.getMessage());
                    work.clear();
                    workCache.cache(work);
                }
            } else {
                //客户端关闭Socket会话
                producer.channel().close();
                producer.clear();
                cacheQueue.cache(producer);
            }
        }

        /**
         * 注册通道可读事件监听
         */
        void register(SocketChannel channel) throws IOException {
            this.begin();
            Producer producer = cacheQueue.get();
            if (producer == null) {
                producer = factory.create();
            }
            producer.reset(channel);
            channel.register(readSelector, SelectionKey.OP_READ, producer);
            this.end();
        }

        /**
         * 开始同步注册
         */
        private void begin() {
            locked = true;
            readSelector.wakeup();
        }

        /**
         * 结束同步注册
         */
        private synchronized void end() {
            locked = false;
            this.notify();
        }
    }

    /**
     * 事务对象
     */
    private static class Work {

        /**
         * Socket会话通道
         */
        SocketChannel channel;

        /**
         * 事务附件
         */
        Object object;

        void reset(SocketChannel channel, Object object) {
            this.channel = channel;
            this.object = object;
        }

        void clear() {
            channel = null;
            object = null;
        }
    }

    /**
     * Worker线程
     * 响应客户端请求
     */
    private class Worker extends Thread {

        public Worker(int i) {
            super("Worker-" + i);
        }

        public void run() {
            while (running) {
                try {
                    Work work = workQueue.take();
                    doWrite(work);
                } catch (InterruptedException e) {
                    logger.warning(e.getMessage());
                }
            }
        }

        /**
         * 响应客户端请求
         */
        private void doWrite(Work work) {
            if (!(work.object instanceof Request)) {
                logger.warning("Failed to convert request class.");
                return;
            }
            Request request = (Request) work.object;
            Response response = processor.process(request);
            if (response == null) {
                logger.warning("Failed to get response object.");
                return;
            }
            try {
                ByteBuffer buffer = ByteBuffer.wrap(response.toBytes());
                int writeBytes = ChannelUtils.write(work.channel, buffer);
                if (writeBytes < 0) {
                    throw new IOException("Socket write failed.");
                }
                if (!buffer.hasRemaining()) {
                    work.clear();
                    workCache.cache(work);
                } else {
                    //注册异步写事件
                    work.object = buffer;
                    writer.submit(work);
                }
            } catch (IOException e) {
                logger.severe(e.getMessage());
                try {
                    work.channel.close();
                    work.clear();
                    workCache.cache(work);
                } catch (IOException ex) {
                    logger.severe(ex.getMessage());
                }
            }
        }
    }

    /**
     * 异步Writer线程
     * 处理客户端异步通信
     */
    private class Writer extends Thread {

        /**
         * 通道选择器
         */
        Selector writeSelector;

        public Writer() throws IOException {
            writeSelector = Selector.open();
        }

        @Override
        public void run() {
            while (running) {
                try {
                    registerWriters();
                    int state = writeSelector.select(1000);
                    if (state == 0) {
                        continue;
                    }
                    Iterator<SelectionKey> it = writeSelector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        if (key.isValid() && key.isWritable()) {
                            doAsyncWrite(key);
                        }
                    }
                } catch (IOException e) {
                    logger.warning(e.getMessage());
                }
            }
        }

        /**
         * 遍历异步事务队列并注册可写事件监听
         */
        private void registerWriters() {
            Iterator<Work> it = asyncWorkQueue.iterator();
            while (it.hasNext()) {
                Work work = it.next();
                it.remove();
                SelectionKey key = work.channel.keyFor(writeSelector);
                try {
                    //如果通道尚未注册通道选择器
                    if (key == null) {
                        try {
                            //为通道注册可写事件监听
                            work.channel.register(writeSelector, SelectionKey.OP_WRITE, work);
                        } catch (ClosedChannelException e) {
                            logger.warning(e.getMessage());
                        }
                    } else {
                        //设置通道选择器只对通道可写事件感兴趣
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                } catch (CancelledKeyException e) {
                    logger.warning(e.getMessage());
                }
            }
        }

        /**
         * 向异步事务队列递交一个事务
         */
        private void submit(Work work) {
            asyncWorkQueue.add(work);
            writeSelector.wakeup();
        }

        /**
         * 异步处理可写通道
         */
        private void doAsyncWrite(SelectionKey key) throws IOException {
            Work work = (Work) key.attachment();
            if (work == null) {
                logger.warning("Failed to get Work object.");
                return;
            }
            if (!(work.object instanceof ByteBuffer)) {
                logger.warning(String.format("The attachment type is wrong." +
                        " The current attachment type is: %s", work.object.getClass()));
                return;
            }
            ByteBuffer buffer = (ByteBuffer) work.object;
            int writeBytes = ChannelUtils.write(work.channel, buffer);
            if (writeBytes < 0 || buffer.remaining() == 0) {
                try {
                    key.interestOps(0);
                } catch (CancelledKeyException e) {
                    logger.warning(e.getMessage());
                }
                work.clear();
                workCache.cache(work);
            } else {
                logger.warning("Asynchronous " +
                        "write response information failed.");
            }
        }
    }
}
