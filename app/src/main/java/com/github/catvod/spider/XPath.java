package com.github.catvod.spider;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.crawler.SpiderReq;
import com.github.catvod.crawler.SpiderReqResult;
import com.github.catvod.crawler.SpiderUrl;
import com.github.catvod.xpath.XPathRule;

import org.json.JSONArray;
import org.json.JSONObject;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XPath extends Spider {

    @Override
    public void init(Context context) {
        super.init(context);
    }

    private XPathRule rule = null;

    public void init(Context context, String extend) {
        super.init(context, extend);
        rule = XPathRule.fromJson(extend);
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            String webUrl = rule.getHomeUrl();
            SpiderDebug.log(webUrl);
            SpiderUrl su = new SpiderUrl(webUrl, getHeaders(webUrl));
            SpiderReqResult srr = SpiderReq.get(su);
            JSONObject result = new JSONObject();
            JSONArray classes = new JSONArray();
            JXDocument doc = JXDocument.create(srr.content);
            List<JXNode> navNodes = doc.selN(rule.getCateNode());
            for (int i = 0; i < navNodes.size(); i++) {
                String name = navNodes.get(i).selOne(rule.getCateName()).asString().trim();
                name = rule.getCateNameR(name);
                String id = navNodes.get(i).selOne(rule.getCateId()).asString().trim();
                id = rule.getCateIdR(id);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type_id", id);
                jsonObject.put("type_name", name);
                classes.put(jsonObject);
            }
            result.put("class", classes);
            try {
                if (!rule.getHomeVodNode().isEmpty()) {
                    JSONArray videos = new JSONArray();
                    List<JXNode> vodNodes = doc.selN(rule.getHomeVodNode());
                    for (int i = 0; i < vodNodes.size(); i++) {
                        String name = vodNodes.get(i).selOne(rule.getHomeVodName()).asString().trim();
                        name = rule.getHomeVodNameR(name);
                        String id = vodNodes.get(i).selOne(rule.getHomeVodId()).asString().trim();
                        id = rule.getHomeVodIdR(id);
                        String pic = vodNodes.get(i).selOne(rule.getHomeVodImg()).asString().trim();
                        pic = rule.getHomeVodImgR(pic);
                        pic = fixUrl(webUrl, pic);
                        String mark = vodNodes.get(i).selOne(rule.getHomeVodMark()).asString().trim();
                        mark = rule.getHomeVodMarkR(mark);
                        JSONObject v = new JSONObject();
                        v.put("vod_id", id);
                        v.put("vod_name", name);
                        v.put("vod_pic", pic);
                        v.put("vod_remarks", mark);
                        videos.put(v);
                    }
                    result.put("list", videos);
                }
            } catch (Exception e) {
                SpiderDebug.log(e);
            }
            return result.toString();
        } catch (
                Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    private HashMap<String, String> getHeaders(String url) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", rule.getUa().isEmpty()
                ? "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.54 Safari/537.36"
                : rule.getUa());
        return headers;
    }

    @Override
    public String homeVideoContent() {
        try {
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            String webUrl = rule.getCateUrl().replace("{cateId}", tid).replace("{catePg}", pg);
            SpiderDebug.log(webUrl);
            SpiderUrl su = new SpiderUrl(webUrl, getHeaders(webUrl));
            SpiderReqResult srr = SpiderReq.get(su);
            JSONArray videos = new JSONArray();
            JXDocument doc = JXDocument.create(srr.content);
            List<JXNode> vodNodes = doc.selN(rule.getCateVodNode());
            for (int i = 0; i < vodNodes.size(); i++) {
                String name = vodNodes.get(i).selOne(rule.getCateVodName()).asString().trim();
                name = rule.getCateVodNameR(name);
                String id = vodNodes.get(i).selOne(rule.getCateVodId()).asString().trim();
                id = rule.getCateVodIdR(id);
                String pic = vodNodes.get(i).selOne(rule.getCateVodImg()).asString().trim();
                pic = rule.getCateVodImgR(pic);
                pic = fixUrl(webUrl, pic);
                String mark = "";
                if (!rule.getCateVodMark().isEmpty()) {
                    try {
                        vodNodes.get(i).selOne(rule.getCateVodMark()).asString().trim();
                        mark = rule.getCateVodMarkR(mark);
                    } catch (Exception e) {
                        SpiderDebug.log(e);
                    }
                }
                JSONObject v = new JSONObject();
                v.put("vod_id", id);
                v.put("vod_name", name);
                v.put("vod_pic", pic);
                v.put("vod_remarks", mark);
                videos.put(v);
            }
            JSONObject result = new JSONObject();
            result.put("page", pg);
            result.put("pagecount", Integer.MAX_VALUE);
            result.put("limit", 90);
            result.put("total", Integer.MAX_VALUE);
            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }


    @Override
    public String detailContent(List<String> ids) {
        try {
            String webUrl = rule.getDetailUrl().replace("{vid}", ids.get(0));
            SpiderDebug.log(webUrl);
            SpiderUrl su = new SpiderUrl(webUrl, getHeaders(webUrl));
            SpiderReqResult srr = SpiderReq.get(su);
            JSONArray videos = new JSONArray();
            JXDocument doc = JXDocument.create(srr.content);
            JXNode vodNode = doc.selNOne(rule.getDetailNode());

            String cover = "", title = "", desc = "", category = "", area = "", year = "", remark = "", director = "", actor = "";

            title = vodNode.selOne(rule.getDetailName()).asString().trim();
            title = rule.getDetailNameR(title);

            cover = vodNode.selOne(rule.getDetailImg()).asString().trim();
            cover = rule.getDetailImgR(cover);
            cover = fixUrl(webUrl, cover);

            if (!rule.getDetailCate().isEmpty()) {
                try {
                    category = vodNode.selOne(rule.getDetailCate()).asString().trim();
                    category = rule.getDetailCateR(category);
                } catch (Exception e) {
                    SpiderDebug.log(e);
                }
            }
            if (!rule.getDetailYear().isEmpty()) {
                try {
                    year = vodNode.selOne(rule.getDetailYear()).asString().trim();
                    year = rule.getDetailYearR(year);
                } catch (Exception e) {
                    SpiderDebug.log(e);
                }
            }
            if (!rule.getDetailArea().isEmpty()) {
                try {
                    area = vodNode.selOne(rule.getDetailArea()).asString().trim();
                    area = rule.getDetailAreaR(area);
                } catch (Exception e) {
                    SpiderDebug.log(e);
                }
            }
            if (!rule.getDetailMark().isEmpty()) {
                try {
                    remark = vodNode.selOne(rule.getDetailMark()).asString().trim();
                    remark = rule.getDetailMarkR(remark);
                } catch (Exception e) {
                    SpiderDebug.log(e);
                }
            }
            if (!rule.getDetailActor().isEmpty()) {
                try {
                    actor = vodNode.selOne(rule.getDetailActor()).asString().trim();
                    actor = rule.getDetailActorR(actor);
                } catch (Exception e) {
                    SpiderDebug.log(e);
                }
            }
            if (!rule.getDetailActor().isEmpty()) {
                try {
                    actor = vodNode.selOne(rule.getDetailActor()).asString().trim();
                    actor = rule.getDetailActorR(actor);
                } catch (Exception e) {
                    SpiderDebug.log(e);
                }
            }
            if (!rule.getDetailDirector().isEmpty()) {
                try {
                    director = vodNode.selOne(rule.getDetailDirector()).asString().trim();
                    director = rule.getDetailDirectorR(director);
                } catch (Exception e) {
                    SpiderDebug.log(e);
                }
            }
            if (!rule.getDetailDesc().isEmpty()) {
                try {
                    desc = vodNode.selOne(rule.getDetailDesc()).asString().trim();
                    desc = rule.getDetailDescR(desc);
                } catch (Exception e) {
                    SpiderDebug.log(e);
                }
            }

            JSONObject vod = new JSONObject();
            vod.put("vod_id", ids.get(0));
            vod.put("vod_name", title);
            vod.put("vod_pic", cover);
            vod.put("type_name", category);
            vod.put("vod_year", year);
            vod.put("vod_area", area);
            vod.put("vod_remarks", remark);
            vod.put("vod_actor", actor);
            vod.put("vod_director", director);
            vod.put("vod_content", desc);

            ArrayList<String> playFrom = new ArrayList<>();

            List<JXNode> fromNodes = doc.selN(rule.getDetailFromNode());
            for (int i = 0; i < fromNodes.size(); i++) {
                String name = fromNodes.get(i).selOne(rule.getDetailFromName()).asString().trim();
                name = rule.getDetailFromNameR(name);
                playFrom.add(name);
            }

            ArrayList<String> playList = new ArrayList<>();
            List<JXNode> urlListNodes = doc.selN(rule.getDetailUrlNode());
            for (int i = 0; i < urlListNodes.size(); i++) {
                List<JXNode> urlNodes = urlListNodes.get(i).sel(rule.getDetailUrlSubNode());
                List<String> vodItems = new ArrayList<>();
                for (int j = 0; j < urlNodes.size(); j++) {
                    String name = urlNodes.get(j).selOne(rule.getDetailUrlName()).asString().trim();
                    name = rule.getDetailUrlNameR(name);
                    String id = urlNodes.get(j).selOne(rule.getDetailUrlId()).asString().trim();
                    id = rule.getDetailUrlIdR(id);
                    vodItems.add(name + "$" + id);
                }
                playList.add(TextUtils.join("#", vodItems));
            }

            String vod_play_from = TextUtils.join("$$$", playFrom);
            String vod_play_url = TextUtils.join("$$$", playList);
            vod.put("vod_play_from", vod_play_from);
            vod.put("vod_play_url", vod_play_url);

            JSONObject result = new JSONObject();
            JSONArray list = new JSONArray();
            list.put(vod);
            result.put("list", list);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            String webUrl = rule.getPlayUrl().isEmpty() ? id : rule.getPlayUrl().replace("{playUrl}", id);
            SpiderDebug.log(webUrl);
            JSONObject result = new JSONObject();
            result.put("parse", 1);
            result.put("playUrl", "");
            if (!rule.getPlayUa().isEmpty()) {
                result.put("ua", rule.getPlayUa());
            }
            result.put("url", webUrl);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String webUrl = rule.getSearchUrl().replace("{wd}", URLEncoder.encode(key));
            SpiderDebug.log(webUrl);
            SpiderUrl su = new SpiderUrl(webUrl, getHeaders(webUrl));
            SpiderReqResult srr = SpiderReq.get(su);
            JSONObject result = new JSONObject();
            JXDocument doc = JXDocument.create(srr.content);
            JSONArray videos = new JSONArray();
            List<JXNode> vodNodes = doc.selN(rule.getSearchVodNode());
            for (int i = 0; i < vodNodes.size(); i++) {
                String name = vodNodes.get(i).selOne(rule.getSearchVodName()).asString().trim();
                name = rule.getSearchVodNameR(name);
                String id = vodNodes.get(i).selOne(rule.getSearchVodId()).asString().trim();
                id = rule.getSearchVodIdR(id);
                String pic = vodNodes.get(i).selOne(rule.getSearchVodImg()).asString().trim();
                pic = rule.getSearchVodImgR(pic);
                pic = fixUrl(webUrl, pic);
                String mark = vodNodes.get(i).selOne(rule.getSearchVodMark()).asString().trim();
                mark = rule.getSearchVodMarkR(mark);
                JSONObject v = new JSONObject();
                v.put("vod_id", id);
                v.put("vod_name", name);
                v.put("vod_pic", pic);
                v.put("vod_remarks", mark);
                videos.put(v);
            }
            result.put("list", videos);
            return result.toString();
        } catch (
                Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    private String fixUrl(String base, String src) {
        try {
            if (!src.contains("://")) {
                Uri parse = Uri.parse(base);
                src = parse.getScheme() + "://" + parse.getHost() + src;
            }
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return src;
    }

    @Override
    public boolean manualVideoCheck() {
        return true;
    }

    private String[] videoFormatList = new String[]{".m3u8", ".mp4", ".mpeg", ".flv"};

    @Override
    public boolean isVideoFormat(String url) {
        url = url.toLowerCase();
        if (url.contains("=http") || url.contains("=https") || url.contains("=https%3a%2f") || url.contains("=http%3a%2f")) {
            return false;
        }
        for (String format : videoFormatList) {
            if (url.contains(format)) {
                return true;
            }
        }
        return false;
    }
}
