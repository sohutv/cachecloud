package com.sohu.cache.ssh;

import com.sohu.cache.exception.SSHException;
import com.sohu.cache.util.ConstUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.scp.client.ScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SSH操作模板类
 */
@Component
public class SSHTemplate {
	private static final Logger logger = LoggerFactory.getLogger(SSHTemplate.class);

    public static final List<PosixFilePermission> PERMS = Arrays.asList(PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ);

    @Autowired
    private GenericKeyedObjectPool<SSHMachineInfo, ClientSession> clientSessionPool;

    private static final int CONNCET_TIMEOUT = 5000;

	private static final int OP_TIMEOUT = 10000;

	public Result execute(String ip, SSHCallback callback) throws SSHException{
		return execute(ip,ConstUtils.SSH_PORT_DEFAULT, ConstUtils.USERNAME,
				ConstUtils.PASSWORD, callback);
	}

	/**
	 * 通过回调执行命令
	 * @param ip
	 * @param port
	 * @param username
	 * @param password
	 * @param callback 可以使用Session执行多个命令
	 * @throws SSHException
	 */
    public Result execute(String ip, int port, String username, String password,
    		SSHCallback callback) throws SSHException{
        ClientSession session = null;
        SSHMachineInfo sshMachineInfo = SSHMachineInfo.builder().ip(ip).username(username)
                .authType(ConstUtils.SSH_AUTH_TYPE).password(password).build();
        try {
            session = clientSessionPool.borrowObject(sshMachineInfo);
            return callback.call(new SSHSession(session, ip));
        } catch (Exception e) {
            throw new SSHException("SSH exception: " + e.getMessage(), e);
        } finally {
            close(sshMachineInfo, session);
        }
    }

    private DefaultLineProcessor generateDefaultLineProcessor(StringBuilder buffer) {
        return new DefaultLineProcessor() {
            public void process(String line, int lineNum) throws Exception {
                if (lineNum > 1) {
                    buffer.append(System.lineSeparator());
                }
                buffer.append(line);
            }
        };
    }

    /**
     * 获取调用命令后的返回结果
     * @param is 输入流
     * @return 如果获取结果有异常或者无结果，那么返回null
     */
    private String getResult(InputStream is) {
    	final StringBuilder buffer = new StringBuilder();
    	LineProcessor lp = new DefaultLineProcessor() {
			public void process(String line, int lineNum) throws Exception {
				if(lineNum > 1) {
					buffer.append(System.lineSeparator());
				}
				buffer.append(line);
			}
    	};
    	processStream(is, lp);
    	return buffer.length() > 0 ? buffer.toString() : null;
    }

    /**
     * 从流中获取内容
     * @param is
     */
    private void processStream(InputStream is, LineProcessor lineProcessor) {
    	BufferedReader reader = null;
        try {
        	reader = new BufferedReader(new InputStreamReader(is));
	    	String line = null;
	    	int lineNum = 1;
	        while ((line = reader.readLine()) != null) {
	        	try {
					lineProcessor.process(line, lineNum);
				} catch (Exception e) {
					logger.error("err line:" + line, e);
				}
                if (lineProcessor instanceof DefaultLineProcessor) {
                    ((DefaultLineProcessor) lineProcessor).setLineNum(lineNum);
                }
				lineNum++;
	        }
            lineProcessor.finish();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
        	close(reader);
        }
    }

