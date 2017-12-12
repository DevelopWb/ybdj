package yangTalkback.Codec.FFCodec;

import AXLib.Model.*;

public class AVCodecCfg extends ByteObj   {
	
	@ByteObjMember(index=10) public int codec_id;//FFMPEG�б�����ID
	@ByteObjMember(index=20) public int codec_type;//FFMPEG�б�������
	@ByteObjMember(index=30) public int bit_rate;//������
	@ByteObjMember(index=40) public int width;//���
	@ByteObjMember(index=50) public int height;//�߶�
	@ByteObjMember(index=60) public int time_base_den;//֡��
	@ByteObjMember(index=70) public int time_base_num;//֡��
	@ByteObjMember(index=80) public int gop_size;//
	@ByteObjMember(index=90) public int pix_fmt;//ͼƬ��ʽ
	@ByteObjMember(index=100) public int max_b_frames;//���B֡���
	@ByteObjMember(index=110) public int sample_rate;//����
	@ByteObjMember(index=120) public int channels;//ͨ��
    
    
    public AVCodecCfg()
    {
 
    }
 
    @Override
	public int getSize() {
		return 4*12;
	}
 
    public static  AVCodecCfg  CreateAudio(int channels, int sample_rate, FFCode codec_id , int bit_rate)
    {
    	AVCodecCfg r = new AVCodecCfg();
        r.codec_id = codec_id.getId();
        r.codec_type = FFCode.CODEC_TYPE_AUDIO.getId();
        r.bit_rate = bit_rate;
        r.time_base_den = 0;
        r.time_base_num = 0;
        r.gop_size = 0;
        r.pix_fmt = 0;
        r.max_b_frames = 0;
        r.sample_rate = sample_rate;
        r.channels = channels;
        return r;
    }
    public static AVCodecCfg CreateVideo(int width, int height, FFCode codec_id , int bit_rate )
    {
    	AVCodecCfg r = new AVCodecCfg();
        r.codec_id = codec_id.getId();
        r.width = width;
        r.height = height;
        r.codec_type = FFCode.CODEC_TYPE_VIDEO.getId();
        r.bit_rate = bit_rate;
        r.time_base_den = 15;
        r.time_base_num = 2;
        r.gop_size = 12;
        r.pix_fmt = FFCode.PIX_FMT_YUV420P.getId();
        return r;

    }
 
}
