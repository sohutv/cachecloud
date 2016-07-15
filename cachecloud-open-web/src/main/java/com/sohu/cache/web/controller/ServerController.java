package com.sohu.cache.web.controller;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.sohu.cache.entity.ServerInfo;
import com.sohu.cache.entity.ServerStatus;
import com.sohu.cache.web.service.ServerDataService;

/**
 * 获取服务器状态
 */
@Controller
@RequestMapping("/server")
public class ServerController extends BaseController{

	@Resource
	private ServerDataService serverDataService;
	
	private DecimalFormat df = new DecimalFormat("0.0");
	/**
	 * 跳转到主页
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping("/index")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response, Model model) {
		String ip = request.getParameter("ip");
		model.addAttribute("ip", ip);
		String date = request.getParameter("date");
		if(date == null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			date = sdf.format(new Date());
		}
		model.addAttribute("date", date);
        return new ModelAndView("server/index");
	}
	/**
	 * 服务器信息概览
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping("/overview")
	public ModelAndView overview(HttpServletRequest request, HttpServletResponse response, Model model) {
        String ip = request.getParameter("ip");
        String date = request.getParameter("date");
        //获取服务器静态信息
        ServerInfo info = serverDataService.queryServerInfo(ip);
        if(info != null) {
	        model.addAttribute("info", info);
	        //解析ulimit
	        String ulimit = info.getUlimit();
			if(!StringUtils.isEmpty(ulimit)) {
				String[] tmp = ulimit.split(";");
				if(tmp.length ==2) {
					String[] a = tmp[0].split(",");
					if(a != null && a.length == 2) {
						if("f".equals(a[0])) {
							model.addAttribute("file", a[1]);
						}
					}
					a = tmp[1].split(",");
					if(a != null && a.length == 2) {
						if("p".equals(a[0])) {
							model.addAttribute("process", a[1]);
						}
					}
				}
			}
        }
		//获取服务器状态
        List<ServerStatus> list = serverDataService.queryServerOverview(ip, date);
    	//x轴坐标
		List<String> xAxis = new ArrayList<String>();
		
		//1分钟最大load
		float maxLoad1 = 0;
		//load1总量
		double totalLoad1 = 0;
		
		//最大user
		float maxUser = 0;
		//最大sys
		float maxSys = 0;
		//最大wio
		float maxWa = 0;
		
		//当前可用内存
		float curFree = 0;
		//最大内存使用量
		float maxUse = 0;
		//最大内存cache量
		float maxCache = 0;
		//最大内存buffer量
		float maxBuffer = 0;
		//最大swap使用量
		float maxSwapUse = 0;
		
		//最大网络流入速度
		float maxNetIn = 0;
		//最大网络流出速度
		float maxNetOut = 0;
		//最大连接ESTABLISHED数
		int maxConn = 0;
		//最大连接TIME_WAIT数
		int maxWait = 0;
		//最大连接ORPHAN数
		int maxOrphan = 0;
		
		//最大读取速率
		float maxRead = 0;
		//最大写入速率
		float maxWrite = 0;
		//最繁忙程度
		float maxBusy = 0;
		//最大iops量
		float maxIops = 0;
		
		//load serie
		Series<Float> load1Serie = new Series<Float>("1-min");
		Series<Float> load5Serie = new Series<Float>("5-min");
		Series<Float> load15Serie = new Series<Float>("15-min");
		
		//cpu serie
		Series<Float> userSerie = new Series<Float>("user");
		Series<Float> sysSerie = new Series<Float>("sys");
		Series<Float> waSerie = new Series<Float>("wa");
		
		//memory serie
		Series<Float> totalSerie = new Series<Float>("total");
		Series<Float> useSerie = new Series<Float>("use");
		useSerie.setType("area");
		Series<Float> cacheSerie = new Series<Float>("cache");
		cacheSerie.setType("area");
		Series<Float> bufferSerie = new Series<Float>("buffer");
		bufferSerie.setType("area");
		Series<Float> swapSerie = new Series<Float>("total");
		Series<Float> swapUseSerie = new Series<Float>("use");
		
		//net serie
		Series<Float> netInSerie = new Series<Float>("in");
		Series<Float> netOutSerie = new Series<Float>("out");
		
		//tcp serie
		Series<Integer> establishedSerie = new Series<Integer>("established");
		Series<Integer> twSerie = new Series<Integer>("time wait");
		Series<Integer> orphanSerie = new Series<Integer>("orphan");
		
		//disk serie
		Series<Float> readSerie = new Series<Float>("read");
		readSerie.setType("column");
		Series<Float> writeSerie = new Series<Float>("write");
		writeSerie.setType("column");
		Series<Float> busySerie = new Series<Float>("busy");
		busySerie.setYAxis(1);
		Series<Float> iopsSerie = new Series<Float>("iops");
		iopsSerie.setYAxis(2);
		
		for(int i = 0; i < list.size(); ++i) {
			ServerStatus ss = list.get(i);
			//x axis
			xAxis.add(ss.getCtime().substring(0, 2) + ":" + ss.getCtime().substring(2));
			//load相关
			load1Serie.addData(ss.getCload1());
			load5Serie.addData(ss.getCload5());
			load15Serie.addData(ss.getCload15());
			maxLoad1 = getBigger(maxLoad1, ss.getCload1());
			totalLoad1 += ss.getCload1();
			//cpu相关
			userSerie.addData(ss.getCuser());
			sysSerie.addData(ss.getCsys());
			waSerie.addData(ss.getCwio());
			maxUser = getBigger(maxUser, ss.getCuser());
			maxSys = getBigger(maxSys, ss.getCsys());
			maxWa = getBigger(maxWa, ss.getCwio());
			//memory相关
			totalSerie.addData(ss.getMtotal());
			float use = ss.getMtotal()-ss.getMfree()-ss.getMcache()-ss.getMbuffer();
			useSerie.addData(use);
			cacheSerie.addData(ss.getMcache());
			bufferSerie.addData(ss.getMbuffer());
			maxUse = getBigger(maxUse, use);
			maxCache = getBigger(maxCache, ss.getMcache());
			maxBuffer = getBigger(maxBuffer, ss.getMbuffer());
			if(i == list.size() - 1) {
				curFree = ss.getMtotal() - use;
			}
			//swap相关
			swapSerie.addData(ss.getMswap());
			float swapUse = ss.getMswap() - ss.getMswapFree();
			swapUse = floor(swapUse);
			swapUseSerie.addData(swapUse);
			maxSwapUse = getBigger(maxSwapUse, swapUse);
			//net相关
			netInSerie.addData(ss.getNin());
			netOutSerie.addData(ss.getNout());
			maxNetIn = getBigger(maxNetIn, ss.getNin());
			maxNetOut = getBigger(maxNetOut, ss.getNout());
			//tcp相关
			establishedSerie.addData(ss.getTuse());
			twSerie.addData(ss.getTwait());
			orphanSerie.addData(ss.getTorphan());
			maxConn = getBigger(maxConn, ss.getTuse());
			maxWait = getBigger(maxWait, ss.getTwait());
			maxOrphan = getBigger(maxOrphan, ss.getTorphan());
			//disk相关
			readSerie.addData(ss.getDread());
			writeSerie.addData(ss.getDwrite());
			busySerie.addData(ss.getDbusy());
			iopsSerie.addData(ss.getDiops());
			maxRead = getBigger(maxRead, ss.getDread());
			maxWrite = getBigger(maxWrite, ss.getDwrite());
			maxBusy = getBigger(maxBusy, ss.getDbusy());
			maxIops = getBigger(maxIops, ss.getDiops());
		}
		//x axis
		model.addAttribute("xAxis", JSON.toJSONString(xAxis));
		//load
		model.addAttribute("load1", JSON.toJSONString(load1Serie));
		model.addAttribute("load5", JSON.toJSONString(load5Serie));
		model.addAttribute("load15", JSON.toJSONString(load15Serie));
		model.addAttribute("maxLoad1", maxLoad1);
		model.addAttribute("avgLoad1", format(totalLoad1, list.size()));
		//cpu
		model.addAttribute("user", JSON.toJSONString(userSerie));
		model.addAttribute("sys", JSON.toJSONString(sysSerie));
		model.addAttribute("wa", JSON.toJSONString(waSerie));
		model.addAttribute("maxUser", maxUser);
		model.addAttribute("maxSys", maxSys);
		model.addAttribute("maxWa", maxWa);
		//memory
		model.addAttribute("mtotal", JSON.toJSONString(totalSerie));
		model.addAttribute("muse", JSON.toJSONString(useSerie));
		model.addAttribute("mcache", JSON.toJSONString(cacheSerie));
		model.addAttribute("mbuffer", JSON.toJSONString(bufferSerie));
		model.addAttribute("curFree", format(curFree, 1024));
		model.addAttribute("maxUse", format(maxUse, 1024));
		model.addAttribute("maxCache", format(maxCache, 1024));
		model.addAttribute("maxBuffer", format(maxBuffer, 1024));
		//swap
		model.addAttribute("mswap", JSON.toJSONString(swapSerie));
		model.addAttribute("mswapUse", JSON.toJSONString(swapUseSerie));
		model.addAttribute("maxSwap", maxSwapUse);
		//net
		model.addAttribute("nin", JSON.toJSONString(netInSerie));
		model.addAttribute("nout", JSON.toJSONString(netOutSerie));
		model.addAttribute("maxNetIn", format(maxNetIn, 1024));
		model.addAttribute("maxNetOut", format(maxNetOut, 1024));
		//tcp
		model.addAttribute("testab", JSON.toJSONString(establishedSerie));
		model.addAttribute("twait", JSON.toJSONString(twSerie));
		model.addAttribute("torph", JSON.toJSONString(orphanSerie));
		model.addAttribute("maxConn", maxConn);
		model.addAttribute("maxWait", maxWait);
		model.addAttribute("maxOrphan", maxOrphan);
		//disk
		model.addAttribute("dread", JSON.toJSONString(readSerie));
		model.addAttribute("dwrite", JSON.toJSONString(writeSerie));
		model.addAttribute("dbusy", JSON.toJSONString(busySerie));
		model.addAttribute("diops", JSON.toJSONString(iopsSerie));
		model.addAttribute("maxRead", format(maxRead, 1024));
		model.addAttribute("maxWrite", format(maxWrite, 1024));
		model.addAttribute("maxBusy", maxBusy);
		model.addAttribute("maxIops", maxIops);
        model.addAttribute("date", date);
        return new ModelAndView("server/overview");
	}
	
	private String format(double a, int b) {
		if(b <= 0) {
			return "0";
		}
		return df.format(a/b);
	}
	
	private float getBigger(float a, float b) {
		if(a > b) {
			return a;
		}
		return b;
	}
	
	private int getBigger(int a, int b) {
		if(a > b) {
			return a;
		}
		return b;
	}
	
	/**
	 * 保留一位小数，四舍五入
	 * @param v
	 * @return
	 */
	private float floor(float v) {
		return new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
	}
	
