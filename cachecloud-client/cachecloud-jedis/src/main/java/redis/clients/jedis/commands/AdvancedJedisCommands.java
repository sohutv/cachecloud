package redis.clients.jedis.commands;

import java.util.List;

import redis.clients.jedis.AccessControlUser;
import redis.clients.jedis.params.MigrateParams;
import redis.clients.jedis.params.ClientKillParams;
import redis.clients.jedis.params.ClientTrackingParams;
import redis.clients.jedis.params.MigrateParams;
import redis.clients.jedis.util.LatencyHistoryItem;
import redis.clients.jedis.util.LatencyItem;
import redis.clients.jedis.util.Slowlog;

import java.util.List;

public interface AdvancedJedisCommands {
  List<String> configGet(String pattern);

  String configSet(String parameter, String value);

  String slowlogReset();

  Long slowlogLen();

  List<Slowlog> slowlogGet();

  List<Slowlog> slowlogGet(long entries);

  Long objectRefcount(String key);

  String objectEncoding(String key);

  Long objectIdletime(String key);

  List<String> objectHelp();

  Long objectFreq(String key);

  String migrate(String host, int port, String key, int destinationDB, int timeout);

  String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, String... keys);

  String clientKill(String ipPort);

  String clientKill(String ip, int port);

  Long clientKill(ClientKillParams params);

  String clientGetname();

  String clientList();

  String clientSetname(String name);

  String memoryDoctor();

  List<LatencyItem> latencyLatest();

  List<LatencyHistoryItem> latencyHistory(String event);

  Long latencyReset(String... events);

  String latencyDoctor();

  Long memoryUsage(String key);

  List<Object> memoryStats();

  String memoryPurge();

  Long clientId();

  Long clientGetRedir();

  String clientTracking(ClientTrackingParams.Latch latch);

  String clientTracking(ClientTrackingParams.Latch latch, ClientTrackingParams params);

  String aclWhoAmI();

  String aclGenPass();

  List<String> aclList();

  List<String> aclUsers();

  AccessControlUser aclGetUser(String name);

  String aclSetUser(String name);

  String aclSetUser(String name, String... keys);

  Long aclDelUser(String name);

  List<String> aclCat();

  List<String> aclCat(String category);

  // TODO: Implements ACL LOAD/SAVE commands
}
