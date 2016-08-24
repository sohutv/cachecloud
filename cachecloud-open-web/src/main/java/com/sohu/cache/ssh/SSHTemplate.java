package com.sohu.cache.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.util.ConstUtils;
/**
 * SSH操作模板类
 */
public class SSHTemplate {
	private static final Logger logger = LoggerFactory.getLogger(SSHTemplate.class);
	
	private static final int CONNCET_TIMEOUT = 6000;
	
	private static final int OP_TIMEOUT = 12000;
	
	private static ThreadPoolExecutor taskPool = new ThreadPoolExecutor(
			200, 200, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1000), 
			new ThreadFactoryBuilder().setNameFormat("SSH-%d").setDaemon(true).build());
	
	public Result execute(String ip, SSHCallback callback) throws SSHException{
		return execute(ip,ConstUtils.DEFAULT_SSH_PORT_DEFAULT, ConstUtils.USERNAME, 
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
        Connection conn = null;
        try {
            conn = getConnection(ip, port, username, password);
            return callback.call(new SSHSession(conn, ip+":"+port));
        } catch (Exception e) {
            throw new SSHException("SSH err: " + e.getMessage(), e);
        } finally {
        	close(conn);
        }
    }
    
    /**
     * 获取连接并校验
     * @param ip
     * @param port
     * @param username
     * @param password
     * @return Connection
     * @throws Exception
     */
    private Connection getConnection(String ip, int port, 
    		String username, String password) throws Exception {
    	Connection conn = new Connection(ip, port);
        conn.connect(null, CONNCET_TIMEOUT, CONNCET_TIMEOUT);
        boolean isAuthenticated = conn.authenticateWithPassword(username, password);
        if (isAuthenticated == false) {
            throw new Exception("SSH authentication failed with [ userName: " + 
            		username + ", password: " + password + "]");
        }
        return conn;
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
        	reader = new BufferedReader(new InputStreamReader(new StreamGobbler(is)));
	    	String line = null;
	    	int lineNum = 1;
	        while ((line = reader.readLine()) != null) {
	        	try {
					lineProcessor.process(line, lineNum);
				} catch (Exception e) {
					logger.error("err line:"+line, e);
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
    
    private void close(Connection conn) {
    	if (conn != null) {
            try {
            	conn.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
    private static void close(Session session) {
    	if (session != null) {
            try {
            	session.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * 可以调用多次executeCommand， 并返回结果
     */
    public class SSHSession{
    	private String address;
    	private Connection conn;
    	private SSHSession(Connection conn, String address) {
    		this.conn = conn;
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
    		Session session = null;
    		try {
    			session = conn.openSession();
    			return executeCommand(session, cmd, timoutMillis, lineProcessor);
			} catch (Exception e) {
				logger.error("execute ip:"+conn.getHostname()+" cmd:"+cmd, e);
				return new Result(e);
			} finally {
				close(session);
			}
    	}
    	
    	public Result executeCommand(final Session session, final String cmd, 
    			final int timoutMillis, final LineProcessor lineProcessor) throws Exception{
    		Future<Result> future = taskPool.submit(new Callable<Result>() {
				public Result call() throws Exception {
					session.execCommand(cmd);
					//如果客户端需要进行行处理，则直接进行回调
					if(lineProcessor != null) {
						processStream(session.getStdout(), lineProcessor);
					} else {
						//获取标准输出
						String rst = getResult(session.getStdout());
						if(rst != null) {
							return new Result(true, rst);
						}
						//返回为null代表可能有异常，需要检测标准错误输出，以便记录日志
						Result errResult = tryLogError(session.getStderr(), cmd);
						if(errResult != null) {
							return errResult;
						}
					}
					return new Result(true, null);
				}
			});
    		Result rst = null;
    		try {
    			rst = future.get(timoutMillis, TimeUnit.MILLISECONDS);
    			future.cancel(true);
    		} catch (TimeoutException e) {
    			logger.error("exec ip:{} {} timeout:{}", conn.getHostname(), cmd, timoutMillis);
    			throw new SSHException(e);
    		}
    		return rst;
    	}
    	
    	private Result tryLogError(InputStream is, String cmd) {
    		String errInfo = getResult(is);
        	if(errInfo != null) {
        		logger.error("address "+address+" execute cmd:({}), err:{}", cmd, errInfo);
        		return new Result(false, errInfo);
        	}
        	return null;
    	}
    	
    	/**
    	 * Copy a set of local files to a remote directory, uses the specified mode when
    	 * creating the file on the remote side.
    	 * @param localFiles
    	 *            Path and name of local file.
    	 * @param remoteFiles
    	 *            name of remote file.
    	 * @param remoteTargetDirectory
    	 *            Remote target directory. Use an empty string to specify the default directory.
    	 * @param mode
    	 *            a four digit string (e.g., 0644, see "man chmod", "man open")
    	 * @throws IOException
    	 */
    	public Result scp(String[] localFiles, String[] remoteFiles, String remoteTargetDirectory, String mode) {
    		try {
    			SCPClient client = conn.createSCPClient();
				client.put(localFiles, remoteFiles, remoteTargetDirectory, mode);
				return new Result(true);
			} catch (Exception e) {
				logger.error("scp local="+Arrays.toString(localFiles)+" to "+
						remoteTargetDirectory+" remote="+Arrays.toString(remoteFiles)+" err", e);
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
    		return scp(new String[] { localFile }, new String[] { remoteFile }, remoteTargetDirectory, "0744");
    	}
    }
    
    /**
     * 结果封装
     */
    public class Result{
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
	public static interface LineProcessor{
		/**
		 * 处理行
		 * @param line  内容
		 * @param lineNum   行号，从1开始
		 * @throws Exception
		 */
		void process(String line, int lineNum) throws Exception;
		
		/**
		 * 所有的行处理完毕回调该方法
		 */
		void finish();
	}
	
	public static abstract class DefaultLineProcessor implements LineProcessor{
		public void finish() {}
	}
}
