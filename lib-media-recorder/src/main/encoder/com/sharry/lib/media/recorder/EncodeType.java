package com.sharry.lib.media.recorder;

import android.media.MediaFormat;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-08-20 11:16
 */
public class EncodeType {

    public enum Video {

        H264("H.264");

        private String desc;

        Video(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

    }

    public enum Audio {

        AAC(MediaFormat.MIMETYPE_AUDIO_AAC, ".aac");

        private String outputFormat;
        private String suffix;

        /**
         * 定义录音类型
         *
         * @param mime   编码格式
         * @param suffix 录音文件扩展名
         */
        Audio(String mime, String suffix) {
            this.outputFormat = mime;
            this.suffix = suffix;
        }

        public String getMIME() {
            return outputFormat;
        }

        public String getFileSuffix() {
            return suffix;
        }

    }

}
