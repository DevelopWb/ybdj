package yangTalkback.Comm;

public enum MonitorPublishStatus {
	/**
	 * ����
	 */
	Stop(0),
	/**
	 * ����
	 */
	Ready(1),

	/**
	 * ������
	 */
	Publishing(2),
	/**
	 * �ж�
	 */
	Interrupt(3),

	/**
	 * �Ͽ���
	 */
	Closing(4);

	private int intValue;
	private static java.util.HashMap<Integer, MonitorPublishStatus> mappings;

	private synchronized static java.util.HashMap<Integer, MonitorPublishStatus> getMappings() {
		if (mappings == null) {
			mappings = new java.util.HashMap<Integer, MonitorPublishStatus>();
		}
		return mappings;
	}

	private MonitorPublishStatus(int value) {
		intValue = value;
		MonitorPublishStatus.getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static MonitorPublishStatus forValue(int value) {
		return getMappings().get(value);
	}
}