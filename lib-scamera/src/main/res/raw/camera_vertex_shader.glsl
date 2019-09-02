attribute vec4 aVertexCoordinate;  // 传入参数: 顶点坐标, Java 传入
attribute vec4 aTextureCoordinate; // 传入参数: 纹理坐标, Java 传入
uniform mat4 uVertexMatrix;        // 全局参数: 4x4 顶点的裁剪矩阵, Java 传入
uniform mat4 uTextureMatrix;       // 全局参数: 4x4 矩阵纹理变化矩阵, Java 传入
varying vec2 vTextureCoordinate;   // 传出参数: 计算纹理坐标传递给 片元着色器
void main() {
    // 计算纹理坐标, 传出给片元着色器
    vTextureCoordinate = (uTextureMatrix * aTextureCoordinate).xy;
    // 计算顶点坐标, 输出给内建输出变量
    gl_Position = uVertexMatrix * aVertexCoordinate;
}