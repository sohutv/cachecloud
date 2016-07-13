package com.sohu.cache.server.data;
/**
 * 代表操作系统的原始信息
 * 及一些类型定义
 */
public class OSInfo {
	//操作系统信息  uname -a
	private String uname;
	//发布版本 - /etc/issue
	private String issue;
	public String getUname() {
		return uname;
	}
	public void setUname(String uname) {
		this.uname = uname;
	}
	public String getIssue() {
		return issue;
	}
	public void setIssue(String issue) {
		this.issue = issue;
	}
	public String toString() {
		return "OSInfo [uname=" + uname + ", issue=" + issue + "]";
	}
	/**
	 * 操作系统类型
	 */
	public enum OSType{
		LINUX("linux"),
		;
		private String value;
		private OSType(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
		public static OSType findByValue(String value) {
			for(OSType os : values()) {
				if(os.getValue().equals(value)) {
					return os;
				}
			}
			return null;
		}
	}
	
	/**
	 * 操作系统的发行版本
	 */
	public enum DistributionType{
		//通用系列
		LINUX("linux", "@@linux@@", new DistributionVersion[]{DistributionVersion.DEFAULT}),
		LINUX_OLD("linux_old", "@@linux_old@@", new DistributionVersion[]{DistributionVersion.DEFAULT}),
		
		//红帽系列
		REDHAT("rhel", "red hat", new DistributionVersion[]{
				DistributionVersion.REDHAT_4,
				DistributionVersion.REDHAT_45,
				DistributionVersion.REDHAT_5,
				DistributionVersion.REDHAT_52,
				DistributionVersion.REDHAT_54,
				DistributionVersion.REDHAT_6,
				DistributionVersion.REDHAT_65,
				DistributionVersion.REDHAT_7,
				DistributionVersion.REDHAT_71,
				DistributionVersion.REDHAT_72,
				}),
		
		//centos系列
		CENTOS("centos", "centos", new DistributionVersion[]{
				DistributionVersion.CENTOS_6,
				DistributionVersion.CENTOS_7,
				}),
		
		//ubuntu系列
		UBUNTU("ubuntu", "ubuntu", new DistributionVersion[]{
				DistributionVersion.UBUNTU_6,
				DistributionVersion.UBUNTU_7,
				DistributionVersion.UBUNTU_8,
				DistributionVersion.UBUNTU_810,
				DistributionVersion.UBUNTU_9,
				DistributionVersion.UBUNTU_910,
				DistributionVersion.UBUNTU_10,
				DistributionVersion.UBUNTU_1004,
				DistributionVersion.UBUNTU_1010,
				DistributionVersion.UBUNTU_1104,
				DistributionVersion.UBUNTU_1110,
				DistributionVersion.UBUNTU_13,
				DistributionVersion.UBUNTU_14,
				DistributionVersion.UBUNTU_1404,
				DistributionVersion.UBUNTU_1410,
				DistributionVersion.UBUNTU_15,
				DistributionVersion.UBUNTU_1504,
				DistributionVersion.UBUNTU_1510,
		}),
		
		//debian系列
		DEBIAN("debian", "debian", new DistributionVersion[]{
				DistributionVersion.DEBIAN_5,
				DistributionVersion.DEBIAN_50,
				DistributionVersion.DEBIAN_6,
				DistributionVersion.DEBIAN_60,
				DistributionVersion.DEBIAN_7,
				DistributionVersion.DEBIAN_8,
		}),
		
		//fedora系列
		FEDORA("fedora", "fedora", new DistributionVersion[]{
				DistributionVersion.FEDORA_14,
				DistributionVersion.FEDORA_15,
				DistributionVersion.FEDORA_16,
				DistributionVersion.FEDORA_17,
				DistributionVersion.FEDORA_18,
				DistributionVersion.FEDORA_19,
				DistributionVersion.FEDORA_20,
				DistributionVersion.FEDORA_21,
				DistributionVersion.FEDORA_22,
		}),
		
		//mint系列
		MINT("mint", "mint", new DistributionVersion[]{
				DistributionVersion.MINT_7,
				DistributionVersion.MINT_8,
				DistributionVersion.MINT_12,
				DistributionVersion.MINT_14,
				DistributionVersion.MINT_15,
				DistributionVersion.MINT_16,
				DistributionVersion.MINT_17,
		}),
		
