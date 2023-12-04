package com.hx.analyze_h264;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class H264Player implements Runnable {

    private final String TAG = "liudehua";
    private String path;
    private Surface surface;
    private MediaCodec mediaCodec;
    private int endFlag = 0;

    public H264Player(String path, Surface surface) {
        this.path = path;
        this.surface = surface;
        try {
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat videoMediaFormat = MediaFormat.createVideoFormat("video/avc", 368, 384);
            videoMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            // 添加配置
            mediaCodec.configure(videoMediaFormat, surface, null, 0);

        } catch (IOException e) {
            Log.e(TAG, "H264Player: e = " + e);
            throw new RuntimeException(e);
        }
    }

    public void play() {
        mediaCodec.start();
        new Thread(this).start();
    }

    @Override
    public void run() {
        byte[] bytes;
        try {
            Path filePath = Paths.get(path);
            bytes = Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int startIndex = 0;
        final int totalSize = bytes.length;

        // mediaCodec 内部队列 , 可用缓冲区索引
        int inputBufferIndex;
        int outputBufferIndex;

        while (true) {

            // 获取可用缓冲区索引,阻塞等待
            inputBufferIndex = mediaCodec.dequeueInputBuffer(10000);
            if (inputBufferIndex >= 0) {
                // 获取缓冲区冲区对象
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                inputBuffer.clear();
                // 寻找分隔符 ， 0x 00 00 00 01
                int nextFrameStart = findByFrame(bytes, startIndex, totalSize);
                if (nextFrameStart != -1) {
                    // 将每一个Nalu单元数据写入缓冲区
                    inputBuffer.put(bytes, startIndex, nextFrameStart - startIndex);
                    // 提交给MediaCodec ，进行后续的解码操作
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, nextFrameStart - startIndex, 0, 0);

                    // 文件指针偏移量
                    startIndex = nextFrameStart + 1;
                } else {
                    // 文件末尾没有分隔符， 依然把数据给到MediaCodec输入缓冲区
                    inputBuffer.put(bytes, startIndex, totalSize - startIndex);
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, totalSize - startIndex, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    Log.i(TAG, "run: 文件读取完毕，发送结束标志");
                    Log.i(TAG, "run: startIndex = " + startIndex);
                    Log.i(TAG, "run: totalSize - startIndex = " + (totalSize - startIndex));
                    Log.i(TAG, "run: totalSize = " + totalSize);
                    endFlag = -1;
                }

                //得到数据
                MediaCodec.BufferInfo outputBufferInfo = new MediaCodec.BufferInfo();
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(outputBufferInfo, 100000);
                if (outputBufferIndex >= 0) {
                    // 释放并输出到Surface中
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                    if (endFlag == -1) {
                        break;
                    }
                }
            }
        }

        Log.i(TAG, "run: 退出循环");

    }


    /**
     * 根据分隔符取寻找
     */
    private int findByFrame(byte[] bytes, int startIndex, int totalSize) {
        for (int i = startIndex; i < totalSize-4; i++) {
            if (bytes[i] == 0x00 && bytes[i + 1] == 0x00 && bytes[i + 2] == 0x00 && bytes[i + 3] == 0x01) {
                return i;
            }

        }
        return -1;
    }

    public void destroyed() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
    }
}
