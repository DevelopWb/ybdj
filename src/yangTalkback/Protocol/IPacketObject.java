package yangTalkback.Protocol;

//����ӿڶ���
public interface IPacketObject 
{
	 byte[] GetBytes();

	 void SetBytes(byte[] buf);

}