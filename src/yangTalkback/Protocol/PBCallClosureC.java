 package yangTalkback.Protocol;

public class PBCallClosureC extends PBodyJSON
{
	/** 
	 ԭ�� -1�������� 0�ֶ��رգ�1ϵͳ����
	 
	*/
	public int Cause;
 
	@Override
	public String toString()
	{
		return super.toString() + "  Cause:" + (new Integer(Cause)).toString();

	}
}