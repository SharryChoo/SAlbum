// 着色器纹理扩展类型
#extension GL_OES_EGL_image_external : require
// 设置精度，中等精度
precision mediump float;
// varying 可用于相互传值
varying vec2 vPosition;
// 2D 纹理 ，uniform 用于 application 向 gl 传值 （扩展纹理）
uniform sampler2D uSamplerY;
uniform sampler2D uSamplerU;
uniform sampler2D uSamplerV;
void main() {
    float y, u, v;
    y = texture2D(uSamplerY, vPosition).r;
    u = texture2D(uSamplerU, vPosition).r - 0.5;
    v = texture2D(uSamplerV, vPosition).r - 0.5;

    vec3 rgb;
    rgb.r = y + 1.403 * v;
    rgb.g = y - 0.344 * u - 0.714 * v;
    rgb.b = y + 1.770 * u;

    gl_FragColor = vec4(rgb, 1.0);
}