	/**
	 * 获取服务器cpu各个核状态
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping("/cpu")
	public ModelAndView cpu(HttpServletRequest request, HttpServletResponse response, Model model) {
		String ip = request.getParameter("ip");
        String date = request.getParameter("date");
        List<ServerStatus> list = serverDataService.queryServerCpu(ip, date);
        Map<String, CpuChart> subcpuMap = new TreeMap<String, CpuChart>();
        //x轴坐标
		List<String> xAxis = new ArrayList<String>();
        for(ServerStatus ss : list) {
        	String subcpuString = ss.getcExt();
			String[] subCpuArray = subcpuString.split(";");
			xAxis.add(ss.getCtime());
			for(String subcpu : subCpuArray) {
				if(StringUtils.isEmpty(subcpu)) {
					continue;
				}
				String[] cpu = subcpu.split(",");
				CpuChart cpuChart = subcpuMap.get(cpu[0]);
				if(cpuChart == null) {
					cpuChart = new CpuChart(cpu[0]);
					subcpuMap.put(cpu[0], cpuChart);
				}
				float user = NumberUtils.toFloat(cpu[1]);
				float sys = NumberUtils.toFloat(cpu[2]);
				float wa = NumberUtils.toFloat(cpu[3]);
				cpuChart.addUserSeries(user);
				cpuChart.addSysSeries(sys);
				cpuChart.addWaSeries(wa);
				cpuChart.setMaxUser(user);
				cpuChart.setMaxSys(sys);
				cpuChart.setMaxWa(wa);
				cpuChart.addUser(user);
				cpuChart.addSys(sys);
				cpuChart.addWa(wa);
			}
        }
        //x axis
		model.addAttribute("xAxis", JSON.toJSONString(xAxis));
        model.addAttribute("cpu", subcpuMap.values());
        return new ModelAndView("server/cpu");
	}
	
	/**
	 * 获取服务器各网卡状态
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping("/net")
	public ModelAndView net(HttpServletRequest request, HttpServletResponse response, Model model) {
		String ip = request.getParameter("ip");
        String date = request.getParameter("date");
        List<ServerStatus> list = serverDataService.queryServerNet(ip, date);
        Map<String, NetChart> subnetMap = new TreeMap<String, NetChart>();
        //x轴坐标
		List<String> xAxis = new ArrayList<String>();
        for(ServerStatus ss : list) {
        	xAxis.add(ss.getCtime());
        	addNetMap(ss.getNinExt(), subnetMap, true);
        	addNetMap(ss.getNoutExt(), subnetMap, false);
        }
        //x axis
		model.addAttribute("xAxis", JSON.toJSONString(xAxis));
        model.addAttribute("net", subnetMap.values());
        return new ModelAndView("server/net");
	}
	
	/**
	 * parse net to map
	 * @param netString
	 * @param subnetMap
	 * @param isIn
	 */
	private void addNetMap(String netString, Map<String, NetChart> subnetMap, boolean isIn) {
		String[] subnetArray = netString.split(";");
		for(String subnet : subnetArray) {
			if(StringUtils.isEmpty(subnet)) {
				continue;
			}
			String[] net = subnet.split(",");
			NetChart netChart = subnetMap.get(net[0]);
			if(netChart == null) {
				netChart = new NetChart(net[0]);
				subnetMap.put(net[0], netChart);
			}
			float v = NumberUtils.toFloat(net[1]);
			if(isIn) {
				netChart.addInSeries(v);
				netChart.addTotalIn(v);
				netChart.setMaxIn(v);
			}else {
				netChart.addOutSeries(v);
				netChart.addTotalOut(v);
				netChart.setMaxOut(v);
			}
		}
	}
	
