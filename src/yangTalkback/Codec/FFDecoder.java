package yangTalkback.Codec;

import java.nio.ByteBuffer;

import yangTalkback.Codec.FFCodec.AVCodecCfg;
import yangTalkback.Codec.FFCodec.FFCode;
import yangTalkback.Codec.FFCodec.FFObj;
import yangTalkback.Media.MediaFrame;
import yangTalkback.Media.VideoDisplayFrame;
import android.graphics.Bitmap;

import AXLib.Utility.IDisposable;

//FFMPEG ������
public class FFDecoder implements IDisposable {
	FFObj ffObj = null;
	AVCodecCfg cfg = null;
	boolean inited = false;
	private int _width = 0;
	private int _height = 0;

	private int decodedFrame[];
	// ͼƬ���ش������
	private byte[] mPixel = null;
	// ͼƬ����ڴ�����
	private ByteBuffer bmpBuffer = null;
	// ������ͼƬ
	private Bitmap VideoBit = null;
	private FFCode _ffCode;

	// ����һ��ͼƬ���¼�
	// public final Event<Bitmap> Decoded = new Event<Bitmap>();

	public FFDecoder(FFCode code) {
		_ffCode = code;
	}

	// ��ʼ��
	private void Init(MediaFrame mf) throws Exception {
 
	}

	// ��ʼ��
	private boolean tryResetSize(MediaFrame mf) throws Exception {
		return false;
	}

	public VideoDisplayFrame Deocde(MediaFrame mf) throws Exception {
		 
		return null;
	}

	// // ������������¼�
	// private void OnDecoded(Bitmap bitmap) {
	// if (Decoded.getHandleCount() >= 0)
	// Decoded.Trigger(this, bitmap);
	// }

	@Override
	public void Dispose() {
		if (ffObj != null)
			ffObj.Dispose();
	}
}
