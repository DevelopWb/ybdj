package yangTalkback.Protocol;

//ͨ����Ϣ���ͣ�һ�����C��Ϊ���ã�R��Ϊ����
public enum MessageType {
	Unknow, Login_C, // ��¼
	Login_R, Logout_C, // �ǳ�
	Logout_R, Heart_C, // ����
	Heart_R, AllID_C, // ��ȡ����ID
	AllID_R, Call_C, // ����
	Call_R, CallClosureC, // �رպ���
	CallClosureR, Media, // ý������
	MonitorOpen_C, // ���ÿ������
	MonitorOpen_R, // ���ؿ������
	MonitorClose_C, // ���ùرռ��
	MonitorClose_R, // ���ʹرռ��
	Cmd_C, // ��������
	Cmd_R, // �����
	Cmd_M;// ����ͨ��

	public int getValue() {
		return this.ordinal();
	}

	public static MessageType forValue(int value) {
		return values()[value];
	}
}