package yangTalkback.Protocol;

import java.io.*;

import yangTalkback.*;
import yangTalkback.App.AppConfig;
import yangTalkback.Comm.*;

import AXLib.Utility.*;

public class StreamParser {
	final int MAX_MEDIAFRAME_PARK = 320 * 4;// ���ְ�
	static java.util.HashMap<MessageType, java.lang.Class> _dicType = new java.util.HashMap<MessageType, java.lang.Class>();

	private StreamSocket _stream = null;// ���������շ���
	private StreamSocket _reader = null;// ���������շ���
	private StreamSocket _writer = null;// ���������շ���
	private boolean isAllowDiscard = false;//�Ƿ�������
	private boolean _isPartMode = true;// �ְ�ģʽ����ģʽֻ����Ƶ����ģʽ��������
	private int _sendMode = 0;// 0��Ƶ���ȣ�1˳����
	private boolean _isWorking = false;
	private Thread _analyzeThread = null;
	private Thread _readThread = null;
	private Thread _sendThread = null;
	private Packet _lastVideoPacketPartObj = null;
	private AQueue<PBMedia> _qVideoPacketPark = new AQueue<PBMedia>();// ý����ֶζ���
	private AQueue<Packet> _qMsg = new AQueue<Packet>();// ͨ����Ϣ����
	private AQueue<Packet> _qVideo = new AQueue<Packet>();// ���͵���Ƶ����,ֻ����Ƶ����ģʽ��ʹ��
	private AQueue<Packet> _qAudio = new AQueue<Packet>();// ���͵���Ƶ����,ֻ����Ƶ����ģʽ��ʹ��
	private AQueue<Packet> _qMedia = new AQueue<Packet>();// ���͵���Ƶ����,ֻ��˳����ģʽ��ʹ��
	private AQueue<byte[]> _qReceive = new AQueue<byte[]>();// ���ն���
	private WaitResult<Object> _sendSemaphore = new WaitResult<Object>();
	private Object _syncReceive = new Object();
	private Object _syncSend = new Object();
	public final Event<Packet> Readed = new Event<Packet>();// ��ȡһ�������İ��������¼�
	public final Event<Exception> Error = new Event<Exception>();// �����쳣ʱ�������¼�

	static {
		// �������ͽ����Ӧ��PBBody����һ���PBBodyΪJSON���л����ַ���

		_dicType.put(MessageType.Login_C, PBLoginC.class);
		_dicType.put(MessageType.Login_R, PBLoginR.class);
		_dicType.put(MessageType.Logout_C, PBLogoutC.class);
		_dicType.put(MessageType.Logout_R, PBLogoutR.class);
		_dicType.put(MessageType.Heart_C, PBHeart.class);
		_dicType.put(MessageType.Heart_R, PBHeart.class);
		_dicType.put(MessageType.Call_C, PBCallC.class);
		_dicType.put(MessageType.Call_R, PBCallR.class);
		_dicType.put(MessageType.AllID_C, PBAllIDC.class);
		_dicType.put(MessageType.AllID_R, PBAllIDR.class);
		_dicType.put(MessageType.CallClosureC, PBCallClosureC.class);
		_dicType.put(MessageType.CallClosureR, PBCallClosureR.class);
		_dicType.put(MessageType.Media, PBCallR.class);
		_dicType.put(MessageType.MonitorOpen_C, PBMonitorOpenC.class);
		_dicType.put(MessageType.MonitorOpen_R, PBMonitorOpenR.class);
		_dicType.put(MessageType.MonitorClose_C, PBMonitorCloseC.class);
		_dicType.put(MessageType.MonitorClose_R, PBMonitorCloseR.class);
		_dicType.put(MessageType.Cmd_C, PBCmdC.class);
		_dicType.put(MessageType.Cmd_M, PBCmdM.class);
		_dicType.put(MessageType.Cmd_R, PBCmdR.class);
	}

	public StreamParser(StreamSocket stream) {
		_stream = stream;
		_reader = _stream;
		_writer = _stream;
	}

