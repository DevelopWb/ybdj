package yangTalkback.Media;

import android.graphics.Bitmap;
//��Ƶ��ʾ֡
public class VideoDisplayFrame {
	public final Bitmap BMP;
	public final long TimeTick;

	public VideoDisplayFrame(Bitmap bmp, long timeTick) {
		BMP = bmp;
		TimeTick = timeTick;
	}
}
