package yangTalkback.Cpt.GenGridView;

import AXLib.Utility.ListEx;
/** ͨ��GridView���ݽӿ� **/
public interface IGenGridViewDataInterface<T> {
	/** ��ȡָ��ҳ���� **/
	ListEx<T> getData(int pageIndex);
}