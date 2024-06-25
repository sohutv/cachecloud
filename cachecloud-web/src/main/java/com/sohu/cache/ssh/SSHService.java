package com.sohu.cache.ssh;

import com.sohu.cache.exception.SSHException;


/**
 * @author leifu
 */
public interface SSHService {

    /**
     * 执行ssh命令
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param command
     * @return
     * @throws SSHException
     */
    String execute(String ip, int port, String username, String password, final String command) throws SSHException;

    /**
     * 执行ssh命令
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param command
     * @return
     * @throws SSHException
     */
    SSHTemplate.Result executeWithResult(String ip, int port, String username, String password, final String command) throws SSHException;

    /**
     * 执行ssh命令,可设置超时时间
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param command
     * @param timeoutMills
     * @return
     * @throws SSHException
     */
    SSHTemplate.Result executeWithResult(String ip, int port, String username, String password, final String command, int timeoutMills) throws SSHException;

    /**
     * 拷贝文件到远程目录
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param localPath
     * @param remoteDir
     * @return
     * @throws SSHException
     */
    SSHTemplate.Result scpFileToRemote(String ip, int port, String username,
            String password, final String localPath, final String remoteDir) throws SSHException;

    /**
     * 拷贝文件到远程目录
     * @param ip
     * @param localPath
     * @param remoteDir
     * @return
     * @throws SSHException
     */
    SSHTemplate.Result scpFileToRemote(String ip, String localPath, String remoteDir) throws SSHException;

    /**
     * 执行命令
     * @param ip
     * @param cmd
     * @return
     * @throws SSHException
     */
    String execute(String ip, String cmd) throws SSHException;

    /**
     * 执行命令
     * @param ip
     * @param cmd
     * @return
     * @throws SSHException
     */
    SSHTemplate.Result executeWithResult(String ip, String cmd) throws SSHException;

    /**
     * 执行命令,可设置超时时间
     * @param ip
     * @param cmd
     * @return
     * @throws SSHException
     */
    SSHTemplate.Result executeWithResult(String ip, String cmd, int millsSecond) throws SSHException;

    /**
     * 查看机器ip上的端口port是否已被占用；
     * @param ip
     * @param port
     * @return
     * @throws SSHException
     */
    boolean isPortUsed(String ip, int port) throws SSHException;

    /**
     * 通过ip来判断ssh端口
     * @param ip
     * @return
     */
    int getSshPort(String ip);

}
