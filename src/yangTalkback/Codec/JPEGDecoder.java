package yangTalkback.Codec;

import yangTalkback.Media.MediaFrame;
import yangTalkback.Media.VideoDisplayFrame;

//ͼƬ������
public class JPEGDecoder extends H264AndroidDecoder {
	private void Init(MediaFrame mf) {
 
		inited = true;
	}

	public VideoDisplayFrame Deocde(MediaFrame mf) throws Exception {
 
		return null;
	}

	@Override
	public void Dispose() {
		if (VideoBit != null)
			VideoBit.recycle();
	}
}
