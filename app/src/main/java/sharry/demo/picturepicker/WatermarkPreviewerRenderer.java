package sharry.demo.picturepicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.sharry.lib.camera.PreviewerRendererImpl;
import com.sharry.lib.camera.PreviewerRendererWrapper;
import com.sharry.lib.opengles.GlUtil;

import java.nio.FloatBuffer;

/**
 * 带水印效果的渲染器
 * <p>
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-08-01 16:04
 */
public class WatermarkPreviewerRenderer extends PreviewerRendererWrapper {

    private static final String VERTEX_SHADER_STR = "attribute vec4 aVertexPosition;\n" +
            "    attribute vec2 aTexturePosition;\n" +
            "    varying vec2 vPosition;\n" +
            "    void main() {\n" +
            "        vPosition = aTexturePosition;\n" +
            "        gl_Position = aVertexPosition;\n" +
            "    }";


    private static final String FRAGMENT_SHADER_STR = "precision mediump float;\n" +
            "varying vec2 vPosition;\n" +
            "uniform sampler2D uTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor=texture2D(uTexture, vPosition);\n" +
            "}";

    /**
     * 相机顶点坐标
     */
    private final float[] mCameraVertexCoords = new float[]{
            -1f, 1f,  // 左上
            -1f, -1f, // 左下
            1f, 1f,   // 右上
            1f, -1f,   // 右下
    };

    /**
     * 相机纹理映射坐标
     */
    private final float[] mCameraTextureCoords = new float[]{
            0f, 1f,   // 左上
            0f, 0f,   // 左下
            1f, 1f,   // 右上
            1f, 0f    // 右下
    };

    /**
     * 水印顶点坐标
     */
    private final float[] mWatermarkVertexCoords = new float[]{
            0f, 0f,  // 左上
            0f, 0f,  // 左下
            0f, 0f,  // 右上
            0f, 0f,  // 右下
    };

    /**
     * 水印纹理坐标, 水印从 Bitmap 中加载, 坐标系相反
     */
    private final float[] mWatermarkTextureCoords = new float[]{
            0f, 0f,   // 左下
            0f, 1f,   // 左上
            1f, 0f,   // 右下
            1f, 1f    // 右上
    };

    /**
     * 相机纹理顶点和纹理坐标
     */
    private final FloatBuffer mCameraTextureVertexBuffer = GlUtil.createFloatBuffer(mCameraVertexCoords);
    private final FloatBuffer mCameraTextureBuffer = GlUtil.createFloatBuffer(mCameraTextureCoords);

    /**
     * 水印纹理顶点和纹理坐标
     */
    private final FloatBuffer mWatermarkVertexBuffer = GlUtil.createFloatBuffer(mWatermarkVertexCoords);
    private final FloatBuffer mWatermarkTextureBuffer = GlUtil.createFloatBuffer(mWatermarkTextureCoords);

    private final Context mContext;
    private int mProgramId;
    private int aVertexPosition;
    private int aTexturePosition;
    private int mVboId;
    private int mFramebufferId;
    private int mTextureId;
    private int uTexture;
    private int mWaterTextureId = 0;

    public WatermarkPreviewerRenderer(Context context) {
        super(new PreviewerRendererImpl(context));
        this.mContext = context;
    }

