package com.sharry.lib.media.recorder;

import android.media.MediaCodec;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-07-23 14:04
 */
class ACCUtil {

    /**
     * 音频常用的采样数组
     */
    private static int[] SAMPLE_RATES = new int[]{
            96000, 88200, 64000, 48000, 44100, 32000,
            24000, 22050, 16000, 12000, 11025, 8000, 7350
    };

    private static final int ACC_HEADER_ADTS_LENGTH = 7;

    static void writeToFile(FileOutputStream mFileOutputSteam,
                            int mSampleRate,
                            int channelCount,
                            ByteBuffer byteBuffer,
                            MediaCodec.BufferInfo bufferInfo) throws IOException {
        // 2.5 将输出流写入到文件中
        if (mFileOutputSteam != null) {
            int len = bufferInfo.size + ACC_HEADER_ADTS_LENGTH;                                  // 多开辟 7 个 byte 给流数据添加 ADTS 头部字段
            byte[] accBytes = new byte[len];                                                     // 创建输出数组
            addADTStoPacket(accBytes, len, mSampleRate, channelCount);                          // 填充 ACC 首部字段
            byteBuffer.get(accBytes, ACC_HEADER_ADTS_LENGTH, bufferInfo.size);                   // 获取编码后的数据
            // 写入文件
            mFileOutputSteam.write(accBytes);
        }
    }

    /**
     * 给 ACC 码流添加 ADTS 头字段
     *
     * @param packet    空出 7 个字节, 填充 ADTS 字段
     * @param packetLen 真实数据的长度
     */
    private static void addADTStoPacket(byte[] packet, int packetLen, int sampleRate, int channelCount) {
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
    private static int sampleRateMapperFrequency(int sampleRate) {
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
