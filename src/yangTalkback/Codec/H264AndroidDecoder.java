package yangTalkback.Codec;

import java.nio.ByteBuffer;

import yangTalkback.Media.MediaFrame;
import yangTalkback.Media.VideoDisplayFrame;

import android.graphics.Bitmap;

import h264.com.VView;
import AXLib.Utility.Event;
import AXLib.Utility.IDisposable;
import AXLib.Utility.Queue;

//H264������,û���õ�
public class H264AndroidDecoder extends VView implements IDisposable {

	// ͼƬ���ش������
	protected byte[] mPixel = null;
	// ͼƬ����ڴ�����
	protected ByteBuffer bmpBuffer = null;
	// ������ͼƬ
	protected Bitmap VideoBit = null;

	// ��Ƶ���ݽ��ջ�����
	protected Queue<MediaFrame> qFrame = null;
	// ��ʶ�����߳��Ƿ����ڹ�����
	protected boolean decodeThreadWorking = false;
	// ��󻺳�ʱ��
	protected final int MaxBufferTime = 1;

	// ����һ��ͼƬ���¼�
	// public final Event<Bitmap> Decoded = new Event<Bitmap>();

	public final Event<Exception> Error = new Event<Exception>();

	protected boolean inited = false;

	// ��ʼ��
	private void Init(MediaFrame mf) {
		 
	}

	public VideoDisplayFrame Deocde(MediaFrame mf) throws Exception {
		 
		return null;
	}

 
	@Override
	public void Dispose() {
	 
	}
}
