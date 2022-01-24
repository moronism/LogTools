package com.howard.www.handle.bean;
import nl.basjes.parse.core.Field;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NginxAccessLogRecord {
    private static final Logger logger = LogManager.getLogger("dumpData");
    private String urlQuery;

    private String urlPath;

    private String referer;

    private String userAgent;

    private String receiveTime;

    private String status;

    private String method;

    private String clientHost;

    @Field("HTTP.QUERYSTRING:request.firstline.uri.query")
    public void setUrlQuery(String urlQuery) {
        this.urlQuery = urlQuery;
    }
    @Field("HTTP.PATH:request.firstline.uri.path")
    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }
    @Field("HTTP.URI:request.referer")
    public void setReferer(String referer) {
        this.referer = referer;
    }
    @Field("HTTP.USERAGENT:request.user-agent")
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    @Field("TIME.STAMP:request.receive.time")
    public void setReceiveTime(String receiveTime) {
        //01/Jan/2022:09:57:14 +0800
        //dd/MMM/yyyy:HH:mm:ss 格林尼治标准时间+0800
        //Sun Jan 29 14:34:06 格林尼治标准时间+0800 2012
        //EEE MMM dd HH:mm:ss 格林尼治标准时间+0800 yyyy
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss +0800", Locale.ENGLISH);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(receiveTime);
            //logger.info(simpleDateFormat.format(date));
            this.receiveTime=simpleDateFormat.format(date);
        }catch (Exception e){
            this.receiveTime = receiveTime;
        }

    }
    @Field("STRING:request.status.last")
    public void setStatus(String status) {
        this.status = status;
    }
    @Field("HTTP.METHOD:request.firstline.method")
    public void setMethod(String method) {
        this.method = method;
    }
    @Field("IP:connection.client.host")
    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public String getUrlQuery() {
        return urlQuery;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public String getReferer() {
        return referer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getReceiveTime() {

        return receiveTime;
    }

    public String getStatus() {
        return status;
    }

    public String getMethod() {
        return method;
    }

    public String getClientHost() {
        return clientHost;
    }
}
