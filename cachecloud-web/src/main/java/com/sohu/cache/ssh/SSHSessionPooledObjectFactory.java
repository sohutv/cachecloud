package com.sohu.cache.ssh;

import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.SshAuthTypeEnum;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.sshd.client.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

/**
 * ssh session链接池工厂
 *
 * @Auther: yongfeigao
 * @Date: 2023/10/20
 */
public class SSHSessionPooledObjectFactory implements KeyedPooledObjectFactory<String, ClientSession> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SSHClient sshClient;

    public SSHSessionPooledObjectFactory() throws GeneralSecurityException, IOException {
        sshClient = new SSHClient();
        sshClient.setServerUser(ConstUtils.USERNAME);
        sshClient.setServerPassword(ConstUtils.PASSWORD);
        sshClient.setServerPort(ConstUtils.SSH_PORT_DEFAULT);
        sshClient.setServerConnectTimeout(ConstUtils.SSH_CONNECTION_TIMEOUT);
        if (ConstUtils.SSH_AUTH_TYPE == SshAuthTypeEnum.PUBLIC_KEY.getValue()) {
            sshClient.setPrivateKeyPath(ConstUtils.PUBLIC_KEY_PEM);
        }
        sshClient.init();
    }

    @Override
    public PooledObject<ClientSession> makeObject(String ip) throws Exception {
        int port = ConstUtils.SSH_PORT_DEFAULT;
        ClientSession session = sshClient.getClient().connect(ConstUtils.USERNAME, ip,
                port).verify(ConstUtils.SSH_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS).getSession();
        session.auth().verify(ConstUtils.SSH_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        logger.info("create object, key:{}", ip);
        return new DefaultPooledObject<>(session);
    }

    @Override
    public void destroyObject(String ip, PooledObject<ClientSession> pooledObject) throws Exception {
        ClientSession clientSession = pooledObject.getObject();
        if (clientSession != null) {
            try {
                clientSession.close();
            } catch (Exception e) {
                logger.warn("close err, key:{}", ip, e);
            }
        }
        logger.info("destroy object {}", ip);
    }

    @Override
    public boolean validateObject(String ip, PooledObject<ClientSession> pooledObject) {
        boolean closed = pooledObject.getObject().isClosed();
        if (closed) {
            logger.warn("{} session closed", ip);
            return false;
        }
        return true;
    }

    @Override
    public void activateObject(String ip, PooledObject<ClientSession> pooledObject) throws Exception {

    }

    @Override
    public void passivateObject(String ip, PooledObject<ClientSession> pooledObject) throws Exception {

    }
}
