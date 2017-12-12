package yangTalkback.Media;

import yangTalkback.Codec.FFDecoder;
import yangTalkback.Codec.H264AndroidDecoder;
import yangTalkback.Codec.JPEGDecoder;
import yangTalkback.Codec.Cfg.VideoEncodeCfg;
import yangTalkback.Codec.FFCodec.FFCode;
import yangTalkback.Comm.*;
import yangTalkback.Media.VideoImage.ScaleMode;
import android.graphics.Bitmap;

import AXLib.Utility.CallBack;
import AXLib.Utility.IDisposable;
import AXLib.Utility.Queue;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
//import AXVChat.Codec.H264Decoder;

//��Ƶ���Ŷ���
public class VideoPlay implements IDisposable {
	private H264AndroidDecoder _h264Dec = null;// H264������,û���õ�
	private FFDecoder _ffDec = null;// FFMPEG������
	private JPEGDecoder _jpgDec = null;// ͼƬ������
	private VideoImage _img = null;// ��Ƶ��ʾ����
	private Bitmap _bmp = null;// ��ʾ����ͼƬ
	private int _decSelect = 1;// 0ʹ��H264AndroidDecoder��1ʹ��FFDecoder
	private VideoEncodeCfg _cfg = null;// ��Ƶ�������
	private boolean _inited = false;// ��ʼ��״̬
	private boolean _working = false;// ����״̬
	private boolean _isPlay = true;// �Ƿ񲥷���Ƶ
	private Object _asynObj = new Object();
	private Queue<MediaFrame> _qPlay = new Queue<MediaFrame>();// ��Ƶ�����ж�
	public boolean KeyFrameMode = false;// �Ƿ�ֻ���ŵ���֡
	public Thread _playThread = null;// �����߳�

	public VideoPlay(VideoImage img) {
		this._img = img;
	}

	private void Init(MediaFrame mf) {
		if (mf.nIsKeyFrame != 1)
			return;
		_cfg = VideoEncodeCfg.Create(mf);
		if (_decSelect == 0) {
			_h264Dec = new H264AndroidDecoder();
		} else {
			// ���ݱ���������ѡ�������
			if (_cfg.encodeName.equalsIgnoreCase("h264"))
				_ffDec = new FFDecoder(FFCode.CODEC_ID_H264);
			else if (_cfg.encodeName.equalsIgnoreCase("h263"))
				_ffDec = new FFDecoder(FFCode.CODEC_ID_H263);
			else if (_cfg.encodeName.equalsIgnoreCase("flv1"))
				_ffDec = new FFDecoder(FFCode.CODEC_ID_FLV1);
			else if (_cfg.encodeName.equalsIgnoreCase("JPEG"))
				_jpgDec = new JPEGDecoder();

			else {
				CLLog.Error("ý������������ʹ���");
				throw RuntimeExceptionEx.Create("ý������������ʹ���");
			}
		}
		_inited = true;
	}

	// ������Ƶ
	private VideoDisplayFrame Decode(MediaFrame mf) {
		VideoDisplayFrame vdFrame = null;
		if (KeyFrameMode && mf.nIsKeyFrame == 0)
			return null;
		try {
			if (!_inited)
				Init(mf);
			if (!_inited)
				return null;
			if (_jpgDec != null) {
				vdFrame = _jpgDec.Deocde(mf);
			} else {
				if (_decSelect == 0 && _h264Dec != null) {
					vdFrame = _h264Dec.Deocde(mf);
				} else if (_decSelect == 1 && _ffDec != null) {
					vdFrame = _ffDec.Deocde(mf);
				}
			}
		} catch (Exception e) {
			CLLog.Error("�������", e);
			String stackString = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create("�������", e);
		}
		return vdFrame;
	}

	// ����
	public void Play(MediaFrame mf) {
		_qPlay.offer(mf);
		synchronized (_asynObj) {
			_asynObj.notify();
		}
	}

	// �����߳�
	public void PlayThread() {
		while (_working) {
			if (_qPlay.size() > 0) {
				MediaFrame mf = null;
				if (_qPlay.size() > 60) {
					while (_qPlay.size() != 20)
						_qPlay.poll();

					while (_qPlay.size() > 0) {
						mf = _qPlay.poll();
						if (mf != null && mf.nIsKeyFrame == 1) {
							break;
						}
					}
				} else {
					mf = _qPlay.poll();
				}

				if (mf == null)
					continue;
				VideoDisplayFrame vdFrame = Decode(mf);
				if (_isPlay && vdFrame != null)
					_img.Play(vdFrame);
			} else {
				try {
					synchronized (_asynObj) {
						_asynObj.wait();
					}
				} catch (Exception e) {
					if (_working) {
						String stack = RuntimeExceptionEx.GetStackTraceString(e);
						throw RuntimeExceptionEx.Create(e);
					}
				}
			}
		}
	}

	public void Start() {
		if (_working)
			return;
		_working = true;
		_playThread = ThreadEx.GetThreadHandle(new CallBack(this, "PlayThread"), "��Ƶ�����߳�");
		_playThread.start();
	}

	public void Stop() {
		if (!_working)
			return;
		_working = false;
		ThreadEx.stop(_playThread);
		_qPlay.clear();
		Clean();
	}

	@Override
	public void Dispose() {
		try {
			Stop();
			if (_h264Dec != null)
				_h264Dec.Dispose();
			if (_ffDec != null)
				_ffDec.Dispose();
			if (_jpgDec != null)
				_jpgDec.Dispose();
		} catch (Throwable e) {
			String stackString = RuntimeExceptionEx.GetStackTraceString(e);
			RuntimeExceptionEx.PrintException(e);
		}
	}

	public void Clean() {
		if (_img != null)
			_img.Clean();
	}

	public void PlaySwitch(boolean status) {
		_isPlay = status;
	}

	public void SetScaleMode(ScaleMode mode) {
		_img.SetScaleMode(mode);

	}

	public ScaleMode GetScaleMode() {
		return _img.Scale;

	}
}
