package com.lh.im.platform.vo.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = false)
public class PageVo<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private Collection<T> records;

	private int current;

	private int size;

	private int total;

	public PageVo() {
	}

	public PageVo(int total, Collection<T> records) {
		this(0, 0, total, records);
	}

	public PageVo(int current, int size, int total, Collection<T> records) {
		this.current = current;
		this.size = size;
		this.total = total;
		this.records = records;
	}

	public PageVo(Page<T> page) {
		this.total = Math.toIntExact(page.getTotal());
		this.records = page.getRecords();
	}

	public PageVo<T> data(Collection<T> records) {
		this.records = (records == null || records.isEmpty()) ? null : records;
		return this;
	}

	public PageVo<T> total(int total) {
		this.total = total;
		return this;
	}
	
}