	public final void Start() {
		synchronized (this) {
			if (_isWorking) {
				return;
			}
			_isWorking = true;

			_readThread = new Thread(ThreadEx.GetThreadHandle(new CallBack(this, "ReadThread"), "�������ݽ����߳�"));
			_sendThread = new Thread(ThreadEx.GetThreadHandle(new CallBack(this, "SendThread"), "�������ݷ����߳�"));
			_analyzeThread = new Thread(ThreadEx.GetThreadHandle(new CallBack(this, "AnalyzeThread"), "�������ݽ����߳�"));
			_readThread.start();
			_sendThread.start();
			_analyzeThread.start();
		}
	}

	public final void Stop() {
		synchronized (this) {
			if (!_isWorking) {
				return;
			}
			_isWorking = false;
			ThreadEx.stop(_readThread);
			ThreadEx.stop(_sendThread);
			ThreadEx.stop(_analyzeThread);

			_readThread = null;
			_sendThread = null;
			_analyzeThread = null;
		}

	}

	// �������ݽ����߳�
	public void ReadThread() {
		while (_isWorking) {
			try {
				int len = _reader.readInt();
				byte[] buf = _reader.readFully(len);
				_qReceive.Enqueue(buf);
				synchronized (_syncReceive) {
					_syncReceive.notify();
				}
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				OnError(e);
				break;
			}
		}
	}

	// ���ݽ����߳�
	public void AnalyzeThread() {
		while (_isWorking) {
			if (_qReceive.size() > 0) {
				byte[] buf = _qReceive.Dequeue();
				if (buf == null)
					continue;
				Packet pack = ReadPacket(buf);
				// if (pack.MsgType == MessageType.Media)
				// _DebugEx.Trace("StreamParser", string.Format("RECE:{0}",
				// pack.ToString()));
				if (pack.MsgType != MessageType.Media)
					_DebugEx.Trace("StreamParser", String.format("RECE:%1$s", pack.toString()));
				this.Readed.Trigger(this, pack);
			} else {
				try {
					synchronized (_syncReceive) {
						_syncReceive.wait();
					}
				} catch (Exception e) {
					if (_isWorking) {
						String stack = RuntimeExceptionEx.GetStackTraceString(e);
						CLLog.Error(e);
						OnError(e);
						break;
					}
				}
			}
		}
	}

	public void SendThread() {
		while (_isWorking) {
			Packet pack = GetNextSendPack();
			if (pack != null) // �������Ϊ������
			{
				byte[] buf = pack.GetBytes();
				try {
					// _DebugEx.Trace("StreamParser", string.Format("����һ��{0}��",
					// pack.MsgType == MessageType.Media ? "ý��" : "����"));

					_writer.writeInt(buf.length);
					_writer.write(buf);
					_writer.flush();
				} catch (Exception e) {
					String stack = RuntimeExceptionEx.GetStackTraceString(e);
					OnError(e);
					break;
				}
			} else {
				try {
					synchronized (_syncSend) {
						_syncSend.wait();
					}
				} catch (Exception e) {
					if (_isWorking) {
						String stack = RuntimeExceptionEx.GetStackTraceString(e);
						CLLog.Error(e);
						OnError(e);
						break;
					}
				}
			}
		}
	}

	private Packet GetNextSendPack() {
		Packet pack = GetNextSendPack_Msg();
		if (pack != null)
			return pack;

		pack = GetNextSendPack_Media();
		return pack;
	}

	private Packet GetNextSendPack_Msg() {
		Packet pack = null;
		if (_qMsg.size() > 0)// ���ȷ�����Ϣ��
			pack = _qMsg.Dequeue();
		return pack;
	}

	private Packet GetNextSendPack_Media() {
		if (_sendMode == 0)// ��Ƶ����
			return GetNextSendPack_Media_AudioPriority();
		else if (_sendMode == 1)
			return GetNextSendPack_Media_Sequence();
		else
			throw new RuntimeExceptionEx("not imp");
	}

