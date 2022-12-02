package pers.movie_website.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import pers.movie_website.mapper.UserMapper;
import pers.movie_website.pojo.Movie;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class GrabRun {
    public static int threadNum = 1;
    @Resource
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public int grabMovie() {
//        ExecutorService executorService = new ThreadPoolExecutor(2,2,
//                0, TimeUnit.SECONDS,
//                new ArrayBlockingQueue<>(512),
//                new ThreadPoolExecutor.DiscardOldestPolicy());
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        Set<Future<Movie>> setList = new HashSet<Future<Movie>>();
        for (int i = 0; i < threadNum; i++) {
            Grab grab = new Grab();
            Future<Movie> set = executorService.submit(grab);
            if(!setList.contains(set)){
                setList.add(set);
            }
        }
        executorService.shutdown();
        int num=0;
        for (Future<Movie> fs: setList
             ) {
            try{
              Movie movie  = fs.get();
              String name  = movie.getName();
//              if(!stringRedisTemplate.hasKey(name)){
//                  stringRedisTemplate.opsForValue().append(name,"1");
                   num += userMapper.inserMovie(movie);
            //  }
            }catch (InterruptedException e){
                e.printStackTrace();
            }catch (ExecutionException e){
                e.printStackTrace();
            }
        }
        System.out.println("此次入库"+num+"条电影数据");
        return num;
    }
}











