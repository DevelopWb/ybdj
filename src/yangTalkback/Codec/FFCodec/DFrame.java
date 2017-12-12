package yangTalkback.Codec.FFCodec;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;

import AXLib.Model.ByteObj;
import AXLib.Model.ByteObjMember;
import AXLib.Utility.RuntimeExceptionEx;

public class DFrame extends ByteObj {

	@ByteObjMember(index = 10)
	public int nRawType;// ԭ�������ͣ�0δѹ����1ѹ��
	@ByteObjMember(index = 20)
	public int nIsKeyFrame; // �Ƿ�Ϊ�ؼ�֡
	@ByteObjMember(index = 30)
	public int nTimetick; // ʱ���
	@ByteObjMember(index = 40)
	public int nIsAudio; // �Ƿ�Ϊ��Ƶ,0:��Ƶ,1:��Ƶ
	@ByteObjMember(index = 50)
	public int nSize; // ���ݴ�С,�����Ÿýṹ�������ý������
	public byte[] Data;

	@Override
	public int getSize() {
		return 4 * 5;
	}

	@Override
	public void setBytes(byte[] bytes) {
		try {
			DataInput intput = createDataInput(bytes);
			nRawType = intput.readInt();
			nIsKeyFrame = intput.readInt();
			nTimetick = intput.readInt();
			nIsAudio = intput.readInt();
			nSize = intput.readInt();
		} catch (Exception e) {

		}
	}

	@Override
	public byte[] getBytes() {

		try {
			ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
			DataOutput output = createDataOutput(bOutput);
			output.writeInt(nRawType);

			output.writeInt(nIsKeyFrame);
			output.writeInt(nTimetick);
			output.writeInt(nIsAudio);
			output.writeInt(nSize);
			return bOutput.toByteArray();
		} catch (Exception e) {
			throw RuntimeExceptionEx.Create(e);
		}
	}

}