	/**
	 * 获取硬盘各分区状态
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping("/disk")
	public ModelAndView disk(HttpServletRequest request, HttpServletResponse response, Model model) {
		String ip = request.getParameter("ip");
        String date = request.getParameter("date");
        List<ServerStatus> list = serverDataService.queryServerDisk(ip, date);
        DiskChart readChart = new DiskChart();
        DiskChart writeChart = new DiskChart();
        DiskChart busyChart = new DiskChart();
        DiskChart iopsChart = new DiskChart();
        DiskChart spaceChart = new DiskChart();
        //x轴坐标
		List<String> xAxis = new ArrayList<String>();
        for(ServerStatus ss : list) {
        	xAxis.add(ss.getCtime());
        	//解析use
        	String dext = ss.getdExt();
			if(!StringUtils.isEmpty(dext)) {
				String[] items = dext.split(";");
				if(items != null) {
					for(String item : items) {
						String[] sds = item.split("=");
						if(sds.length == 2) {
							if("DISKXFER".equals(sds[0])) {
								addToChart(sds[1], iopsChart);
							} else if("DISKREAD".equals(sds[0])) {
								addToChart(sds[1], readChart);
							} else if("DISKWRITE".equals(sds[0])) {
								addToChart(sds[1], writeChart);
							} else if("DISKBUSY".equals(sds[0])) {
								addToChart(sds[1], busyChart);
							}
						}
					}
				}
			}
			//解析space
			String space = ss.getDspace();
			addToChart(space, spaceChart);
        }
        //x axis
		model.addAttribute("xAxis", JSON.toJSONString(xAxis));
        model.addAttribute("read", readChart);
        model.addAttribute("write", writeChart);
        model.addAttribute("busy", busyChart);
        model.addAttribute("iops", iopsChart);
        model.addAttribute("space", spaceChart);
        return new ModelAndView("server/disk");
	}
	
	private void addToChart(String line, DiskChart chart) {
		String[] parts = line.split(",");
		for(String part : parts) {
			if(StringUtils.isEmpty(part)) {
				continue;
			}
			String[] values = part.split(":");
			float d = NumberUtils.toFloat(values[1]);
			chart.addSeries(values[0], d);
			chart.setMax(d);
			chart.addTotal(d);
		}
	}
	
	/**
	 * net chart
	 */
	public class NetChart{
		private String name;
		private Series<Float> inSeries = new Series<Float>("in");
		private Series<Float> outSeries = new Series<Float>("out");
		private float maxIn;
		private float maxOut;
		private float totalIn;
		private float totalOut;
		public NetChart(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Series<Float> getInSeries() {
			return inSeries;
		}
		public void addInSeries(float d) {
			this.inSeries.addData(d);
		}
		public Series<Float> getOutSeries() {
			return outSeries;
		}
		public void addOutSeries(float d) {
			this.outSeries.addData(d);
		}
		public float getMaxIn() {
			return maxIn;
		}
		public void setMaxIn(float in) {
			if(this.maxIn < in) {
				this.maxIn = in;
			}
		}
		public float getMaxOut() {
			return maxOut;
		}
		public void setMaxOut(float out) {
			if(this.maxOut < out) {
				this.maxOut = out;
			}
		}
		public void addTotalIn(float in) {
			this.totalIn += in;
		}
		public void addTotalOut(float out) {
			this.totalOut += out;
		}
		public String getAvgIn() {
			return format(totalIn, inSeries.getData().size());
		}
		public String getAvgOut() {
			return format(totalOut, outSeries.getData().size());
		}
	}
	
