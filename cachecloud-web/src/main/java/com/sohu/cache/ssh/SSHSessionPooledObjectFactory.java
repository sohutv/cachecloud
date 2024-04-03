package com.sohu.cache.ssh;

import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.SshAuthTypeEnum;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * ssh session链接池工厂
 *
 * @Auther: yongfeigao
 * @Date: 2023/10/20
 */
public class SSHSessionPooledObjectFactory implements KeyedPooledObjectFactory<SSHMachineInfo, ClientSession> {

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
    public PooledObject<ClientSession> makeObject(SSHMachineInfo sshMachineInfo) throws Exception {
        int port = ConstUtils.SSH_PORT_DEFAULT;
        ClientSession session = sshClient.getClient().connect(ConstUtils.USERNAME, sshMachineInfo.getIp(),
                port).verify(ConstUtils.SSH_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS).getSession();
        session.setUsername(sshMachineInfo.getUsername());
        if(sshMachineInfo.getAuthType() == SshAuthTypeEnum.PASSWORD.getValue()){
            session.addPasswordIdentity(sshMachineInfo.getPassword());
        }else if(sshMachineInfo.getAuthType() == SshAuthTypeEnum.PUBLIC_KEY.getValue()){
            KeyPairResourceLoader loader = SecurityUtils.getKeyPairResourceParser();
            Collection<KeyPair> keys = loader.loadKeyPairs(null, Paths.get(ConstUtils.PUBLIC_KEY_PEM), null);
            session.addPublicKeyIdentity(keys.iterator().next());
        }
        session.auth().verify(ConstUtils.SSH_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        logger.info("create object, key:{}", sshMachineInfo);
        return new DefaultPooledObject<>(session);
    }

    @Override
    public void destroyObject(SSHMachineInfo sshMachineInfo, PooledObject<ClientSession> pooledObject) throws Exception {
        ClientSession clientSession = pooledObject.getObject();
        if (clientSession != null) {
            try {
                clientSession.close();
            } catch (Exception e) {
                logger.warn("close err, key:{}", sshMachineInfo, e);
            }
        }
        logger.info("destroy object {}", sshMachineInfo);
    }

    @Override
    public boolean validateObject(SSHMachineInfo sshMachineInfo, PooledObject<ClientSession> pooledObject) {
        boolean closed = pooledObject.getObject().isClosed();
        if (closed) {
            logger.warn("{} session closed", sshMachineInfo);
            return false;
        }
        return true;
    }

    @Override
    public void activateObject(SSHMachineInfo sshMachineInfo, PooledObject<ClientSession> pooledObject) throws Exception {

    }

    @Override
    public void passivateObject(SSHMachineInfo sshMachineInfo, PooledObject<ClientSession> pooledObject) throws Exception {

    }
}
