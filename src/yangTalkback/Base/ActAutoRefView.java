package yangTalkback.Base;

import java.lang.reflect.Field;

import AXLib.Utility.Event;
import AXLib.Utility.EventArg;
import AXLib.Utility.ListEx;
import AXLib.Utility.RuntimeExceptionEx;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class ActAutoRefView extends ActBase {
	// ����¼����ü���
	ListEx<BindEventModel<View>> ClickEvents = new ListEx<BindEventModel<View>>();

	// �����¼����ü���
	ListEx<BindEventModel<MotionEvent>> TouchEvents = new ListEx<BindEventModel<MotionEvent>>();

	protected int CurScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RefView_Activity();
	}

	/*
	 * ���ö�Ӧ��ͼ
	 * 
	 * @see android.app.Activity#setContentView(int)
	 */
	@Override
	public void setContentView(int id) {
		super.setContentView(id);
		try {
			RefView_Field();
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}
	}

	
	
	
	protected boolean ScreenOrientationIsFit() {
		return true;
 
	}

	public void RefView_Activity() {
		AutoRefView r = this.getClass().getAnnotation(AutoRefView.class);
		if (r != null) {
			int layout = r.layout();
			if ((layout & 0x01) == 0x01)// ����
				requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(r.id());

			if ((layout & 0x08) == 0x08)
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// ȡ���ֻ���Ļ�Զ�����

		}
	}

	/*
	 * �����ؼ�
	 */
	public void RefView_Field() {
		// ��ȡ��ǰ���ֶγ�Ա
		Field[] fs = this.getClass().getFields();
		for (Field f : fs) {
			// ��ȡ�ֶε�Annotation
			AutoRefView r = f.getAnnotation(AutoRefView.class);
			if (r != null) {
				int id = r.id();
				int includeid = r.includeid();
				View v = null;
				if (includeid != -1) {
					View pv = findViewById(includeid);
					v = pv.findViewById(id);
				} else
					v = findViewById(id);
				if (v != null) {

					try {
						if (f.get(this) instanceof yangTalkback.Cpt.IContainer) {
							yangTalkback.Cpt.IContainer ic = ((yangTalkback.Cpt.IContainer) f.get(this));
							ic.SetView(v);

							// ��������˵���¼�����󶨵���¼�
							if (r.click() != "")
								ic.getClickEvent().add(this, r.click());

							// ��������˴����¼�����󶨴����¼�
							if (r.touch() != "")
								ic.getTouchEvent().add(this, r.touch());

						} else {
							f.set(this, v);// ������ͼ

							// ��������˵���¼�����󶨵���¼�
							if (r.click() != "")
								this.BindClickEvent(v, r.click());
							// ��������˴����¼�����󶨴����¼�
							if (r.touch() != "")
								this.BindTouchEvent(v, r.touch());
						}

					} catch (Exception e) {
						String stack = RuntimeExceptionEx.GetStackTraceString(e);
						String fname = f.getName();
						throw RuntimeExceptionEx.Create(String.format("�Զ�������ͼ�������ƣ�%s", f.getName()), e);
					}

				}
			}
		}
	}

	/**
	 * �󶨵���¼�
	 * 
	 * @param v
	 *            Ҫ���¼��Ķ���
	 * @param modeth
	 *            �����¼��ķ�����,����ԭ�ͣ�public void modethName(EventArg<View> arg)
	 */
	public void BindClickEvent(View v, String modeth) {
		BindEventModel<View> model = null;
		for (BindEventModel<View> m : ClickEvents) {
			if (m.getView().equals(v)) {
				model = m;
				break;
			}
		}
		if (model == null) {
			model = new BindEventModel<View>(v);
			ClickEvents.add(model);
			final BindEventModel<View> tModel = model;
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					tModel.getEvent().Trigger(v, v);
				}
			});
		}
		model.getEvent().add(this, modeth);
	}

	/**
	 * �󶨴����¼�
	 * 
	 * @param v
	 *            Ҫ���¼��Ķ���
	 * @param modeth
	 *            �����¼��ķ�����,����ԭ�ͣ�public void modethName(EventArg<MotionEvent>
	 *            arg)
	 */
	public void BindTouchEvent(View v, String modeth) {
		BindEventModel<MotionEvent> model = null;
		for (BindEventModel<MotionEvent> m : TouchEvents) {
			if (m.getView().equals(v)) {
				model = m;
				break;
			}
		}
		if (model == null) {
			model = new BindEventModel<MotionEvent>(v);
			TouchEvents.add(model);
			final BindEventModel<MotionEvent> tModel = model;
			v.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					tModel.getEvent().Trigger(v, event);
					return false;
				}
			});
		}
		model.getEvent().add(this, modeth);
	}

	/*
	 * �¼��󶨶���
	 */
	protected class BindEventModel<T> {
		private Event<T> event = new Event<T>();
		private View view;

		public BindEventModel(View view) {
			this.view = view;
		}

		public View getView() {
			return view;
		}

		public Event<T> getEvent() {
			return event;
		}

		public void Call(EventArg<T> arg) {
			event.Trigger(view, arg.e);
		}
	}
}
