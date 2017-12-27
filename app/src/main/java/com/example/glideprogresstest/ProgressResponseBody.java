package com.example.glideprogresstest;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * @author: 巴黎没有摩天轮Li
 * @description:
 * @date: Created in 下午8:31 2017/12/26
 * @modified by:
 */
public class ProgressResponseBody extends ResponseBody {
    private static final String TAG = "ProgressResponseBody";
    private BufferedSource mBufferedSource;
    private ResponseBody mResponseBody;
    private ProgressListener mListener;

    /**
     * 获取拦截器传递过来的responseBody
     */
    public ProgressResponseBody(String url, ResponseBody responseBody) {
        this.mResponseBody = responseBody;
        mListener = ProgressInterceptor.LISTENER_MAP.get(url);
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (mBufferedSource == null) {
            mBufferedSource = Okio.buffer(new ProgressSource(mResponseBody.source()));
        }
        return mBufferedSource;
    }

    private class ProgressSource extends ForwardingSource {
        long totalBytesRead = 0;
        int currentProgress;

        public ProgressSource(Source source) {
            super(source);
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            //读到的总字节数 也就是进行下载过程中的字节数
            long bytesRead = super.read(sink, byteCount);
            //整个文件的size
            long fullLength = mResponseBody.contentLength();

            if (bytesRead == -1) {
                //说明下载完毕
                totalBytesRead = fullLength;
            } else {
                //下载进行中
                totalBytesRead += bytesRead;
            }
            //获取进度
            int progress = (int) (100f * totalBytesRead / fullLength);
            Log.d(TAG, "Download progress is" + progress);

            //回调进度
            if (mListener != null && progress != currentProgress) {
                mListener.onProgress(progress);
            }
            if (mListener != null && totalBytesRead == fullLength) {
                mListener = null;
            }

            currentProgress = progress;
            return bytesRead;

        }
    }
}