	// ˳����
	private Packet GetNextSendPack_Media_Sequence() {
		if (_qMedia.size() > 0)
			return _qMedia.Dequeue();
		return null;

	}

	// ��Ƶ����ģʽ
	private Packet GetNextSendPack_Media_AudioPriority() {
		Packet pack = GetNextSendPack_Media_AudioPriority_Audio();
		if (pack != null)
			return pack;
		pack = GetNextSendPack_Media_AudioPriority_Video();
		return pack;
	}

	private Packet GetNextSendPack_Media_AudioPriority_Audio() {
		Packet pack = null;
		AQueue<Packet> queue = _qAudio;
		if (queue.size() > 0)// �ڶ����ȷ�����Ƶ��
		{
			pack = _qAudio.Dequeue();
			if (((PBMedia) pack.Body).Frame.IsAllowDiscard()) {
				// ������������������ﵽ���ֵ�ѷ��͵�һ����Ƶ�������
				if (_qAudio.size() > Global.Instance.AudioSendQueueMax && isAllowDiscard) {
					_DebugEx.Trace("StreamParser", "��Ƶ�����������������Ƶ��");
					pack = queue.Dequeue();
					while (queue.size() > 0) {
						pack = queue.Dequeue();
						if (((PBMedia) pack.Body).Frame.IsAllowDiscard())// �ж��Ƿ�Ϊ����֡
							break;
					}
				}
			}
		}
		return pack;
	}

	private Packet GetNextSendPack_Media_AudioPriority_Video() {
		Packet pack = null;
		// �Ƿ�ʹ�÷ְ�ģʽ
		if (_isPartMode) {
			// ��ȡ�ְ�
			pack = GetNextSendPack_Media_AudioPriority_Video_Part();
			if (pack != null)
				return pack;
		}
		AQueue<Packet> queue = _qVideo;

		if (queue.size() > 0) {

			// ����Ƶ������������ﵽ���ֵ���ѷ��͵�һ����Ƶ�����������һ���ؼ���
			if (_qVideo.size() > Global.Instance.VideoSendQueueMax) {
				pack = queue.Dequeue();
				if (((PBMedia) pack.Body).Frame.IsAllowDiscard()) {
					Packet[] ps = new Packet[queue.size()];
					queue.toArray(ps);
					for (int i = ps.length - 1; i >= 0; i--) {
						if (ps[i] != null && ps[i].Body != null) {
							PBMedia pbmedia = ((PBMedia) ps[i].Body);
							if (pbmedia != null && pbmedia.Frame.nIsKeyFrame == 1) // �ж��Ƿ�Ϊ�ؼ���
							{
								pack = ps[i];
								// ������е����һ���ؼ���
								while (queue.size() > 0 && pack != queue.Dequeue())
									;
								break;
							}
						} else {
							if (AppConfig._D)
								throw new RuntimeExceptionEx("frame is null");
						}
					}
				}
			} else {
				pack = queue.Dequeue();
			}
			if (_isPartMode) {
				// �ְ�����
				_lastVideoPacketPartObj = pack;
				ListEx<PBMedia> list = GetPBMediaParks((PBMedia) pack.Body);
				_qVideoPacketPark = new AQueue<PBMedia>(list);

				// ��ȡ�ְ����з���
				pack = GetNextSendPack_Media_AudioPriority_Video_Part();
			}
		}
		return pack;
	}

	private Packet GetNextSendPack_Media_AudioPriority_Video_Part() {
		Packet pack = _lastVideoPacketPartObj;
		if (pack != null) {
			// �ְ����������Ƿ��зְ�
			if (_qVideoPacketPark.size() > 0) {
				pack.Body = _qVideoPacketPark.Dequeue();
				if (pack.Body == null)
					throw new RuntimeExceptionEx("");
			}
			// ������зְ����Ѿ��������������ò���
			if (_qVideoPacketPark.size() == 0) {
				_lastVideoPacketPartObj = null;
				_qVideoPacketPark = null;
			}

		}
		return pack;

	}

