package yangTalkback.Base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * @author chaobinw �Զ�������ͼ�����붨��Ϊpublic����
 * @AutoRefView(id = R.id.tvMsg) public TextView tvMsg;
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRefView {
	/**
	 * ��ͼ�ؼ�id
	 * 
	 * @return
	 */
	int id();

	/**
	 * �Զ���ؼ�ID,������ø�ֵ��󶨿ؼ��Ӹ�ID�Ŀؼ����ӿؼ��в���
	 * 
	 * @return
	 */
	int includeid() default -1;

	/**
	 * �󶨵ĵ���¼�,�¼�����������Ϊpublic ԭ��Ϊpublic void modethName(EventArg<View> arg)
	 * 
	 * @return
	 */
	String click() default "";

	/**
	 * �󶨵Ĵ����¼�,�¼�����������Ϊpublic ԭ��Ϊpublic void modethName(EventArg<MotionEvent>
	 * arg)
	 * 
	 * @return
	 */
	String touch() default "";

	/**
	 * @see bit list
	 * @see 1b :����,0�б���,1�ޱ���
	 * @see 2b-3b :����,0�Զ���1����2ģ��
	 * @see 4b :������0�Զ���1����������
	 * 0000 0000
	 * @return
	 */
	byte layout() default 0x00;

 
}
