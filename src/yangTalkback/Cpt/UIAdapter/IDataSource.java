package yangTalkback.Cpt.UIAdapter;

import AXLib.Utility.ListEx;

//�󶨵����ݼ���ͼ������Դ�ӿ�
public interface IDataSource<T> {
	//�Ƿ񵽴��β
	boolean isEnd();
	//��ȡ����
	int getCount();
	//��ȡָ����
	T getItem(int index);
	//��һҳ
	ListEx<T> nextPage();
	//ListEx<T> getPage(int index);

}
