package pers.movie_website.utils;

import com.csvreader.CsvReader;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Data
public class CsvImportUtil {
    //上传文件的路径
    private final static URL PATH = Thread.currentThread().getContextClassLoader().getResource("");
    /**
     * @return File  一般文件类型
     * @Description 上传文件的文件类型
     * @Param multipartFile
     **/
    public static File uploadFile(MultipartFile multipartFile) {
        // 获 取上传 路径
        String path = PATH.getPath() + multipartFile.getOriginalFilename();
        try {
            // 通过将给定的路径名字符串转换为抽象路径名来创建新的 File实例
            File file = new File(path);
            // 此抽象路径名表示的文件或目录是否存在
            if (!file.getParentFile().exists()) {
                // 创建由此抽象路径名命名的目录，包括任何必需但不存在的父目录
                file.getParentFile().mkdirs();
            }
            // 转换为一般file 文件
            multipartFile.transferTo(file);
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * @return List<List<String>>
     * @Description 读取CSV文件的内容（不含表头）
     * @Param filePath 文件存储路径，colNum 列数
     **/
    public static List<String[]> readCSV(String filePath, int colNum) {
        List<String[]> list = new ArrayList<>();
        try {
            CsvReader reader = new CsvReader(filePath, ',', Charset.forName("GBK"));
            reader.readHeaders();
            while (reader.readRecord()) {
                list.add(reader.getValues());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
