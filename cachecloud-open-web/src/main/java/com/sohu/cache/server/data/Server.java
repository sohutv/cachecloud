package com.sohu.cache.server.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
/**
 *	服务器基本状态
 */
public class Server implements LineParser{
	public static final String TIME_FLAG = "ZZZZ";
	public static final String FLAG = "AAA";
	public static final Pattern pattern = Pattern.compile("^BBBP,[0-9]+,/proc/cpuinfo,\"model name");
	public static final String OPEN_FILES = "open files";
	public static final String MX_PROCESS = "max user processes";
	//标志时间的字段
	private String timeKey;
	//收集日期 类似18:49:21,31-MAY-2016格式
	private String dateTime;
	private Date collectTime;
	private String ip;
	//host
	private String host;
	//逻辑cpu个数
	private int cpus;
	//nmon版本
	private String nmon;
	//cpu型号
	private String cpuModel;
	//内核版本
	private String kernel;
	//发行版本
	private String dist;
	//ulimit
	private String ulimit = "";
	
	private CPU cpu;
	private Memory mem;
	private Load load;
	private Disk disk;
	private Net net;
	private Connection connection;
	
	public Server() {
		cpu = new CPU();
		mem = new Memory();
		load = new Load();
		disk = new Disk();
		net = new Net();
		connection = new Connection();
	}
	
	public static void main(String[] args) throws ParseException {
		String s = "18:49:21,31-MAY-2016";
		SimpleDateFormat sdf = new SimpleDateFormat(
				"HH:mm:ss,dd-MMM-yyyy", Locale.ENGLISH);
		System.out.println(sdf.parse(s));
	}

	/**
	 * line format:
	 * ZZZZ,T0001,09:50:01,01-JUL-2016
	 * AAA,host,localhost
	 * AAA,version,14g
	 * AAA,cpus,16
	 * AAA,OS,Linux,2.6.18-348.el5,#1 SMP Wed Nov 28 21:22:00 EST 2012,x86_64
	 * BBBP,374,/proc/cpuinfo,"model name      : Intel(R) Xeon(R) CPU           E5620  @ 2.40GHz"
	 * open files                      (-n) 65535
	 * max user processes              (-u) 65535
	 */
	public void parse(String line, String key) throws Exception {
		if(line.startsWith(TIME_FLAG)) {
			String[] items = line.split(",", 3);
			if(items.length == 3) {
				this.timeKey = items[1];
				this.dateTime = items[2];
				SimpleDateFormat sdf = new SimpleDateFormat(
						"HH:mm:ss,dd-MMM-yyyy", Locale.ENGLISH);
				this.collectTime = sdf.parse(dateTime);
			}
		} else if(line.startsWith(FLAG)) {
			String[] items = line.split(",", 3);
			if(items.length > 2) {
				if("host".equals(items[1])) {
					host = items[2];
				} else if("version".equals(items[1])) {
					nmon = items[2];
				} else if("cpus".equals(items[1])) {
					cpus = NumberUtils.toInt(items[2]);
				} else if("OS".equals(items[1])) {
					kernel = items[2];
				}
			}
		} else if(cpuModel == null && pattern.matcher(line).find()) {
			String[] tmp = line.split("model name");
			if(tmp.length == 2) {
				cpuModel = tmp[1].trim();
				cpuModel = cpuModel.substring(1, cpuModel.length() - 1);
			}
		}
		String mxFile = parseULimit(line, OPEN_FILES, "f");
		if(mxFile != null) {
			ulimit += mxFile;
		}
		String mxProcess = parseULimit(line, MX_PROCESS, "p");
		if(mxProcess != null) {
			ulimit += mxProcess;
		}
		
		load.parse(line, timeKey);
		cpu.parse(line, timeKey);
		mem.parse(line, timeKey);
		net.parse(line, timeKey);
		disk.parse(line, timeKey);
		connection.parse(line, timeKey);
	}
	
	private String parseULimit(String line, String prefix, String flag) {
		String result = null;
		if(line.startsWith(prefix)) {
			String[] tmp = line.split("\\s+");
			if(tmp.length > 0) {
				int v = NumberUtils.toInt(tmp[tmp.length - 1]);
				if(v > 0) {
					result = flag + "," + v +";";
				}
			}
		}
		return result;
	}
	public String getDateTime() {
		return dateTime;
	}
	public Date getCollectTime() {
		return collectTime;
	}

	public Connection getConnection() {
		return connection;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHost() {
		return host;
	}

	public int getCpus() {
		return cpus;
	}

	public String getUlimit() {
		return ulimit;
	}

	public String getNmon() {
		return nmon;
	}

	public String getCpuModel() {
		return cpuModel;
	}

	public String getKernel() {
		return kernel;
	}

	public String getDist() {
		return dist;
	}

	public CPU getCpu() {
		return cpu;
	}

	public Memory getMem() {
		return mem;
	}
	public String getTime() {
		return new SimpleDateFormat("HHmm").format(collectTime);
	}
	public Load getLoad() {
		return load;
	}

	public Disk getDisk() {
		return disk;
	}

	public Net getNet() {
		return net;
	}
	public void setHost(String host) {
		this.host = host;
	}

	public void setCpus(int cpus) {
		this.cpus = cpus;
	}

	public void setNmon(String nmon) {
		this.nmon = nmon;
	}

	public void setCpuModel(String cpuModel) {
		this.cpuModel = cpuModel;
	}
	public void setDist(String dist) {
		this.dist = dist;
	}

	public void setKernel(String kernel) {
		this.kernel = kernel;
	}

	public void setUlimit(String ulimit) {
		this.ulimit = ulimit;
	}

	@Override
	public String toString() {
		return "Server [timeKey=" + timeKey + ", dateTime=" + dateTime
				+ ", collectTime=" + collectTime + ", ip=" + ip + ", host="
				+ host + ", cpus=" + cpus + ", nmon=" + nmon + ", cpuModel="
				+ cpuModel + ", kernel=" + kernel + ", dist=" + dist
				+ ", ulimit=" + ulimit + ", cpu=" + cpu + ", mem=" + mem
				+ ", load=" + load + ", disk=" + disk + ", net=" + net
				+ ", connection=" + connection + "]";
	}
}
