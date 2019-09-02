package com.sharry.lib.media.recorder;


import androidx.annotation.NonNull;

/**
 * The VideoOptions associated with VideoRecorder.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/11/7 18:30
 */
public class VideoOptions {

    private static final int DEFAULT_FRAME_RATE = 24;
    private static final EncodeType.Video DEFAULT_VIDEO_ENCODE_TYPE = EncodeType.Video.H264;
    private static final MuxerType DEFAULT_VIDEO_MUXER_TYPE = MuxerType.MP4;

    /**
     * 视频的 编码类型
     */
    private EncodeType.Video videoEncodeType = DEFAULT_VIDEO_ENCODE_TYPE;

    /**
     * 录制时, 音频的配置
     */
    private AudioOptions audioOptions = AudioOptions.DEFAULT;

    /**
     * 音视频 封装类型
     */
    private MuxerType muxerType = DEFAULT_VIDEO_MUXER_TYPE;

    /**
     * 视频的 帧率
     */
    private int frameRate = DEFAULT_FRAME_RATE;

    /**
     * 录制后的 输出目录
     */
    private String outputDir;

    private VideoOptions() {
    }

    /**
     * Get an instance of Builder.
     */
    public Builder reBuilder() {
        return new Builder(this);
    }

    public EncodeType.Video getVideoEncodeType() {
        return videoEncodeType;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public AudioOptions getAudioOptions() {
        return audioOptions;
    }

    public MuxerType getMuxerType() {
        return muxerType;
    }

    /**
     * Build options instance easier.
     */
    public static class Builder {

        private VideoOptions mCfg;

        public Builder() {
            mCfg = new VideoOptions();
        }

        private Builder(VideoOptions videoOptions) {
            this.mCfg = videoOptions;
        }

        /**
         * 设置帧率
         */
        public Builder setFrameRate(int frameRate) {
            mCfg.frameRate = frameRate;
            return this;
        }

        /**
         * 设置视频编码格式(H.264)
         */
        public Builder setEncodeType(@NonNull EncodeType.Video type) {
            mCfg.videoEncodeType = type;
            return this;
        }

        /**
         * 设置视频的封装格式( MP4 )
         */
        public Builder setMuxerType(@NonNull MuxerType muxerType) {
            mCfg.muxerType = muxerType;
            return this;
        }

        /**
         * 设置一个可用的文件输出目录
         *
         * @param outputDir 指定一个用于输出视频文件的文件夹(内部会自行创建)
         */
        public Builder setOutputDir(@NonNull String outputDir) {
            mCfg.outputDir = outputDir;
            return this;
        }

        /**
         * 设置音频录制的配置
         */
        public Builder setAudioOptions(@NonNull AudioOptions audioOptions) {
            mCfg.audioOptions = audioOptions;
            return this;
        }

        public VideoOptions build() {
            return mCfg;
        }
    }

}
