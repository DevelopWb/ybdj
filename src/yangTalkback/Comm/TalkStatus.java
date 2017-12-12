package yangTalkback.Comm;

/** ��Ƶͨ��״̬ */
public enum TalkStatus {
	/** ���� */
	Idle(0),
	/** ������ */
	Requesting(1),
	/** ��Ӧ�� */
	Repling(2),
	/** ͨ���� */
	Talking(3),
	/** �Ͽ��� */
	Closing(4);

	private int intValue;
	private static java.util.HashMap<Integer, TalkStatus> mappings;

	private synchronized static java.util.HashMap<Integer, TalkStatus> getMappings() {
		if (mappings == null) {
			mappings = new java.util.HashMap<Integer, TalkStatus>();
		}
		return mappings;
	}

	private TalkStatus(int value) {
		intValue = value;
		TalkStatus.getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static TalkStatus forValue(int value) {
		return getMappings().get(value);
	}
}