	/**
	 * disk chart
	 */
	public class DiskChart{
		private float max;
		private float total;
		private Map<String, Series<Float>> seriesMap = new TreeMap<String, Series<Float>>();
		public void addSeries(String partition, float d) {
			Series<Float> series = seriesMap.get(partition);
			if(series == null) {
				series = new Series<Float>(partition);
				seriesMap.put(partition, series);
			}
			series.addData(d);
		}
		public Collection<Series<Float>> getSeries() {
			return seriesMap.values();
		}
		public float getMax() {
			return max;
		}
		public void setMax(float max) {
			if(this.max < max) {
				this.max = max;
			}
		}
		public String getAvg() {
			Collection<Series<Float>> coll = seriesMap.values();
			int size = 0;
			if(coll != null) {
				for(Series<Float> series : coll) {
					size += series.getData().size();
				}
			}
			return format(total, size);
		}
		public void addTotal(float total) {
			this.total += total;
		}
	}
	
	/**
	 * cpu chart
	 */
	public class CpuChart{
		private String name;
		private Series<Float> userSeries = new Series<Float>("user");
		private Series<Float> sysSeries = new Series<Float>("sys");
		private Series<Float> waSeries = new Series<Float>("wa");
		private float maxUser;
		private float maxSys;
		private float maxWa;
		private float totalUser;
		private float totalSys;
		private float totalWa;
		public CpuChart(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public float getMaxUser() {
			return maxUser;
		}
		public void setMaxUser(float user) {
			if(this.maxUser < user) {
				this.maxUser = user;
			}
		}
		public float getMaxSys() {
			return maxSys;
		}
		public void setMaxSys(float sys) {
			if(this.maxSys < sys) {
				this.maxSys = sys;
			}
		}
		public float getMaxWa() {
			return maxWa;
		}
		public void setMaxWa(float wa) {
			if(this.maxWa < wa) {
				this.maxWa = wa;
			}
		}
		public String getAvgUser() {
			return format(totalUser, userSeries.getData().size());
		}
		public String getAvgSys() {
			return format(totalSys, sysSeries.getData().size());
		}
		public String getAvgWa() {
			return format(totalWa, waSeries.getData().size());
		}
		public void addUser(float user) {
			this.totalUser += user;
		}
		public void addSys(float sys) {
			this.totalSys += sys;
		}
		public void addWa(float wa) {
			this.totalWa += wa;
		}
		public Series<Float> getUserSeries() {
			return userSeries;
		}
		public void addUserSeries(Float v) {
			this.userSeries.addData(v);
		}
		public Series<Float> getSysSeries() {
			return sysSeries;
		}
		public void addSysSeries(Float v) {
			this.sysSeries.addData(v);
		}
		public Series<Float> getWaSeries() {
			return waSeries;
		}
		public void addWaSeries(Float v) {
			this.waSeries.addData(v);
		}
	}
	
	/**
	 * Highchars Series
	 * @param <T> 
	 */
	public class Series<T>{
		private String name;
		private List<T> data = new ArrayList<T>();
		private String type = "spline";
		private int yAxis;
		public String toJson() {
			return JSON.toJSONString(this);
		}
		public Series(String name) {
			this.name = name;
		}
		public int getYAxis() {
			return yAxis;
		}
		public void setYAxis(int yAxis) {
			this.yAxis = yAxis;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getType() {
			return type;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void addData(T d) {
			data.add(d);
		}
		public List<T> getData() {
			return data;
		}
		@Override
		public String toString() {
			return "Serie [name=" + name + ", data=" + data + ", type=" + type
					+ ", yAxis=" + yAxis + "]";
		}
	}
}
