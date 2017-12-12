package yangTalkback.Cpt;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import AXLib.Utility.EventArg;
import yangTalkback.Base.ActCLBase;
import yangTalkback.Base.AutoRefView;
import yangTalkback.Comm.TalkbackStatus;
import yangTalkback.Net.Model.TalkbackStatusInfo;
import yangTalkback.Act.R;

public class itemTalkbackInfo extends cptBase implements IContainer, yangTalkback.Cpt.GenGridView.ActGenDataViewActivity1.IGridViewItemViewCPT<TalkbackStatusInfo> {

	@AutoRefView(id = R.item_talkback_info.llLayout)
	public LinearLayout llLayout;
	@AutoRefView(id = R.item_talkback_info.tvID)
	public TextView tvID;
	@AutoRefView(id = R.item_talkback_info.tvStatus)
	public TextView tvStatus;
	@AutoRefView(id = R.item_talkback_info.ivImage)
	public ImageView ivImage;

	private Activity _act = null;
	private boolean _controlSeted = false;
	protected TalkbackStatusInfo _model = null;

	public itemTalkbackInfo(Activity act, TalkbackStatusInfo model) {
		super(act);
		_act = act;
		_model = model;

	}

	public void SetAct(ActCLBase act) {
		_act = act;
	}

	@Override
	public void SetView(View v) {
		super.SetView(v);

	}

	public void setControl() {
		String statusName = "";
		TalkbackStatus status = TalkbackStatus.forValue(_model.IDModel.TalkbackStatus);
		if (status == TalkbackStatus.Idle)
			statusName = "δ����";
		if (status == TalkbackStatus.Entering)
			statusName = "���ڽ���";
		if (status == TalkbackStatus.Leaveing)
			statusName = "�����˳�";
		if (status == TalkbackStatus.Talkbacking)
			statusName = "�ڽ���";
		if (_model.JoinStatus == 0 || _model.JoinStatus == -1)
			statusName = "δ����";
		else if (_model.JoinStatus == 1)
			statusName = "�Խ���";
		tvStatus.setText("״̬:" + statusName);

		// ����״̬ -1���뿪 0δ���� 1�Խ���
		if (_model.JoinStatus == 0)
			ivImage.setImageResource(R.drawable.ico_talkback_offline);
		if (_model.JoinStatus == -1)
			ivImage.setImageResource(R.drawable.ico_talkback_leave);
		if (_model.JoinStatus == 1)
			ivImage.setImageResource(_model.IsTalking ? R.drawable.ico_talkback_talking : R.drawable.ico_talkback_online);

		tvID.setText(String.valueOf(_model.IDModel.Name));

	}

	public View getContainer() {
		return llLayout;

	}

	public void OnClick(EventArg<View> arg) {
		OnClick(arg.e);
	}

	public TalkbackStatusInfo getModel() {
		return _model;
	}

	public void setModel(TalkbackStatusInfo model) {
		if (!_controlSeted || _model != model) {
			_model = model;
			setControl();
			_controlSeted = true;
		}
	}

}
