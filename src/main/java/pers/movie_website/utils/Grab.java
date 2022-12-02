package pers.movie_website.utils;


import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import pers.movie_website.pojo.Movie;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 *  抓取豆瓣电影
 */


public class Grab implements Callable<Movie> {
    public static String movie_from = "https://movie.douban.com/j/new_search_subjects?sort=U&range=0,10&tags=";

    @Override
    public Movie call() throws Exception {
        Movie movie = null;
        JSONObject jObjectList = new JSONObject(getContent(movie_from+"电影&start=0"));
        JSONArray jsonArray = jObjectList.getJSONArray("data");
        for (int i = 0; i < jsonArray.length(); i++) {
            movie = new Movie();
            JSONObject jsonObjectDetail = jsonArray.getJSONObject(i);
            movie.setName(jsonObjectDetail.getString("title"));
            movie.setMovieUrl(jsonObjectDetail.getString("url"));
           // movie.setDate(jsonObjectDetail.getString(""));
            movie.setScore(jsonObjectDetail.getString("rate"));
        }
        return movie;
    }

    public String getContent(String url) {
        String resp = "";
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        CloseableHttpResponse closeableHttpResponse  = null;
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("User-Agent",
                ": Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36 Edg/92.0.902.78");
        RequestConfig requestConfig =RequestConfig.custom()
                .setConnectTimeout(10*1000).setSocketTimeout(10*100).build();
        httpGet.setConfig(requestConfig);
        try{
            closeableHttpResponse = closeableHttpClient.execute(httpGet);
            if(closeableHttpResponse.getStatusLine().getStatusCode()  == 200){
                resp = EntityUtils.toString(closeableHttpResponse.getEntity(),"UTF-8");
            }
        }catch (IOException i){
            i.printStackTrace();
        }finally {
            try {
                if(closeableHttpClient != null){
                    closeableHttpClient.close();
                }
                if(closeableHttpResponse != null){
                    closeableHttpResponse.close();
                }
            }catch (IOException i){
                i.printStackTrace();
            }
        }
        return resp;
    }

}
