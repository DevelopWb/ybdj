package yangTalkback.Cpt.GenGridView;

import AXLib.Utility.ListEx;
import yangTalkback.Base.ActDBBase.DataSourceBase;
import yangTalkback.Cpt.UIAdapter.IDataSource;

/** ͨ��GridView����Դ **/
public class GenGridViewDataSource<T> extends DataSourceBase<T> implements IDataSource<T> {
	private ActGetDataViewActivity<T> _act = null;

	public GenGridViewDataSource(ActGetDataViewActivity<T> act) {
		_act = act;
		LoadFirstPage();
	}

	/** ˢ�� **/
	public void Reflash() {
		_isEnd = false;
		_curPageIndex = 1;
		_list.clear();
		LoadFirstPage();
	}

	/** ���ص�һҳ���� **/
	public void LoadFirstPage() {
		ListEx<T> list = nextPage();
		if (list.size() == 0)
			_act.Notice("δ�ҵ���Ӧ�����ݡ�");
	}

	/** ��ȡ��һҳ���� **/
	@Override
	public ListEx<T> nextPage() {
		ListEx<T> list = _act.getData(_curPageIndex++);

		if (list != null) {
			_list.addAll(list);

			if (list.size() == 0)
				_isEnd = true;
		} else {
			list = new ListEx<T>();
		}
		return list;
	}
}