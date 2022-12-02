package pers.movie_website.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import pers.movie_website.mapper.UserMapper;
import pers.movie_website.pojo.*;
import pers.movie_website.utils.JwtUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author 18028
 */
@Service
public class UserService {


    @Resource
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final  String MOVIESCORE = "movieRank";

    /**
     * 登录
     *
     * @param account_number 账号
     * @param pass 密码
     * @return 用户登录返回用户信息
     */
    public HttpResult userLogin(String account_number, String pass,int level) {
        User userinfo = null ;
        if(level == 1){
            userinfo = userMapper.existsAdmin(account_number);
        }else {
            userinfo = userMapper.existsUserFromStr(account_number);
        }
        if(!ObjectUtils.isEmpty(userinfo) && bCryptPasswordEncoder.matches(pass,userinfo.getPassWord())){
            // 使用UUID作为token值
            // String token = UUID.randomUUID().toString().replaceAll("-", "");
            // 将用户的ID信息存入redis缓存，并设置两小时的过期时间
            // stringRedisTemplate.opsForValue().set(token,user, 7200, TimeUnit.SECONDS);
            String token = jwtUtils.generateToken(account_number);
            return HttpResult.success(token);
        }
        return HttpResult.failure(ResultCodeEnum.USER_NOT_LOGIN);
    }

    /**
     * 注册
     *
     * @param account_number
     * @param pass
     * @return
     */
    public HttpResult registerUser(String account_number, String pass) {
        User user = userMapper.existsUserFromStr(account_number);
        if (ObjectUtils.isEmpty(user)) {
            String password = bCryptPasswordEncoder.encode(pass);
            User userinfo = new User();
            userinfo.setAccountNumber(account_number);
            userinfo.setPassWord(password);
            int num = userMapper.insertUser(userinfo);
            if (num == 1) {
                return HttpResult.success(ResultCodeEnum.SUCCESS, "注册成功");
            }
        }
      return HttpResult.failure(ResultCodeEnum.USER_EXISTS);
    }

    //用户信息
    public HttpResult userInfo(String token) {
        Claims claim = jwtUtils.getClaimByToken(token);
        if(ObjectUtils.isEmpty(claim)){
            throw new JwtException("token 异常");
        }
        if(jwtUtils.isTokenExpired(claim)){
            throw new JwtException("token已经过期");
        }
        String username = claim.getSubject() ;
        User user = new User();
        user.setAccountNumber(username);
        User user1 = userMapper.existsUser(user);
        return HttpResult.success(user1);
    }

    //获取电影信息以及写入用户观看
    public HttpResult getMovieInfo(String token,int movieId){
        Movie movie = userMapper.selectMovieFromId(movieId);
        List<Object> date = new ArrayList<>();
        if(movie == null){
            return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
        }
        Double score = redisTemplate.opsForZSet().score(MOVIESCORE, movie.getName());
        date.add(movie);
        date.add(score);
        return HttpResult.success(movie);
    }


