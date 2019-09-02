// 定义一个属性，顶点坐标
attribute vec4 aVertexPosition;
// 定义一个属性，纹理坐标
attribute vec2 aTexturePosition;
// 顶点变换矩阵
uniform mat4 uVertexMatrix;
// varying 可用于相互传值
varying vec2 vPosition;
void main() {
    // 赋值 ft_Position，gl_Position 变量是 gl 内置的
    vPosition = aTexturePosition;
    // 根据 u_Matrix 计算出裁剪坐标系的位置
    gl_Position = aVertexPosition * uVertexMatrix;
}
