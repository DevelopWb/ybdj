package yangTalkback.Cpt.GenGridView;

import yangTalkback.Base.ActDBBase;
import yangTalkback.Cpt.UIAdapter.BaseAdapterEx.IItemViewCreater;

import android.view.View;
import android.view.ViewGroup;

import AXLib.Utility.EventArg;
import AXLib.Utility.ListEx;

/** ͨ��GridView���� **/
public abstract class ActGetDataViewActivity<T> extends ActDBBase implements IItemViewCreater, IGenGridViewDataInterface<T> {
	/** ��ȡ������ͼ **/
	public abstract View getItemView(int position, View convertView, ViewGroup parent);

	/** ��ȡ��ҳ���� **/
	public abstract ListEx<T> getData(int pageIndex);

	/** �б��а�ť����¼� **/
	public abstract void ItemClickEvent(EventArg<T> arg);

	/** ��ʼ���б� **/
	protected abstract void InitGridView();

}
