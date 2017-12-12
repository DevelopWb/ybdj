package yangTalkback.Comm;

//����ͨ���رյ�ԭ��
public enum ClosureCase
{
	Error(-1), //����
	OperClose(0), //�ֶ��ر�
	OperCancel(1), //�ֶ��ر�
	System(2); //ϵͳ

	private int intValue;
	private static java.util.HashMap<Integer, ClosureCase> mappings;
	private synchronized static java.util.HashMap<Integer, ClosureCase> getMappings()
	{
		if (mappings == null)
		{
			mappings = new java.util.HashMap<Integer, ClosureCase>();
		}
		return mappings;
	}

	private ClosureCase(int value)
	{
		intValue = value;
		ClosureCase.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static ClosureCase forValue(int value)
	{
		return getMappings().get(value);
	}
}