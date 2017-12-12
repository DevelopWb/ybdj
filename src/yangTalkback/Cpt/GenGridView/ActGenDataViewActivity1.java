package yangTalkback.Cpt.GenGridView;

import yangTalkback.Cpt.IContainer;
import yangTalkback.Cpt.UIAdapter.BaseAdapterEx.IItemViewCreater;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;

import AXLib.Utility.EventArg;
import AXLib.Utility.ListEx;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;

/** ͨ��GridView���� **/
public abstract class ActGenDataViewActivity1<T> extends ActGetDataViewActivity<T> implements IItemViewCreater, IGenGridViewDataInterface<T> {

	/** ��ȡ��ҳ���� **/
	public abstract ListEx<T> getData(int pageIndex);

	/** �б��а�ť����¼� **/
	public abstract void ItemClickEvent(EventArg<T> arg);

	protected abstract IGridViewItemViewCPT<T> CreateItem(T model);

	protected GridView _grid;
	protected int _numColumns = 1;
	protected int _itemViewID = -1;
	protected GenGridViewAdapter<T> gridAdp = null;// �б�����Դ������
	protected GenGridViewDataSource<T> _dataSource = null;// �б�����Դ

	protected void InitGridViewActivity(GridView grid, int numColumns, int itemViewID) {
		InitGridViewActivity(grid, numColumns, itemViewID, -1, -1);
	}

	protected void InitGridViewActivity(GridView grid, int numColumns, int itemViewID, int itemWidth, int itemHeight) {
		_grid = grid;
		_numColumns = numColumns;
		_itemViewID = itemViewID;
		this.itemWidth = itemWidth;
		this.itemHeight = itemHeight;
		this.CallByNewThread("CheckLayoutReady");// �������̵߳��÷���
	}

	public void CheckLayoutReady() {
		if (_dataSource == null) {
			_dataSource = new GenGridViewDataSource<T>(this);
		}
		while (true) {
			final int width = _grid.getMeasuredWidth();
			if (width != 0) {
				post(new Runnable() {
					@Override
					public void run() {
						InitGridView();
					}
				});
				break;
			}
			ThreadEx.sleep(100);
		}
	}

	/** ��ʼ���б� **/
	protected void InitGridView() {
		if (_grid == null)
			throw RuntimeExceptionEx.Create("δ��ʼ��gvGrid�����ȵ���InitGridViewActivity");
		_grid.setNumColumns(_numColumns);
		try {
			gridAdp = new GenGridViewAdapter<T>(this, this, _dataSource, _grid);
			gridAdp.ItemClickEvent.add(this, "ItemClickEvent");
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}
	}

	// ��ȡ�б���Ԫ���е���ͼ
	@Override
	public View getItemView(int position, View convertView, ViewGroup parent) {
		if (_itemViewID == -1)
			throw RuntimeExceptionEx.Create("δ��ʼ��_itemViewID�����ȵ���InitGridViewActivity");

		T model = _dataSource.getItem(position);
		if (model == null)
			return null;
		IGridViewItemViewCPT<T> cpt = null;
		if (convertView == null) {
			convertView = _inflater.inflate(_itemViewID, null);
			cpt = CreateItem(model);
			cpt.SetView(convertView);
			View view = cpt.getContainer();
			cpt.getClickEvent().add(this, "ItemClickEvent");
			cpt.getTouchEvent().add(this, "ItemTouchEvent");
			cpt.getExecutionEvent().add(this, "ItemExecutionEvent");
			if (this.itemWidth == -1 && this.itemHeight == -1)
				view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));
			else if (this.itemWidth != -1 && this.itemHeight == -1)
				view.setLayoutParams(new AbsListView.LayoutParams(this.itemWidth, AbsListView.LayoutParams.MATCH_PARENT));
			else if (this.itemWidth == -1 && this.itemHeight != -1)
				view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, this.itemHeight));
			else
				view.setLayoutParams(new AbsListView.LayoutParams(this.itemWidth, this.itemHeight));
		} else {
			cpt = (IGridViewItemViewCPT<T>) convertView.getTag();
		}
		cpt.setModel(model);
		convertView.setTag(cpt);
		return convertView;
	}

	// ˢ��
	public void Reflash() {
		_dataSource.Reflash();
		post(new Runnable() {
			@Override
			public void run() {
				try {
					if(gridAdp!=null)
					gridAdp.notifyDataSetChanged();
				} catch (Exception e) {
					throw RuntimeExceptionEx.Create(e);
				}
			}
		});

	}

	/** �б��д����¼� **/
	public void ItemTouchEvent(EventArg<MotionEvent> arg) {

	}

	/** �б�����չ�¼� **/
	public void ItemExecutionEvent(EventArg<Object> arg) {

	}

	public static interface IGridViewItemViewCPT<T> extends IContainer {

		void setModel(T model);

		T getModel();

		View getContainer();

	}
}