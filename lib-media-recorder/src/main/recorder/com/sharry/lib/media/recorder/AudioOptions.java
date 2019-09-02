package com.sharry.lib.media.recorder;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The AudioOptions associated with AudioRecorder.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/11/7 18:30
 */
public class AudioOptions {

    public static final int SAMPLE_RATE_44_1 = 44100;
    public static final int CHANNEL_LAYOUT_CENTER = 2;
    public static final int PER_SAMPLE_SIZE = 2;

    @IntDef(flag = true, value = {
            SAMPLE_RATE_44_1,
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
    private static final IPCMProvider DEFAULT_PROVIDER = new OpenSLESPCMProvider();

    public static final AudioOptions DEFAULT = new AudioOptions.Builder().build();

    /**
     * 音频的采样率
     */
    private int sampleRate = SAMPLE_RATE_44_1;

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
    private IPCMProvider pcmProvider = DEFAULT_PROVIDER;

    /**
     * 录制后输出的目录
     */
    private String outputDir;

    private AudioOptions() {
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

        private AudioOptions mCfg;

        public Builder() {
            mCfg = new AudioOptions();
        }

        private Builder(AudioOptions audioOptions) {
            this.mCfg = audioOptions;
        }

        /**
         * 设置采样率
         */
        public Builder setSampleSize(@SampleRate int sampleSize) {
            mCfg.sampleRate = sampleSize;
            return this;
        }

        /**
         * 设置音频文件的输出类型
         */
        public Builder setEncodeType(EncodeType.Audio type) {
            mCfg.audioEncodeType = type;
            return this;
        }

        /**
         * 设置音频文件最大录制时长
         *
         * @param duration Unit millisecond.
         */
        public Builder setDuration(int duration) {
            mCfg.duration = duration;
            return this;
        }

        /**
         * 设置一个可用的文件输出目录
         *
         * @param outputDir 指定一个用于输出音频文件的文件夹(内部会自行创建)
         */
        public Builder setOutputDir(@NonNull String outputDir) {
            mCfg.outputDir = outputDir;
            return this;
        }

        /**
         * 是否只进行编码, 不进行文件写入
         */
        public Builder setIsJustEncode(boolean isJustEncode) {
            mCfg.isJustEncode = isJustEncode;
            return this;
        }

        public Builder setPerSampleSize(@PerSampleSize int perSampleSize) {
            mCfg.perSampleSize = perSampleSize;
            return this;
        }

        public Builder setChannelLayout(@ChannelLayout int channelLayout) {
            mCfg.channelLayout = channelLayout;
            return this;
        }

        public Builder setPcmProvider(@NonNull IPCMProvider pcmProvider) {
            mCfg.pcmProvider = pcmProvider;
            return this;
        }

        public AudioOptions build() {
            return mCfg;
        }


    }

}