    private void close(BufferedReader read) {
    	if (read != null) {
            try {
                read.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void close(SSHMachineInfo sshMachineInfo, ClientSession session) {
        if (session != null) {
            try {
                clientSessionPool.returnObject(sshMachineInfo, session);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 可以调用多次executeCommand， 并返回结果
     */
    public class SSHSession {

        private String address;
        private ClientSession clientSession;

        private SSHSession(ClientSession clientSession, String address) {
            this.clientSession = clientSession;
            this.address = address;
        }

        /**
         * 执行命令并返回结果，可以执行多次
         * @param cmd
         * @return 执行成功Result为true，并携带返回信息,返回信息可能为null
         *         执行失败Result为false，并携带失败信息
         *         执行异常Result为false，并携带异常
         */
        public Result executeCommand(String cmd) {
            return executeCommand(cmd, OP_TIMEOUT);
        }

        public Result executeCommand(String cmd, int timoutMillis) {
            return executeCommand(cmd, null, timoutMillis);
        }

        public Result executeCommand(String cmd, LineProcessor lineProcessor) {
            return executeCommand(cmd, lineProcessor, OP_TIMEOUT);
        }

        /**
         * 执行命令并返回结果，可以执行多次
         * @param cmd
         * @param lineProcessor 回调处理行
         * @return 如果lineProcessor不为null,那么永远返回Result.true
         */
        public Result executeCommand(String cmd, LineProcessor lineProcessor, int timoutMillis) {
            try (ByteArrayOutputStream stdout = new ByteArrayOutputStream();
                 ByteArrayOutputStream stderr = new ByteArrayOutputStream();
                 ClientChannel channel = clientSession.createExecChannel(cmd)) {
                channel.setOut(stdout);
                channel.setErr(stderr);
                channel.open().verify(timoutMillis);
                // Wait (forever) for the channel to close - signalling command finished
                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0L);
                LineProcessor tmpLP = lineProcessor;
                // 如果客户端需要进行行处理，则直接进行回调
                if (tmpLP != null) {
                    processStream(new ByteArrayInputStream(stdout.toByteArray()), tmpLP);
                } else {
                    StringBuilder buffer = new StringBuilder();
                    tmpLP = generateDefaultLineProcessor(buffer);
                    processStream(new ByteArrayInputStream(stdout.toByteArray()), tmpLP);
                    if (buffer.length() > 0) {
                        return new Result(true, buffer.toString());
                    }
                }
                if(tmpLP.lineNum() == 0) {
                    // 返回为null代表可能有异常，需要检测标准错误输出，以便记录日志
                    Result errResult = tryLogError(new ByteArrayInputStream(stderr.toByteArray()), cmd);
                    if (errResult != null) {
                        return errResult;
                    }
                }
                return new Result(true, null);
            } catch (Exception e) {
                logger.error("execute ip:{} cmd:{}", address, cmd, e);
                return new Result(e);
            }
        }

        private Result tryLogError(InputStream is, String cmd) {
            StringBuilder buffer = new StringBuilder();
            LineProcessor lp = generateDefaultLineProcessor(buffer);
            processStream(is, lp);
            String errInfo = buffer.length() > 0 ? buffer.toString() : null;
            if (errInfo != null) {
                logger.error("address " + address + " execute cmd:({}), err:{}", cmd, errInfo);
                return new Result(false, errInfo);
            }
            return null;
        }

        /**
         * Copy a set of local files to a remote directory, uses the specified mode when
         * creating the file on the remote side.
         * @param localFiles
         *            Path and name of local file.
         * @param remoteFile
         *            name of remote file.
         * @param remoteTargetDirectory
         *            Remote target directory. Use an empty string to specify the default directory.
         * @param mode
         *            a four digit string (e.g., 0644, see "man chmod", "man open")
         * @throws IOException
         */
        public Result scp(String[] localFiles, String remoteFile, String remoteTargetDirectory, String mode) {
            try {
                ScpClient client = ScpClientCreator.instance().createScpClient(clientSession);
                String separator = FileSystems.getDefault().getSeparator();
                if(localFiles.length == 1){
                    if(StringUtils.isBlank(remoteFile)){
                        client.upload(localFiles, remoteTargetDirectory, ScpClient.Option.TargetIsDirectory);
                        int index = localFiles[0].lastIndexOf(separator);
                        if(index <= 0){
                            index = 0;
                        }else{
                            index = index + 1;
                        }
                        String fileName = localFiles[0].substring(index);
                        clientSession.executeRemoteCommand("chmod " + mode + " \"" + remoteTargetDirectory + "/" + fileName + "\"");
                    } else {
                        client.upload(localFiles, remoteTargetDirectory + "/" + remoteFile);
                        clientSession.executeRemoteCommand("chmod " + mode + " \"" + remoteTargetDirectory + "/" + remoteFile + "\"");
                    }
                } else {
                    client.upload(localFiles, remoteTargetDirectory, ScpClient.Option.TargetIsDirectory);
                    StringBuffer sb = new StringBuffer();
                    List<String> files = Arrays.asList(localFiles);
                    String remoteFiles = files.stream().map(file -> {
                        int index = file.lastIndexOf(separator);
                        if(index <= 0){
                            index = 0;
                        }else{
                            index = index + 1;
                        }
                        return " \"" + remoteTargetDirectory + "/" + file.substring(index) + "\"";
                    }).collect(Collectors.joining(" "));
                    clientSession.executeRemoteCommand("chmod " + mode + " " + remoteFiles);
                }
                return new Result(true);
            } catch (Exception e) {
                logger.error("scp local="+Arrays.toString(localFiles) + " to " +
                        remoteTargetDirectory + " remote=" + remoteFile + " err", e);
                return new Result(e);
            }
        }

        public Result scpToDir(String localFile, String remoteTargetDirectory) {
            return scpToDir(localFile, remoteTargetDirectory, "0744");
        }

        public Result scpToDir(String localFile, String remoteTargetDirectory, String mode) {
            return scp(new String[] { localFile }, null, remoteTargetDirectory, mode);
        }

        public Result scpToDir(String[] localFile, String remoteTargetDirectory) {
            return scp(localFile, null, remoteTargetDirectory, "0744");
        }
        public Result scpToFile(String localFile, String remoteFile, String remoteTargetDirectory) {
            return scpToFile(localFile, remoteFile, remoteTargetDirectory, "0744");
        }
        public Result scpToFile(String localFile, String remoteFile, String remoteTargetDirectory, String mode) {
            return scp(new String[] { localFile }, remoteFile, remoteTargetDirectory, "0744");
        }
    }

    /**
     * 结果封装
     */
    public static class Result{
        private boolean success;
        private String result;
        private Exception excetion;

        public Result(boolean success) {
            this.success = success;
        }

        public Result(boolean success, String result) {
            this.success = success;
            this.result = result;
        }

        public Result(Exception excetion) {
            this.success = false;
            this.excetion = excetion;
        }

        public Exception getExcetion() {
            return excetion;
        }

        public void setExcetion(Exception excetion) {
            this.excetion = excetion;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return "Result [success=" + success + ", result=" + result
                    + ", excetion=" + excetion + "]";
        }
    }

    /**
     *	执行命令回调
     */
    public interface SSHCallback{
        /**
         * 执行回调
         * @param session
         */
        Result call(SSHSession session);
    }

    /**
     * 从流中直接解析数据
     */
    public static interface LineProcessor {
        /**
         * 处理行
         * @param line  内容
         * @param lineNum   行号，从1开始
         * @throws Exception
         */
        void process(String line, int lineNum) throws Exception;

        /**
         * 返回内容的行数，如果为0需要检测错误流
         * @return
         */
        int lineNum();

        /**
         * 所有的行处理完毕回调该方法
         */
        void finish();
    }

    public static abstract class DefaultLineProcessor implements LineProcessor {
        protected int lineNum;

        @Override
        public int lineNum() {
            return lineNum;
        }

        public void setLineNum(int lineNum) {
            this.lineNum = lineNum;
        }

        public void finish() {}
    }
}
