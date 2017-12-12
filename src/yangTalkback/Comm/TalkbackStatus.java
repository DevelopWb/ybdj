package yangTalkback.Comm;

/** ʵʱ�Խ�״̬ */
public enum TalkbackStatus {
	/** ���� */
	Idle(0),
	/** �˳��� */
	Leaveing(1),
	/** ������ */
	Entering(2),
	/** �Խ��� */
	Talkbacking(3),
	/** �Ͽ� */
	Break(4);

	private int intValue;
	private static java.util.HashMap<Integer, TalkbackStatus> mappings;

	private synchronized static java.util.HashMap<Integer, TalkbackStatus> getMappings() {
		if (mappings == null) {
			mappings = new java.util.HashMap<Integer, TalkbackStatus>();
		}
		return mappings;
	}

	private TalkbackStatus(int value) {
		intValue = value;
		TalkbackStatus.getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static TalkbackStatus forValue(int value) {
		return getMappings().get(value);
	}
}