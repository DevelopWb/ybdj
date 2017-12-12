package yangTalkback.Media;

import AXLib.Utility.CallBack;
import AXLib.Utility.IDisposable;
import AXLib.Utility.Queue;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
import yangTalkback.App.App;
import yangTalkback.Codec.SpeexDecode;
import yangTalkback.Codec.SpeexEchoAC;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

//����������
public class AudioPlay implements IDisposable {
	private AudioTrack track = null;// �������Ŷ���
	private SpeexDecode decode = null;// ������
	private int _speakMode = 0;// �Խ�ģʽ
	private int chl = -1;// ͨ��
	private int fmt = -1;// λԪ
	private int freq = -1;// ����
	private int minBufferSize = -1;// �����С
	private float volume = 1f;// ����
	protected boolean _working = false;// ��ǰ�����Ƿ����ڹ���
	protected boolean playing = false;// �Ƿ����ڲ���
	private boolean _isPlay = true;// ������������
	private Thread playThread = null;// �����߳�
	protected Queue<MediaFrame> qFrames = new Queue<MediaFrame>();// ����֡����
	protected Queue<short[]> qDecQueue = new Queue<short[]>();// ������Ƶ����
	private String _playSyncKey = null;// ��������ͬ�߱�ʶ,û���õ�
	private AudioManager _am = null;// ��Ƶ���Ź�����
	private boolean _isReal = false;// �Ƿ�ʵʱ
	private boolean _isFastPlay = false;// �Ƿ���ٲ��ţ���ֵ�����ڵ������粨��ʱ��ʱ����

	/*
	 * mode 0����ģʽ��1����ģʽ
	 */
	public AudioPlay(int speakMode, boolean isReal) {
		_speakMode = speakMode;
		_isReal = isReal;
	}

	public void Play(MediaFrame mf) {
		if (!_working)
			return;
		if (mf.nIsAudio != 1)
			throw RuntimeExceptionEx.Create("��Ƶ֡����");

		int mode = _speakMode == 0 ? AudioManager.STREAM_MUSIC : (_isReal ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC);
		if (track == null) {
			freq = 8000;// ����
			chl = AudioFormat.CHANNEL_OUT_MONO;// ͨ����
			fmt = AudioFormat.ENCODING_PCM_16BIT;// λ��
			minBufferSize = AudioTrack.getMinBufferSize(freq, chl, fmt);
			track = new AudioTrack(mode, freq, chl, fmt, minBufferSize, AudioTrack.MODE_STREAM);

			track.setPlaybackRate(freq);
			decode = new SpeexDecode();
			playing = true;
			playThread = ThreadEx.GetThreadHandle(new CallBack(this, "PlayThread"));
			playThread.start();
			track.play();
			// track.setStereoVolume(volume, volume);// ���õ�ǰ������С

			// �����仰�������Ǵ��豸������
			_am = (AudioManager) App.FirstAct.getSystemService(Context.AUDIO_SERVICE);
			// _am.setMode(AudioManager.ROUTE_SPEAKER);

			if (_speakMode == 0)
				_am.setSpeakerphoneOn(true);
			else
				_am.setSpeakerphoneOn(false);

			if (_isReal && _speakMode != 0) {
				int v1 = _am.getStreamVolume(mode);
				int v2 = _am.getStreamMaxVolume(mode);
				_am.setStreamVolume(mode, 4, mode);
			}

		}
		if (track != null) {
			short[] data = decode.Deocde(mf);
			qDecQueue.add(data);
		}
	}

	public void PlayThread() throws Exception {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
		while (_working && playing && playThread != null) {
			int size = qDecQueue.size();
			if (_isReal) {
				if (size > 500) {
					short[] data = qDecQueue.remove();
					continue;
				} else if (size > 200 && !_isFastPlay) {
					_isFastPlay = true;
					track.setPlaybackRate(freq + freq / 5);
				} else if (size > 100 && !_isFastPlay) {
					_isFastPlay = true;
					track.setPlaybackRate(freq + freq / 10);
				}
			}
			if (size > 0) {

				short[] data = qDecQueue.remove();
				if (_isPlay) {
					if (_playSyncKey != null)
						SpeexEchoAC.Play(_playSyncKey, track, data, data.length);
					else
						track.write(data, 0, data.length);

					int v = _am.getStreamVolume(AudioManager.STREAM_MUSIC);
					// track.setStereoVolume(v, v);// ���õ�ǰ������С
				}
			} else {
				_isFastPlay = false;
				track.setPlaybackRate(freq);
				ThreadEx.sleep(10);
			}
		}
		playThread = null;
		playing = false;
	}

	public void Start() {
		if (_working)
			return;
		_working = true;

	}

	public void Stop() {
		if (!_working)
			return;
		_working = false;
		try {
			if (playThread != null) {
				ThreadEx.waitStop(playThread, 50);
				playThread = null;
			}
			playing = false;
			qFrames.clear();
			qDecQueue.clear();
			if (track != null)
				track.stop();

		} catch (Exception e) {
			RuntimeExceptionEx.PrintException(e);
		}
	}

	public void SetAudioSyncKey(String key) {
		_playSyncKey = key;
	}

	public void PlaySwitch(boolean status) {
		_isPlay = status;
	}

	@Override
	public void Dispose() {
		Stop();
		qDecQueue.clear();
		qFrames.clear();
		if (track != null)
			track.release();
		if (decode != null)
			decode.Dispose();
	}

}