    //豆瓣抓取
    public HttpResult toGrab() {
        String movie_from = "https://movie.douban.com/j/new_search_subjects?sort=U&range=0,10&tags=";
        Movie movie = null;
        JSONObject jObjectList = null;
        try{
            String data = Jsoup.connect(movie_from+"电影&start=0").get().outerHtml();
            jObjectList =  new JSONObject(data);
        }catch (IOException e){
            e.printStackTrace();
        }
        JSONArray jsonArray = jObjectList.getJSONArray("data");
        Set<Movie> set = new HashSet<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            movie = new Movie();
            JSONObject jsonObjectDetail = jsonArray.getJSONObject(i);
            movie.setName(jsonObjectDetail.getString("title"));
            movie.setMovieUrl(jsonObjectDetail.getString("url"));
            movie.setScore(jsonObjectDetail.getString("rate"));
            if(!redisTemplate.hasKey(movie.getName())){
                set.add(movie);
                redisTemplate.opsForValue().append(movie.getName(),"1");
            }
        }
        //redisTemplate.opsForSet().add("movie",set);
        int num =0;
        for ( Movie m: set
             ) {
            num += userMapper.inserMovie(m);
        }
        return HttpResult.success(num);
    }

    public HttpResult csvInput(List<String[] > lists){
        if(lists.size() < 1){
            return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
        }
        List<String> listMovieName = userMapper.selectAllMovieName();
        Movie movie = null;
        for (String[] sc: lists
             ) {
            String name = sc[2].split(" ")[0];
            if(!listMovieName.contains(name)){
                movie = new Movie();
                movie.setMovieUrl(sc[1]);
                movie.setName(name);
                movie.setScore(sc[3]);
                movie.setDate(sc[4]);
                movie.setType(sc[5]);
                //movie.
                userMapper.inserMovie(movie);
            }
        }
        return HttpResult.success();
    }

    public HttpResult upMovieNum(){
      int num =  userMapper.selectMovieNum();
      return HttpResult.success(num);
    }

    public HttpResult getUserNum(){
        int num = userMapper.selectUserNum();
        return HttpResult.success(num);
    }

    public HttpResult getUserInfo(String name){
        if("".equals(name)){
            return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
        }
        User user = new User();
        user.setAccountNumber(name);
        user = userMapper.existsUser(user);
        if(user == null){
            return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
        }
        return HttpResult.success(user);
    }

    public HttpResult getAllMovie(int pageNum){

        List<Movie> list = userMapper.selectMovieAll((pageNum-1)*16,16);
//        int num = userMapper.selectMovieNum();
//        Map<String,Integer> map = new HashMap<>();
//        map.put("movieNum",num);
//        List lists = new ArrayList();
//        lists.add(map);
//        lists.add(list);
        return HttpResult.success(list);
    }

    //抓取腾讯视频图片和豆瓣简介
    public HttpResult getMovieImg(){
        List<Movie> list = userMapper.selectMovieImg();
        int num =0;
        for (Movie movie: list
             ) {
           try{
               String html = Jsoup.connect("https://v.qq.com/x/search/?q="+movie.getName()+"&stag=0&smartbox_ab=").get().outerHtml();
               Pattern p = Pattern.compile("class=\"figure_pic\" src=\"//(\\S*)\"");
               Matcher matcher = p.matcher(html);
               if (matcher.find()){
                   movie.setMovieImg("https://"+matcher.group(1));
               }

              num += userMapper.updateMovieImg(movie);
           }catch (Exception e){
               e.printStackTrace();
           }
        }
        List<Movie> list1 = userMapper.selectMovieText();
        for (Movie m : list1) {
            try {
                Document document = Jsoup.connect("https://v.qq.com/x/search/?q="+m.getName()+"&stag=0&smartbox_ab=").get();
                String movieText = document.select("span[class=desc_text]").text();
                userMapper.UpdateMovieText(movieText,m.getId());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return HttpResult.success();
    }


    public HttpResult createdRecommend(){
        List<Movie> list = userMapper.selectMovie();
        Map<String,List<String>> map = new HashMap<>();
        for (Movie m : list
                ) {
            String movieType = m.getType();
            String[] snum =  movieType.split("/");
            for (int i = 0; i < snum.length; i++) {
                List<String> listMap = new ArrayList<>();
                if(map.containsKey(snum[i])){
                    listMap = map.get(snum[i]);
                }
                listMap.add(m.getName());
                map.put(snum[i],listMap);
            }
        }
        for (Map.Entry<String,List<String>> entry : map.entrySet()
             ) {
            redisTemplate.opsForList().leftPushAll(entry.getKey(),entry.getValue());
        }
        return HttpResult.success(map.size());
    }

    //推荐算法
    public HttpResult recommendMovie(String token){
        Claims claim = jwtUtils.getClaimByToken(token);
        if(ObjectUtils.isEmpty(claim)){
            throw new JwtException("token 异常");
        }
        if(jwtUtils.isTokenExpired(claim)){
            throw new JwtException("token已经过期");
        }
        String username = claim.getSubject() ;
        int userId = userMapper.selectUserId(username);
        Integer movieIdNum = userMapper.selectUserRatingFrom(userId);
        List<Movie> resultItems = new ArrayList<>();
        //对于初始新用户推荐,搜索用户评分表，如何没有记录，则系统认为这是新用户
        if(movieIdNum == 0){
            resultItems = newUserRec();
        }else{  //有了评分记录的用户
            //读取用户评分列表
            List<UserRating> list = userMapper.selectAllRating();
            //所有用户的物品集合 第一个Integer为用户ID，第二个Integer为电影ID
            //遍历评分列表，数据写入map
            Map<Integer,Map<Integer,Double>> mapId = new HashMap<>();
            for (UserRating userRating : list) {
                //物品集合
                Map<Integer,Double> map = new HashMap<>();
                //如果
                if(mapId.containsKey(userRating.getUid())){
                    map = mapId.get(userRating.getUid());
                }
                map.put(userRating.getMid(),userRating.getRating());
                mapId.put(userRating.getUid(),map);
            }
            //获取目标用户的矩阵
            Map<Integer,Double> nowUser = mapId.get(userId);
            //除去目标用户矩阵
            mapId.remove(userId);
            //余弦值计算结果 用户id与余弦值
            Map<Integer,Double> mapCos = new HashMap<>();
            //用户之间余弦值计算
            int num1 ;
            double num2 ;
            for (Map.Entry<Integer, Map<Integer,Double>> entry : mapId.entrySet()) {
                //求出分子 用户数据集共有的
                num1 = 0;
                //遍历目标用户和每个用户，求出余弦分子
                for (Map.Entry<Integer,Double> nowUserMap: nowUser.entrySet()) {
                    for (Map.Entry<Integer,Double> entry1 : entry.getValue().entrySet()) {
                        if(nowUserMap.getKey() == entry1.getKey()){
                            num1+=1;
                        }
                    }
                }
                num2 = Math.sqrt(nowUser.size()*entry.getValue().size());
                mapCos.put(entry.getKey(),num1/num2);
            }
            //将mapCos从余弦值高到低排列
            List<Map.Entry<Integer,Double>> list2 = new ArrayList<Map.Entry<Integer,Double>>(mapCos.entrySet());
            Collections.sort(list2,new Comparator<Map.Entry<Integer,Double>>() {
                @Override
                public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            //相似度最高的用户前5集合
            Map<Integer,Double> mapUserCosValue = new HashMap<>();
            List<Integer> listUserId = new ArrayList<>();
            int num = 0;
            for(Map.Entry<Integer,Double> mapping:list2){
                //相似度为0的用户不要
                if(mapping.getValue() != 0){
                    listUserId.add(mapping.getKey());
                    mapUserCosValue.put(mapping.getKey(),mapping.getValue());
                }
                if(num == 5){
                    break;
                }
                num++;
            }
            //相似度最高的前5用户 的电影id，评分集合
            List<Integer> movieId = new ArrayList<>(); // 物品集合
            Map<Integer,Map<Integer,Double>> MapMovieId = new HashMap<>();
            for (Integer i:listUserId) {
                for (Map.Entry<Integer,Map<Integer,Double>> entry : mapId.entrySet()) {
                    //如果用户id相同 添加到集合
                    if(entry.getKey() == i){
                        for (Map.Entry<Integer,Double> mapEntry: nowUser.entrySet()) {
                            //去掉与目标用户重复的
                            entry.getValue().remove(mapEntry.getKey());
                        }
                        //找出所有不重复的电影集合
                        for (Map.Entry<Integer,Double> mapEntry: entry.getValue().entrySet()) {
                            if(!movieId.contains(mapEntry.getKey())){
                                movieId.add(mapEntry.getKey());
                            }
                        }
                        MapMovieId.put(i,entry.getValue());
                    }
                }
            }
            //计算目标用户对于列表内物品喜爱程度
            Map<Integer,Double> mapMovieValue = new HashMap<>();
            //对于每个物品 ,计算  每个用户与目标用户的相似度 * 其对物品评分 相加
            for (Integer i: movieId) {
                double movieValue=0;
                for (Map.Entry<Integer,Map<Integer,Double>> entry : MapMovieId.entrySet()) {
                    for (Map.Entry<Integer,Double> entry1 : entry.getValue().entrySet()) {
                        if(entry1.getKey() == i){
                            movieValue +=  mapUserCosValue.get(entry.getKey()) * entry1.getValue();
                        }
                    }
                }
                mapMovieValue.put(i,movieValue);
            }
            //从高到底排列用户对物品喜爱
            List<Map.Entry<Integer,Double>> listMovieId = new ArrayList<Map.Entry<Integer,Double>>(mapMovieValue.entrySet());
            Collections.sort(list2,new Comparator<Map.Entry<Integer,Double>>() {
                @Override
                public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });

            //根据id获取电影
            for (Map.Entry<Integer,Double> entry1 : listMovieId) {
                resultItems.add(userMapper.selectMovieFromId(entry1.getKey()));
            }
        }
        if(resultItems.size() == 0){
            resultItems = newUserRec();
        }
        return HttpResult.success(resultItems);
    }

    //新用户推荐列表和老用户空推荐列表
    public List<Movie> newUserRec(){
        List<Movie> resultItems = new ArrayList<>();
        Map<String, List<Movie>> map = getMovieType();
        //遍历Map,依次对每个分类中的分数值进行排序，取出分类前3个，元素为电影id
        List<Movie> list1 = null;
        for (Map.Entry<String, List<Movie>> entry : map.entrySet()) {
            list1 = entry.getValue();
            //此处由于对Movie类重写compareTo方法，改为降序
            Collections.sort(list1);
            for (int i = 0,len = list1.size() >= 3 ? 3: list1.size() ; i < len ; i++) {
                //如果没有就加入
                if (!resultItems.contains(list1.get(i))) {
                    resultItems.add(list1.get(i));
                }
            }
        }
        return resultItems;
    }
    //获取所有电影分类
    public Map<String, List<Movie>> getMovieType(){
        //对于所有电影，根据分类存入map
        List<Movie> list = userMapper.selectMovie();
        Map<String, List<Movie>> map = new HashMap<>();
        for (Movie m : list) {
            String movieType = m.getType();
            String[] snum = movieType.split("/");
            List<Movie> listMap = null;
            for (int i = 0; i < snum.length; i++) {
                listMap = new ArrayList<>();
                //如果已经有此分类，则在原来基础上再添加
                if (map.containsKey(snum[i])) {
                    listMap = map.get(snum[i]);
                }
                listMap.add(m);
                map.put(snum[i], listMap);
            }
        }
        return map;
    }

    //收藏电影
    public HttpResult upUserCollection(String token,int movieId){
        Claims claim = jwtUtils.getClaimByToken(token);
        if(ObjectUtils.isEmpty(claim)){
            throw new JwtException("token 异常");
        }
        if(jwtUtils.isTokenExpired(claim)){
            throw new JwtException("token已经过期");
        }
        String username = claim.getSubject() ;
        int userId = userMapper.selectUserId(username)!=null ? userMapper.selectUserId(username) : -1;
        if(userId != -1){
            //没有则写入
            if (userMapper.selectUserCollection(userId, movieId) == 0) {
                Date date = new Date();
                DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String punchTime = simpleDateFormat.format(date);
                userMapper.inserUserCollection(userId, movieId, punchTime);
            }
            return HttpResult.success();
        }
        return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
    }

    //获取收藏电影
    public HttpResult seleceUserCollection(String token){
        Claims claim = jwtUtils.getClaimByToken(token);
        if(ObjectUtils.isEmpty(claim)){
            throw new JwtException("token 异常");
        }
        if(jwtUtils.isTokenExpired(claim)){
            throw new JwtException("token已经过期");
        }
        String username = claim.getSubject() ;
        int userId = userMapper.selectUserId(username);
        List<Integer> list =  userMapper.selectCollectionFromId(userId);
        List<Movie> collectionMovieList = new ArrayList<>();
        for (Integer i:list
             ) {
            collectionMovieList.add(userMapper.selectMovieFromId(i));
        }
        return HttpResult.success(collectionMovieList);
    }

    //取消收藏
    public HttpResult deleteUserCollection(String token,int movieId){
        Claims claim = jwtUtils.getClaimByToken(token);
        if(ObjectUtils.isEmpty(claim)){
            throw new JwtException("token 异常");
        }
        if(jwtUtils.isTokenExpired(claim)){
            throw new JwtException("token已经过期");
        }
        String username = claim.getSubject() ;
        int userId = userMapper.selectUserId(username);
        int num = userMapper.deleteCollection(userId,movieId);
        return HttpResult.success(num);
    }

    //搜索电影
    public HttpResult searchMovie(String movieName){
        List<Movie> list = new ArrayList<>();
        Movie movie = userMapper.selectMovieFromName(movieName);
        if(movie == null){
            return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
        }
        list.add(movie);
        return HttpResult.success(list);
    }

    //删除用户
    public HttpResult deleteUser(String token){
        Claims claim = jwtUtils.getClaimByToken(token);
        if(ObjectUtils.isEmpty(claim)){
            throw new JwtException("token 异常");
        }
        if(jwtUtils.isTokenExpired(claim)){
            throw new JwtException("token已经过期");
        }
        String username = claim.getSubject() ;
        if(username != null){
            userMapper.deleteUser(username);
            return HttpResult.success();
        }
        return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
    }

    public HttpResult deleteMovie(String movieName){
        int num =  userMapper.deleteMovie(movieName);
        if(num == 1){
            return HttpResult.success();
        }
        return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
    }
    public HttpResult adminSearchMovie(String movieName){
        Movie movie = userMapper.selectMovieFromName(movieName);
        if(movie == null){
            return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
        }
        return HttpResult.success(movie);
    }

    public HttpResult userMovieScore(String token,int movieId,int movieRating){
        Claims claim = jwtUtils.getClaimByToken(token);
        if(ObjectUtils.isEmpty(claim)){
            throw new JwtException("token 异常");
        }
        if(jwtUtils.isTokenExpired(claim)){
            throw new JwtException("token已经过期");
        }
        String username = claim.getSubject() ;
        int userId = userMapper.selectUserId(username)!=null ? userMapper.selectUserId(username) : -1;
        if(userId != -1) {
            //没有则写入
            if (userMapper.selectUserRating(userId, movieId) == 0) {
                Date date = new Date();
                DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String punchTime = simpleDateFormat.format(date);
                userMapper.inserUserRating(userId, movieId,movieRating, punchTime);
            }
        }
        return HttpResult.success();
    }
    //观看统计
    public  HttpResult userWatchDate(){
       int i = userMapper.selectBestMovieWatch();
       Movie movie = userMapper.selectMovieFromId(i);
       if(movie != null){
           return HttpResult.success(movie);
       }
       return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
    }

    public HttpResult userWatchTongJi(){
        int i = userMapper.selectBestMovieColl();
        Movie movie = userMapper.selectMovieFromId(i);
        if(movie != null){
            return HttpResult.success(movie);
        }
        return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
    }

    public HttpResult selectUserRating(String token){
        Claims claim = jwtUtils.getClaimByToken(token);
        if(ObjectUtils.isEmpty(claim)){
            throw new JwtException("token 异常");
        }
        if(jwtUtils.isTokenExpired(claim)){
            throw new JwtException("token已经过期");
        }
        String username = claim.getSubject() ;
        int userId = userMapper.selectUserId(username)!=null ? userMapper.selectUserId(username) : -1;
        if(userId != -1){
          List<UserRating> list = userMapper.selectRatingFromId(userId);
          List<ResultRating> listResult = new ArrayList<>();
            for (UserRating u: list
                 ) {
               Movie movie = userMapper.selectMovieFromId(u.getMid());
               ResultRating resultRating = new ResultRating();
               resultRating.setMovieName(movie.getName());
               resultRating.setRating(u.getRating());
               resultRating.setDateTime(u.getDatetime());
                listResult.add(resultRating);
            }
          return HttpResult.success(listResult);
        }
        return HttpResult.failure(ResultCodeEnum.TOKEN_OUT);
    }

    public HttpResult modifyMovie(int movieId,String movieText){
       int i =  userMapper.UpdateMovieText(movieText,movieId);
       if(i == 1) {
           return HttpResult.success();
       }
       return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
    }


    public HttpResult getBestWatchMoviePie(){
        List<MoviePie> moviePieList = userMapper.selectBestMovieWatchPie();
        List<MoviePie2> restList = new ArrayList<>();
        for (MoviePie moviePie: moviePieList) {
            Movie movie = userMapper.selectMovieFromId(moviePie.getName());
            if(movie != null){
                MoviePie2 moviePie2 = new MoviePie2();
                moviePie2.setValue(moviePie.getValue());
                moviePie2.setName(movie.getName());
                restList.add(moviePie2);
            }
        }
        if(restList.size() > 0){
            return HttpResult.success(restList);
        }
        return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
    }

    public HttpResult getBestCollMoviePie(){
        List<MoviePie> moviePieList = userMapper.selectBestMovieCollPie();
        List<MoviePie2> restList = new ArrayList<>();
        for (MoviePie moviePie: moviePieList) {
            Movie movie = userMapper.selectMovieFromId(moviePie.getName());
            if(movie != null){
                MoviePie2 moviePie2 = new MoviePie2();
                moviePie2.setValue(moviePie.getValue());
                moviePie2.setName(movie.getName());
                restList.add(moviePie2);
            }
        }
        if(restList.size() > 0){
            return HttpResult.success(restList);
        }
        return HttpResult.failure(ResultCodeEnum.Not_Acceptable);
    }

    //电影库获取所有电影分类
    public HttpResult getAllMovieType(){
    //  Map<String,List<Movie>> map = getMovieType();
        List<Movie> list = userMapper.selectMovie();
        List<String> list1 = new ArrayList<>();
        list1.add("全部");
        for (Movie m : list) {
            String movieType = m.getType();
            String[] snum = movieType.split("/");
            for (int i = 0; i < snum.length; i++) {
               if(!list1.contains(snum[i])){
                   list1.add(snum[i]);
               }
            }
        }
        list1.remove("情色");
        list1.remove("同性");
        return HttpResult.success(list1);
    }

    //根据电影分类找电影
    public HttpResult getTypeMovie(String movieType,int num){
        List<Movie> list = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        if(movieType.equals("全部")){
            list = userMapper.selectMovieAll((num-1)*16,16);
            map.put("num",userMapper.selectMovieNum());
        }else {
           List<Movie> list1 = userMapper.selectMovie();
            List<Movie> list2 = new ArrayList<>();
            for (Movie m : list1) {
                String[] snum = m.getType().split("/");
                for (int i = 0; i < snum.length; i++) {
                    if(snum[i].equals(movieType)){
                        list2.add(m);
                        break;
                    }
                }
            }
            map.put("num",list2.size());
            int begin = (num-1)*16;
            int end = list2.size() > begin+16 ? begin+16 : list2.size();
            if(begin <= list2.size()){
                for (int i = begin; i < end; i++) {
                    list.add(list2.get(i));
                }
            }
        }
        map.put("data",list);
        return HttpResult.success(map);
    }

    public  HttpResult getMovieWatchWebsite(String token ,String movieName){

        if(token != null) {
            Claims claim = jwtUtils.getClaimByToken(token);
            if(ObjectUtils.isEmpty(claim)){
                throw new JwtException("token 异常");
            }
            if(jwtUtils.isTokenExpired(claim)){
                throw new JwtException("token已经过期");
            }
            redisTemplate.opsForZSet().incrementScore(MOVIESCORE, movieName, 1);
            String username = claim.getSubject() ;
            int userId = userMapper.selectUserId(username)!=null ? userMapper.selectUserId(username) : -1;
            Movie movie = userMapper.selectMovieFromName(movieName);
            int movieId  = movie.getId();
            if(userId != -1) {
                //没有则写入
                if (userMapper.seleceUserAndMovidWatch(userId, movieId) == 0) {
                    Date date = new Date();
                    DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String punchTime = simpleDateFormat.format(date);
                    userMapper.inserUserWatch(userId, movieId, punchTime);
                }else {
                    //否则更新时间
                    Date date = new Date();
                    DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String punchTime = simpleDateFormat.format(date);
                    userMapper.updateUserWatchDate(userId, movieId, punchTime);
                }
            }
        }
        String tenxunMovieUrl = "https://v.qq.com/x/search/?q=";
        String tenxunMovieUrlEnd = "&stag=&smartbox_ab=";
        List<String> list = new ArrayList<>();
        String movieUrl = null;
        try{
            Document document = Jsoup.connect(tenxunMovieUrl+movieName+tenxunMovieUrlEnd).get();
            Element element = document.select("div[data-len=1]").first();
            movieUrl = element.select("a[href]").first().attr("href");
            list.add(movieUrl);
        }catch (Exception e){
            e.printStackTrace();
        }
        return HttpResult.success(list);
    }

    //删除用户后对与其他用户数据的删除
  //  public void
}
