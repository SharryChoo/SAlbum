package com.sharry.lib.media.recorder;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * ACC 音频的编码器
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/11/22 10:22
 */
@SuppressLint("NewApi")
public class ACCEncoder implements IAudioEncoder {

    /**
     * 音频常用的采样数组
     */
    private static int[] SAMPLE_RATES = new int[]{
            96000, 88200, 64000, 48000, 44100, 32000,
            24000, 22050, 16000, 12000, 11025, 8000, 7350
    };

    private static final int ACC_HEADER_ADTS_LENGTH = 7;

    private static final String MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC;  // 当前编码器要编码的类型描述
    private final MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private FileOutputStream mFileOutputSteam;
    private Context mContext;
    private MediaCodec mImpl;
    private long mPts = 0;

    @Override
    public void prepare(@NonNull Context context) throws IOException {
        mContext = context;
        // 执行编码前的准备
        MediaFormat audioFormat = MediaFormat.createAudioFormat(MIME_TYPE, mContext.sampleRate, mContext.channelCount);
        int bitRate = mContext.sampleRate * mContext.channelCount * context.perSampleSize;
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, mContext.channelCount);
        audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, mContext.sampleRate);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bitRate);
        // 根据参数判断是否需要写入到文件
        if (!mContext.isJustEncode) {
            mFileOutputSteam = new FileOutputStream(context.outputFile);
        }
        // 初始化编码器
        mImpl = MediaCodec.createEncoderByType(MIME_TYPE);
        mImpl.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mImpl.start();
    }

    @Override
    public void encode(@Nullable byte[] pcmBytes) {
        if (pcmBytes == null) {
            return;
        }
        // 1. 将输入流传递给编码器的 inputBuffer 队列中
        final int indexOfInputBuffer = mImpl.dequeueInputBuffer(0);
        if (indexOfInputBuffer >= 0) {
            final ByteBuffer inputBuffer = mImpl.getInputBuffer(indexOfInputBuffer);
            if (null == inputBuffer) {
                return;
            }
            inputBuffer.clear();
            inputBuffer.put(pcmBytes);
            // 计算录制时间戳
            calcPresentationTimeUs(pcmBytes.length, mContext.sampleRate, mContext.channelCount,
                    mContext.perSampleSize);
            mImpl.queueInputBuffer(indexOfInputBuffer, 0, pcmBytes.length, mPts, 0);
        } else {
            // ignore.
            return;
        }
        // 从 MediaCodec 中获取编码后的输出流
        int outBufferIndex = mImpl.dequeueOutputBuffer(mBufferInfo, 0);
        // 处理输出缓冲索引小于 0 的情况
        if (outBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            mContext.callback.onAudioFormatChanged(mImpl.getOutputFormat());
        } else {
            while (outBufferIndex >= 0) {
                // 获取数据
                final ByteBuffer outBuffer = mImpl.getOutputBuffer(outBufferIndex);
                if (null == outBuffer) {
                    continue;
                }
                outBuffer.position(mBufferInfo.offset);
                outBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
                // 回调音频编码数据
                mContext.callback.onAudioEncoded(outBuffer, mBufferInfo);
                // 写到文件
                writeToFile(outBuffer, mBufferInfo);
                // 释放 encoderStatus 索引处的输出缓冲流
                mImpl.releaseOutputBuffer(outBufferIndex, false);
                outBufferIndex = mImpl.dequeueOutputBuffer(mBufferInfo, 0);
            }
        }
    }

    @Override
    public void release() {
        if (mImpl != null) {
            mImpl.flush();
            mImpl.stop();
            mImpl.release();
            mImpl = null;
        }
    }

    /**
     * 计算录制时间戳
     *
     * @param size          采样数据的大小
     * @param sampleRate    采样率
     * @param channel       通道数
     * @param preSampleSize 采样点大小
     */
    private void calcPresentationTimeUs(int size, int sampleRate, int channel, int preSampleSize) {
        mPts += (long) (1.0 * size / (sampleRate * channel * preSampleSize) * 1000000.0);
    }

    /**
     * 将编码后的 ACC 添加头部并且写入文件
     */
    private void writeToFile(ByteBuffer outBuffer, MediaCodec.BufferInfo bufferInfo) {
        if (mFileOutputSteam == null) {
            return;
        }
        // 多开辟 7 个 byte 给流数据添加 ADTS 头部字段
        int len = bufferInfo.size + ACC_HEADER_ADTS_LENGTH;
        byte[] accBytes = new byte[len];
        // 为数组填充 ADTS 头部信息
        addADTStoPacket(accBytes, len, mContext.sampleRate, mContext.channelCount);
        // 将 ACC 数据写入到头部信息之后
        outBuffer.get(accBytes, ACC_HEADER_ADTS_LENGTH, bufferInfo.size);
        // 写入文件
        try {
            mFileOutputSteam.write(accBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 给 ACC 码流添加 ADTS 头字段
     *
     * @param packet    空出 7 个字节, 填充 ADTS 字段
     * @param packetLen 真实数据的长度
     */
    private void addADTStoPacket(byte[] packet, int packetLen, int sampleRate, int channelCount) {
        int profile = 2; // AAC LC
        int freqIdx = sampleRateMapperFrequency(sampleRate);
        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (channelCount >> 2));
        packet[3] = (byte) (((channelCount & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    /**
     * 获取采样率对应的频率 ID.
     * <p>
     * 0: 96000 Hz
     * 1: 88200 Hz
     * 2: 64000 Hz
     * 3: 48000 Hz
     * 4: 44100 Hz
     * 5: 32000 Hz
     * 6: 24000 Hz
     * 7: 22050 Hz
     * 8: 16000 Hz
     * 9: 12000 Hz
     * 10: 11025 Hz
     * 11: 8000 Hz
     * 12: 7350 Hz
     * 13: Reserved
     * 14: Reserved
     * 15: frequency is written explictly
     * </p>
     *
     * @return the sample rate mapped special frequency id.
     */
    private int sampleRateMapperFrequency(int sampleRate) {
        int frequencyId = -1;
        for (int index = 0; index < SAMPLE_RATES.length; index++) {
            if (sampleRate == SAMPLE_RATES[index]) {
                frequencyId = index;
                break;
            }
        }
        return frequencyId;
    }

}
