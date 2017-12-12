package yangTalkback.Act;

import AXLib.Utility.Event;
import AXLib.Utility.RuntimeExceptionEx;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class HeadSetReceiver extends BroadcastReceiver {

	public final static Event<Integer> ReceivedEvent = new Event<Integer>();

	// ��д���췽�������ӿڰ󶨡���Ϊ����ĳ�ʼ���������ԡ�
	public HeadSetReceiver() {

	}

	@Override
	public void onReceive(Context context, Intent intent) {

		String intentAction = intent.getAction();
		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			// ���KeyEvent����
			KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

			try {
				if (keyEvent.getAction() == KeyEvent.ACTION_UP || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					ReceivedEvent.Trigger(this, keyEvent.getAction());
					// ��ֹ�㲥(���ñ�ĳ����յ��˹㲥�����ܸ���)
					//abortBroadcast();
				}
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				throw RuntimeExceptionEx.Create(e);

			}

		}

	}
}