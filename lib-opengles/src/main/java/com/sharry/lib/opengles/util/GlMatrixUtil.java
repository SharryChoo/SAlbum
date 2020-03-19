package com.sharry.lib.opengles.util;

import android.opengl.Matrix;

/**
 * 用于快捷构建 GL 的 Matrix
 *
 * @author zhuxiaoyu <a href="zhuxiaoyu.sharry@bytedance.com">Contact me.</a>
 * @version 1.0
 * @since 2020/3/5
 */
public class GlMatrixUtil {

    /**
     * 矩阵操作的类型
     */
    public static final int TYPE_FIT_XY = 0;
    public static final int TYPE_CENTER_CROP = 1;
    public static final int TYPE_CENTER_INSIDE = 2;
    public static final int TYPE_FIT_START = 3;
    public static final int TYPE_FIT_END = 4;

    public static void getMatrix(float[] matrix, int type, int imgWidth, int imgHeight, int viewWidth,
                                 int viewHeight) {
        if (imgHeight == 0 || imgWidth == 0 || viewWidth == 0 || viewHeight == 0) {
            return;
        }
        // 构建视图矩阵
        float[] viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0,
                0, 0, 1,
                0, 0, 0,
                0, 1, 0
        );
        // 根据 ScaleType 构建投影矩阵
        float[] projectionMatrix = new float[16];
        float aspectImg = (float) imgWidth / imgHeight;
        float aspectView = (float) viewWidth / viewHeight;
        if (aspectImg > aspectView) {
            switch (type) {
                case TYPE_CENTER_CROP:
                    Matrix.orthoM(projectionMatrix, 0, -aspectView / aspectImg, aspectView / aspectImg, -1, 1, -1, 1);
                    break;
                case TYPE_CENTER_INSIDE:
                    Matrix.orthoM(projectionMatrix, 0, -1, 1, -aspectImg / aspectView, aspectImg / aspectView, -1, 1);
                    break;
                case TYPE_FIT_START:
                    Matrix.orthoM(projectionMatrix, 0, -1, 1, 1 - 2 * aspectImg / aspectView, 1, -1, 1);
                    break;
                case TYPE_FIT_END:
                    Matrix.orthoM(projectionMatrix, 0, -1, 1, -1, 2 * aspectImg / aspectView - 1, -1, 1);
                    break;
                case TYPE_FIT_XY:
                default:
                    // 初始化投影矩阵
                    Matrix.orthoM(projectionMatrix, 0, -1, 1, -1, 1, -1, 1);
                    break;
            }
        } else {
            switch (type) {
                case TYPE_CENTER_CROP:
                    Matrix.orthoM(projectionMatrix, 0, -1, 1, -aspectImg / aspectView, aspectImg / aspectView, -1, 1);
                    break;
                case TYPE_CENTER_INSIDE:
                    Matrix.orthoM(projectionMatrix, 0, -aspectView / aspectImg, aspectView / aspectImg, -1, 1, -1, 1);
                    break;
                case TYPE_FIT_START:
                    Matrix.orthoM(projectionMatrix, 0, -1, 2 * aspectView / aspectImg - 1, -1, 1, -1, 1);
                    break;
                case TYPE_FIT_END:
                    Matrix.orthoM(projectionMatrix, 0, 1 - 2 * aspectView / aspectImg, 1, -1, 1, -1, 1);
                    break;
                case TYPE_FIT_XY:
                default:
                    // 初始化投影矩阵
                    Matrix.orthoM(projectionMatrix, 0, -1, 1, -1, 1, -1, 1);
                    break;
            }
        }
        // 合并观察矩阵和视图矩阵 -> 裁剪坐标系
        Matrix.multiplyMM(matrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }

    public static float[] flip(float[] m, boolean x, boolean y) {
        if (x || y) {
            Matrix.scaleM(m, 0, x ? -1 : 1, y ? -1 : 1, 1);
        }
        return m;
    }

    public static float[] scale(float[] m, float x, float y) {
        Matrix.scaleM(m, 0, x, y, 1);
        return m;
    }

    public static float[] getOriginalMatrix() {
        return new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        };
    }

}