	// ��һ��ý��֡�����
	private ListEx<PBMedia> GetPBMediaParks(PBMedia pb) {
		byte[] frameBuf = pb.Frame.GetBytes();
		if (frameBuf.length <= MAX_MEDIAFRAME_PARK) {
			ListEx<PBMedia> listEx = new ListEx<PBMedia>();
			listEx.add(pb);
			return listEx;
		}
		ByteArrayInputStream ms = new ByteArrayInputStream(frameBuf);
		LittleEndianDataInputStream br = new LittleEndianDataInputStream(ms);
		ListEx<PBMedia> list = new ListEx<PBMedia>();

		while (ms.available() > 0) {
			byte[] buf = null;
			try {
				if (ms.available() >= MAX_MEDIAFRAME_PARK)
					buf = br.readFully(MAX_MEDIAFRAME_PARK);
				else
					buf = br.readFully(ms.available());
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				throw RuntimeExceptionEx.Create(e);
			}

			PBMedia item = new PBMedia();

			item.To = pb.To;
			item.From = pb.From;
			item.Message = pb.Message;
			item.Result = pb.Result;
			item.PartData = buf;

			if (list.size() == 0)
				item.Part = PBMediaPart.First;
			else if (ms.available() == 0)
				item.Part = PBMediaPart.End;
			else
				item.Part = PBMediaPart.Mid;
			list.add(item);
		}
		return list;

	}

	// ������
	protected Packet ReadPacket(byte[] packBuffer) {
		Packet pack = null;
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(packBuffer);
			LittleEndianDataInputStream _reader = new LittleEndianDataInputStream(stream);
			pack = new Packet();
			if (pack.HeadFlag != _reader.readByte())
				throw RuntimeExceptionEx.Create("HeadFlag����");
			pack.MsgType = MessageType.forValue(_reader.readByte());
			pack.From = _reader.readShort();
			pack.To = _reader.readShort();
			int bodyBufLen = _reader.readInt();

			byte[] buf = _reader.readFully(bodyBufLen);
			if (pack.EndFlag != _reader.readByte())
				throw RuntimeExceptionEx.Create("EndFlag����");
			pack.Body = ReadPacketBody(pack.MsgType, buf);

		} catch (IOException e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}
		return pack;
	}

	// ������
	protected PBodyBase ReadPacketBody(MessageType msgType, byte[] buf) {

		if (msgType != MessageType.Media) {
			String txt = BitConverter.ToString(buf, 0, buf.length);
			Object obj = JSONHelper.forJSON(txt, _dicType.get(msgType));
			return (PBodyBase) obj;
		} else {
			return new PBMedia(buf);
		}
	}

	// ���Ͱ�,�ŵ���Ӧ�Ķ��еȴ�����
	public final void SendPack(Packet pack) {

		if (pack.MsgType == MessageType.Media) {
			// _DebugEx.Trace("StreamParser", string.Format("SEND:{0}",
			// pack.ToString()));
			if (_sendMode == 0) {// ��Ƶ����
				PBMedia body = (PBMedia) ((pack.Body instanceof PBMedia) ? pack.Body : null);
				if (body != null && body.Frame != null) {
					// ���������ͷŵ���Ӧ�Ķ���
					if (body.Frame.nIsAudio == 1) {
						_qAudio.Enqueue(pack);
					} else {
						_qVideo.Enqueue(pack);
					}
				}
			} else if (_sendMode == 1) {// ˳����
				_qMedia.Enqueue(pack);
			} else {
				throw RuntimeExceptionEx.Create("");
			}
		} else {
			_DebugEx.Trace("StreamParser", String.format("SEND:%1$s", pack.toString()));
			_qMsg.Enqueue(pack);

		}
		synchronized (_syncSend) {
			_syncSend.notify();
		}
	}

	private void OnError(Exception e) {
		Error.Trigger(this, e);

	}
}