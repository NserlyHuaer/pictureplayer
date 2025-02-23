#include <jni.h>
#include <immintrin.h>
#include <stdint.h>

// 提前声明边界处理函数
static void processBoundary(int32_t* src, int32_t* dest, int width, int height, int halfKernel);
static inline void processPixel(int32_t* src, int32_t* dest, int width, int height, int x, int y, int halfKernel);

// 修复JNI调用语法和类型转换
JNIEXPORT void JNICALL Java_Tools_ImageManager_AdvancedImageBlur_processWithSIMD(
    JNIEnv *env, jclass clazz,
    jobject srcSegment, jobject destSegment,
    jint width, jint height, jint kernelSize) {

    // 正确获取内存地址
    void* srcPtr = (*env)->GetDirectBufferAddress(env, srcSegment);
    void* destPtr = (*env)->GetDirectBufferAddress(env, destSegment);
    
    int32_t* src = (int32_t*)srcPtr;
    int32_t* dest = (int32_t*)destPtr;

    const int halfKernel = kernelSize / 2;
    const int vecLanes = 8;

    // 手动实现向量除法（因AVX2没有整数除法指令）
    __m256i divisor = _mm256_set1_epi32(kernelSize * kernelSize);

    #pragma omp parallel for collapse(2)
    for (int y = halfKernel; y < height - halfKernel; y++) {
        for (int x = halfKernel; x < width - halfKernel; x += vecLanes) {
            __m256i red_sum = _mm256_setzero_si256();
            __m256i green_sum = _mm256_setzero_si256();
            __m256i blue_sum = _mm256_setzero_si256();

            for (int ky = -halfKernel; ky <= halfKernel; ky++) {
                for (int kx = -halfKernel; kx <= halfKernel; kx++) {
                    __m256i pixels = _mm256_loadu_si256(
                        (__m256i*)(src + (y + ky) * width + x + kx)
                    );

                    // 使用移位替代掩码操作
                    __m256i red = _mm256_srli_epi32(pixels, 16);
                    red = _mm256_and_si256(red, _mm256_set1_epi32(0xFF));
                    
                    __m256i green = _mm256_srli_epi32(pixels, 8);
                    green = _mm256_and_si256(green, _mm256_set1_epi32(0xFF));
                    
                    __m256i blue = _mm256_and_si256(pixels, _mm256_set1_epi32(0xFF));

                    red_sum = _mm256_add_epi32(red_sum, red);
                    green_sum = _mm256_add_epi32(green_sum, green);
                    blue_sum = _mm256_add_epi32(blue_sum, blue);
                }
            }

            // 通过乘法实现除法（仅适用于kernelSize平方数）
            __m256i inv_divisor = _mm256_set1_epi32(0x10000000 / (kernelSize * kernelSize));
            __m256i avg_red = _mm256_mullo_epi32(red_sum, inv_divisor);
            avg_red = _mm256_srli_epi32(avg_red, 28);
            
            __m256i avg_green = _mm256_mullo_epi32(green_sum, inv_divisor);
            avg_green = _mm256_srli_epi32(avg_green, 28);
            
            __m256i avg_blue = _mm256_mullo_epi32(blue_sum, inv_divisor);
            avg_blue = _mm256_srli_epi32(avg_blue, 28);

            __m256i result = _mm256_slli_epi32(avg_red, 16)
                           | _mm256_slli_epi32(avg_green, 8)
                           | avg_blue;

            _mm256_storeu_si256((__m256i*)(dest + y * width + x), result);
        }
    }

    processBoundary(src, dest, width, height, halfKernel);
}

// 边界处理函数
static void processBoundary(int32_t* src, int32_t* dest, 
                           int width, int height, int halfKernel) {
    // 上/下边界
    for (int y = 0; y < halfKernel; y++) {
        for (int x = 0; x < width; x++) {
            processPixel(src, dest, width, height, x, y, halfKernel);
        }
    }
    for (int y = height - halfKernel; y < height; y++) {
        for (int x = 0; x < width; x++) {
            processPixel(src, dest, width, height, x, y, halfKernel);
        }
    }

    // 左/右边界
    for (int y = halfKernel; y < height - halfKernel; y++) {
        for (int x = 0; x < halfKernel; x++) {
            processPixel(src, dest, width, height, x, y, halfKernel);
        }
        for (int x = width - halfKernel; x < width; x++) {
            processPixel(src, dest, width, height, x, y, halfKernel);
        }
    }
}

// 单个像素处理
static inline void processPixel(int32_t* src, int32_t* dest, 
                               int width, int height,
                               int x, int y, int halfKernel) {
    int red = 0, green = 0, blue = 0;
    int count = 0;

    for (int ky = -halfKernel; ky <= halfKernel; ky++) {
        for (int kx = -halfKernel; kx <= halfKernel; kx++) {
            int px = x + kx;
            int py = y + ky;
            
            if (px >= 0 && px < width && py >= 0 && py < height) {
                int32_t pixel = src[py * width + px];
                red += (pixel >> 16) & 0xFF;
                green += (pixel >> 8) & 0xFF;
                blue += pixel & 0xFF;
                count++;
            }
        }
    }

    dest[y * width + x] = ((red / count) << 16) 
                         | ((green / count) << 8) 
                         | (blue / count);
}