package com.sohu.cache.ssh;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.password.PasswordIdentityProvider;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.security.SecurityUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * ssh client
 *
 * @Auther: yongfeigao
 * @Date: 2023/10/23
 */
@Data
public class SSHClient {

    // 服务器 ssh 用户
    private String serverUser;

    // 服务器 ssh 密码
    private String serverPassword;

    // 服务器 ssh 端口
    private Integer serverPort;

    // 服务器 ssh 链接建立超时时间
    private Integer serverConnectTimeout;

    // 服务器 ssh 操作超时时间
    private Integer serverOPTimeout;

    // 服务器 ssh 私钥
    private String privateKeyPath;

    private SshClient client;

    public void init() throws GeneralSecurityException, IOException {
        client = buildSshClient();
        if (StringUtils.isNotEmpty(privateKeyPath)) {
            setAuthByKey(client);
        }
        client.setPasswordIdentityProvider(PasswordIdentityProvider.wrapPasswords(getServerPassword()));
        client.start();
    }

    private SshClient buildSshClient() {
        SshClient client = SshClient.setUpDefaultClient();
        client.setSessionHeartbeat(Session.HeartbeatType.IGNORE, TimeUnit.SECONDS, 10);
        return client;
    }

    private void setAuthByKey(SshClient client) throws GeneralSecurityException, IOException {
        KeyPairResourceLoader loader = SecurityUtils.getKeyPairResourceParser();

        Collection<KeyPair> keys = loader.loadKeyPairs(null, Paths.get(privateKeyPath), null);
        client.setKeyIdentityProvider(KeyIdentityProvider.wrapKeyPairs(keys));
    }

    /**
     * 连接服务器
     *
     * @param ip
     * @return
     * @throws IOException
     */
    public ClientSession connect(String ip) throws IOException {
        ClientSession session = getClient().connect(getServerUser(), ip, getServerPort()).verify(getServerConnectTimeout(), TimeUnit.MILLISECONDS).getSession();
        session.auth().verify(getServerConnectTimeout(), TimeUnit.MILLISECONDS);
        return session;
    }

}
