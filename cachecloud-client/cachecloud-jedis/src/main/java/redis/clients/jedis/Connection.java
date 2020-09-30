package redis.clients.jedis;

import com.sohu.tv.cc.client.spectator.CommandTracker;
import com.sohu.tv.cc.client.spectator.StatsCollector;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.util.IOUtils;
import redis.clients.jedis.util.RedisInputStream;
import redis.clients.jedis.util.RedisOutputStream;
import redis.clients.jedis.util.SafeEncoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Connection implements Closeable {

  private static final byte[][] EMPTY_ARGS = new byte[0][];

  private JedisSocketFactory jedisSocketFactory;
  private Socket socket;
  private RedisOutputStream outputStream;
  private RedisInputStream inputStream;
  private boolean broken = false;
  private StatsCollector statsCollector;


  public Connection() {
    this(Protocol.DEFAULT_HOST);
  }

  public Connection(final String host) {
    this(host, Protocol.DEFAULT_PORT);
  }

  public Connection(final String host, final int port) {
    this(host, port, false);
  }

  public Connection(final String host, final int port, final StatsCollector statsCollector) {
    this(host, port, false, statsCollector);
  }

  public Connection(final String host, final int port, final boolean ssl) {
    this(host, port, ssl, null, null, null);
  }

  public Connection(final String host, final int port, final boolean ssl, final StatsCollector statsCollector) {
    this(host, port, ssl, null, null, null, statsCollector);
  }

  public Connection(final String host, final int port, final boolean ssl,
                     SSLSocketFactory sslSocketFactory, SSLParameters sslParameters,
                     HostnameVerifier hostnameVerifier) {
    this(new DefaultJedisSocketFactory(host, port, Protocol.DEFAULT_TIMEOUT,
            Protocol.DEFAULT_TIMEOUT, ssl, sslSocketFactory, sslParameters, hostnameVerifier));
  }

  public Connection(final String host, final int port, final boolean ssl,
                    SSLSocketFactory sslSocketFactory, SSLParameters sslParameters,
                    HostnameVerifier hostnameVerifier, StatsCollector statsCollector) {
    this(new DefaultJedisSocketFactory(host, port, Protocol.DEFAULT_TIMEOUT,
            Protocol.DEFAULT_TIMEOUT, ssl, sslSocketFactory, sslParameters, hostnameVerifier));
    this.statsCollector = statsCollector;
  }

  public Connection(final JedisSocketFactory jedisSocketFactory) {
    this.jedisSocketFactory = jedisSocketFactory;
  }

  public Socket getSocket() {
    return socket;
  }

  public int getConnectionTimeout() {
    return jedisSocketFactory.getConnectionTimeout();
  }

  public int getSoTimeout() {
    return jedisSocketFactory.getSoTimeout();
  }

  public void setConnectionTimeout(int connectionTimeout) {
    jedisSocketFactory.setConnectionTimeout(connectionTimeout);
  }

  public void setSoTimeout(int soTimeout) {
    jedisSocketFactory.setSoTimeout(soTimeout);
  }

  public void setTimeoutInfinite() {
    try {
      if (!isConnected()) {
        connect();
      }
      socket.setSoTimeout(0);
    } catch (SocketException ex) {
      broken = true;
      throw new JedisConnectionException(ex);
    }
  }

  public void rollbackTimeout() {
    try {
      socket.setSoTimeout(jedisSocketFactory.getSoTimeout());
    } catch (SocketException ex) {
      broken = true;
      throw new JedisConnectionException(ex);
    }
  }

  public void sendCommand(final ProtocolCommand cmd, final String... args) {
    final byte[][] bargs = new byte[args.length][];
    for (int i = 0; i < args.length; i++) {
      bargs[i] = SafeEncoder.encode(args[i]);
    }
    sendCommand(cmd, bargs);
  }

  public void sendCommand(final ProtocolCommand cmd) {
    sendCommand(cmd, EMPTY_ARGS);
  }

  public void sendCommand(final ProtocolCommand cmd, final byte[]... args) {
    long invokeTime = System.currentTimeMillis();
    long startTime = System.nanoTime();
    boolean isFailed = false;
    try {
      connect();
      Protocol.sendCommand(outputStream, cmd, args);
    } catch (JedisConnectionException ex) {
      /*
       * When client send request which formed by invalid protocol, Redis send back error message
       * before close connection. We try to read it to provide reason of failure.
       */
      try {
        String errorMessage = Protocol.readErrorLineIfPossible(inputStream);
        if (errorMessage != null && errorMessage.length() > 0) {
          ex = new JedisConnectionException(errorMessage, ex.getCause());
        }
      } catch (Exception e) {
        /*
         * Catch any IOException or JedisConnectionException occurred from InputStream#read and just
         * ignore. This approach is safe because reading error message is optional and connection
         * will eventually be closed.
         */
      }
      //tracker.commandFailed(statsCollector);
      isFailed = true;
      // Any other exceptions related to connection?
      broken = true;
      throw ex;
    } finally {
      if(statsCollector != null){
        CommandTracker tracker = CommandTracker.getCommandTracker();
        tracker.commandStart(statsCollector, cmd.toString().toLowerCase(), getHostPort(), invokeTime, startTime, isFailed, args);
      }
    }
  }

  public String getHost() {
    return jedisSocketFactory.getHost();
  }

  public void setHost(final String host) {
    jedisSocketFactory.setHost(host);
  }

  public int getPort() {
    return jedisSocketFactory.getPort();
  }

  public void setPort(final int port) {
    jedisSocketFactory.setPort(port);
  }

  public void connect() {
    if (!isConnected()) {
      long start = System.nanoTime();
      try {
        socket = jedisSocketFactory.createSocket();

        outputStream = new RedisOutputStream(socket.getOutputStream());
        inputStream = new RedisInputStream(socket.getInputStream());
      } catch (IOException ex) {
        if(statsCollector != null){
          statsCollector.appendConnectExpStat(getHostPort(), (System.nanoTime() - start));
        }
        broken = true;
        throw new JedisConnectionException("Failed connecting to "
            + jedisSocketFactory.getDescription(), ex);
      }
    }
  }

  @Override
  public void close() {
    disconnect();
  }

  public void disconnect() {
    if (isConnected()) {
      try {
        outputStream.flush();
        socket.close();
      } catch (IOException ex) {
        broken = true;
        throw new JedisConnectionException(ex);
      } finally {
        IOUtils.closeQuietly(socket);
      }
    }
  }

  public boolean isConnected() {
    return socket != null && socket.isBound() && !socket.isClosed() && socket.isConnected()
        && !socket.isInputShutdown() && !socket.isOutputShutdown();
  }

  public String getStatusCodeReply() {
    flush();
    final byte[] resp = (byte[]) readProtocolWithCheckingBroken();
    if (null == resp) {
      return null;
    } else {
      return SafeEncoder.encode(resp);
    }
  }

  public String getBulkReply() {
    final byte[] result = getBinaryBulkReply();
    if (null != result) {
      return SafeEncoder.encode(result);
    } else {
      return null;
    }
  }

  public byte[] getBinaryBulkReply() {
    flush();
    return (byte[]) readProtocolWithCheckingBroken();
  }

  public Long getIntegerReply() {
    flush();
    return (Long) readProtocolWithCheckingBroken();
  }

  public List<String> getMultiBulkReply() {
    return BuilderFactory.STRING_LIST.build(getBinaryMultiBulkReply());
  }

  @SuppressWarnings("unchecked")
  public List<byte[]> getBinaryMultiBulkReply() {
    flush();
    return (List<byte[]>) readProtocolWithCheckingBroken();
  }

  @Deprecated
  public List<Object> getRawObjectMultiBulkReply() {
    return getUnflushedObjectMultiBulkReply();
  }

  @SuppressWarnings("unchecked")
  public List<Object> getUnflushedObjectMultiBulkReply() {
    return (List<Object>) readProtocolWithCheckingBroken();
  }

  public List<Object> getObjectMultiBulkReply() {
    flush();
    return getUnflushedObjectMultiBulkReply();
  }

  @SuppressWarnings("unchecked")
  public List<Long> getIntegerMultiBulkReply() {
    flush();
    return (List<Long>) readProtocolWithCheckingBroken();
  }

  public Object getOne() {
    flush();
    return readProtocolWithCheckingBroken();
  }

  public boolean isBroken() {
    return broken;
  }

  public void setBroken(boolean broken) {
    this.broken = broken;
  }

  protected void flush() {
    try {
      outputStream.flush();
    } catch (IOException ex) {
      if(statsCollector != null){
        CommandTracker tracker = CommandTracker.getCommandTracker();
        tracker.flushFailed(statsCollector);
      }
      broken = true;
      throw new JedisConnectionException(ex);
    }
  }

  protected Object readProtocolWithCheckingBroken() {
//    if (broken) {
//      throw new JedisConnectionException("Attempting to read from a broken connection");
//    }
    boolean isFailed = false;
    Object o = null;
    try {
      o = Protocol.read(inputStream);
      return o;
    } catch (JedisConnectionException exc) {
      isFailed = true;
      broken = true;
      throw exc;
    } finally {
      if(statsCollector != null){
        //tracker
        CommandTracker tracker = CommandTracker.getCommandTracker();
        tracker.commandCompleted(o, isFailed, statsCollector);
      }
    }
  }

  public List<Object> getMany(final int count) {
    flush();
    final List<Object> responses = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      try {
        responses.add(readProtocolWithCheckingBroken());
      } catch (JedisDataException e) {
        responses.add(e);
      }
    }
    return responses;
  }

  public String getHostPort(){
      return getHost() + ":" + getPort();
  }
}
