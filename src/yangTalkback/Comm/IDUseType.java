package yangTalkback.Comm;

public enum IDUseType
{
	Center, //����
	Point, //��
	Terminal; //�ն�

	public int getValue()
	{
		return this.ordinal();
	}

	public static IDUseType forValue(int value)
	{
		return values()[value];
	}
}