    @Override
    public void onEGLContextCreated() {
        super.onEGLContextCreated();
        // 上下文变更了, 重置数据
        reset();
        // 初始化程序
        setupShaders();
        // 初始化顶点坐标
        setupCoordinates();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);
        // 启用透明
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        // 确定宽高
        GLES20.glViewport(0, 0, width, height);
        // 配置纹理
        setupTexture(width, height);
        // 配置水印
        setupWatermark(width, height);
        // 配置 fbo
        setupFbo();
    }

    @Override
    protected void onDrawTexture(int textureId) {
        bindFramebuffer();
        // 绘制纹理
        drawCameraTexture2FBO(textureId);
        // 绘制水印
        drawWatermark2FBO();
        // 解绑
        unbindFramebuffer();
        // 绘制到系统自带的缓冲上
        drawToEGLSurface();
    }

    @Override
    public int getTextureId() {
        return mTextureId;
    }

    private void reset() {
        this.mProgramId = 0;
        this.mVboId = 0;
        this.mTextureId = 0;
        this.mFramebufferId = 0;
    }

    private void setupWatermark(int surfaceWidth, int surfaceHeight) {
        Bitmap bitmap = createTextureFromRes(R.drawable.ic_video_record_watermark);
        float height = bitmap.getHeight();
        float width = bitmap.getWidth();
        height = height * (1 / (float) surfaceHeight);
        width = width * (1 / (float) surfaceWidth);
        float left = -0.9f;
        float bottom = -0.9f;
        // 设置水印的位置
        // 左上
        mWatermarkVertexCoords[0] = left;
        mWatermarkVertexCoords[1] = bottom + height;
        // 左下
        mWatermarkVertexCoords[2] = left;
        mWatermarkVertexCoords[3] = bottom;
        // 右上
        mWatermarkVertexCoords[4] = left + width;
        mWatermarkVertexCoords[5] = bottom + height;
        // 右下
        mWatermarkVertexCoords[6] = left + width;
        mWatermarkVertexCoords[7] = bottom;
        // 更新 Buffer
        mWatermarkVertexBuffer.put(mWatermarkVertexCoords, 0, mWatermarkVertexCoords.length)
                .position(0);
        // 更新 VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        // 写入水印顶点坐标
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                mCameraVertexCoords.length * 4 +
                        mCameraTextureCoords.length * 4,
                mWatermarkVertexCoords.length * 4,
                mWatermarkVertexBuffer
        );
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    private void setupShaders() {
        if (mProgramId != 0) {
            return;
        }
        mProgramId = GlUtil.createProgram(VERTEX_SHADER_STR, FRAGMENT_SHADER_STR);
        aVertexPosition = GLES20.glGetAttribLocation(mProgramId, "aVertexPosition");
        aTexturePosition = GLES20.glGetAttribLocation(mProgramId, "aTexturePosition");
        uTexture = GLES20.glGetUniformLocation(mProgramId, "uTexture");
    }

    private void setupCoordinates() {
        if (mVboId != 0) {
            return;
        }
        // 创建 vbo
        int vboSize = 1;
        int[] vboIds = new int[vboSize];
        GLES20.glGenBuffers(vboSize, vboIds, 0);
        // 将顶点坐标写入 vbo
        mVboId = vboIds[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        // 开辟 VBO 空间
        GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                mCameraVertexCoords.length * 4 +
                        mCameraTextureCoords.length * 4
                        + mWatermarkVertexCoords.length * 4
                        + mWatermarkTextureCoords.length * 4,
                null,
                GLES20.GL_STATIC_DRAW
        );
        // 写入相机顶点坐标
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                0,
                mCameraVertexCoords.length * 4,
                mCameraTextureVertexBuffer
        );
        // 写入相机纹理坐标
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                mCameraVertexCoords.length * 4,
                mCameraTextureCoords.length * 4,
                mCameraTextureBuffer
        );
        // 写入水印顶点坐标
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                mCameraVertexCoords.length * 4 +
                        mCameraTextureCoords.length * 4,
                mWatermarkVertexCoords.length * 4,
                mWatermarkVertexBuffer
        );
        // 写入水印纹理坐标
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                mCameraVertexCoords.length * 4 +
                        mCameraTextureCoords.length * 4 +
                        mWatermarkVertexCoords.length * 4,
                mWatermarkTextureCoords.length * 4,
                mWatermarkTextureBuffer
        );
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    private void setupTexture(int width, int height) {
        if (mTextureId == 0) {
            int[] textureIds = new int[1];
            GLES20.glGenTextures(1, textureIds, 0);
            mTextureId = textureIds[0];
        }
        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        // 设置纹理环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        // 设置纹理过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        // 创建一个空的纹理画布
        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                width, height,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null
        );
        // 解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private void setupFbo() {
        if (mFramebufferId != 0) {
            return;
        }
        // 创建 fbo
        int[] fBoIds = new int[1];
        GLES20.glGenBuffers(1, fBoIds, 0);
        mFramebufferId = fBoIds[0];
        // 绑定 fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferId);
        // 将纹理绑定到 FBO 上, 作为颜色附件
        GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,  // 描述为颜色附件
                GLES20.GL_TEXTURE_2D,
                mTextureId,
                0
        );
        // 解绑 fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void bindFramebuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferId);
    }

    private void unbindFramebuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void drawCameraTexture2FBO(int textureId) {
        GLES20.glUseProgram(mProgramId);
        // 绑定相机的纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        // 写入顶点坐标
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        GLES20.glEnableVertexAttribArray(aVertexPosition);
        GLES20.glVertexAttribPointer(aVertexPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        // 写入纹理坐标
        GLES20.glEnableVertexAttribArray(aTexturePosition);
        GLES20.glVertexAttribPointer(aTexturePosition, 2, GLES20.GL_FLOAT, false,
                8, mCameraVertexCoords.length * 4);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        // 给 uTexture 赋值
        GLES20.glUniform1i(uTexture, 0);
        // 绘制到屏幕
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // 解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private void drawWatermark2FBO() {
        GLES20.glUseProgram(mProgramId);
        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWaterTextureId);
        // 写入水印顶点坐标
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        GLES20.glEnableVertexAttribArray(aVertexPosition);
        GLES20.glVertexAttribPointer(aVertexPosition, 2, GLES20.GL_FLOAT, false,
                8, (mCameraVertexCoords.length + mCameraTextureCoords.length) * 4);
        // 写入水印纹理坐标
        GLES20.glEnableVertexAttribArray(aTexturePosition);
        GLES20.glVertexAttribPointer(
                aTexturePosition,
                2,
                GLES20.GL_FLOAT,
                false,
                8,
                (mCameraVertexCoords.length + mCameraTextureCoords.length + mWatermarkVertexCoords.length) * 4
        );
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        // 给 uTexture 赋值
        GLES20.glUniform1i(uTexture, 0);
        // 绘制到屏幕
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // 解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private void drawToEGLSurface() {
        GLES20.glUseProgram(mProgramId);
        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        // 写入顶点坐标
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        GLES20.glEnableVertexAttribArray(aVertexPosition);
        GLES20.glVertexAttribPointer(aVertexPosition, 2, GLES20.GL_FLOAT, false,
                8, 0);
        // 写入纹理坐标
        GLES20.glEnableVertexAttribArray(aTexturePosition);
        GLES20.glVertexAttribPointer(aTexturePosition, 2, GLES20.GL_FLOAT, false,
                8, mCameraVertexCoords.length * 4);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        // 给 uTexture 赋值
        GLES20.glUniform1i(uTexture, 0);
        // 绘制到屏幕
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // 解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private Bitmap createTextureFromRes(int resId) {
        // 生成绑定纹理
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mWaterTextureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWaterTextureId);
        // 设置环绕方向
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        // 设置纹理过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        // 将 Bitmap 生成 2D 纹理
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        // 解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return bitmap;
    }

}
