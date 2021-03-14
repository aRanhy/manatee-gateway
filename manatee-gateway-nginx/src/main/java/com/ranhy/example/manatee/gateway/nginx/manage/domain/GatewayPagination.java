/**
 * Copyright (c) 2006-2015 Hzins Ltd. All Rights Reserved.
 *
 * This code is the confidential and proprietary information of
 * Hzins. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the agreements
 * you entered into with Hzins,http://www.hzins.com.
 *
 */
package com.ranhy.example.manatee.gateway.nginx.manage.domain;

import java.util.List;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
public class GatewayPagination<T> {

	public static final int DEFAULT_PAGENO = 0;

	public static final int DEFAULT_PAGESIZE = 10;

	private int total; // 总共多少条

	private int pageSize = 20; // 每页多少条

	private int pageNo = 0; // 第几页，从1开始

	protected int skip = 0;// 跳过多少条数据

	private boolean nextPage;

	private List<T> data;

	public GatewayPagination() {
	}

	public GatewayPagination(List<T> data, int total) {
		this.total = total;
		this.data = data;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public int getSkip() {
		int skip = this.pageNo > 0 ? (this.pageNo - 1) * this.pageSize : 0;
		return skip;
	}

	public void setSkip(int skip) {
		this.skip = skip;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
		if (total > pageNo * pageSize) {
			nextPage = true;
		}else{
			nextPage = false;
		}
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public boolean hasNextPage() {
		return nextPage;
	}

	public void setNextPage(boolean nextPage) {
		this.nextPage = nextPage;
	}

}
