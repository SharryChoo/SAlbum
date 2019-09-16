package com.sharry.lib.media.recorder;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-05
 */
public final class Options {

    public static class Video {

        public static final int RESOLUTION_1080P = 1920 * 1080;
        public static final int RESOLUTION_720P = 1280 * 720;
        public static final int RESOLUTION_480P = 720 * 480;

        @IntDef(value = {
                RESOLUTION_1080P,
                RESOLUTION_720P,
                RESOLUTION_480P
        })
        public @interface Resolution {

        }

        private static final int DEFAULT_FRAME_RATE = 24;

        /**
         * 视频的 编码类型
         */
        private EncodeType.Video videoEncodeType = EncodeType.Video.H264;

        /**
         * 录制时, 音频的配置
         */
        private Audio audioOps = Audio.DEFAULT;

        /**
         * 音视频 封装类型
         */
        private MuxerType muxerType = MuxerType.MP4;

        /**
         * 视频的 帧率
         */
        private int frameRate = DEFAULT_FRAME_RATE;

        /**
         * 录制后的 输出目录
         */
        private String outputDir;

        /**
         * 设置录制分辨率
         */
        private int resolution = RESOLUTION_720P;

        private Video() {
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

        public int getResolution() {
            return resolution;
        }

        public String getOutputDir() {
            return outputDir;
        }

        public Audio getAudioOptions() {
            return audioOps;
        }

        public MuxerType getMuxerType() {
            return muxerType;
        }

        /**
         * Build options instance easier.
         */
        public static class Builder {

            private Video mOps;

            public Builder() {
                mOps = new Video();
            }

            private Builder(Video videoOptions) {
                this.mOps = videoOptions;
            }

            /**
             * 设置帧率
             */
            public Builder setFrameRate(int frameRate) {
                mOps.frameRate = frameRate;
                return this;
            }

            /**
             * 设置视频编码格式(H.264)
             */
            public Builder setEncodeType(@NonNull EncodeType.Video type) {
                mOps.videoEncodeType = type;
                return this;
            }

            /**
             * 设置视频的封装格式( MP4 )
             */
            public Builder setMuxerType(@NonNull MuxerType muxerType) {
                mOps.muxerType = muxerType;
                return this;
            }

            /**
             * 设置录制分辨率
             */
            public Builder setResolution(@Resolution int resolution) {
                mOps.resolution = resolution;
                return this;
            }

            /**
             * 设置一个可用的文件输出目录
             *
             * @param outputDir 指定一个用于输出视频文件的文件夹(内部会自行创建)
             */
            public Builder setOutputDir(@NonNull String outputDir) {
                mOps.outputDir = outputDir;
                return this;
            }

            /**
             * 设置音频录制的配置
             */
            public Builder setAudioOptions(@NonNull Audio audioOptions) {
                mOps.audioOps = audioOptions;
                return this;
            }

            public Video build() {
                return mOps;
            }
        }
    }

    public static class Audio {

        public static final int SAMPLE_RATE_44100 = 44100;
        public static final int CHANNEL_LAYOUT_CENTER = 2;
        public static final int PER_SAMPLE_SIZE = 2;

        @IntDef(flag = true, value = {
                SAMPLE_RATE_44100,
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface SampleRate {
        }

        @IntDef(flag = true, value = {
                CHANNEL_LAYOUT_CENTER,
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface ChannelLayout {
        }

        @IntDef(flag = true, value = {
                PER_SAMPLE_SIZE,
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface PerSampleSize {
        }

        private static final int DEFAULT_MAX_DURATION = Integer.MAX_VALUE;
        private static final EncodeType.Audio DEFAULT_RECORD_TYPE = EncodeType.Audio.AAC;

        public static final Audio DEFAULT = new Builder().build();

        /**
         * 音频的采样率
         */
        private int sampleRate = SAMPLE_RATE_44100;

        /**
         * 录制时的声音布局
         * <p>
         * 目前只支持立体声
         */
        private int channelLayout = CHANNEL_LAYOUT_CENTER;

        /**
         * 采样点的 byte 数
         * <p>
         * 目前只支持 2 byte(16 bit)
         */
        private int perSampleSize = PER_SAMPLE_SIZE;

        /**
         * 录制时长
         */
        private int duration = DEFAULT_MAX_DURATION;

        /**
         * 仅进行编码
         */
        private boolean isJustEncode = false;

        /**
         * 录制后编码的类型
         */
        private EncodeType.Audio audioEncodeType = DEFAULT_RECORD_TYPE;

        /**
         * 设置 PCM 数据提供器
         */
        private IPCMProvider pcmProvider;

        /**
         * 录制后输出的目录
         */
        private String outputDir;

        private Audio() {
        }

        /**
         * Get an instance of Builder.
         */
        public Builder reBuilder() {
            return new Builder(this);
        }

        EncodeType.Audio getAudioEncodeType() {
            return audioEncodeType;
        }

        int getDuration() {
            return duration;
        }

        String getOutputDir() {
            return outputDir;
        }

        int getSampleRate() {
            return sampleRate;
        }

        boolean isJustEncode() {
            return isJustEncode;
        }

        int getChannelLayout() {
            return channelLayout;
        }

        int getPerSampleSize() {
            return perSampleSize;
        }

        IPCMProvider getPcmProvider() {
            return pcmProvider;
        }

        /**
         * Build options instance easier.
         */
        public static class Builder {

            private Audio mOps;

            public Builder() {
                mOps = new Audio();
            }

            private Builder(Audio Audio) {
                this.mOps = Audio;
            }

            /**
             * 设置采样率
             */
            public Builder setSampleSize(@SampleRate int sampleSize) {
                mOps.sampleRate = sampleSize;
                return this;
            }

            /**
             * 设置音频文件的输出类型
             */
            public Builder setEncodeType(EncodeType.Audio type) {
                mOps.audioEncodeType = type;
                return this;
            }

            /**
             * 设置音频文件最大录制时长
             *
             * @param duration Unit millisecond.
             */
            public Builder setDuration(int duration) {
                mOps.duration = duration;
                return this;
            }

            /**
             * 设置一个可用的文件输出目录
             *
             * @param outputDir 指定一个用于输出音频文件的文件夹(内部会自行创建)
             */
            public Builder setOutputDir(@NonNull String outputDir) {
                mOps.outputDir = outputDir;
                return this;
            }

            /**
             * 是否只进行编码, 不进行文件写入
             */
            public Builder setIsJustEncode(boolean isJustEncode) {
                mOps.isJustEncode = isJustEncode;
                return this;
            }

            public Builder setPerSampleSize(@PerSampleSize int perSampleSize) {
                mOps.perSampleSize = perSampleSize;
                return this;
            }

            public Builder setChannelLayout(@ChannelLayout int channelLayout) {
                mOps.channelLayout = channelLayout;
                return this;
            }

            public Builder setPcmProvider(@NonNull IPCMProvider pcmProvider) {
                mOps.pcmProvider = pcmProvider;
                return this;
            }

            public Audio build() {
                return mOps;
            }

        }
    }

}