		//opensuse系列
		OPENSUSE("opensuse", "opensuse", new DistributionVersion[]{
				DistributionVersion.OPENSUSE_11,
				DistributionVersion.OPENSUSE_12,
				DistributionVersion.OPENSUSE_13,
		}),
		
		//sles系列(SuSE Linux Enterprise Server)
		SLES("sles", "sles", new DistributionVersion[]{
				DistributionVersion.SLES_11,
				DistributionVersion.SLES_12,
				DistributionVersion.SLES_13,
		}),
		
		//knoppix
		KNOPPIX("knoppix", "knoppix", new DistributionVersion[]{
				DistributionVersion.KNOPPIX_4,
				DistributionVersion.KNOPPIX_5,
				DistributionVersion.KNOPPIX_6,
				DistributionVersion.KNOPPIX_7,
		}),
		
		;
		//nmon文件对应的名字
		private String nmonName;
		//发行版本对应的标志
		private String distSign;
		private DistributionVersion[] versions;
		
		private DistributionType(String nmonName, String distSign, 
				DistributionVersion[] versions) {
			this.nmonName = nmonName;
			this.distSign = distSign;
			this.versions = versions;
		}
		
		public String getNmonName() {
			return nmonName;
		}

		public String getDistSign() {
			return distSign;
		}

		public DistributionVersion[] getVersions() {
			return versions;
		}
		
		public static DistributionType findByContains(String value) {
			for(DistributionType type : values()) {
				if(value.contains(type.getDistSign())) {
					return type;
				}
			}
			return null;
		}
	}
	
	/**
	 * 操作系统的发行版本号
	 */
	public enum DistributionVersion{
		//通用系列无版本
		DEFAULT(""),
		
		//红帽系列
		REDHAT_4("4"),
		REDHAT_45("45"),
		REDHAT_5("5"),
		REDHAT_52("52"),
		REDHAT_54("54"),
		REDHAT_6("6"),
		REDHAT_65("65"),
		REDHAT_7("7"),
		REDHAT_71("71"),
		REDHAT_72("72"),
		
		//centos系列
		CENTOS_6("6"),
		CENTOS_7("7"),
		
		//ubuntu系列
		UBUNTU_6("6"),
		UBUNTU_7("7"),
		UBUNTU_8("8"),
		UBUNTU_810("810"),
		UBUNTU_9("9"),
		UBUNTU_910("910"),
		UBUNTU_10("10"),
		UBUNTU_1004("1004"),
		UBUNTU_1010("1010"),
		UBUNTU_1104("1104"),
		UBUNTU_1110("1110"),
		UBUNTU_13("13"),
		UBUNTU_14("14"),
		UBUNTU_1404("1404"),
		UBUNTU_1410("1410"),
		UBUNTU_15("15"),
		UBUNTU_1504("1504"),
		UBUNTU_1510("1510"),
		
		//debian系列
		DEBIAN_5("5"),
		DEBIAN_50("50"),
		DEBIAN_6("6"),
		DEBIAN_60("60"),
		DEBIAN_7("7"),
		DEBIAN_8("8"),
		
		//fedora系列
		FEDORA_14("14"),
		FEDORA_15("15"),
		FEDORA_16("16"),
		FEDORA_17("17"),
		FEDORA_18("18"),
		FEDORA_19("19"),
		FEDORA_20("20"),
		FEDORA_21("21"),
		FEDORA_22("22"),
		
		//mint系列
		MINT_7("7"),
		MINT_8("8"),
		MINT_12("12"),
		MINT_14("14"),
		MINT_15("15"),
		MINT_16("16"),
		MINT_17("17"),
		
		//opensuse系列
		OPENSUSE_11("11"),
		OPENSUSE_12("12"),
		OPENSUSE_13("13"),
		
		//sles系列(SuSE Linux Enterprise Server)
		SLES_11("11"),
		SLES_12("12"),
		SLES_13("13"),
		
		//knoppix
		KNOPPIX_4("4"),
		KNOPPIX_5("5"),
		KNOPPIX_6("6"),
		KNOPPIX_7("7"),
		;
		
		private String value;
		
		private DistributionVersion(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}
	
	/**
	 * 处理器架构
	 */
	public enum ProcessorArchitecture{
		X86_64("x86_64"),
		X86("x86"),
		UNKONW(""),
		;
		private String value;
		
		private ProcessorArchitecture(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
		
	}
}
