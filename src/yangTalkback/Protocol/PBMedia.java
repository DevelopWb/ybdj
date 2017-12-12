package yangTalkback.Protocol;

import java.io.*;

import yangTalkback.Comm.*;
import yangTalkback.Media.MediaFrame;
import AXLib.Utility.*;

/**
 * ý���
 */
public class PBMedia extends PBodyBase {

	public PBMediaPart Part = PBMediaPart.Complete;
	// ����Ϣ
	public MediaFrame Frame;
	// ���Ϊ�ְ��������Ϊ�ְ���byte����
	public byte[] PartData;

	public PBMedia() {
	}

	public PBMedia(short from, short to, MediaFrame frame) {
		this.From = from;
		this.To = to;
		this.Frame = frame;
	}

	public PBMedia(byte[] buf) {
		SetBytes(buf);
	}

	@Override
	public byte[] GetBytes() {

		ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
		DataOutput bw = new LittleEndianDataOutputStream(bOutput);
		try {
			bw.writeShort(From);
			bw.writeShort(To);
			bw.writeByte(Part.getValue());
			byte[] buf = null;
			if (Part == PBMediaPart.Complete)
				buf = Frame.GetBytes();
			else
				buf = PartData;
			bw.write(buf, 0, buf.length);
		} catch (Exception e) {
			CLLog.Error(e);
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}
		byte[] buf = bOutput.toByteArray();
		return buf;
	}

	@Override
	public void SetBytes(byte[] buf) {

		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(buf);
			DataInput br = new LittleEndianDataInputStream(stream);

			From = br.readShort();
			To = br.readShort();
			Part = PBMediaPart.forValue(br.readByte());
			byte[] tbuf = new byte[buf.length - 5];
			br.readFully(tbuf, 0, tbuf.length);
			if (Part == PBMediaPart.Complete)
				Frame = new MediaFrame(tbuf);
			else
				PartData = tbuf;
		} catch (Exception e) {
			CLLog.Error(e);
			String stackString = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}

	}
}