package com.lh.im.common.enums;

public enum FlagStateEnum {

	/**
	 * 启用状态
	 */
	ENABLED(1 << 0, "启用"),

	/**
	 * 锁定状态
	 */
	LOCKED(1 << 1, "锁定"),

	/**
	 * 隐藏状态
	 */
	HIDDEN(1 << 2, "隐藏"),

	/**
	 * 过期状态
	 */
	EXPIRED(1 << 3, "过期"),

	/**
	 * 停用状态
	 */
	DISABLED(1 << 4, "停用"),

	/**
	 * 审核状态
	 */
	AUDITED(1 << 5, "待审核"),

	/**
	 * 注销状态
	 */
	LOGOFF(1 << 6, "注销"),

	/**
	 * 信息完整状态  1待完善 0已完善
	 */
	COMPLETED(1 << 7, "待完善"),

	/**
	 * 逻辑删除状态
	 */
	DELETED(1 << 30, "删除");

	private int id;
	private String name;

	private FlagStateEnum(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int value() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public static FlagStateEnum ofValue(int value) {
		for (FlagStateEnum flag : values())
			if (flag.value() == value)
				return flag;
		return null;
	}
}
