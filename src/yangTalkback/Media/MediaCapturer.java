package yangTalkback.Media;

import yangTalkback.App.AppConfig;
import yangTalkback.Codec.CameraEncoder;
import yangTalkback.Codec.CameraEncoderJPEG;
import yangTalkback.Codec.MicEncoder;
import yangTalkback.Codec.Cfg.AudioEncodeCfg;
import yangTalkback.Codec.Cfg.VideoEncodeCfg;

import android.view.Surface;

import AXLib.Utility.Console;
import AXLib.Utility.Event;
import AXLib.Utility.EventArg;
import AXLib.Utility.IAction;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.Ex.StringEx;

//ý��ɼ���
public class MediaCapturer implements CameraEncoder.CameraEncoderDataReceiver, MicEncoder.MicEncoderDataReceiver {

	private CameraEncoder _ce;// ����ͷ�ɼ�
	private MicEncoder _me;// ��˷�ɼ�
	private boolean _isWorking = false;// ����״̬
	private boolean _firstAudioFristFrame = true;
	private boolean _firstVideoFristFrame = true;
	private VideoEncodeCfg _vcfg = null;// ��Ƶ����
	private AudioEncodeCfg _acfg = null;// ��Ƶ����
	IAction<MediaFrame> _captured = null;// �ɼ��ص�
	public boolean IsVideoPub = true;// �Ƿ�ص���Ƶ
	public boolean IsAudioPub = true;// �Ƿ�ص���Ƶ
	public final Event<Exception> Error = new Event<Exception>();// ����������
	public IAction<MediaCapturer> OnStoped;// ����ȫֹͣ����

	public MediaCapturer(VideoEncodeCfg vCfg, AudioEncodeCfg aCfg, IAction<MediaFrame> captured) {
		_vcfg = vCfg;
		_acfg = aCfg;
		_captured = captured;
		_ce = new CameraEncoder(vCfg, AppConfig.Instance.VideoCaptrueRestartMinutes, this);
		if (StringEx.equalsIgnoreCase(vCfg.encodeName, "JPEG"))
			_ce = new CameraEncoderJPEG(vCfg, AppConfig.Instance.VideoCaptrueRestartMinutes, this);
		else
			_ce = new CameraEncoder(vCfg, AppConfig.Instance.VideoCaptrueRestartMinutes, this);
		_me = new MicEncoder(aCfg, this);
		_ce.Error.add(this, "NS_Error");

	}

	public void Start() throws Exception {

		if (_isWorking)
			return;
		_isWorking = true;
		if (IsVideoPub)
			_ce.start();
		if (IsAudioPub)
			_me.start();
	}

	public void Stop() {
		if (!_isWorking)
			return;
		_isWorking = false;
		try {
			Console.d("STOP", "��ʼֹͣ��Ƶ�ɼ�");
			_me.stop();
			Console.d("STOP", "���ֹͣ��Ƶ�ɼ�");
			Console.d("STOP", "��ʼֹͣ��Ƶ�ɼ�");
			_ce.stop();
			Console.d("STOP", "���ֹͣ��Ƶ�ɼ�");
			if (OnStoped != null)
				OnStoped.invoke(this);
		} catch (Exception e) {
			RuntimeExceptionEx.PrintException(e);
		}
	}

	public void AutoFocus() {
		if (_ce != null)
			_ce.AutoFocus();
	}

	@Override
	public void Received(MediaFrame frame) {

		if (_captured != null) {
			if (frame.nIsAudio == 1 && IsAudioPub) {
				if (_firstAudioFristFrame && !frame.IsCommandFrame()) {
					frame.nEx = 0;
					_firstAudioFristFrame = false;
				}
				_captured.invoke(frame);
			}
			if (frame.nIsAudio == 0 && IsVideoPub) {
				if (_firstVideoFristFrame && !frame.IsCommandFrame()) {
					frame.nEx = 0;
					_firstVideoFristFrame = false;
				}
				_captured.invoke(frame);
			}
		}

	}

	public void NS_Error(EventArg<Exception> arg) throws Exception {
		Stop();
		Error.Trigger(this, arg.e);
	}

	public void SetAudioSyncKey(String key) {
		if (_me != null)
			_me.SetAudioSyncKey(key);
		else {
			throw RuntimeExceptionEx.Create("MicEncoderδʵ����");
		}
	}

	public void VideoSwitch(boolean status) {
		this.IsVideoPub = status;

	}

	public void AudioSwitch(boolean status) {
		this.IsAudioPub = status;

	}

	public boolean GetIsWorking() {
		return _isWorking;
	}

	public void PausePreview() {
		if (_ce != null) {
			_ce.Error.remove(this, "NS_Error");
			_ce.stop();
			_ce = null;
		}
	}

	public void RestartPreview(Surface surface) {
		if (_ce != null) {
			_ce.Error.remove(this, "NS_Error");
			_ce.stop();
			_ce = null;
		}
		_vcfg.surface = surface;
		_ce = new CameraEncoder(_vcfg, AppConfig.Instance.VideoCaptrueRestartMinutes, this);
		if (_isWorking)
			_ce.start();
	}

	public void ChangeCfg(VideoEncodeCfg vCfg, AudioEncodeCfg aCfg) {
		_vcfg = vCfg;
		_acfg = aCfg;
		RestartPreview(vCfg.surface);
	}
}
