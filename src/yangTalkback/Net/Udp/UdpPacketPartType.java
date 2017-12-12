package yangTalkback.Net.Udp;


public enum UdpPacketPartType {
	Complete(0), // ������ý�����û�б����
	First(1), // ���ý���ʱ��һ�����
	Mid(2), // ���ý���ʱ�м�ķ��
	Last(3); // ���ý���ʱ���һ�����

	private int intValue;
	private static java.util.HashMap<Integer, UdpPacketPartType> mappings;

	private synchronized static java.util.HashMap<Integer, UdpPacketPartType> getMappings() {
		if (mappings == null) {
			mappings = new java.util.HashMap<Integer, UdpPacketPartType>();
		}
		return mappings;
	}

	private UdpPacketPartType(int value) {
		intValue = value;
		UdpPacketPartType.getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static UdpPacketPartType forValue(int value) {
		return getMappings().get(value);
	}
}