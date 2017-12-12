package yangTalkback.Base;

import java.io.Serializable;

import AXLib.Model.KeyValue;
import AXLib.Utility.CallBack;
import AXLib.Utility.Event.EventReceiver;
import AXLib.Utility.EventArg;
import AXLib.Utility.IAction;
import AXLib.Utility.ICallback;
import AXLib.Utility.ListEx;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.TH;
import AXLib.Utility.THPrint;
import AXLib.Utility.ThreadEx;

import yangTalkback.App.App;
import yangTalkback.Base.Prompt.AlertDialogManage;
import yangTalkback.Base.Prompt.PromptButton;
import yangTalkback.Comm.*;

import android.annotation.SuppressLint;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class ActBase extends ActivityGroup implements Serializable {
	static Thread _uiThread = null;
	static THPrint th_pring = new THPrint();
	static ThreadExceptionHandler _threadExceptionHandler = new ThreadExceptionHandler();
	static {
		TH.getThrowEvent().add(th_pring, "Print");
		TH.getThrowEvent().add(new EventReceiver<Throwable>(new IAction<EventArg<Throwable>>() {

			@Override
			public void invoke(EventArg<Throwable> obj) {
				CLLog.Error("δ�����쳣", obj.e);
			}
		}));
		Thread.setDefaultUncaughtExceptionHandler(_threadExceptionHandler);
	}
	private static Handler _handlerCallback = null;// UIͬ���ص�
	private static Object _lockHandler = new Object();// uiͬ����
	protected Loading _loading = null;// ���ش������
	protected boolean _isLoading = false;// ��ǰ�Ƿ����ڼ�����
	protected ActBase _parentAct;// ��ǰ����ͼ����
	protected ActBase _curAct = this;// ��ǰ��ͼ����
	public boolean IsFinished = false;// ��ǰ��ͼ�Ƿ��Ѿ�����
	public boolean IsSubActivity = false;// ��ǰ��ͼ�Ƿ�Ϊ����ͼ

	protected void onManage() {
		_loading = new Loading(this);
		if (_handlerCallback == null) {
			_handlerCallback = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					if (msg.obj != null && msg.obj instanceof Runnable) {
						((Runnable) msg.obj).run();
					}
				}
			};
		}
		Object obj = GetActivityExtraValue("_parentActivity");
		if (obj == null)
			App.PushAct(this);
		else
			setParentActivity((ActBase) obj);

	}

	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		onManage();
		super.onCreate(savedInstanceState);
		// App appState = (App) this.getApplication();
		// appState.addActivity(this);
	}

	// ��ȡ����ͼ
	public View getSubActivityView(ActBase act, Class<?> cls, ListEx<KeyValue<String, Serializable>> extras) {
		Intent intent = new Intent(act, cls);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("_parentActivity", this);
		if (extras != null) {
			for (KeyValue<String, Serializable> item : extras) {
				intent.putExtra(item.Key, item.Value);
			}
		}
		Window widow = this.getLocalActivityManager().startActivity(cls.toString(), intent);
		View view = widow.getDecorView();
		return view;
	}

	// ��ȡ����ͼ
	public View getSubActivityView(Class<?> cls) {
		return getSubActivityView(this, cls, null);
	}

	// ���õ�ǰ��ͼ�ĸ���ͼ
	public void setParentActivity(ActBase act) {
		_parentAct = act;
		IsSubActivity = true;
	}

	// ��ȡ��ǰ����ͼ�Ķ�����ͼ
	public ActBase getTopAct() {
		if (this.IsSubActivity && _parentAct != null)
			return _parentAct.getTopAct();
		else
			return this;
	}

	// ͬ����UI�̵߳���
	public void post(final ICallback cb) {
		post(new Runnable() {
			@Override
			public void run() {
				cb.invoke();
			}
		});
	}

	// ͬ����UI�̵߳���
	public void post(Runnable run) {
		if (IsUIThread()) {
			run.run();
		} else {
			synchronized (_lockHandler) {
				Message msg = new Message();
				msg.obj = run;
				_handlerCallback.sendMessage(msg);
			}
		}
	}

	// ��ȡ��һ��ͼ���ݵĲ���
	@SuppressWarnings("unchecked")
	public <T> T GetActivityDefaultExtraValue(boolean noExistAlert) {
		Object object = GetActivityDefaultExtraValue();
		if (object != null)
			return (T) object;
		else {
			if (noExistAlert)
				AlertAndOut("��������");
			return null;
		}
	}

	// ��ȡ��һ��ͼ���ݵĲ���Ĭ��ֵ
	public Object GetActivityDefaultExtraValue() {
		return GetActivityExtraValue("_default");
	}

	// ��ȡ��һ��ͼ���ݵĲ���
	public Object GetActivityExtraValue(String key) {
		Intent intent = getIntent();
		if (intent == null)
			return null;
		Bundle extras = intent.getExtras();
		if (extras == null)
			return null;

		Object object = extras.get(key);
		return object;
	}

	// �л�����ͼ
	@Override
	public void startActivity(final Intent intent) {
		if (IsUIThread()) {
			if (this.IsSubActivity) {
				((ActBase) this.getParent()).startActivity(intent);
			} else {
				if (_isLoading)
					CloseLoading();
				super.startActivityForResult(intent, 0x08);
			}
			// super.startActivity(intent);
		} else {
			post(new Runnable() {
				@Override
				public void run() {
					startActivity(intent);
				}
			});
		}
	}

	// �л�����ͼ
	public void startActivity(final Class<?> cls) {
		startActivity(this, cls);
	}

	// �л�����ͼ,�����ݲ���
	public void startActivity(final Class<?> cls, Serializable value) {
		Intent intent = new Intent(this, cls);
		intent.putExtra("_default", value);
		startActivity(intent);
	}

	// �л�����ͼ
	public void startActivity(final Context ctx, final Class<?> cls) {
		startActivity(new Intent(ctx, cls));

	}

	// �жϵ�ǰ�߳��Ƿ�ΪUI�߳�
	protected boolean IsUIThread() {
		Thread thread = Thread.currentThread();
		return App.UIThread == thread;
	}

	/*
	 * ��ʾ��Ϣ���˳���ǰAct
	 */
	public void AlertAndOut(final String msg) {
		if (!IsFinished) {
			this.Prompt(msg, new IAction<Prompt.PromptButton>() {
				@Override
				public void invoke(PromptButton obj) {
					finish();
				}
			});
		} else {
			this.Alert(msg, true);
		}
	}

	/*
	 * ��ʾ��Ϣ���˳�APP
	 */
	public void AlertAndExit(final String msg) {

		this.Prompt(msg, new IAction<Prompt.PromptButton>() {
			@Override
			public void invoke(PromptButton obj) {
				App.exit();
			}
		});
	}

	/*
	 * �����Ի���
	 */
	public void Alert(final String msg) {
		Alert(msg, "��ʾ��Ϣ");
	}

	// ������ʾ�Ի���
	public void Alert(final String msg, boolean closeLoading) {
		if (closeLoading)
			CloseLoading();
		Alert(msg, "��ʾ��Ϣ");
	}

	// ������ʾ�Ի���
	public void Alert(Throwable e) {
		RuntimeExceptionEx re = null;
		Throwable te = e;
		ListEx<RuntimeExceptionEx> list = new ListEx<RuntimeExceptionEx>();
		while (e != null) {
			if (e instanceof RuntimeExceptionEx && ((RuntimeExceptionEx) e).Message != null) {
				list.add((RuntimeExceptionEx) e);
				break;
			} else {
				e = e.getCause();
			}
		}
		if (list.size() > 0)
			Alert(list.get(0).Message);
		else {
			Alert(te.getMessage());
		}
	}

	/*
	 * �����Ի���
	 */
	public void Alert(final String msg, final String title) {
		if (this.IsSubActivity) {
			this.getTopAct().Alert(msg, title);
			return;
		}
		final AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(title).setMessage(msg).setPositiveButton(" ȷ �� ", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		post(new Runnable() {
			@Override
			public void run() {
				if (!IsFinished)
					builder.show();
				else {
					App.LastAct.Alert(msg, title);
				}

			}
		});
	}

	// ����ʾ�Ի���
	public void Prompt(String msg, final IAction<PromptButton> onClick) {
		if (this.IsSubActivity) {
			this.getTopAct().Prompt(msg, onClick);
			return;
		}
		Prompt.Open(this, msg, PromptButton.YES, onClick);
	}

	// ����ʾ�Ի���,onClickΪ���°�ť��Ļص�
	public AlertDialogManage Prompt(String msg, PromptButton btn, final IAction<PromptButton> onClick) {
		if (this.IsSubActivity) {
			return this.getTopAct().Prompt(msg, btn, onClick);
		}
		if (!IsFinished)
			return Prompt.Open(this, msg, btn, onClick);
		else
			return App.LastAct.Prompt(msg, btn, onClick);
	}

	// �ر���ʾ�Ի���
	public void ClosePrompt(AlertDialogManage manage) {
		if (this.IsSubActivity) {
			this.getTopAct().ClosePrompt(manage);
		}
		if (!IsFinished)
			Prompt.Close(manage);
		else
			App.LastAct.ClosePrompt(manage);
	}

	// ��ʾ��ʾ��Ϣ
	public void Notice(final String msg) {
		Notice(msg, Toast.LENGTH_SHORT);

	}

	// ��ʾ��ʾ��Ϣ,durationΪ��ʾʱ��
	public void Notice(final String msg, final int duration) {
		post(new Runnable() {
			@Override
			public void run() {
				if (!IsFinished)
					Toast.makeText(getBaseContext(), msg, duration).show();
			}
		});
	}

	// ��loading
	public void OpenLoading(String msg) {
		OpenLoading(msg, true, null);
	}

	// ��loading. msgΪ��ʾ��Ϣ,canCancelΪ�Ƿ����ȡ��,cancelCallBackΪȡ����ص��¼�
	public void OpenLoading(final String msg, final boolean canCancel, final ICallback cancelCallBack) {
		// �����ǰΪ����ͼ������丸��ͼ����loading
		if (this.IsSubActivity) {
			this.getTopAct().OpenLoading(msg, canCancel, cancelCallBack);
			return;
		}
		// ͬ����ui
		post(new Runnable() {
			@Override
			public void run() {
				_isLoading = true;
				if (!IsFinished)
					_loading.Open(msg, canCancel, cancelCallBack);
			}
		});
	}

	// �ر�loading
	public void CloseLoading() {
		// �����ǰΪ����ͼ���ڸ���ͼ�йر�
		if (this.IsSubActivity) {
			this.getTopAct().CloseLoading();
			return;
		}
		// ͬ����UI�߳�
		post(new Runnable() {
			@Override
			public void run() {
				_isLoading = false;
				if (!IsFinished)
					_loading.Close();
			}
		});
	}

	// ʹ�����̵߳��ô���ķ���
	public Thread CallByNewThread(String method) {
		Thread thread = ThreadEx.GetThreadHandle(new CallBack(this, method));
		thread.start();
		return thread;
	}

	// �����°��������
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean result = true;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			result = result && OnKeyDown_Back();
		}
		return result && super.onKeyDown(keyCode, event);
	}

	// �����º��˼� �����
	public boolean OnKeyDown_Back() {
		if (IsSubActivity)
			return false;
		return true;
	}

	// ��������ǰ��ͼ
	@Override
	public void finish() {
		CloseLoading();
		IsFinished = true;
		if (!IsSubActivity)
			App.popAct(this);
		super.finish();

	}
}
