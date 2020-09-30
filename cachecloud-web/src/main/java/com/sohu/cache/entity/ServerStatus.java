package com.sohu.cache.entity;

import lombok.Data;

/**
 * 服务器状态
 */
@Data
public class ServerStatus {
	private String cdate;
	private String ctime;
	private float cuser;
	private float csys;
	private float cwio;
	private String cExt;
	private float cload1;
	private float cload5;
	private float cload15;
	private float mtotal;
	private float mfree;
	private float mcache;
	private float mbuffer;
	private float mswap;
	private float mswapFree;
	private float nin;
	private float nout;
	private int tuse;
	private int torphan;
	private int twait;
	private String ninExt;
	private String noutExt;
	private float dread;
	private float dwrite;
	private float diops;
	private float dbusy;
	private String dExt;
	private String dspace;
}
