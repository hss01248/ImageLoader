package com.hss01248.downloader_m3u8.m3u8;

import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M3U8Utils {

    /**
     * parse network M3U8 file.
     *
     * @return
     * @throws IOException
     */


    public static M3U8 parseLocalM3U8File(File m3u8File) throws IOException {
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(m3u8File));
            bufferedReader = new BufferedReader(inputStreamReader);
            M3U8 m3u8 = new M3U8();
            float tsDuration = 0;
            String byteRange = "";
            int targetDuration = 0;
            int tsIndex = 0;
            int version = 0;
            int sequence = 0;
            boolean hasDiscontinuity = false;
            boolean hasKey = false;
            boolean hasInitSegment = false;
            String method = null;
            String encryptionIV = null;
            String encryptionKeyUri = null;
            String initSegmentUri = null;
            String segmentByteRange = null;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                LogUtils.i(DownloadConstants.TAG, "line = " + line);
                if (line.startsWith(M3U8Constants.TAG_PREFIX)) {
                    if (line.startsWith(M3U8Constants.TAG_MEDIA_DURATION)) {
                        String ret = parseStringAttr(line, M3U8Constants.REGEX_MEDIA_DURATION);
                        if (!TextUtils.isEmpty(ret)) {
                            tsDuration = Float.parseFloat(ret);
                        }
                    }  else if (line.startsWith(M3U8Constants.TAG_BYTERANGE)) {
                        byteRange = parseStringAttr(line, M3U8Constants.REGEX_BYTERANGE);
                    } else if (line.startsWith(M3U8Constants.TAG_TARGET_DURATION)) {
                        String ret = parseStringAttr(line, M3U8Constants.REGEX_TARGET_DURATION);
                        if (!TextUtils.isEmpty(ret)) {
                            targetDuration = Integer.parseInt(ret);
                        }
                    } else if (line.startsWith(M3U8Constants.TAG_VERSION)) {
                        String ret = parseStringAttr(line, M3U8Constants.REGEX_VERSION);
                        if (!TextUtils.isEmpty(ret)) {
                            version = Integer.parseInt(ret);
                        }
                    } else if (line.startsWith(M3U8Constants.TAG_MEDIA_SEQUENCE)) {
                        String ret = parseStringAttr(line, M3U8Constants.REGEX_MEDIA_SEQUENCE);
                        if (!TextUtils.isEmpty(ret)) {
                            sequence = Integer.parseInt(ret);
                        }
                    } else if (line.startsWith(M3U8Constants.TAG_DISCONTINUITY)) {
                        hasDiscontinuity = true;
                    } else if (line.startsWith(M3U8Constants.TAG_KEY)) {
                        hasKey = true;
                        method = parseOptionalStringAttr(line, M3U8Constants.REGEX_METHOD);
                        String keyFormat = parseOptionalStringAttr(line, M3U8Constants.REGEX_KEYFORMAT);
                        if (!M3U8Constants.METHOD_NONE.equals(method)) {
                            encryptionIV = parseOptionalStringAttr(line, M3U8Constants.REGEX_IV);
                            if (M3U8Constants.KEYFORMAT_IDENTITY.equals(keyFormat) || keyFormat == null) {
                                if (M3U8Constants.METHOD_AES_128.equals(method)) {
                                    // The segment is fully encrypted using an identity key.
                                    encryptionKeyUri = parseStringAttr(line, M3U8Constants.REGEX_URI);
                                } else {
                                    // Do nothing. Samples are encrypted using an identity key,
                                    // but this is not supported. Hopefully, a traditional DRM
                                    // alternative is also provided.
                                }
                            } else {
                                // Do nothing.
                            }
                        }
                    } else if (line.startsWith(M3U8Constants.TAG_INIT_SEGMENT)) {
                        initSegmentUri = parseStringAttr(line, M3U8Constants.REGEX_URI);
                        if (!TextUtils.isEmpty(initSegmentUri)) {
                            hasInitSegment = true;
                            segmentByteRange = parseOptionalStringAttr(line, M3U8Constants.REGEX_ATTR_BYTERANGE);
                        }
                    }
                    continue;
                }
                M3U8Seg ts = new M3U8Seg();
                ts.initTsAttributes(line, tsDuration, tsIndex, sequence++, hasDiscontinuity, byteRange);
                if (hasKey) {
                    ts.setKeyConfig(method, encryptionKeyUri, encryptionIV);
                }
                if (hasInitSegment) {
                    ts.setInitSegmentInfo(initSegmentUri, segmentByteRange);
                }
                m3u8.addTs(ts);
                tsIndex++;
                tsDuration = 0;
                hasDiscontinuity = false;
                hasKey = false;
                hasInitSegment = false;
                method = null;
                encryptionKeyUri = null;
                encryptionIV = null;
                initSegmentUri = null;
                segmentByteRange = null;
            }
            m3u8.setTargetDuration(targetDuration);
            m3u8.setVersion(version);
            m3u8.setSequence(sequence);
            return m3u8;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            VideoDownloadUtils.close(inputStreamReader);
            VideoDownloadUtils.close(bufferedReader);
        }
    }

    public static String parseStringAttr(String line, Pattern pattern) {
        if (pattern == null)
            return null;
        Matcher matcher = pattern.matcher(line);
        if (matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1);
        }
        return null;
    }

    public static String parseOptionalStringAttr(String line, Pattern pattern) {
        if (pattern == null)
            return null;
        Matcher matcher = pattern.matcher(line);
        return matcher.find() ? matcher.group(1) : null;
    }



    public static String getM3U8AbsoluteUrl(String videoUrl, String line) {
        if (TextUtils.isEmpty(videoUrl) || TextUtils.isEmpty(line)) {
            return "";
        }
        if (videoUrl.startsWith("file://") || videoUrl.startsWith("/")) {
            return videoUrl;
        }
        String baseUriPath = getBaseUrl(videoUrl);
        String hostUrl = getHostUrl(videoUrl);
        if (line.startsWith("//")) {
            String tempUrl = getSchema(videoUrl) + ":" + line;
            return tempUrl;
        }
        if (line.startsWith("/")) {
            String pathStr = getPathStr(videoUrl);
            String longestCommonPrefixStr = getLongestCommonPrefixStr(pathStr, line);
            if (hostUrl.endsWith("/")) {
                hostUrl = hostUrl.substring(0, hostUrl.length() - 1);
            }
            String tempUrl = hostUrl + longestCommonPrefixStr + line.substring(longestCommonPrefixStr.length());
            return tempUrl;
        }
        if (line.startsWith("http")) {
            return line;
        }
        return baseUriPath + line;
    }

    private static String getSchema(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        int index = url.indexOf("://");
        if (index != -1) {
            String result = url.substring(0, index);
            return result;
        }
        return "";
    }

    /**
     * 例如https://xvideo.d666111.com/xvideo/taohuadao56152307/index.m3u8
     * 我们希望得到https://xvideo.d666111.com/xvideo/taohuadao56152307/
     *
     * @param url
     * @return
     */
    public static String getBaseUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        int slashIndex = url.lastIndexOf("/");
        if (slashIndex != -1) {
            return url.substring(0, slashIndex + 1);
        }
        return url;
    }

    /**
     * 例如https://xvideo.d666111.com/xvideo/taohuadao56152307/index.m3u8
     * 我们希望得到https://xvideo.d666111.com/
     *
     * @param url
     * @return
     */
    public static String getHostUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        try {
            URL formatURL = new URL(url);
            String host = formatURL.getHost();
            if (host == null) {
                return url;
            }
            int hostIndex = url.indexOf(host);
            if (hostIndex != -1) {
                int port = formatURL.getPort();
                String resultUrl;
                if (port != -1) {
                    resultUrl = url.substring(0, hostIndex + host.length()) + ":" + port + "/";
                } else {
                    resultUrl = url.substring(0, hostIndex + host.length()) + "/";
                }
                return resultUrl;
            }
            return url;
        } catch (Exception e) {
            return url;
        }
    }

    /**
     * 例如https://xvideo.d666111.com/xvideo/taohuadao56152307/index.m3u8
     * 我们希望得到   /xvideo/taohuadao56152307/index.m3u8
     *
     * @param url
     * @return
     */
    public static String getPathStr(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String hostUrl = getHostUrl(url);
        if (TextUtils.isEmpty(hostUrl)) {
            return url;
        }
        return url.substring(hostUrl.length() - 1);
    }

    /**
     * 获取两个字符串的最长公共前缀
     * /xvideo/taohuadao56152307/500kb/hls/index.m3u8   与     /xvideo/taohuadao56152307/index.m3u8
     * <p>
     * /xvideo/taohuadao56152307/500kb/hls/jNd4fapZ.ts  与     /xvideo/taohuadao56152307/500kb/hls/index.m3u8
     *
     * @param str1
     * @param str2
     * @return
     */
    public static String getLongestCommonPrefixStr(String str1, String str2) {
        if (TextUtils.isEmpty(str1) || TextUtils.isEmpty(str2)) {
            return "";
        }
        if (TextUtils.equals(str1, str2)) {
            return str1;
        }
        char[] arr1 = str1.toCharArray();
        char[] arr2 = str2.toCharArray();
        int j = 0;
        while (j < arr1.length && j < arr2.length) {
            if (arr1[j] != arr2[j]) {
                break;
            }
            j++;
        }
        return str1.substring(0, j);
    }
}

