#extension GL_OES_EGL_image_external : require
// 设置精度，中等精度
precision mediump float;
// 由顶点着色器输出, 经过栅格化转换之后的纹理坐标
varying vec2 vTextureCoordinate;
// 2D 纹理, uniform 用于 application 向 gl 传值 （扩展纹理）
uniform samplerExternalOES uTexture;
void main(){
    gl_FragColor = texture2D(uTexture, vTextureCoordinate);
}